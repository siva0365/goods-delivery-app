package com.deliveryapp.dao;

import com.deliveryapp.db.DB;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RouteDAO {

    public int createRoute(int driverId, LocalDate routeDate, String warehouseAddress) throws SQLException {
        String sql = "INSERT INTO routes(driver_id, route_date, warehouse_address) VALUES(?,?,?)";
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, driverId);
            ps.setDate(2, Date.valueOf(routeDate));
            ps.setString(3, warehouseAddress);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public void addStop(int routeId, int deliveryId, int stopOrder) throws SQLException {
        String sql = "INSERT INTO route_stops(route_id, delivery_id, stop_order) VALUES(?,?,?)";
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, routeId);
            ps.setInt(2, deliveryId);
            ps.setInt(3, stopOrder);
            ps.executeUpdate();
        }
    }

    public int createMissionForRoute(int routeId) throws SQLException {
    	String sql = "INSERT INTO missions(route_id, status) VALUES(?, 'IN_PROGRESS')";
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, routeId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public List<Object[]> listDrivers() throws SQLException {
    	String sql = "SELECT id, CONCAT(first_name,' ',last_name,' (',email,')') AS label, truck_capacity_kg " +
                "FROM users WHERE role='DRIVER' ORDER BY first_name";
        List<Object[]> out = new ArrayList<>();
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
        	while (rs.next()) out.add(new Object[]{
        	        rs.getInt("id"),
        	        rs.getString("label"),
        	        rs.getInt("truck_capacity_kg")
        	});
        }
        return out;
    }

    public List<Object[]> listAssignedMissionsForDriver(int driverId, boolean completed) throws Exception {
        List<Object[]> rows = new java.util.ArrayList<>();

       
        String sql = """
        	    SELECT
        	        m.id AS mission_id,
        	        r.route_date AS route_date,
        	        r.warehouse_address AS warehouse,

        	        d.customer_id AS customer_id,
        	        CONCAT(cu.first_name,' ',cu.last_name) AS customer_name,

        	        d.delivery_address AS delivery_address,
        	        GROUP_CONCAT(d.id ORDER BY rs.stop_order SEPARATOR ',') AS delivery_ids,


        	        GROUP_CONCAT(
        	            CONCAT(p.name, ' (', d.qty_kg, 'kg)')
        	            ORDER BY p.name SEPARATOR ', '
        	        ) AS products_with_qty,

        	        SUM(d.qty_kg) AS total_qty,
        	        d.status AS status,

        	        MIN(rs.stop_order) AS stop_order
        	    FROM missions m
        	    JOIN routes r       ON r.id = m.route_id
        	    JOIN route_stops rs ON rs.route_id = r.id
        	    JOIN deliveries d   ON d.id = rs.delivery_id
        	    JOIN users cu       ON cu.id = d.customer_id
        	    JOIN products p     ON p.id = d.product_id
        	    WHERE r.driver_id = ?
        	      AND d.status = ?
        	    GROUP BY
        	        m.id,
        	        r.route_date,
        	        r.warehouse_address,
        	        d.customer_id,
        	        customer_name,
        	        d.delivery_address,
        	        m.status
        	    ORDER BY r.route_date DESC, stop_order
        	    """;




        try (java.sql.Connection c = com.deliveryapp.db.DB.get();
             java.sql.PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, driverId);
            ps.setString(2, completed ? "DELIVERED" : "ASSIGNED");
          

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                	String products = rs.getString("products_with_qty");
                	int totalQty = rs.getInt("total_qty");

                

             
                	String deliveryIds = rs.getString("delivery_ids"); // ✅ NEW

                	rows.add(new Object[]{
                	        rs.getInt("mission_id"),
                	        rs.getDate("route_date"),
                	        rs.getString("warehouse"),
                	        rs.getString("customer_name"),
                	        rs.getString("delivery_address"),
                	        products,          // you can add .replace(", ", "\n") if you want line-by-line
                	        totalQty,
                	        rs.getString("status"),
                	        rs.getString("delivery_ids"),       // ✅ NEW (hidden column in table)
                	});




                }
            }
        }

        return rows;
    }


    public void markMissionCompleted(int missionId) throws SQLException {
        String sql = "UPDATE missions SET status='COMPLETED', completed_at=NOW() WHERE id=?";
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, missionId);
            ps.executeUpdate();
        }
        // mark deliveries delivered
        String sql2 = """
            UPDATE deliveries d
            JOIN route_stops rs ON rs.delivery_id=d.id
            JOIN routes r ON r.id=rs.route_id
            JOIN missions m ON m.route_id=r.id
            SET d.status='DELIVERED'
            WHERE m.id=?
        """;
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(sql2)) {
            ps.setInt(1, missionId);
            ps.executeUpdate();
        }
    }
    public void markDeliveriesCompleted(String deliveryIdsCsv) throws SQLException {
        if (deliveryIdsCsv == null || deliveryIdsCsv.trim().isEmpty()) return;

        String[] ids = deliveryIdsCsv.split(",");

        try (Connection c = DB.get()) {

            // Mark ONLY selected deliveries as delivered
            String sql = "UPDATE deliveries SET status='DELIVERED' WHERE id=?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                for (String s : ids) {
                    ps.setInt(1, Integer.parseInt(s.trim()));
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            //Find mission_id of these deliveries (all belong to same mission because they are grouped row)
            int missionId = -1;
            String findMission = """
                SELECT m.id AS mission_id
                FROM missions m
                JOIN routes r       ON r.id = m.route_id
                JOIN route_stops rs ON rs.route_id = r.id
                WHERE rs.delivery_id = ?
                LIMIT 1
            """;
            try (PreparedStatement ps = c.prepareStatement(findMission)) {
                ps.setInt(1, Integer.parseInt(ids[0].trim()));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) missionId = rs.getInt("mission_id");
                }
            }

            if (missionId == -1) return;

            // If ALL deliveries in that mission are DELIVERED, mark mission COMPLETED
            String checkRemaining = """
                SELECT COUNT(*) AS remaining
                FROM missions m
                JOIN routes r       ON r.id = m.route_id
                JOIN route_stops rs ON rs.route_id = r.id
                JOIN deliveries d   ON d.id = rs.delivery_id
                WHERE m.id = ?
                  AND d.status <> 'DELIVERED'
            """;
            int remaining = 0;
            try (PreparedStatement ps = c.prepareStatement(checkRemaining)) {
                ps.setInt(1, missionId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) remaining = rs.getInt("remaining");
                }
            }

            if (remaining == 0) {
                String completeMission = "UPDATE missions SET status='COMPLETED', completed_at=NOW() WHERE id=?";
                try (PreparedStatement ps = c.prepareStatement(completeMission)) {
                    ps.setInt(1, missionId);
                    ps.executeUpdate();
                }
            }
        }
    }
    

}
