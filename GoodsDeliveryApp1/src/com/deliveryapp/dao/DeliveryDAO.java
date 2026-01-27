package com.deliveryapp.dao;

import com.deliveryapp.db.DB;
import com.deliveryapp.model.Delivery;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DeliveryDAO {

    public int createDelivery(int customerId, int productId, int qtyKg, String address, LocalDate date) throws SQLException {
    	String sql = "INSERT INTO deliveries(customer_id, product_id, qty_kg, delivery_address, delivery_date, status) VALUES(?,?,?,?,?,?)";
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, customerId);
            ps.setInt(2, productId);
            ps.setInt(3, qtyKg);
            ps.setString(4, address);
            ps.setDate(5, Date.valueOf(date));
            ps.setString(6, "PENDING");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public List<Delivery> listByCustomer(int customerId) throws SQLException {
        String sql = """
            SELECT d.*, p.name AS product_name
            FROM deliveries d
            JOIN products p ON p.id=d.product_id
            WHERE d.customer_id=?
            ORDER BY d.delivery_date DESC, d.id DESC
        """;
        List<Delivery> out = new ArrayList<>();
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        }
        return out;
    }

    public List<Delivery> listByDate(LocalDate date) throws SQLException {
        String sql = """
            SELECT d.*, p.name AS product_name, CONCAT(u.first_name,' ',u.last_name) AS customer_name
            FROM deliveries d
            JOIN products p ON p.id=d.product_id
            JOIN users u ON u.id=d.customer_id
            WHERE d.delivery_date=?
              AND (d.status IS NULL OR d.status='PENDING')
            ORDER BY d.id
        """;

        List<Delivery> out = new ArrayList<>();
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Delivery d = map(rs);
                    d.customerName = rs.getString("customer_name");
                    out.add(d);
                }
            }
        }
        return out;
    }


    public void updateStatus(int deliveryId, String status) throws SQLException {
        String sql = "UPDATE deliveries SET status=? WHERE id=?";
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, deliveryId);
            ps.executeUpdate();
        }
    }

    private Delivery map(ResultSet rs) throws SQLException {
        Delivery d = new Delivery();
        d.id = rs.getInt("id");
        d.customerId = rs.getInt("customer_id");
        d.productId = rs.getInt("product_id");
        d.productName = rs.getString("product_name");
        d.qtyKg = rs.getInt("qty_kg");
        d.address = rs.getString("delivery_address");
        d.deliveryDate = rs.getDate("delivery_date").toLocalDate();
        d.status = rs.getString("status");
        return d;
    }
}
