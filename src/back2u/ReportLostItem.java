
package back2u;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class ReportLostItem extends JFrame {

    private static final int REVIEW_THRESHOLD = 50;

    private final int userId;

    private JPanel itemPanel, lostPanel, extraPanel;
    private JTextField itemName, dateLost, timeLost, locationLost;
    private JTextArea description, uniqueDetails;
    private JScrollPane scroll, uniqueScroll;
    private JButton submit, cancel;
    private JComboBox<String> categoryBox;

    public ReportLostItem(int userId) {

        this.userId = userId;

        setTitle("REPORT LOST ITEM");
        setLayout(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JLabel head = new JLabel("REPORT LOST ITEM");
        head.setBounds(250, 50, 400, 30);
        head.setFont(new Font("Raleway", Font.BOLD, 30));
        add(head);

        // Item information panel
        itemPanel = new JPanel();
        itemPanel.setLayout(null);
        itemPanel.setBounds(50, 100, 800, 230);
        itemPanel.setBackground(Color.LIGHT_GRAY);
        add(itemPanel);

        JLabel itemTitle = new JLabel("Item Information");
        itemTitle.setBounds(20, 10, 250, 25);
        itemTitle.setFont(new Font("Raleway", Font.BOLD, 20));
        itemPanel.add(itemTitle);

        JLabel categoryLabel = new JLabel("Item Category:");
        categoryLabel.setBounds(30, 50, 150, 30);
        categoryLabel.setFont(new Font("Raleway", Font.BOLD, 16));
        itemPanel.add(categoryLabel);

        String[] categories = {
            "Select Category",
            "Electronics",
            "Bags",
            "Books & Stationery",
            "Clothing",
            "ID Cards",
            "Keys",
            "Wallets",
            "Other"
        };

        categoryBox = new JComboBox<>(categories);
        categoryBox.setBounds(220, 50, 450, 30);
        itemPanel.add(categoryBox);

        JLabel nameLabel = new JLabel("Item Name:");
        nameLabel.setBounds(30, 90, 150, 30);
        nameLabel.setFont(new Font("Raleway", Font.BOLD, 16));
        itemPanel.add(nameLabel);

        itemName = new JTextField();
        itemName.setBounds(220, 90, 450, 30);
        itemPanel.add(itemName);

        JLabel descriptionLabel = new JLabel("Description:");
        descriptionLabel.setBounds(30, 130, 150, 30);
        descriptionLabel.setFont(new Font("Raleway", Font.BOLD, 16));
        itemPanel.add(descriptionLabel);

        description = new JTextArea();
        description.setLineWrap(true);
        description.setWrapStyleWord(true);

        scroll = new JScrollPane(description);
        scroll.setBounds(220, 130, 450, 80);
        itemPanel.add(scroll);

        // Lost details panel
        lostPanel = new JPanel();
        lostPanel.setLayout(null);
        lostPanel.setBounds(50, 340, 800, 175);
        lostPanel.setBackground(Color.LIGHT_GRAY);
        add(lostPanel);

        JLabel lostTitle = new JLabel("Lost Details");
        lostTitle.setBounds(20, 10, 250, 25);
        lostTitle.setFont(new Font("Raleway", Font.BOLD, 20));
        lostPanel.add(lostTitle);

        JLabel dateLabel = new JLabel("Date Lost:");
        dateLabel.setBounds(30, 45, 150, 25);
        dateLabel.setFont(new Font("Raleway", Font.BOLD, 16));
        lostPanel.add(dateLabel);

        dateLost = new JTextField();
        dateLost.setBounds(220, 45, 450, 30);
        dateLost.setToolTipText("Enter date as yyyy-mm-dd");
        lostPanel.add(dateLost);

        JLabel timeLabel = new JLabel("Approximate Time:");
        timeLabel.setBounds(30, 85, 180, 25);
        timeLabel.setFont(new Font("Raleway", Font.BOLD, 16));
        lostPanel.add(timeLabel);

        timeLost = new JTextField();
        timeLost.setBounds(220, 85, 450, 30);
        timeLost.setToolTipText("Example: 10:30 AM");
        lostPanel.add(timeLost);

        JLabel locationLabel = new JLabel("Location Lost:");
        locationLabel.setBounds(30, 125, 150, 25);
        locationLabel.setFont(new Font("Raleway", Font.BOLD, 16));
        lostPanel.add(locationLabel);

        locationLost = new JTextField();
        locationLost.setBounds(220, 125, 450, 30);
        locationLost.setToolTipText(
                "Example: Library, Main Block, Canteen");
        lostPanel.add(locationLost);

        // Additional information panel
        extraPanel = new JPanel();
        extraPanel.setLayout(null);
        extraPanel.setBounds(50, 525, 800, 130);
        extraPanel.setBackground(Color.LIGHT_GRAY);
        add(extraPanel);

        JLabel extraTitle = new JLabel("Additional Information");
        extraTitle.setBounds(20, 5, 300, 25);
        extraTitle.setFont(new Font("Raleway", Font.BOLD, 20));
        extraPanel.add(extraTitle);

        JLabel uniqueLabel =
                new JLabel("Unique Identifying Details:");
        uniqueLabel.setBounds(30, 35, 250, 25);
        uniqueLabel.setFont(new Font("Raleway", Font.BOLD, 16));
        extraPanel.add(uniqueLabel);

        uniqueDetails = new JTextArea();
        uniqueDetails.setLineWrap(true);
        uniqueDetails.setWrapStyleWord(true);

        uniqueScroll = new JScrollPane(uniqueDetails);
        uniqueScroll.setBounds(30, 60, 700, 55);
        extraPanel.add(uniqueScroll);

        // Buttons
        cancel = new JButton("Cancel");
        cancel.setBounds(480, 660, 150, 35);
        cancel.setFont(new Font("Raleway", Font.BOLD, 16));
        add(cancel);

        submit = new JButton("Submit Report");
        submit.setBounds(650, 660, 200, 35);
        submit.setFont(new Font("Raleway", Font.BOLD, 16));
        add(submit);

        cancel.addActionListener(e -> dispose());
        submit.addActionListener(e -> submitLostItem());

        getContentPane().setBackground(Color.WHITE);
        setSize(900, 780);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void submitLostItem() {

        String selectedCategory =
                (String) categoryBox.getSelectedItem();

        String name = itemName.getText().trim();
        String itemDescription = description.getText().trim();
        String date = dateLost.getText().trim();
        String time = timeLost.getText().trim();
        String location = locationLost.getText().trim();
        String details = uniqueDetails.getText().trim();

        // Validate required fields
        if (categoryBox.getSelectedIndex() == 0) {
            showError("Please select a category.");
            return;
        }

        if (name.isEmpty()) {
            showError("Please enter the item name.");
            return;
        }

        if (itemDescription.isEmpty()) {
            showError("Please enter the description.");
            return;
        }

        if (date.isEmpty()) {
            showError("Please enter the date lost.");
            return;
        }

        if (time.isEmpty()) {
            showError("Please enter the approximate time.");
            return;
        }

        if (location.isEmpty()) {
            showError("Please enter the location lost.");
            return;
        }

        if (details.isEmpty()) {
            showError(
                    "Please enter unique identifying details.");
            return;
        }

        if (userId <= 0) {
            showError("Invalid user ID. Please log in again.");
            return;
        }

        // Validate date format and prevent future dates.
        final Date sqlDate;

        try {
            LocalDate parsedDate = LocalDate.parse(date);

            if (parsedDate.isAfter(LocalDate.now())) {
                showError("The lost date cannot be in the future.");
                return;
            }

            sqlDate = Date.valueOf(parsedDate);

        } catch (DateTimeParseException ex) {
            showError("Invalid date. Use yyyy-mm-dd.");
            return;
        }

        // Calculate initial listing-quality score.
        InitialTrustScoreCalculator calculator =
                new InitialTrustScoreCalculator();

        int initialScore = calculator.calculateScore(
                name,
                selectedCategory,
                itemDescription,
                date,
                time,
                location,
                details
        );

        int trustScore = initialScore;

        String reviewStatus =
                trustScore < REVIEW_THRESHOLD
                        ? "PENDING_REVIEW"
                        : "APPROVED";

        String sql =
                "INSERT INTO items "
                + "(user_id, report_type, item_name, category, "
                + "description, date_occurred, time_occurred, "
                + "location, status, unique_details, "
                + "initial_trust_score, trust_score, "
                + "ad_review_status) "
                + "VALUES (?, 'LOST', ?, ?, ?, ?, ?, ?, "
                + "'OPEN', ?, ?, ?, ?)";

        Conn conn = new Conn();

        if (conn.c == null) {
            showError(
                    "Database connection failed. "
                    + "Please check Conn.java.");
            return;
        }

        try (PreparedStatement ps = conn.c.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, userId);
            ps.setString(2, name);
            ps.setString(3, selectedCategory);
            ps.setString(4, itemDescription);
            ps.setDate(5, sqlDate);
            ps.setString(6, time);
            ps.setString(7, location);
            ps.setString(8, details);
            ps.setInt(9, initialScore);
            ps.setInt(10, trustScore);
            ps.setString(11, reviewStatus);

            int rows = ps.executeUpdate();

            if (rows > 0) {

                int newItemId = -1;

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        newItemId = keys.getInt(1);
                    }
                }

                String message =
                        "Lost item report saved successfully!\n"
                        + "Initial trust score: " + trustScore
                        + "/100\n";

                if ("PENDING_REVIEW".equals(reviewStatus)) {
                    message +=
                            "Status: Pending administrator review.";
                } else {
                    message +=
                            "Status: Approved and eligible for matching.";
                }

                if (newItemId > 0) {
                    message += "\nReport ID: " + newItemId;
                }

                JOptionPane.showMessageDialog(
                        this,
                        message,
                        "Report Submitted",
                        JOptionPane.INFORMATION_MESSAGE
                );

                dispose();
            }

        } catch (SQLException ex) {

            showError("Could not save report: " + ex.getMessage());
            ex.printStackTrace();

        } finally {

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

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Validation Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ReportLostItem(1));
    }
}
