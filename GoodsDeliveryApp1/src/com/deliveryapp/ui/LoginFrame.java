package com.deliveryapp.ui;

import com.deliveryapp.AppSession;
import com.deliveryapp.dao.UserDAO;
import com.deliveryapp.model.Role;
import com.deliveryapp.model.User;
import com.deliveryapp.util.PasswordUtil;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final JTextField email = new JTextField(22);
    private final JPasswordField pass = new JPasswordField(22);

    public LoginFrame() {
        super("Goods Delivery Application - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(420, 260);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));

        JLabel title = new JLabel("Goods Delivery Application", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        root.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(0,1,8,8));
        form.add(labeled("Email", email));
        form.add(labeled("Password", pass));
        root.add(form, BorderLayout.CENTER);

        JButton login = new JButton("Login");
        login.setBackground(Color.BLUE);
        login.setForeground(Color.yellow);
        login.setOpaque(true);
        getRootPane().setDefaultButton(login);

        login.setBorderPainted(false);
        JButton register = new JButton("Register");
        register.setBackground(Color.RED);
        register.setForeground(Color.white);
        register.setOpaque(true);
        register.setBorderPainted(false);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER,50,0));
        actions.add(login);
        actions.add(register);
        root.add(actions, BorderLayout.SOUTH);

        setContentPane(root);

        register.addActionListener(e -> {
            dispose();
            new RegisterFrame().setVisible(true);
        });

        login.addActionListener(e -> doLogin());
    }

    private JPanel labeled(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(6,6));
        p.add(new JLabel(label), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void doLogin() {
        try {
            String em = email.getText().trim();
            String pwPlain = new String(pass.getPassword()).trim();

            // 1) Required field validation (NEW)
            if (em.isEmpty() || pwPlain.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Email and password are required.",
                        "Login",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // 2) Your existing hashing + login logic (same behavior)
            String ph = PasswordUtil.sha256(pwPlain);
            User u = new UserDAO().login(em, ph);

            if (u == null) {
                JOptionPane.showMessageDialog(this, "Invalid credentials.", "Login", JOptionPane.ERROR_MESSAGE);
                return;
            }

            AppSession.currentUser = u;
            dispose();

            if (u.role == Role.CUSTOMER) new CustomerDashboardFrame().setVisible(true);
            else if (u.role == Role.SCHEDULER) new SchedulerDashboardFrame().setVisible(true);
            else new DriverDashboardFrame().setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
