package com.deliveryapp.ui;

import com.deliveryapp.AppSession;
import com.deliveryapp.dao.UserDAO;
import com.deliveryapp.model.Role;
import com.deliveryapp.model.User;
import com.deliveryapp.util.PasswordUtil;

import javax.swing.*;
import java.awt.*;

public class EditProfileDialog extends JDialog {

    private final JTextField email = new JTextField(22);
    private final JPasswordField pass = new JPasswordField(22);
    private final JTextField phone = new JTextField(22);
    private final JTextField first = new JTextField(22);
    private final JTextField last = new JTextField(22);

    private final JPanel truckPanel = new JPanel(new GridLayout());
    private final JTextField truckNo = new JTextField(22);
    private final JTextField truckCap = new JTextField(22);

    public EditProfileDialog(JFrame owner) {
        super(owner, "Edit Profile", true);
        setSize(480, 520);
        setLocationRelativeTo(owner);

        User u = AppSession.currentUser;

        email.setText(u.email);
        email.setEditable(false);
        email.setToolTipText("Email cannot be changed");

        phone.setText(u.phone);
        first.setText(u.firstName == null ? "" : u.firstName);
        last.setText(u.lastName == null ? "" : u.lastName);

        if (u.role == Role.DRIVER) {
            truckNo.setText(u.truckNumber == null ? "" : u.truckNumber);
            truckCap.setText(u.truckCapacityKg == null ? "" : String.valueOf(u.truckCapacityKg));
        }

        JPanel form = new JPanel(new GridLayout(0,1,8,8));
        form.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        form.add(labeled("Email", email));
        form.add(labeled("New Password (leave empty to keep)", pass));
        form.add(labeled("Phone", phone));
        form.add(labeled("First Name", first));
        form.add(labeled("Last Name", last));

     // Compact truck panel: 2 rows (label + field on same line) so it fits and is editable
        truckPanel.removeAll();
        truckPanel.setBorder(BorderFactory.createTitledBorder("Truck Details (Driver Only)"));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 6, 4, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Truck Number
        gc.gridy = 0;

        gc.gridx = 0;
        gc.weightx = 0;
        truckPanel.add(new JLabel("Truck Number"), gc);

        gc.gridx = 1;
        gc.weightx = 1;
        truckNo.setColumns(22);
        truckPanel.add(truckNo, gc);

        // Row 1: Truck Capacity
        gc.gridy = 1;

        gc.gridx = 0;
        gc.weightx = 0;
        truckPanel.add(new JLabel("Truck Capacity(Kg)"), gc);

        gc.gridx = 1;
        gc.weightx = 1;
        truckCap.setColumns(22);
        truckPanel.add(truckCap, gc);

        truckPanel.setVisible(u.role == Role.DRIVER);
        form.add(truckPanel);


        JButton update = new JButton("Update");
        update.setBackground(Color.GREEN);
        update.setForeground(Color.BLACK);
        update.setFocusPainted(false);
        update.setOpaque(true);
        JButton close = new JButton("Back to Dashboard");
        close.setBackground(Color.YELLOW);
        close.setForeground(Color.BLACK);
        close.setFocusPainted(false);
        close.setOpaque(true);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER ,10,8));
        bottom.add(update);
        bottom.add(close);

        add(form, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        close.addActionListener(e -> dispose());
        update.addActionListener(e -> doUpdate());
    }

    private JPanel labeled(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(6,6));
        p.add(new JLabel(label), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void doUpdate() {
        try {
            UserDAO dao = new UserDAO();
            User u = AppSession.currentUser;

            String typedEmail = email.getText().trim();
            if (!typedEmail.equalsIgnoreCase(u.email)) {
                JOptionPane.showMessageDialog(this,
                        "Email cannot be changed. Please keep the same email.",
                        "Update Error",
                        JOptionPane.ERROR_MESSAGE);
                email.setText(u.email); // reset to original
                return;
            }

            u.phone = phone.getText().trim();
            u.firstName = first.getText().trim();
            u.lastName = last.getText().trim();

            String newPass = new String(pass.getPassword()).trim();
            if (!newPass.isEmpty()) u.passwordHash = PasswordUtil.sha256(newPass);

            if (u.role == Role.DRIVER) {
                String tNo = truckNo.getText().trim();
                String capStr = truckCap.getText().trim();

                if (tNo.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Truck number is required for Driver.",
                            "Update Error",
                            JOptionPane.ERROR_MESSAGE);
                    truckNo.requestFocus();
                    return;
                }

                if (capStr.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Truck capacity is required for Driver.",
                            "Update Error",
                            JOptionPane.ERROR_MESSAGE);
                    truckCap.requestFocus();
                    return;
                }

                int cap;
                try {
                    cap = Integer.parseInt(capStr);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            "Truck capacity must be a valid number (Kg).",
                            "Update Error",
                            JOptionPane.ERROR_MESSAGE);
                    truckCap.requestFocus();
                    return;
                }

                if (cap <= 0) {
                    JOptionPane.showMessageDialog(this,
                            "Truck capacity must be greater than 0 Kg.",
                            "Update Error",
                            JOptionPane.ERROR_MESSAGE);
                    truckCap.requestFocus();
                    return;
                }

                // only set if valid
                u.truckNumber = tNo;
                u.truckCapacityKg = cap;
            }


            dao.update(u);
            AppSession.currentUser = dao.findById(u.id);

            JOptionPane.showMessageDialog(this, "Profile updated.");
           
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Update Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
