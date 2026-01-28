package com.deliveryapp.ui;

import com.deliveryapp.AppSession;
import com.deliveryapp.dao.DeliveryDAO;
import com.deliveryapp.dao.ProductDAO;
import com.deliveryapp.model.Product;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class ProductSelectDialog extends JDialog {

    public ProductSelectDialog(JFrame owner) {
        super(owner, "Select a Product", true);
        setSize(520, 420);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // Main list panel (vertical)
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(listPanel);
        add(scrollPane, BorderLayout.CENTER);

        // Load products and create a "row" for each product
        try {
            List<Product> products = new ProductDAO().listAll();

            if (products.isEmpty()) {
                JLabel empty = new JLabel("No products found.");
                empty.setBorder(new EmptyBorder(10, 10, 10, 10));
                listPanel.add(empty);
            } else {
                for (Product p : products) {
                    listPanel.add(createProductRow(p));
                    listPanel.add(Box.createVerticalStrut(8)); // spacing between cards
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // A single "product card" with border + Select/Cancel buttons
    private JPanel createProductRow(Product p) {
        JPanel row = new JPanel(new BorderLayout(10, 10));

        // Small border around each product row
        row.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel name = new JLabel(p.name + " (" + p.price + "/kg)");
        name.setFont(name.getFont().deriveFont(Font.BOLD, 13f));
        row.add(name, BorderLayout.CENTER);

        JButton selectBtn = new JButton("Select");
        selectBtn.setBackground(Color.GREEN); // green
        selectBtn.setForeground(Color.BLACK);
        selectBtn.setFocusPainted(false);
        selectBtn.setOpaque(true);
        

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btns.add(selectBtn);
        row.add(btns, BorderLayout.EAST);
        JButton backToDashboard = new JButton("Back to Dashboard");
        backToDashboard.setBackground(Color.yellow);
        backToDashboard.setForeground(Color.BLACK);
        backToDashboard.setFocusPainted(false);
        backToDashboard.setOpaque(true);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.add(backToDashboard);

        add(bottom, BorderLayout.SOUTH);

        backToDashboard.addActionListener(e -> dispose());


      

        // Select button: ask qty + address + date + create delivery
        selectBtn.addActionListener(e -> handleSelect(p));

        return row;
    }

    private void handleSelect(Product p) {
        // Quantity popup
    	// Quantity popup (keep asking until valid or cancelled)
    	Integer qty = null;

    	while (qty == null) {
    	    String qtyStr = JOptionPane.showInputDialog(
    	            this,
    	            "Enter Quantity (Kg):",
    	            "Quantity",
    	            JOptionPane.QUESTION_MESSAGE
    	    );

    	    if (qtyStr == null) {
    	        // user pressed Cancel
    	        return;
    	    }

    	    qtyStr = qtyStr.trim();

    	    if (qtyStr.isEmpty()) {
    	        JOptionPane.showMessageDialog(this, "Please enter the quantity.");
    	        continue; // ask again
    	    }

    	    try {
    	        int q = Integer.parseInt(qtyStr);
    	        if (q <= 0) {
    	            JOptionPane.showMessageDialog(this, "Quantity must be > 0.");
    	            continue; // ask again
    	        }
    	        qty = q; // valid
    	    } catch (Exception ex) {
    	        JOptionPane.showMessageDialog(this, "Quantity must be a number.");
    	        // ask again
    	    }
    	}


        // Address + Date
    	String addr = null;
    	LocalDate d = null;

    	// Keep showing delivery details dialog until valid or cancelled
    	while (addr == null || d == null) {

    	    JTextField address = new JTextField();
    	    JTextField date = new JTextField(LocalDate.now().toString()); // yyyy-mm-dd

    	    JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
    	    panel.add(new JLabel("Delivery Address:"));
    	    panel.add(address);
    	    panel.add(new JLabel("Delivery Date (yyyy-mm-dd):"));
    	    panel.add(date);

    	    int ok = JOptionPane.showConfirmDialog(this, panel, "Delivery Details", JOptionPane.OK_CANCEL_OPTION);
    	    if (ok != JOptionPane.OK_OPTION) {
    	        return; // user cancelled
    	    }

    	    String tempAddr = address.getText().trim();
    	    if (tempAddr.isEmpty()) {
    	        JOptionPane.showMessageDialog(this, "Address is required.");
    	        continue; // show dialog again
    	    }

    	    LocalDate tempDate;
    	    try {
    	        tempDate = LocalDate.parse(date.getText().trim());
    	    } catch (Exception ex) {
    	        JOptionPane.showMessageDialog(this, "Date must be yyyy-mm-dd.");
    	        continue; // show dialog again
    	    }

    	    LocalDate today = LocalDate.now();
    	    if (tempDate.isBefore(today)) {
    	        JOptionPane.showMessageDialog(this, "Please select today or a future date.");
    	        continue; //show dialog again
    	    }

    	    // valid
    	    addr = tempAddr;
    	    d = tempDate;
    	}


        try {
            new DeliveryDAO().createDelivery(AppSession.currentUser.id, p.id, qty, addr, d);
            JOptionPane.showMessageDialog(this, "Delivery request created!");
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
