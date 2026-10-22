
package back2u;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class ReportFounditem extends JFrame implements ActionListener {

    private static final int REVIEW_THRESHOLD = 50;

    private JComboBox<String> category;
    private JTextField itemText, dateText, timeText, locationText;
    private JTextArea descriptionText;
    private JButton cancel, submit;

    private final int userId;

    public ReportFounditem(int userId) {

        this.userId = userId;

        setTitle("Report Found Item");
        setSize(900, 760);
        setLocationRelativeTo(null);
        setLayout(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JLabel heading = new JLabel("Report Found Item");
        heading.setFont(new Font("Arial", Font.BOLD, 26));
        heading.setHorizontalAlignment(SwingConstants.CENTER);
        heading.setBounds(40, 20, 440, 40);
        add(heading);

        JLabel subheading = new JLabel(
                "Fill in the details below to report the item you found.");
        subheading.setBounds(40, 60, 650, 25);
        add(subheading);

        JLabel section1 = new JLabel("Item Information");
        section1.setFont(new Font("Raleway", Font.BOLD, 20));
        section1.setBounds(40, 120, 250, 30);
        add(section1);

        JLabel categoryLabel = new JLabel("Item Category:");
        categoryLabel.setFont(new Font("Raleway", Font.BOLD, 16));
        categoryLabel.setBounds(40, 170, 130, 25);
        add(categoryLabel);

        category = new JComboBox<>(new String[]{
            "Select Category",
            "Electronics",
            "Bags",
            "Books",
            "Clothing",
            "Accessories",
            "Others"
        });
        category.setBounds(180, 170, 280, 30);
        add(category);

        JLabel itemNameLabel = new JLabel("Item Name:");
        itemNameLabel.setFont(new Font("Raleway", Font.BOLD, 16));
        itemNameLabel.setBounds(40, 220, 110, 30);
        add(itemNameLabel);

        itemText = new JTextField();
        itemText.setBounds(180, 220, 280, 30);
        add(itemText);

        JLabel descriptionLabel = new JLabel("Description:");
        descriptionLabel.setFont(new Font("Raleway", Font.BOLD, 16));
        descriptionLabel.setBounds(40, 270, 110, 30);
        add(descriptionLabel);

        descriptionText = new JTextArea();
        descriptionText.setLineWrap(true);
        descriptionText.setWrapStyleWord(true);

        JScrollPane descriptionScroll =
                new JScrollPane(descriptionText);
        descriptionScroll.setBounds(180, 270, 280, 75);
        add(descriptionScroll);

        JLabel section2 = new JLabel("Found Details");
        section2.setFont(new Font("Raleway", Font.BOLD, 20));
        section2.setBounds(40, 370, 300, 30);
        add(section2);

        JLabel dateLabel = new JLabel("Date Found:");
        dateLabel.setFont(new Font("Raleway", Font.BOLD, 16));
        dateLabel.setBounds(40, 415, 110, 30);
        add(dateLabel);

        dateText = new JTextField();
        dateText.setToolTipText("Enter date as yyyy-MM-dd");
        dateText.setBounds(180, 415, 280, 30);
        add(dateText);

        JLabel timeLabel = new JLabel("Approximate Time:");
        timeLabel.setFont(new Font("Raleway", Font.BOLD, 16));
        timeLabel.setBounds(40, 460, 145, 30);
        add(timeLabel);

        timeText = new JTextField();
        timeText.setToolTipText("Example: 10:30 AM");
        timeText.setBounds(180, 460, 280, 30);
        add(timeText);

        JLabel locationLabel = new JLabel("Found Location:");
        locationLabel.setFont(new Font("Raleway", Font.BOLD, 16));
        locationLabel.setBounds(40, 505, 135, 30);
        add(locationLabel);

        locationText = new JTextField();
        locationText.setBounds(180, 505, 280, 30);
        add(locationText);

        JLabel note = new JLabel(
                "<html>Do not include private ownership details. "
                + "The owner can verify the item later.</html>");
        note.setBounds(40, 555, 440, 40);
        add(note);

        cancel = new JButton("Cancel");
        cancel.setBounds(180, 620, 120, 35);
        cancel.addActionListener(this);
        add(cancel);

        submit = new JButton("Submit");
        submit.setBounds(320, 620, 140, 35);
        submit.addActionListener(this);
        add(submit);

        getContentPane().setBackground(Color.WHITE);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == cancel) {

            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Do you want to clear the form and close it?",
                    "Confirm Cancel",
                    JOptionPane.YES_NO_OPTION
            );

            if (choice == JOptionPane.YES_OPTION) {
                clearForm();
                dispose();
            }

        } else if (e.getSource() == submit) {
            submitFoundItem();
        }
    }

    private void submitFoundItem() {

        String itemName = itemText.getText().trim();
        String itemCategory =
                (String) category.getSelectedItem();
        String description = descriptionText.getText().trim();
        String date = dateText.getText().trim();
        String time = timeText.getText().trim();
        String location = locationText.getText().trim();

        if (userId <= 0) {
            showError("Invalid user ID. Please log in again.");
            return;
        }

        if (category.getSelectedIndex() == 0) {
            showError("Please select a category.");
            return;
        }

        if (isBlank(itemName)
                || isBlank(description)
                || isBlank(date)
                || isBlank(time)
                || isBlank(location)) {

            showError("Please fill in all required fields.");
            return;
        }

        final LocalDate foundDate;

        try {
            foundDate = LocalDate.parse(date);

            if (foundDate.isAfter(LocalDate.now())) {
                showError("The found date cannot be in the future.");
                return;
            }

        } catch (DateTimeParseException ex) {
            showError("Enter a valid date in yyyy-MM-dd format.");
            return;
        }

        // Found advertisements do not require unique ownership details.
        // Score the available fields without penalizing the missing field.
        InitialTrustScoreCalculator calculator =
                new InitialTrustScoreCalculator();

        int initialScore = calculator.calculateFoundScore(
        itemName,
        itemCategory,
        description,
        date,
        time,
        location
);

        // Redistribute the missing unique-details points so that a
        // found listing can still earn a score out of 100.
        

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
                + "VALUES (?, 'FOUND', ?, ?, ?, ?, ?, ?, "
                + "'OPEN', NULL, ?, ?, ?)";

        Conn conn = new Conn();

        if (conn.c == null) {
            showError("Database connection failed. Check Conn.java.");
            return;
        }

        try (PreparedStatement ps = conn.c.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, itemName);
            ps.setString(3, itemCategory);
            ps.setString(4, description);
            ps.setDate(5, Date.valueOf(foundDate));
            ps.setString(6, time);
            ps.setString(7, location);
            ps.setInt(8, initialScore);
            ps.setInt(9, trustScore);
            ps.setString(10, reviewStatus);

            int rows = ps.executeUpdate();

            if (rows > 0) {

                String message =
                        "Found item reported successfully!\n"
                        + "Initial trust score: " + trustScore
                        + "/100\n";

                if ("PENDING_REVIEW".equals(reviewStatus)) {
                    message +=
                            "Status: Pending administrator review.";
                } else {
                    message +=
                            "Status: Approved and eligible for matching.";
                }

                JOptionPane.showMessageDialog(
                        this,
                        message,
                        "Report Submitted",
                        JOptionPane.INFORMATION_MESSAGE
                );

                clearForm();
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void clearForm() {
        itemText.setText("");
        descriptionText.setText("");
        dateText.setText("");
        timeText.setText("");
        locationText.setText("");
        category.setSelectedIndex(0);
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
        SwingUtilities.invokeLater(() -> new ReportFounditem(1));
    }
}
