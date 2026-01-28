package com.deliveryapp.ui;

import com.deliveryapp.AppSession;

import javax.swing.*;
import java.awt.*;

public class CustomerDashboardFrame extends JFrame {

    public CustomerDashboardFrame() {
        super("Customer Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(520, 260);
        setLocationRelativeTo(null);

        JLabel title = new JLabel("Customer Dashboard", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        JButton make = new JButton("Make Delivery Request");
        make.setBackground(Color.GREEN);
        make.setForeground(Color.BLACK);
        make.setOpaque(true);
        make.setBorderPainted(false);
        JButton prev = new JButton("Previous Orders");
        prev.setBackground(Color.blue);
        prev.setForeground(Color.WHITE);
        prev.setOpaque(true);
        prev.setBorderPainted(false);
        JButton edit = new JButton("Edit Profile");
        edit.setBackground(Color.GRAY);
        edit.setForeground(Color.BLACK);
        edit.setOpaque(true);
        edit.setBorderPainted(false);
        JButton logout = new JButton("Logout");
        logout.setBackground(Color.RED);
        logout.setForeground(Color.WHITE);
        logout.setOpaque(true);
        logout.setBorderPainted(false);

     // Center vertical panel
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(20, 80, 20, 80));

     // Center alignment for top buttons
        make.setAlignmentX(Component.CENTER_ALIGNMENT);
        prev.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Optional: same size for both buttons
        Dimension topBtnSize = new Dimension(220, 35);
        make.setPreferredSize(topBtnSize);
        prev.setPreferredSize(topBtnSize);
        make.setMaximumSize(topBtnSize);
        prev.setMaximumSize(topBtnSize);

        // Add top buttons
        center.add(make);
        center.add(Box.createVerticalStrut(25));
        center.add(prev);
        center.add(Box.createVerticalStrut(25));


        // Bottom panel for Edit + Logout
        JPanel bottomButtons = new JPanel(new GridLayout(1, 2, 20, 0));
        bottomButtons.add(edit);
        bottomButtons.add(logout);

        // Add bottom buttons
        center.add(bottomButtons);


        setLayout(new BorderLayout());
        add(title, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        make.addActionListener(e -> new ProductSelectDialog(this).setVisible(true));
        prev.addActionListener(e -> new PreviousOrdersDialog(this, AppSession.currentUser.id).setVisible(true));
        edit.addActionListener(e -> new EditProfileDialog(this).setVisible(true));
        logout.addActionListener(e -> { dispose(); AppSession.currentUser = null; new LoginFrame().setVisible(true); });
    }
}
