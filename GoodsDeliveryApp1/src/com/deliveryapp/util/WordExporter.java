package com.deliveryapp.util;

import com.deliveryapp.db.DB;
import org.apache.poi.xwpf.usermodel.*;

import java.io.FileOutputStream;
import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.File;
import javax.swing.filechooser.FileSystemView;

public class WordExporter {

	public static void exportDailyMissions(LocalDate day, String outputPath) throws Exception {

	    String sql = """
	        SELECT
	            CONCAT(u.first_name,' ',u.last_name) AS driver_name,
	            r.warehouse_address,
	            d.delivery_address,
	            d.delivery_date,

	            -- ✅ combine products in one row if same address
	            GROUP_CONCAT(p.name ORDER BY p.name SEPARATOR ', ') AS products,

	            -- ✅ keep stop order (use the first stop order for that address)
	            MIN(rs.stop_order) AS stop_order

	        FROM missions m
	        JOIN routes r       ON r.id = m.route_id
	        JOIN users u        ON u.id = r.driver_id
	        JOIN route_stops rs ON rs.route_id = r.id
	        JOIN deliveries d   ON d.id = rs.delivery_id
	        JOIN products p     ON p.id = d.product_id

	        WHERE r.route_date = ?

	        GROUP BY
	            driver_name,
	            r.warehouse_address,
	            d.delivery_address,
	            d.delivery_date

	        ORDER BY driver_name, stop_order
	    """;

	    XWPFDocument doc = new XWPFDocument();

	    // Title 
	    XWPFParagraph title = doc.createParagraph();
	    title.setAlignment(ParagraphAlignment.CENTER);
	    XWPFRun tr = title.createRun();
	    tr.setBold(true);
	    tr.setFontSize(16);
	    tr.setText("Daily Delivery Missions - " + day);

	    doc.createParagraph(); 

	    try (Connection c = DB.get();
	         PreparedStatement ps = c.prepareStatement(sql)) {

	        ps.setDate(1, Date.valueOf(day));

	        try (ResultSet rs = ps.executeQuery()) {

	            String currentDriver = null;
	            XWPFTable table = null;
	            int rowNum = 1;

	            while (rs.next()) {

	                String driver = rs.getString("driver_name");

	                // New driver → new section
	                if (!driver.equals(currentDriver)) {
	                    currentDriver = driver;
	                    rowNum = 1;

	                    // Driver heading
	                    XWPFParagraph dp = doc.createParagraph();
	                    XWPFRun dr = dp.createRun();
	                    dr.setBold(true);
	                    dr.setFontSize(13);
	                    dr.setText("Driver: " + driver);

	                    //table with Products column (5 columns)
	                    table = doc.createTable(1, 5);
	                    XWPFTableRow header = table.getRow(0);
	                    header.getCell(0).setText("Stop");
	                    header.getCell(1).setText("Warehouse");
	                    header.getCell(2).setText("Delivery Address");
	                    header.getCell(3).setText("Products");
	                    header.getCell(4).setText("Delivery Date");
	                }

	                // Add row
	                XWPFTableRow row = table.createRow();
	                row.getCell(0).setText(String.valueOf(rowNum++));
	                row.getCell(1).setText(rs.getString("warehouse_address"));
	                row.getCell(2).setText(rs.getString("delivery_address"));
	                row.getCell(3).setText(rs.getString("products")); // ✅ "Rice, Oil"
	                row.getCell(4).setText(rs.getDate("delivery_date").toString());
	            }
	        }
	    }

	    try (FileOutputStream fos = new FileOutputStream(outputPath)) {
	        doc.write(fos);
	    }

	    doc.close();
	}


}
