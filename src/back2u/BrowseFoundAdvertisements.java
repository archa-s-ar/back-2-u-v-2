
package back2u;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class BrowseFoundAdvertisements extends JFrame {

    private final int userId;
    private final JTable table;
    private final DefaultTableModel model;

    public BrowseFoundAdvertisements(int userId) {
        this.userId = userId;

        setTitle("Back2U - Browse Found Advertisements");
        setSize(1050, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JLabel heading = new JLabel(
                "Browse Found Advertisements",
                SwingConstants.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 22));
        heading.setBorder(
                BorderFactory.createEmptyBorder(15, 10, 10, 10));
        add(heading, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{
            "Item ID", "Item Name", "Category", "Description",
            "Location", "Trust Score", "Average Rating"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(25);
        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton rateButton = new JButton("Rate Advertisement");
        JButton refreshButton = new JButton("Refresh");
        JButton closeButton = new JButton("Close");

        JPanel buttons = new JPanel();
        buttons.add(rateButton);
        buttons.add(refreshButton);
        buttons.add(closeButton);

        add(buttons, BorderLayout.SOUTH);

        rateButton.addActionListener(e -> rateSelectedAdvertisement());
        refreshButton.addActionListener(e -> loadAdvertisements());
        closeButton.addActionListener(e -> dispose());

        loadAdvertisements();
    }

    private void loadAdvertisements() {
        model.setRowCount(0);

        String sql =
                "SELECT i.item_id, i.item_name, i.category, "
                + "i.description, i.location, i.trust_score, "
                + "AVG(r.rating) AS average_rating "
                + "FROM items i "
                + "LEFT JOIN advertisement_ratings r "
                + "ON r.item_id = i.item_id "
                + "WHERE i.report_type = 'FOUND' "
                + "AND i.status = 'OPEN' "
                + "AND i.ad_review_status = 'APPROVED' "
                + "AND i.user_id <> ? "
                + "GROUP BY i.item_id, i.item_name, i.category, "
                + "i.description, i.location, i.trust_score "
                + "ORDER BY i.created_at DESC";

        try (Connection con = new Conn().c) {

            if (con == null) {
                throw new SQLException(
                        "Database connection failed.");
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, userId);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        double average =
                                rs.getDouble("average_rating");

                        String ratingText = rs.wasNull()
                                ? "No ratings yet"
                                : String.format("%.1f / 5", average);

                        model.addRow(new Object[]{
                            rs.getInt("item_id"),
                            rs.getString("item_name"),
                            rs.getString("category"),
                            rs.getString("description"),
                            rs.getString("location"),
                            rs.getInt("trust_score") + "/100",
                            ratingText
                        });
                    }
                }
            }

            if (model.getRowCount() == 0) {
                setTitle(
                    "Back2U - Browse Found Advertisements (None available)");
            } else {
                setTitle("Back2U - Browse Found Advertisements");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "Could not load advertisements:\n"
                            + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rateSelectedAdvertisement() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select an advertisement first.");
            return;
        }

        selectedRow = table.convertRowIndexToModel(selectedRow);

        int itemId = Integer.parseInt(
                model.getValueAt(selectedRow, 0).toString());

        String itemName =
                model.getValueAt(selectedRow, 1).toString();

        new RateAdvertisement(
                this, itemId, userId, itemName
        ).setVisible(true);

        loadAdvertisements();
    }
}
