package com.deliveryapp.ui;

import com.deliveryapp.AppSession;
import com.deliveryapp.dao.RouteDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DriverMissionsDialog extends JDialog {

    public DriverMissionsDialog(JFrame owner, boolean completed) {
        super(owner, completed ? "Completed Missions" : "Assigned Missions", true);
        setSize(820, 360);
        setLocationRelativeTo(owner);

        DefaultTableModel tm = new DefaultTableModel(
                new Object[]{
                        "Mission ID",
                        "Route Date",
                        "Warehouse",
                        "Customer",
                        "Customer Address",
                        "Products",
                        "Total Qty (Kg)",
                        "Status",
                        "Delivery IDs" // ✅ hidden
                }, 0
        ) { public boolean isCellEditable(int r,int c){ return false; } };



        JTable table = new JTable(tm);
        table.getTableHeader().setFont(
                table.getTableHeader().getFont().deriveFont(Font.BOLD)
        );
        int hiddenCol = tm.getColumnCount() - 1;
        table.getColumnModel().getColumn(hiddenCol).setMinWidth(0);
        table.getColumnModel().getColumn(hiddenCol).setMaxWidth(0);
        table.getColumnModel().getColumn(hiddenCol).setWidth(0);


     // Make Products column wider
        int productsCol = 5; 
        table.getColumnModel().getColumn(productsCol).setPreferredWidth(250);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override // tooi tip that is showing full content under mouse bar
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col >= 0) {
                    Object v = table.getValueAt(row, col);
                    table.setToolTipText(v == null ? null : v.toString());
                } else {
                    table.setToolTipText(null);
                }
            }
        });

        

        JButton close = new JButton("Back to Dashboard");
        close.setBackground(Color.RED);
        close.setForeground(Color.WHITE);
        close.setFocusPainted(false);
        close.setOpaque(true);
        JButton mark = new JButton("Mark as Complete");
        mark.setBackground(Color.YELLOW);
        mark.setForeground(Color.BLACK);
        mark.setFocusPainted(false);
        mark.setOpaque(true);
        mark.setEnabled(!completed);

    

        // Bottom center panel
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        if (!completed) bottom.add(mark);
        bottom.add(close);


        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        load(tm, completed);

        close.addActionListener(e -> dispose());

        mark.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;

            // ✅ last column = Delivery IDs (hidden)
            String deliveryIds = String.valueOf(tm.getValueAt(row, tm.getColumnCount() - 1));

            try {
                new RouteDAO().markDeliveriesCompleted(deliveryIds);
                JOptionPane.showMessageDialog(this, "Selected delivery completed!");
                tm.removeRow(row); // remove only selected row
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });


    }

    private void load(DefaultTableModel tm, boolean completed) {
        try {
            List<Object[]> rows = new RouteDAO().listAssignedMissionsForDriver(AppSession.currentUser.id, completed);
            for (Object[] r : rows) tm.addRow(r);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
