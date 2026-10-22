
package back2u;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class PossibleMatches extends JFrame {

    private JTable table;
    private DefaultTableModel model;
    private int lostItemId;

    public PossibleMatches(int lostItemId) {
        this.lostItemId = lostItemId;

        setTitle("Back2U - Possible Matches");
        setSize(850, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        model = new DefaultTableModel(
            new String[]{
                "Found Item ID", "Item Name", "Category",
                "Description", "Location", "Date", "Match Score"
            }, 0
        ) {
            
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(25);
        table.setAutoCreateRowSorter(true);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadMatches());

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());

        JPanel buttons = new JPanel();
        buttons.add(refreshButton);
        buttons.add(closeButton);

        add(buttons, BorderLayout.SOUTH);

        loadMatches();
    }

    private void loadMatches() {
        model.setRowCount(0);

        try {
            List<ItemMatchingService.MatchResult> matches =
                ItemMatchingService.findMatches(lostItemId);

            if (matches.isEmpty()) {
                JOptionPane.showMessageDialog(
                    this,
                    "No possible matches found yet.",
                    "Back2U",
                    JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }

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
}
