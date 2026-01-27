package com.deliveryapp.ui;

import com.deliveryapp.dao.DeliveryDAO;
import com.deliveryapp.dao.RouteDAO;
import com.deliveryapp.model.Delivery;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AssignRouteDialog extends JDialog {

    private final JTextField dateField = new JTextField(LocalDate.now().toString(), 10);
    private final JTextField warehouse = new JTextField("123 Main Street, Anytown, USA 12345", 28);

    private final JComboBox<DriverItem> drivers = new JComboBox<>();

    private final DefaultTableModel deliveriesTM = new DefaultTableModel(
            new Object[]{"Delivery ID","Address","Product","Qty","Status"}, 0
    ) { public boolean isCellEditable(int r,int c){ return false; } };

    private final DefaultTableModel selectedTM = new DefaultTableModel(
            new Object[]{"Stop Order","Delivery ID","Address","Qty"}, 0
    ) { public boolean isCellEditable(int r,int c){ return false; } };


    public AssignRouteDialog(JFrame owner) {
        super(owner, "Assign route to drivers", true);
        setSize(980, 520);
        setLocationRelativeTo(owner);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Route Date:"));
        top.add(dateField);
        top.add(new JLabel("Warehouse:"));
        top.add(warehouse);

        JButton refresh = new JButton("Refresh Deliveries");
        top.add(refresh);

        loadDrivers();
        



        JPanel driverPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        driverPanel.add(new JLabel("Select Driver:"));
        driverPanel.add(drivers);

        JTable deliveriesTable = new JTable(deliveriesTM);
        JTable selectedTable = new JTable(selectedTM);

        JButton add = new JButton("Add Selected Delivery ->");
        JButton remove = new JButton("<- Remove Stop");
        JButton up = new JButton("Move Up");
        JButton down = new JButton("Move Down");

        JPanel midButtons = new JPanel(new GridLayout(0,1,8,8));
        midButtons.add(add);
        midButtons.add(remove);
        midButtons.add(up);
        midButtons.add(down);

        JPanel center = new JPanel(new GridLayout(1,3,10,10));
        center.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        center.add(new JScrollPane(deliveriesTable));
        center.add(midButtons);
        center.add(new JScrollPane(selectedTable));

        JButton save = new JButton("Save Route");
        save.setBackground(Color.GREEN);
        save.setForeground(Color.BLACK);
        save.setFocusPainted(false);
        save.setOpaque(true);
        JButton back = new JButton("Back to Dashboard");
        back.setBackground(Color.YELLOW);
        back.setForeground(Color.BLACK);
        back.setFocusPainted(false);
        back.setOpaque(true);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(back);
        bottom.add(save);

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(driverPanel, BorderLayout.WEST);
        add(center, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        refresh.addActionListener(e -> loadDeliveries());

        add.addActionListener(e -> {
            int row = deliveriesTable.getSelectedRow();
            if (row < 0) return;

            String status = String.valueOf(deliveriesTM.getValueAt(row, 4)).trim().toUpperCase();
            if (!status.equals("PENDING") && !status.equals("REQUESTED") && !status.equals("NEW")) {
                JOptionPane.showMessageDialog(this, "Only pending deliveries can be assigned.");
                return;
            }

            if (drivers.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Please select a driver first.");
                return;
            }

            int deliveryId = Integer.parseInt(String.valueOf(deliveriesTM.getValueAt(row, 0)));
            String addr = String.valueOf(deliveriesTM.getValueAt(row, 1));

            int newQty;
            try {
                newQty = Integer.parseInt(String.valueOf(deliveriesTM.getValueAt(row, 3)));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid delivery quantity.");
                return;
            }



            // ✅ 1) Do not allow duplicates
            for (int i = 0; i < selectedTM.getRowCount(); i++) {
                int already = Integer.parseInt(String.valueOf(selectedTM.getValueAt(i, 1)));
                if (already == deliveryId) {
                    JOptionPane.showMessageDialog(this, "This delivery is already added to the route.");
                    return;
                }
            }

            // ✅ 2) capacity check using selectedTM qty column (index 3)
            DriverItem di = (DriverItem) drivers.getSelectedItem();
            int capacity = di.capacityKg;

            int total = 0;
            for (int i = 0; i < selectedTM.getRowCount(); i++) {
                total += Integer.parseInt(String.valueOf(selectedTM.getValueAt(i, 3)));
            }

            if (capacity > 0 && total + newQty > capacity) {
                JOptionPane.showMessageDialog(this,
                        "Cannot add this delivery.\nTruck capacity: " + capacity + " Kg\nSelected total: " + total + " Kg\nThis delivery: " + newQty + " Kg",
                        "Capacity exceeded",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // ✅ 3) Add to selectedTM (include qty)
            int nextOrder = selectedTM.getRowCount() + 1;
            selectedTM.addRow(new Object[]{nextOrder, deliveryId, addr, newQty});

            // ✅ 4) Remove immediately from pending deliveries (disappear)
            deliveriesTM.removeRow(row);
        });



        remove.addActionListener(e -> {
            int row = selectedTable.getSelectedRow();
            if (row < 0) return;
            selectedTM.removeRow(row);
            renumberStops();
        });



        up.addActionListener(e -> moveRow(selectedTable, -1));
        down.addActionListener(e -> moveRow(selectedTable, +1));

        save.addActionListener(e -> doSave());
        back.addActionListener(e -> dispose());

        loadDeliveries();
    }

    private void moveRow(JTable table, int delta) {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int newRow = row + delta;
        if (newRow < 0 || newRow >= selectedTM.getRowCount()) return;

        Object[] a = new Object[]{
                selectedTM.getValueAt(row,0),
                selectedTM.getValueAt(row,1),
                selectedTM.getValueAt(row,2)
        };
        selectedTM.removeRow(row);
        selectedTM.insertRow(newRow, a);
        renumberStops();
        table.setRowSelectionInterval(newRow, newRow);
    }

    private void renumberStops() {
        for (int i=0; i<selectedTM.getRowCount(); i++) {
            selectedTM.setValueAt(i+1, i, 0);
        }
    }

    private void loadDrivers() {
        drivers.removeAllItems(); // ✅ clear old items
        try {
            RouteDAO dao = new RouteDAO();
            for (Object[] row : dao.listDrivers()) {
                int id = (int) row[0];
                String label = (String) row[1];
                int capKg = row[2] == null ? 0 : ((Number) row[2]).intValue();
                drivers.addItem(new DriverItem(id, label, capKg));
            }
            drivers.setSelectedIndex(-1); // ✅ no default selection
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void loadDeliveries() {
        deliveriesTM.setRowCount(0);
        try {
            LocalDate d = LocalDate.parse(dateField.getText().trim());
            List<Delivery> list = new DeliveryDAO().listByDate(d);

            for (Delivery x : list) {
                String st = (x.status == null) ? "" : x.status.trim().toUpperCase();

                // ✅ show only pending-like deliveries
                // Change these values to match your DB statuses
                boolean isPending =
                        st.equals("PENDING") ||
                        st.equals("REQUESTED") ||
                        st.equals("NEW");

                if (!isPending) continue;

                deliveriesTM.addRow(new Object[]{x.id, x.address, x.productName, x.qtyKg, x.status});

            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    


    private void doSave() {
        try {
            if (drivers.getSelectedItem() == null) throw new IllegalArgumentException("Select a driver.");
            if (selectedTM.getRowCount() == 0) throw new IllegalArgumentException("Add at least one delivery to the route.");

            DriverItem di = (DriverItem) drivers.getSelectedItem();
            LocalDate d = LocalDate.parse(dateField.getText().trim());
            String wh = warehouse.getText().trim();
            if (wh.isEmpty()) throw new IllegalArgumentException("Warehouse address is required.");

            RouteDAO rdao = new RouteDAO();
            DeliveryDAO ddao = new DeliveryDAO();

            int routeId = rdao.createRoute(di.id, d, wh);

            for (int i=0; i<selectedTM.getRowCount(); i++) {
                int stopOrder = (int) selectedTM.getValueAt(i, 0);
                int deliveryId = (int) selectedTM.getValueAt(i, 1);
                rdao.addStop(routeId, deliveryId, stopOrder);
                ddao.updateStatus(deliveryId, "ASSIGNED");
            }

            rdao.createMissionForRoute(routeId);

            JOptionPane.showMessageDialog(this, "Route + mission saved!");
            

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class DriverItem {
        final int id;
        final String label;
        final int capacityKg;

        DriverItem(int id, String label, int capacityKg){
            this.id = id;
            this.label = label;
            this.capacityKg = capacityKg;
        }

        public String toString(){
            return label + " (Cap: " + capacityKg + " Kg)";
        }
    }

}
