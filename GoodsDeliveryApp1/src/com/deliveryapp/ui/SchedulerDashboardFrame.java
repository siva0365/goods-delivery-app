package com.deliveryapp.ui;

import com.deliveryapp.AppSession;

import javax.swing.*;
import java.awt.*;

public class SchedulerDashboardFrame extends JFrame {

    public SchedulerDashboardFrame() {
        super("Scheduler Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(520, 280);
        setLocationRelativeTo(null);

        JLabel title = new JLabel("Scheduler Dashboard", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        JButton view = new JButton("View All Deliveries (By Date)");
        view.setBackground(new Color(52, 152, 219));
        view.setForeground(Color.WHITE);
        view.setFocusPainted(false);
        view.setOpaque(true);
        JButton assign = new JButton("Assign Routes to Drivers");
        assign.setBackground(new Color(46, 204, 113));
        assign.setForeground(Color.WHITE);
        assign.setFocusPainted(false);
        assign.setOpaque(true);
        JButton word = new JButton("Generate Word File");
        word.setBackground(new Color(155, 89, 182));
        word.setForeground(Color.WHITE);
        word.setFocusPainted(false);
        word.setOpaque(true);
        JButton edit = new JButton("Edit Profile");
        edit.setBackground(new Color(243, 156, 18));
        edit.setForeground(Color.WHITE);
        edit.setFocusPainted(false);
        edit.setOpaque(true);
        JButton logout = new JButton("Logout");
        logout.setBackground(new Color(231, 76, 60));
        logout.setForeground(Color.WHITE);
        logout.setFocusPainted(false);
        logout.setOpaque(true);
        
        

        JPanel center = new JPanel(new GridLayout(0,1,10,10));
        center.setBorder(BorderFactory.createEmptyBorder(16,80,16,80));
        center.add(view);
        center.add(assign);
        center.add(word);
        center.add(edit);
        center.add(logout);

        setLayout(new BorderLayout());
        add(title, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        view.addActionListener(e -> new SchedulerDeliveriesDialog(this).setVisible(true));
        assign.addActionListener(e -> new AssignRouteDialog(this).setVisible(true));
        word.addActionListener(e -> new GenerateWordDialog(this).setVisible(true));
        edit.addActionListener(e -> new EditProfileDialog(this).setVisible(true));
        logout.addActionListener(e -> { dispose(); AppSession.currentUser = null; new LoginFrame().setVisible(true); });
    }
}
