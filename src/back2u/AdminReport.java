
package back2u;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AdminReport extends JFrame {

    private JComboBox<String> issuebox;
    private JTextField subtxt;
    private JTextArea descri;
    private JTextField itemIdField;
    private JButton submit;

    private final int userId;

    public AdminReport(int userId) {
        this.userId = userId;

        setTitle("Back2U - Report an Issue");
        setLayout(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JLabel head = new JLabel("Report an Issue");
        head.setFont(new Font("Raleway", Font.BOLD, 32));
        head.setBounds(150, 70, 500, 40);
        add(head);

        JLabel subhead = new JLabel("Back2U - User Support");
        subhead.setFont(new Font("Raleway", Font.BOLD, 14));
        subhead.setBounds(150, 115, 500, 25);
        add(subhead);

        JLabel subdescri = new JLabel(
                "Tell our admin team about a problem or suspicious listing.");
        subdescri.setFont(new Font("Raleway", Font.PLAIN, 14));
        subdescri.setBounds(150, 145, 650, 25);
        add(subdescri);

        JLabel issuetype = new JLabel("Issue Type");
        issuetype.setFont(new Font("Raleway", Font.BOLD, 16));
        issuetype.setBounds(150, 185, 500, 25);
        add(issuetype);

        String[] valissue = {
            "Technical Issue",
            "Login/Registration Problem",
            "Lost/Found Problem",
            "Incorrect Information",
            "User Account Issue",
            "Suspicious Advertisement",
            "Other"
        };

        issuebox = new JComboBox<>(valissue);
        issuebox.setBounds(150, 215, 500, 30);
        issuebox.addActionListener(e -> updateItemIdVisibility());
        add(issuebox);

        JLabel itemIdLabel = new JLabel("Advertisement Item ID");
        itemIdLabel.setFont(new Font("Raleway", Font.BOLD, 16));
        itemIdLabel.setBounds(150, 255, 500, 25);
        add(itemIdLabel);

        itemIdField = new JTextField();
        itemIdField.setBounds(150, 285, 500, 30);
        add(itemIdField);

        JLabel sub = new JLabel("Subject / Report Reason");
        sub.setFont(new Font("Raleway", Font.BOLD, 16));
        sub.setBounds(150, 325, 500, 25);
        add(sub);

        subtxt = new JTextField();
        subtxt.setBounds(150, 355, 500, 30);
        add(subtxt);

        JLabel descrilab = new JLabel("Description");
        descrilab.setFont(new Font("Raleway", Font.BOLD, 16));
        descrilab.setBounds(150, 395, 500, 25);
        add(descrilab);

        descri = new JTextArea();
        descri.setLineWrap(true);
        descri.setWrapStyleWord(true);

        JScrollPane descriptionScroll = new JScrollPane(descri);
        descriptionScroll.setBounds(150, 425, 500, 130);
        add(descriptionScroll);

        submit = new JButton("Submit Report");
        submit.setBounds(300, 585, 180, 35);
        submit.addActionListener(e -> submitReport());
        add(submit);

        updateItemIdVisibility();

        getContentPane().setBackground(Color.WHITE);
        setSize(900, 750);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private boolean isAdvertisementReport() {
        return "Suspicious Advertisement".equals(
                issuebox.getSelectedItem());
    }

    private void updateItemIdVisibility() {
        boolean visible = isAdvertisementReport();

        itemIdField.setVisible(visible);

        // The label is the component immediately before the field
        // in this form's layout.
        Component[] components = getContentPane().getComponents();

        for (Component component : components) {
            if (component instanceof JLabel
                    && "Advertisement Item ID".equals(
                            ((JLabel) component).getText())) {
                component.setVisible(visible);
                break;
            }
        }

        revalidate();
        repaint();
    }

    private void submitReport() {

        String issue = (String) issuebox.getSelectedItem();
        String subject = subtxt.getText().trim();
        String description = descri.getText().trim();

        if (subject.isEmpty() || description.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter both the subject and description.");
            return;
        }

        if (isAdvertisementReport()) {
            submitAdvertisementReport(subject, description);
        } else {
            submitSupportReport(issue, subject, description);
        }
    }

    private void submitSupportReport(
            String issue, String subject, String description) {

        String sql =
                "INSERT INTO admin_reports "
                + "(user_id, issue_type, subject, description) "
                + "VALUES (?, ?, ?, ?)";

        Conn conn = new Conn();

        if (conn.c == null) {
            showDatabaseError("Database connection failed.");
            return;
        }

        try (PreparedStatement ps = conn.c.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, issue);
            ps.setString(3, subject);
            ps.setString(4, description);

            if (ps.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Your support report has been submitted.");
                clearForm();
            }

        } catch (SQLException ex) {
            showDatabaseError(ex.getMessage());
            ex.printStackTrace();

        } finally {
            closeConnection(conn);
        }
    }

    private void submitAdvertisementReport(
            String reason, String details) {

        int itemId;

        try {
            itemId = Integer.parseInt(itemIdField.getText().trim());

            if (itemId <= 0) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Enter a valid positive advertisement item ID.");
            return;
        }

        Conn conn = new Conn();

        if (conn.c == null) {
            showDatabaseError("Database connection failed.");
            return;
        }

        String checkSql =
                "SELECT item_id FROM items WHERE item_id = ?";

        String insertSql =
                "INSERT INTO advertisement_reports "
                + "(item_id, reporter_user_id, reason, details) "
                + "VALUES (?, ?, ?, ?)";

        try {
            // Verify that the advertised item exists.
            try (PreparedStatement check =
                    conn.c.prepareStatement(checkSql)) {

                check.setInt(1, itemId);

                try (ResultSet rs = check.executeQuery()) {
                    if (!rs.next()) {
                        JOptionPane.showMessageDialog(
                                this,
                                "No advertisement exists with that item ID.");
                        return;
                    }
                }
            }

            try (PreparedStatement ps =
                    conn.c.prepareStatement(insertSql)) {

                ps.setInt(1, itemId);
                ps.setInt(2, userId);
                ps.setString(3, reason);

                if (details.isEmpty()) {
                    ps.setNull(4, Types.VARCHAR);
                } else {
                    ps.setString(4, details);
                }

                if (ps.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Suspicious advertisement reported.\n"
                            + "An administrator can review this report.");

                    clearForm();
                }
            }

        } catch (SQLException ex) {
            showDatabaseError(ex.getMessage());
            ex.printStackTrace();

        } finally {
            closeConnection(conn);
        }
    }

    private void clearForm() {
        subtxt.setText("");
        descri.setText("");
        itemIdField.setText("");
        issuebox.setSelectedIndex(0);
    }

    private void showDatabaseError(String message) {
        JOptionPane.showMessageDialog(
                this,
                "Could not submit report: " + message,
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
    }

    private void closeConnection(Conn conn) {
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminReport(1));
    }
}
