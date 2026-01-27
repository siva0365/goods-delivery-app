package com.deliveryapp.ui;

import com.deliveryapp.dao.DeliveryDAO;
import com.deliveryapp.model.Delivery;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class SchedulerDeliveriesDialog extends JDialog {

    public SchedulerDeliveriesDialog(JFrame owner) {
        super(owner, "Delivery Details by Date", true);
        setSize(860, 420);
        setLocationRelativeTo(owner);

        JTextField dateField = new JTextField(LocalDate.now().toString(), 10);
        JButton load = new JButton("Load");

        DefaultTableModel tm = new DefaultTableModel(
                new Object[]{"ID","Date","Customer","Product","Qty","Address","Status"}, 0
        ) { public boolean isCellEditable(int r,int c){ return false; } };

        JTable table = new JTable(tm);
        table.getTableHeader().setFont(
                table.getTableHeader().getFont().deriveFont(Font.BOLD)
        );
        JButton back = new JButton("Back to Dashboard");
        back.setBackground(Color.YELLOW);
        back.setForeground(Color.BLACK);
        back.setFocusPainted(false);
        back.setOpaque(true);
        
        back.addActionListener(e -> dispose());



        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Date (yyyy-mm-dd):"));
        top.add(dateField);
        top.add(load);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        bottom.add(back);
        add(bottom, BorderLayout.SOUTH);


        load.addActionListener(e -> {
            tm.setRowCount(0);
            try {
                LocalDate d = LocalDate.parse(dateField.getText().trim());
                List<Delivery> list = new DeliveryDAO().listByDate(d);
                for (Delivery x : list) {
                    tm.addRow(new Object[]{x.id, x.deliveryDate, x.customerName, x.productName, x.qtyKg, x.address, x.status});
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
