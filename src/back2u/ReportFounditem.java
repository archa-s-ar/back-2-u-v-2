
package back2u;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class ReportFounditem extends JFrame {

    private final int userId;

    private JTextField itemName, dateFound, timeFound, location;
    private JComboBox<String> category;
    private JTextArea description;
    private JButton submit, cancel;

    public ReportFounditem(int userId) {
        this.userId = userId;

        setTitle("Back2U - Report Found Item");
        setSize(600, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        buildForm();
        setVisible(true);
    }

    private void buildForm() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel heading = new JLabel(
                "Report a Found Item", SwingConstants.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 24));
        mainPanel.add(heading, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        itemName = new JTextField(20);

        category = new JComboBox<>(new String[]{
            "Electronics", "Documents", "Wallet/Purse",
            "Keys", "Clothing", "Bags", "Jewellery",
            "Books", "Accessories", "Other"
        });

        description = new JTextArea(3, 20);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);

        dateFound = new JTextField(LocalDate.now().toString(), 20);
        timeFound = new JTextField(
                LocalTime.now().withSecond(0).withNano(0).toString(), 20);
        location = new JTextField(20);

        int row = 0;
        addField(formPanel, gbc, row++, "Item name:", itemName);
        addField(formPanel, gbc, row++, "Category:", category);
        addField(formPanel, gbc, row++, "Description:",
                new JScrollPane(description));
        addField(formPanel, gbc, row++, "Date found (YYYY-MM-DD):",
                dateFound);
        addField(formPanel, gbc, row++, "Time found (HH:MM):",
                timeFound);
        addField(formPanel, gbc, row++, "Location:", location);

        JLabel hint = new JLabel(
                "Use 24-hour time, for example 14:30.");
        hint.setForeground(Color.GRAY);
        gbc.gridx = 1;
        gbc.gridy = row;
        formPanel.add(hint, gbc);

        mainPanel.add(new JScrollPane(formPanel), BorderLayout.CENTER);

        submit = new JButton("Submit Found Report");
        cancel = new JButton("Cancel");

        JPanel buttons = new JPanel();
        buttons.add(submit);
        buttons.add(cancel);
        mainPanel.add(buttons, BorderLayout.SOUTH);

        submit.addActionListener(e -> saveFoundItem());
        cancel.addActionListener(e -> dispose());

        setContentPane(mainPanel);
    }

    private void addField(
            JPanel panel, GridBagConstraints gbc,
            int row, String label, Component field) {

        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
    }

    private void saveFoundItem() {
        String name = itemName.getText().trim();
        String selectedCategory = (String) category.getSelectedItem();
        String itemDescription = description.getText().trim();
        String foundDate = dateFound.getText().trim();
        String time = timeFound.getText().trim();
        String itemLocation = location.getText().trim();

        if (name.isEmpty() || itemDescription.isEmpty()
                || foundDate.isEmpty() || time.isEmpty()
                || itemLocation.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this, "Please complete all fields.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        java.sql.Date sqlDate;

        try {
            sqlDate = java.sql.Date.valueOf(LocalDate.parse(foundDate));
            LocalTime.parse(time);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Enter a valid date (YYYY-MM-DD) and time (HH:MM).",
                    "Invalid Date or Time", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (selectedCategory == null) {
            JOptionPane.showMessageDialog(this, "Select a category.");
            return;
        }

        final int trustScore;

        try {
            InitialTrustScoreCalculator calculator =
                    new InitialTrustScoreCalculator();

            // Found-item reports do not require unique identifying details.
            // An empty string is supplied to the calculator.
            trustScore = Math.max(0, Math.min(100,
                    calculator.calculateScore(
                            name, selectedCategory, itemDescription,
                            foundDate, time, itemLocation, "")));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this, "Could not calculate trust score: "
                            + ex.getMessage(),
                    "Scoring Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String reviewStatus =
                trustScore < 50 ? "PENDING_REVIEW" : "APPROVED";

        String sql =
                "INSERT INTO items " +
                "(user_id, report_type, item_name, category, " +
                "description, date_occurred, time_occurred, location, " +
                "status, unique_details, initial_trust_score, " +
                "trust_score, ad_review_status) " +
                "VALUES (?, 'FOUND', ?, ?, ?, ?, ?, ?, 'OPEN', NULL, ?, ?, ?)";

        Conn conn = null;

        try {
            conn = new Conn();

            if (conn.c == null) {
                throw new SQLException("Database connection unavailable.");
            }

            try (PreparedStatement ps = conn.c.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setString(2, name);
                ps.setString(3, selectedCategory);
                ps.setString(4, itemDescription);
                ps.setDate(5, sqlDate);
                ps.setString(6, time);
                ps.setString(7, itemLocation);
                ps.setInt(8, trustScore);
                ps.setInt(9, trustScore);
                ps.setString(10, reviewStatus);

                int rowsInserted = ps.executeUpdate();

                if (rowsInserted == 0) {
                    throw new SQLException("The report could not be saved.");
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this, "Database error:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            return;

        } finally {
            closeConnection(conn);
        }

        JOptionPane.showMessageDialog(
                this,
                "Found-item report submitted successfully.\n"
                        + "Initial trust score: " + trustScore + "/100\n"
                        + (trustScore < 50
                            ? "Status: Pending administrator review."
                            : "Status: Approved for matching."),
                "Report Submitted",
                JOptionPane.INFORMATION_MESSAGE);

        dispose();
    }

    private void closeConnection(Conn conn) {
        if (conn == null) {
            return;
        }

        try {
            if (conn.s != null) {
                conn.s.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        try {
            if (conn.c != null) {
                conn.c.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
