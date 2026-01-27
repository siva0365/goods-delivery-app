package com.deliveryapp.ui;

import com.deliveryapp.AppSession;

import javax.swing.*;
import java.awt.*;

public class DriverDashboardFrame extends JFrame {

    public DriverDashboardFrame() {
        super("Driver Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(520, 260);
        setLocationRelativeTo(null);

        JLabel title = new JLabel("Driver Dashboard", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        JButton assigned = new JButton("View Assigned Missions");
        assigned.setBackground(Color.GREEN);
        assigned.setForeground(Color.BLACK);
        assigned.setOpaque(true);
        assigned.setBorderPainted(false);
        JButton completed = new JButton("View Completed Missions");
        completed.setBackground(Color.BLUE);
        completed.setForeground(Color.WHITE);
        completed.setOpaque(true);
        completed.setBorderPainted(false);
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

        JPanel center = new JPanel(new GridLayout(0,1,10,10));
        center.setBorder(BorderFactory.createEmptyBorder(16,80,16,80));
        center.add(assigned);
        center.add(completed);
        center.add(edit);
        center.add(logout);
        
     // Bottom panel for Edit + Logout
        JPanel bottomButtons = new JPanel(new GridLayout(1, 2, 20, 0));
        bottomButtons.add(edit);
        bottomButtons.add(logout);

        // Add bottom buttons
        center.add(bottomButtons);


        setLayout(new BorderLayout());
        add(title, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        assigned.addActionListener(e -> new DriverMissionsDialog(this, false).setVisible(true));
        completed.addActionListener(e -> new DriverMissionsDialog(this, true).setVisible(true));
        edit.addActionListener(e -> new EditProfileDialog(this).setVisible(true));
        logout.addActionListener(e -> { dispose(); AppSession.currentUser = null; new LoginFrame().setVisible(true); });
    }
}
