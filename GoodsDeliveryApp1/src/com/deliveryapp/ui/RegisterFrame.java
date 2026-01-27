package com.deliveryapp.ui;

import com.deliveryapp.dao.UserDAO;
import com.deliveryapp.model.Role;
import com.deliveryapp.model.User;
import com.deliveryapp.util.PasswordUtil;

import javax.swing.*;
import java.awt.*;

public class RegisterFrame extends JFrame {

    private final JTextField email = new JTextField(22);
    private final JPasswordField pass = new JPasswordField(22);
    private final JTextField phone = new JTextField(22);

    private final JTextField firstName = new JTextField(22);
    private final JTextField lastName = new JTextField(22);

    private final JComboBox<Role> role = new JComboBox<>(Role.values());

    // ✅ Only change: truckPanel uses GridBagLayout so it fits properly without changing your frame size/layout
    private final JPanel truckPanel = new JPanel(new GridBagLayout());
    private final JTextField truckNo = new JTextField(22);
    private final JTextField truckCap = new JTextField(22);

    public RegisterFrame() {
        super("Create Account");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(460, 520);            // ✅ keep your original size
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Create New Account", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        root.add(title, BorderLayout.NORTH);

        // ✅ keep your original layout
        JPanel form = new JPanel(new GridLayout(0, 1, 8, 8));
        form.add(labeled("Email", email));
        form.add(labeled("Password (min 5 characters)", pass));
        form.add(labeled("Phone Number", phone));
        form.add(labeled("First Name", firstName));
        form.add(labeled("Last Name", lastName));
        form.add(labeled("Role", role));

        // ✅ Truck panel (Driver only) - compact 2 rows so it is fully visible in same dimensions
        truckPanel.setBorder(BorderFactory.createTitledBorder(""));

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
        truckPanel.add(new JLabel("Truck Capacity (Kg)"), gc);

        gc.gridx = 1;
        gc.weightx = 1;
        truckCap.setColumns(22);
        truckPanel.add(truckCap, gc);

        form.add(truckPanel);

        root.add(form, BorderLayout.CENTER);

        JButton create = new JButton("Create Account");
        create.setBackground(Color.BLUE);
        create.setForeground(Color.WHITE);
        create.setOpaque(true);
        create.setBorderPainted(false);
        JButton back = new JButton("Back to Login");
        back.setBackground(Color.YELLOW);
        back.setForeground(Color.BLACK);
        back.setOpaque(true);
        back.setBorderPainted(false);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER,50,0)); // ✅ keep your alignment
        actions.add(create);
        actions.add(back);
        root.add(actions, BorderLayout.SOUTH);

        setContentPane(root);

        // ✅ show/hide truck fields based on role
        role.addActionListener(e -> refreshTruckVisibility());
        refreshTruckVisibility();

        back.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        create.addActionListener(e -> doCreate());
    }

    private void refreshTruckVisibility() {
        Role r = (Role) role.getSelectedItem();
        boolean isDriver = (r == Role.DRIVER);

        truckPanel.setVisible(isDriver);

        // Optional: clear when not driver so old values don't remain
        if (!isDriver) {
            truckNo.setText("");
            truckCap.setText("");
        }

        revalidate();
        repaint();
    }

    private JPanel labeled(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.add(new JLabel(label), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void doCreate() {
        try {
            UserDAO dao = new UserDAO();

            String em = email.getText().trim();
            if (em.isEmpty()) {
                throw new IllegalArgumentException("Email is required.");
            }

            // ✅ Gmail-only email validation
            if (!em.matches("^[a-zA-Z0-9._%+-]+@gmail\\.com$")) {
                throw new IllegalArgumentException("Please enter a valid Gmail address (example@gmail.com).");
            }

            if (dao.emailExists(em)) {
                throw new IllegalArgumentException("Email already exists.");
            }


            String pw = new String(pass.getPassword()).trim();

	         // ✅ 1) required check first
	         if (pw.isEmpty()) {
	             throw new IllegalArgumentException("Password is required.");
	         }
	
	         // ✅ 2) min length check second (min 5)
	         if (pw.length() < 5) {
	             throw new IllegalArgumentException("Password is too short.");
	         }

	         String ph = phone.getText().trim();

		      // ✅ 1) required check
		      if (ph.isEmpty()) {
		          throw new IllegalArgumentException("Phone number is required.");
		      }
	
		      // ✅ 2) numeric-only check
		      if (!ph.matches("\\d+")) {
		          throw new IllegalArgumentException("Phone number must contain only digits.");
		      }


            Role r = (Role) role.getSelectedItem();
            if (r == null) throw new IllegalArgumentException("Role is required.");

            User u = new User();
            u.email = em;
            u.passwordHash = PasswordUtil.sha256(pw);
            u.phone = ph;
            String fn = firstName.getText().trim();
            if (fn.isEmpty()) throw new IllegalArgumentException("First name is required.");
            u.firstName = fn;
            u.lastName = lastName.getText().trim();
            u.role = r;

            // ✅ If Driver, truck details must be entered
            if (r == Role.DRIVER) {
                String tNo = truckNo.getText().trim();
                if (tNo.isEmpty()) throw new IllegalArgumentException("Truck number required for Driver.");

                String capStr = truckCap.getText().trim();
                if (capStr.isEmpty()) throw new IllegalArgumentException("Truck capacity required for Driver.");

                int cap;
                try {
                    cap = Integer.parseInt(capStr);
                } catch (Exception ex) {
                    throw new IllegalArgumentException("Truck capacity must be a number (Kg).");
                }
                if (cap <= 0) throw new IllegalArgumentException("Truck capacity must be greater than 0 Kg.");

                u.truckNumber = tNo;
                u.truckCapacityKg = cap;
            } else {
                u.truckNumber = null;
                u.truckCapacityKg = null;
            }

            dao.create(u);

            JOptionPane.showMessageDialog(this, "Account created successfully.");
            dispose();
            new LoginFrame().setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Register Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
