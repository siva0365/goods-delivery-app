package com.deliveryapp.dao;

import com.deliveryapp.db.DB;
import com.deliveryapp.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    public List<Product> listAll() throws SQLException {
        String sql = "SELECT * FROM products ORDER BY name";
        List<Product> out = new ArrayList<>();
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Product p = new Product();
                p.id = rs.getInt("id");
                p.name = rs.getString("name");
                p.price = rs.getBigDecimal("price");
                out.add(p);
            }
        }
        return out;
    }
}
