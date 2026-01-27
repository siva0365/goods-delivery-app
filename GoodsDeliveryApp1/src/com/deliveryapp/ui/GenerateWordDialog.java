package com.deliveryapp.ui;

import com.deliveryapp.util.WordExporter;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;

public class GenerateWordDialog extends JDialog {

    public GenerateWordDialog(JFrame owner) {
        super(owner, "Generate daily missions", true);
        setSize(520, 220);
        setLocationRelativeTo(owner);

        JTextField dateField = new JTextField(LocalDate.now().toString(), 10);
        JButton generate = new JButton("Generate Word File");
        generate.setBackground(Color.BLUE);
        generate.setForeground(Color.WHITE);
        generate.setFocusPainted(false);
        generate.setOpaque(true);
        JButton close = new JButton("Back to Dashboard");
        close.setBackground(Color.YELLOW);
        close.setForeground(Color.BLACK);
        close.setFocusPainted(false);
        close.setOpaque(true);

        JPanel form = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 20));
        form.add(new JLabel("Select Date (yyyy-mm-dd):"));
        form.add(dateField);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        bottom.add(generate);
        bottom.add(close);


        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        close.addActionListener(e -> dispose());

        generate.addActionListener(e -> {
            try {
                LocalDate d = LocalDate.parse(dateField.getText().trim());

                String userHome = System.getProperty("user.home");

                File desktop = new File(userHome, "OneDrive\\Desktop");
                if (!desktop.exists()) desktop = new File(userHome, "Desktop");
                if (!desktop.exists()) desktop = new File(userHome);

                File out = new File(desktop, "missions_" + d + ".docx");

                WordExporter.exportDailyMissions(d, out.getAbsolutePath());

                JOptionPane.showMessageDialog(this,
                        "Generated:\n" + out.getAbsolutePath());

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Word Error", JOptionPane.ERROR_MESSAGE);
            }
        });

    }
}
