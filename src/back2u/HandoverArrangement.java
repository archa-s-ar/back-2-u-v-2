
package back2u;

import javax.swing.*;
import java.awt.*;
import java.security.SecureRandom;
import java.sql.*;
import java.time.LocalDate;

public class HandoverArrangement extends JFrame {

    private final int matchId;
    private final int ownerUserId;

    private JTextField locationField;
    private JTextField dateField;
    private JTextField timeField;
    private JTextArea instructionsArea;

    public HandoverArrangement(int matchId, int ownerUserId) {
        this.matchId = matchId;
        this.ownerUserId = ownerUserId;

        setTitle("Back2U - Arrange Handover");
        setSize(500, 470);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 6, 8, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        JLabel heading = new JLabel("Arrange Item Handover");
        heading.setFont(new Font("Arial", Font.BOLD, 22));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        form.add(heading, gbc);

        gbc.gridwidth = 1;

        locationField = new JTextField();
        dateField = new JTextField();
        timeField = new JTextField();
        instructionsArea = new JTextArea(4, 20);
        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);

        addRow(form, gbc, 1, "Meeting Location:", locationField);
        addRow(form, gbc, 2, "Meeting Date (YYYY-MM-DD):", dateField);
        addRow(form, gbc, 3, "Meeting Time:", timeField);

        gbc.gridx = 0;
        gbc.gridy = 4;
        form.add(new JLabel("Instructions:"), gbc);

        gbc.gridx = 1;
        form.add(new JScrollPane(instructionsArea), gbc);

        JButton saveButton = new JButton("Save Handover Arrangement");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        form.add(saveButton, gbc);

        saveButton.addActionListener(e -> saveArrangement());

        add(form);
    }

    private void addRow(
            JPanel panel, GridBagConstraints gbc,
            int row, String label, JComponent field) {

        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void saveArrangement() {
        String location = locationField.getText().trim();
        String dateText = dateField.getText().trim();
        String time = timeField.getText().trim();
        String instructions = instructionsArea.getText().trim();

        if (location.isEmpty() || dateText.isEmpty() || time.isEmpty()) {
            JOptionPane.showMessageDialog(
                this, "Please enter the location, date and time.");
            return;
        }

        LocalDate meetingDate;

        try {
            meetingDate = LocalDate.parse(dateText);

            if (meetingDate.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(
                    this, "Meeting date cannot be in the past.");
                return;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this, "Enter the date in YYYY-MM-DD format.");
            return;
        }

        // Generate a six-digit PIN.
        String pin = String.format(
            "%06d", new SecureRandom().nextInt(1_000_000));

        String sql =
            "INSERT INTO handover " +
            "(match_id, meeting_location, meeting_date, " +
            "meeting_time, instructions, pin, status) " +
            "SELECT im.match_id, ?, ?, ?, ?, ?, 'PENDING' " +
            "FROM item_matches im " +
            "JOIN items lost ON lost.item_id = im.lost_item_id " +
            "WHERE im.match_id = ? " +
            "AND lost.user_id = ? " +
            "AND im.match_status = 'VERIFICATION' " +
            "AND im.owner_response = 'CONFIRMED' " +
            "AND im.finder_response = 'CONFIRMED' " +
            "AND NOT EXISTS (" +
            "SELECT 1 FROM handover h " +
            "WHERE h.match_id = im.match_id)";

        try (Connection con = new Conn().c) {
            if (con == null) {
                throw new SQLException("Database connection failed.");
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, location);
                ps.setDate(2, Date.valueOf(meetingDate));
                ps.setString(3, time);
                ps.setString(4, instructions);
                ps.setString(5, pin);
                ps.setInt(6, matchId);
                ps.setInt(7, ownerUserId);

                //int rows = ps.executeUpdate();
                int rows = ps.executeUpdate();

                System.out.println("DEBUG - Match ID: " + matchId);
                System.out.println("DEBUG - Owner User ID: " + ownerUserId);
                System.out.println("DEBUG - Rows inserted: " + rows);

                if (rows == 1) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Handover arrangement saved!\n\n" +
                        "Your handover PIN is: " + pin +
                        "\nShare it securely with the finder " +
                        "when appropriate.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(
                        this,
                        "Could not create the arrangement. " +
                        "Check that you own this confirmed match " +
                        "and that an arrangement does not already exist.");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                this,
                "Database error: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
