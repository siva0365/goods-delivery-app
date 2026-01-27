package com.deliveryapp.dao;

import com.deliveryapp.db.DB;
import com.deliveryapp.model.Role;
import com.deliveryapp.model.User;

import java.sql.*;

public class UserDAO {

    public User login(String email, String passwordHash) throws SQLException {
        String sql = "SELECT * FROM users WHERE email=? AND password_hash=?";
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, passwordHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email=?";
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int create(User u) throws SQLException {
        String sql = "INSERT INTO users(email,password_hash,phone,role,first_name,last_name,truck_number,truck_capacity_kg) " +
                     "VALUES(?,?,?,?,?,?,?,?)";
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.email);
            ps.setString(2, u.passwordHash);
            ps.setString(3, u.phone);
            ps.setString(4, u.role.name());
            ps.setString(5, u.firstName == null ? "" : u.firstName);
            ps.setString(6, u.lastName == null ? "" : u.lastName);
            if (u.role == Role.DRIVER) {
                ps.setString(7, u.truckNumber);
                if (u.truckCapacityKg == null) ps.setNull(8, Types.INTEGER);
                else ps.setInt(8, u.truckCapacityKg);
            } else {
                ps.setNull(7, Types.VARCHAR);
                ps.setNull(8, Types.INTEGER);
            }

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public void update(User u) throws SQLException {
        String sql = "UPDATE users SET email=?, password_hash=?, phone=?, first_name=?, last_name=?, truck_number=?, truck_capacity_kg=? " +
                     "WHERE id=?";
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.email);
            ps.setString(2, u.passwordHash);
            ps.setString(3, u.phone);
            ps.setString(4, u.firstName == null ? "" : u.firstName);
            ps.setString(5, u.lastName == null ? "" : u.lastName);

            if (u.role == Role.DRIVER) {
                ps.setString(6, u.truckNumber);
                if (u.truckCapacityKg == null) ps.setNull(7, Types.INTEGER);
                else ps.setInt(7, u.truckCapacityKg);
            } else {
                ps.setNull(6, Types.VARCHAR);
                ps.setNull(7, Types.INTEGER);
            }
            ps.setInt(8, u.id);
            ps.executeUpdate();
        }
    }

    public User findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id=?";
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.id = rs.getInt("id");
        u.email = rs.getString("email");
        u.passwordHash = rs.getString("password_hash");
        u.phone = rs.getString("phone");
        u.role = Role.valueOf(rs.getString("role"));
        u.firstName = rs.getString("first_name");
        u.lastName = rs.getString("last_name");
        u.truckNumber = rs.getString("truck_number");
        int cap = rs.getInt("truck_capacity_kg");
        u.truckCapacityKg = rs.wasNull() ? null : cap;
        return u;
    }
}
