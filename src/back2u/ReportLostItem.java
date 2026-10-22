package back2u;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class ReportLostItem extends JFrame{
    private final int userId;
    JPanel itemPanel,lostPanel,extraPanel;
    JTextField itemName,dateLost,timeLost,locationLost;
    JTextArea description,uniqueDetails;
    JScrollPane scroll,uniqueScroll;
    JButton submit,cancel;
    
    ReportLostItem(int userId) {
        this.userId = userId;
        setTitle("REPORT LOST ITEM"); 
        setLayout(null);
        
        JLabel head = new JLabel("REPORT LOST ITEM");
        head.setBounds(250, 50, 300, 30);
        head.setFont(new Font("Raleway", Font.BOLD, 30));
        add(head);
        
        itemPanel = new JPanel();
        itemPanel.setLayout(null);
        itemPanel.setBounds(50, 100, 800, 230);
        itemPanel.setBackground(Color.LIGHT_GRAY);
        add(itemPanel);

        JLabel itemTitle = new JLabel("Item Information");
        itemTitle.setBounds(20, 10, 250, 25);
        itemTitle.setFont(new Font("Raleway", Font.BOLD, 20));
        itemTitle.setForeground(Color.BLACK);
        itemPanel.add(itemTitle);

        JLabel categoryLabel = new JLabel("Item Category:");
        categoryLabel.setBounds(30, 50, 150, 30);
        categoryLabel.setFont(new Font("Raleway", Font.BOLD, 16));
        itemPanel.add(categoryLabel);

        String categories[] = {
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

        JComboBox<String> categoryBox = new JComboBox<>(categories);
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
        
        lostPanel = new JPanel();
        lostPanel.setLayout(null);
        lostPanel.setBounds(50, 340, 800, 175);
        lostPanel.setBackground(Color.LIGHT_GRAY);
        add(lostPanel);

        JLabel lostTitle = new JLabel("Lost Details");
        lostTitle.setBounds(20, 10, 250, 25);
        lostTitle.setFont(new Font("Raleway", Font.BOLD, 20));
        lostTitle.setForeground(Color.BLACK);
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
        locationLost.setToolTipText("Example: Library, Main Block, Canteen");
        lostPanel.add(locationLost);
        
        extraPanel = new JPanel();
        extraPanel.setLayout(null);
        extraPanel.setBounds(50, 525, 800, 130);
        extraPanel.setBackground(Color.LIGHT_GRAY);
        add(extraPanel);

        JLabel extraTitle = new JLabel("Additional Information");
        extraTitle.setBounds(20, 5, 300, 25);
        extraTitle.setFont(new Font("Raleway", Font.BOLD, 20));
        extraTitle.setForeground(Color.BLACK);
        extraPanel.add(extraTitle);

        JLabel uniqueLabel = new JLabel("Unique Identifying Details:");
        uniqueLabel.setBounds(30, 35, 250, 25);
        uniqueLabel.setFont(new Font("Raleway", Font.BOLD, 16));
        extraPanel.add(uniqueLabel);
        uniqueDetails = new JTextArea();
        uniqueDetails.setLineWrap(true);
        uniqueDetails.setWrapStyleWord(true);
        uniqueScroll = new JScrollPane(uniqueDetails);
        uniqueScroll.setBounds(30, 60, 700, 55);
        extraPanel.add(uniqueScroll);
        
        cancel = new JButton("Cancel");
        cancel.setBounds(480, 660, 150, 35);
        cancel.setFont(new Font("Raleway", Font.BOLD, 16));
        add(cancel);

        submit = new JButton("Submit Report");
        submit.setBounds(650, 660, 200, 35);
        submit.setFont(new Font("Raleway", Font.BOLD, 16));
        add(submit);

        cancel.addActionListener(e -> dispose());

        submit.addActionListener(e -> {

        String selectedCategory = (String) categoryBox.getSelectedItem();
        String name = itemName.getText().trim();
        String itemDescription = description.getText().trim();
        String date = dateLost.getText().trim();
        String time = timeLost.getText().trim();
        String location = locationLost.getText().trim();
        String details = uniqueDetails.getText().trim();

        if (categoryBox.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Please select a category.");
        }
        else if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the item name.");
        }
        else if (itemDescription.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the description.");
        }
        else if (date.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the date lost.");
        }
        else if (time.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the approximate time.");
        }
        else if (location.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the location lost.");
        }
        else if (details.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter unique identifying details.");
        }          
        else {
            Conn conn = new Conn();

            if (conn.c == null) {
                JOptionPane.showMessageDialog(this,"Database connection failed. Please check Conn.java.");
            } 
            else {
                String sql = "INSERT INTO items "
                    + "(user_id, report_type, item_name, category, "
                    + "description, date_occurred, time_occurred, "
                    + "location, unique_details) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement ps = conn.c.prepareStatement(sql)) {
                    ps.setInt(1, userId);
                    ps.setString(2, "LOST");
                    ps.setString(3, name);
                    ps.setString(4, selectedCategory);
                    ps.setString(5, itemDescription);
                    ps.setDate(6, Date.valueOf(date));
                    ps.setString(7, time);
                    ps.setString(8, location);
                    ps.setString(9, details);

                    int rows = ps.executeUpdate();

                    if (rows > 0) {
                        JOptionPane.showMessageDialog(this,"Lost item report saved successfully!");
                    }
                } 
                catch (SQLException | IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this,"Could not save report: " + ex.getMessage());
                    ex.printStackTrace();
                } 
                finally {
                    try {
                        if (conn.s != null) conn.s.close();
                        if (conn.c != null) conn.c.close();
                    } 
                    catch (SQLException ex) {
                    ex.printStackTrace();
                    }
                }
            }
        }
    });
  
        getContentPane().setBackground(Color.WHITE);
        setSize(900, 780);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String args[]){
        SwingUtilities.invokeLater(() -> new ReportLostItem(1));
    }
}
