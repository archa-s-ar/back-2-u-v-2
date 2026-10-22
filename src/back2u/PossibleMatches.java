
package back2u;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class PossibleMatches extends JFrame {

    private JTable table;
    private DefaultTableModel model;

    private final int lostItemId;
    private final int userId;

    // Backward-compatible constructor for existing calls.
    // Rating is unavailable when the logged-in user's ID is not provided.
    public PossibleMatches(int lostItemId) {
        this(lostItemId, -1);
    }

    // Constructor for the logged-in user.
    public PossibleMatches(int lostItemId, int userId) {

        this.lostItemId = lostItemId;
        this.userId = userId;

        setTitle("Back2U - Possible Matches");
        setSize(950, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        model = new DefaultTableModel(
                new String[]{
                    "Found Item ID",
                    "Item Name",
                    "Category",
                    "Description",
                    "Location",
                    "Date",
                    "Match Score"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(25);
        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );
        table.setAutoCreateRowSorter(true);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadMatches());

        JButton rateButton = new JButton("Rate Advertisement");
        rateButton.setEnabled(userId > 0);
        rateButton.addActionListener(e -> rateSelectedAdvertisement());

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttons.add(refreshButton);
        buttons.add(rateButton);
        buttons.add(closeButton);

        add(buttons, BorderLayout.SOUTH);

        loadMatches();
    }

    private void loadMatches() {

        model.setRowCount(0);

        try {
            List<ItemMatchingService.MatchResult> matches =
                    ItemMatchingService.findMatches(lostItemId);

            for (ItemMatchingService.MatchResult match : matches) {
                model.addRow(new Object[]{
                    match.foundItemId,
                    match.foundItemName,
                    match.category,
                    match.description,
                    match.location,
                    match.date,
                    match.score + "/100"
                });
            }

            if (matches.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "No approved possible matches found yet.",
                        "Back2U",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

        } catch (SQLException ex) {
            ex.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "Could not load matches. Check the database connection.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void rateSelectedAdvertisement() {

        if (userId <= 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Your user ID is unavailable. Please log in again.",
                    "Rating Unavailable",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select an advertisement to rate.",
                    "No Advertisement Selected",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        // Convert the visible row to its model row because sorting is enabled.
        int modelRow = table.convertRowIndexToModel(selectedRow);
        int foundItemId = (int) model.getValueAt(modelRow, 0);

        AdvertisementRatingPanel ratingPanel =
                new AdvertisementRatingPanel(foundItemId, userId);

        JDialog dialog = new JDialog(
                this,
                "Rate Advertisement",
                true
        );

        dialog.setDefaultCloseOperation(
                JDialog.DISPOSE_ON_CLOSE
        );

        dialog.add(ratingPanel);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(350, 170));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
