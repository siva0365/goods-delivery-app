package com.deliveryapp.ui;

import com.deliveryapp.dao.DeliveryDAO;
import com.deliveryapp.model.Delivery;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PreviousOrdersDialog extends JDialog {

    public PreviousOrdersDialog(JFrame owner, int customerId) {
        super(owner, "My Previous Orders", true);
        setSize(760, 360);
        setLocationRelativeTo(owner);

        DefaultTableModel tm = new DefaultTableModel(
                new Object[]{"ID","Product","Qty (Kg)","Date","Address","Status"}, 0
        ) { public boolean isCellEditable(int r,int c){ return false; } };

        JTable table = new JTable(tm);
     // Make table header bold
        table.getTableHeader().setFont(
                table.getTableHeader().getFont().deriveFont(Font.BOLD)
        );


        try {
            List<Delivery> list = new DeliveryDAO().listByCustomer(customerId);
            for (Delivery d : list) {
                tm.addRow(new Object[]{d.id, d.productName, d.qtyKg, d.deliveryDate, d.address, d.status});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }

        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton back = new JButton("Back to Dashboard");
        back.setBackground(Color.YELLOW);
        back.setForeground(Color.BLACK);
        back.setFocusPainted(false);
        back.setOpaque(true);


        back.addActionListener(e -> dispose());
       

        // Centered bottom panel
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        bottom.add(back);
        add(bottom, BorderLayout.SOUTH);

    }
}
