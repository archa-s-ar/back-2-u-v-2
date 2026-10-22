
package back2u;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class DetailsMatch extends JFrame {
    
    private int ownerUserId;
    private int matchId;

    private int lostItemId;
    private int foundItemId;

    private JLabel itemName = new JLabel();
    private JLabel category = new JLabel();
    private JLabel description = new JLabel();
    private JLabel dateLost = new JLabel();
    private JLabel timeLost = new JLabel();
    private JLabel locationLost = new JLabel();

    private JLabel finderName = new JLabel();
    private JLabel finderStudentId = new JLabel();
    private JLabel finderPhone = new JLabel();

    JPanel itemPanel, finderPanel, buttonPanel;

    public DetailsMatch(
        int lostItemId, int foundItemId,int matchId, int ownerUserId) {

        this.lostItemId = lostItemId;
        this.foundItemId = foundItemId;
        this.matchId = matchId;
        this.ownerUserId = ownerUserId;

        setTitle("DETAILS MATCH");
        setLayout(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JLabel heading = new JLabel("DETAILS MATCHED!");
        heading.setBounds(180, 25, 400, 40);
        heading.setFont(new Font("Raleway", Font.BOLD, 28));
        heading.setForeground(new Color(30, 140, 70));
        add(heading);

        itemPanel = new JPanel();
        itemPanel.setLayout(null);
        itemPanel.setBounds(40, 85, 600, 285);
        itemPanel.setBackground(Color.LIGHT_GRAY);
        add(itemPanel);

        JLabel itemTitle = new JLabel("Lost Item Information");
        itemTitle.setBounds(20, 10, 300, 30);
        itemTitle.setFont(new Font("Raleway", Font.BOLD, 20));
        itemPanel.add(itemTitle);

        addDetail(itemPanel, "Item Name:", itemName, 55);
        addDetail(itemPanel, "Category:", category, 90);
        addDetail(itemPanel, "Description:", description, 125);
        addDetail(itemPanel, "Date Lost:", dateLost, 160);
        addDetail(itemPanel, "Time:", timeLost, 195);
        addDetail(itemPanel, "Location:", locationLost, 230);

        finderPanel = new JPanel();
        finderPanel.setLayout(null);
        finderPanel.setBounds(40, 385, 600, 165);
        finderPanel.setBackground(Color.LIGHT_GRAY);
        add(finderPanel);

        JLabel finderTitle = new JLabel("Finder Information");
        finderTitle.setBounds(20, 10, 300, 30);
        finderTitle.setFont(new Font("Raleway", Font.BOLD, 20));
        finderPanel.add(finderTitle);

        addDetail(finderPanel, "Finder Name:", finderName, 50);
        addDetail(finderPanel, "Student ID:", finderStudentId, 85);
        addDetail(finderPanel, "Phone Number:", finderPhone, 120);

        JButton contactButton = new JButton("Contact Finder for Handover");
        contactButton.setBounds(200, 580, 300, 40);
        contactButton.setFont(new Font("Raleway", Font.BOLD, 15));
        add(contactButton);

        JButton cancelButton = new JButton("Cancel Proceeding");
        cancelButton.setBounds(220, 630, 250, 40);
        cancelButton.setFont(new Font("Raleway", Font.BOLD, 15));
        add(cancelButton);

        contactButton.addActionListener(e -> {
    if ("Not found".equals(finderName.getText())
            || "Not linked to a user".equals(finderName.getText())) {
        JOptionPane.showMessageDialog(
            this, "Finder information is unavailable.");
        return;
    }

    new HandoverArrangement(matchId, ownerUserId).setVisible(true);
});

        cancelButton.addActionListener(e -> {
            int answer = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to cancel proceeding?",
                    "Cancel Proceeding",
                    JOptionPane.YES_NO_OPTION);

            if (answer == JOptionPane.YES_OPTION) {
                dispose();
            }
        });

        getContentPane().setBackground(Color.WHITE);
        setSize(700, 750);
        setLocationRelativeTo(null);
        loadDetails();
        setVisible(true);
    }

    private void addDetail(
            JPanel panel, String text, JLabel value, int y) {

        JLabel label = new JLabel(text);
        label.setBounds(25, y, 175, 25);
        label.setFont(new Font("Raleway", Font.BOLD, 15));
        panel.add(label);

        value.setBounds(205, y, 370, 25);
        value.setFont(new Font("Raleway", Font.PLAIN, 15));
        panel.add(value);
    }

    private void loadDetails() {
        Conn conn = new Conn();
        if (conn.c == null) {
            JOptionPane.showMessageDialog(this,"Database connection failed. Check Conn.java.");
            dispose();
            return;
        }
        try {
            String itemQuery =
                    "SELECT item_name, category, description, "
                    + "date_occurred, time_occurred, location "
                    + "FROM items "
                    + "WHERE item_id = ? AND report_type = 'LOST'";

            try (PreparedStatement ps = conn.c.prepareStatement(itemQuery)) {
                ps.setInt(1, lostItemId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        itemName.setText(rs.getString("item_name"));
                        category.setText(rs.getString("category"));
                        description.setText(rs.getString("description"));
                        dateLost.setText(String.valueOf(rs.getDate("date_occurred")));
                        timeLost.setText(rs.getString("time_occurred"));
                        locationLost.setText(rs.getString("location"));
                    } 
                    else {
                        JOptionPane.showMessageDialog(this,"LOST item not found.");
                        dispose();
                        return;
                    }
                }
            }

            String finderQuery =
        "SELECT a.name, a.student_id, a.phone, i.user_id "
        + "FROM items i "
        + "LEFT JOIN accountdetails a ON a.user_id = i.user_id "
        + "WHERE i.item_id = ? "
        + "AND i.report_type = 'FOUND'";
            
       

try (PreparedStatement ps = conn.c.prepareStatement(finderQuery)) {
    ps.setInt(1, foundItemId);

    try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
            String name = rs.getString("name");

            if (name != null) {
                finderName.setText(name);
                finderStudentId.setText(rs.getString("student_id"));
                finderPhone.setText(rs.getString("phone"));
            } else {
                finderName.setText("Not linked to a user");
                finderStudentId.setText("Not available");
                finderPhone.setText("Not available");

                System.out.println(
                        "Found item user_id: " + rs.getObject("user_id"));
            }
        } else {
            finderName.setText("Not found");
            finderStudentId.setText("Not found");
            finderPhone.setText("Not found");

            JOptionPane.showMessageDialog(
                    this,
                    "Found item ID " + foundItemId
                    + " does not exist or is not a FOUND item.");
        }
    }
}   
        } 
        catch (SQLException e) {
            JOptionPane.showMessageDialog(this,"Database error: " + e.getMessage());
            e.printStackTrace();
        } 
        finally {
            try {
                if (conn.s != null) {
                    conn.s.close();
                }
                if (conn.c != null) {
                    conn.c.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new DetailsMatch(1, 3, 1, 1);
        });
    }
}

