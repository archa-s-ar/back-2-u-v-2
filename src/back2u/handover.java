package back2u;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class handover extends JFrame {

    private JTextArea detailsArea;
    private JButton loadButton;
    private JButton enterPinButton;
    private int matchId;
    
    private int handoverId = -1;
    private String correctPin = "";

    public handover(int matchId) {
        
        this.matchId=matchId;
        setTitle("Back2U - Handover Details");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Top panel
        JPanel topPanel = new JPanel(new FlowLayout());

        JLabel titleLabel = new JLabel("Handover Details");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));

        loadButton = new JButton("Load Handover Details");

        topPanel.add(titleLabel);
        topPanel.add(loadButton);

        // Details area
        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(detailsArea);

        // Bottom panel
        JPanel bottomPanel = new JPanel(new FlowLayout());

        enterPinButton = new JButton("Enter Handover PIN");
        enterPinButton.setEnabled(false);

        bottomPanel.add(enterPinButton);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        loadButton.addActionListener(e -> loadDetails());
        enterPinButton.addActionListener(e -> openPinWindow());
    }

    
private void loadDetails() {
    String query =
        "SELECT h.handover_id, h.meeting_location, " +
        "h.meeting_date, h.meeting_time, h.instructions, " +
        "h.status, h.pin " +
        "FROM handover h " +
        "WHERE h.match_id = ?";

    try (Connection con = new Conn().c) {
        if (con == null) {
            JOptionPane.showMessageDialog(
                this, "Database connection failed.");
            return;
        }

        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, matchId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    handoverId = rs.getInt("handover_id");
                    correctPin = rs.getString("pin");

                    String status = rs.getString("status");

                    detailsArea.setText(
                        "HANDOVER ARRANGEMENT\n" +
                        "--------------------\n" +
                        "Location: " + rs.getString("meeting_location") + "\n" +
                        "Date: " + rs.getDate("meeting_date") + "\n" +
                        "Time: " + rs.getString("meeting_time") + "\n" +
                        "Instructions: " + rs.getString("instructions") + "\n\n" +
                        "Status: " + status
                    );

                    enterPinButton.setEnabled(
                        "PENDING".equalsIgnoreCase(status));

                } else {
                    handoverId = -1;
                    correctPin = "";
                    detailsArea.setText(
                        "No handover arrangement has been created for this match.");
                    enterPinButton.setEnabled(false);
                }
            }
        }
    } catch (SQLException ex) {
        showError("Could not load handover details: " + ex.getMessage());
    }
}

private void showError(String message) {
    JOptionPane.showMessageDialog(
        this, message, "Handover Error", JOptionPane.ERROR_MESSAGE);
}


    private void openPinWindow() {

        if (handoverId == -1 || correctPin.isEmpty()) {
            JOptionPane.showMessageDialog(
                this, "Please load valid handover details first.");
            return;
        }

        JDialog pinDialog =
            new JDialog(this, "Handover PIN Verification", true);

        pinDialog.setSize(350, 180);
        pinDialog.setLocationRelativeTo(this);
        pinDialog.setLayout(new GridLayout(4, 1, 5, 5));

        JLabel promptLabel = new JLabel(
            "Enter the 6-digit Handover PIN:",
            SwingConstants.CENTER
        );

        JPasswordField pinField = new JPasswordField();
        JButton verifyButton =
            new JButton("Verify & Complete Handover");

        pinDialog.add(promptLabel);
        pinDialog.add(pinField);
        pinDialog.add(new JLabel(""));
        pinDialog.add(verifyButton);

        verifyButton.addActionListener(e -> {

            String enteredPin =
                new String(pinField.getPassword()).trim();

            if (!enteredPin.matches("\\d{6}")) {
                JOptionPane.showMessageDialog(
                    pinDialog,
                    "Please enter a valid 6-digit PIN.",
                    "Invalid PIN",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (enteredPin.equals(correctPin)) {

                if (updateHandoverStatus()) {
                    pinDialog.dispose();
                    loadDetails();
                }

            } else {
                JOptionPane.showMessageDialog(
                    pinDialog,
                    "Incorrect PIN! Please try again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });

        pinDialog.setVisible(true);
    }

    private boolean updateHandoverStatus() {

    String handoverSql =
        "UPDATE handover " +
        "SET status = 'COMPLETED', returned_at = NOW() " +
        "WHERE handover_id = ? AND status = 'PENDING'";

    String matchSql =
        "UPDATE item_matches " +
        "SET match_status = 'COMPLETED' " +
        "WHERE match_id = ? " +
        "AND match_status = 'VERIFICATION' " +
        "AND owner_response = 'CONFIRMED' " +
        "AND finder_response = 'CONFIRMED'";

    String itemsSql =
        "UPDATE items " +
        "SET status = 'COMPLETED' " +
        "WHERE item_id IN (?, ?) " +
        "AND status = 'MATCHED'";

    try (Connection con = new Conn().c) {

        if (con == null) {
            showError("Database connection failed.");
            return false;
        }

        con.setAutoCommit(false);

        try {
            // 1. Complete the handover
            try (PreparedStatement ps =
                     con.prepareStatement(handoverSql)) {

                ps.setInt(1, handoverId);

                if (ps.executeUpdate() != 1) {
                    throw new SQLException(
                        "Handover is already completed or unavailable.");
                }
            }

            // 2. Complete the match
            try (PreparedStatement ps =
                     con.prepareStatement(matchSql)) {

                ps.setInt(1, matchId);

                if (ps.executeUpdate() != 1) {
                    throw new SQLException(
                        "Match is not in the expected verification state.");
                }
            }

            // 3. Complete both items
            String getItemsSql =
                "SELECT lost_item_id, found_item_id " +
                "FROM item_matches WHERE match_id = ?";

            int lostItemId;
            int foundItemId;

            try (PreparedStatement ps =
                     con.prepareStatement(getItemsSql)) {

                ps.setInt(1, matchId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Match not found.");
                    }

                    lostItemId = rs.getInt("lost_item_id");
                    foundItemId = rs.getInt("found_item_id");
                }
            }

            try (PreparedStatement ps =
                     con.prepareStatement(itemsSql)) {

                ps.setInt(1, lostItemId);
                ps.setInt(2, foundItemId);

                if (ps.executeUpdate() != 2) {
                    throw new SQLException(
                        "Could not update both item statuses.");
                }
            }

            con.commit();

            JOptionPane.showMessageDialog(
                this,
                "Handover completed successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE
            );

            return true;

        } catch (SQLException ex) {
            con.rollback();
            showError("Handover completion failed: " + ex.getMessage());
            return false;

        } finally {
            con.setAutoCommit(true);
        }

    } catch (SQLException ex) {
        showError("Database error: " + ex.getMessage());
        return false;
    }
}

    public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        new handover(48).setVisible(true);
    });
}
}