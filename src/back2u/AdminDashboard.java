
package back2u;

import java.awt.Color;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.awt.event.*;
import javax.swing.table.DefaultTableModel;

public class AdminDashboard extends JFrame implements ActionListener{
    private JTable userTable, lostItemsTable, foundItemsTable,reportTable;
    private JLabel totalUsersCount,lostReportsCount,foundReportsCount;
    private CardLayout cardLayout;
    private JPanel contentCards;
    private JButton updateStatusBtn,refreshBtn;
    private JComboBox statusBox;

            AdminDashboard(){
                setTitle("Back2U - Admin Dashboard");
                setLayout(new BorderLayout(10,5));
                
                /*HEAD PANE*/
                JPanel panel1 = new JPanel(new BorderLayout(20, 0));
                panel1.setBackground(new Color(30, 41, 59));
                
                JLabel logo =new JLabel("Back2U");
                logo.setFont(new Font("Raleway",Font.BOLD,26));
                logo.setForeground(Color.WHITE);
                logo.setBorder(BorderFactory.createEmptyBorder(0,20,0,0));
                
                JLabel admin = new JLabel("Admin Dashboard");
                admin.setFont(new Font("Raleway", Font.BOLD, 26));
                admin.setForeground(Color.WHITE);
                admin.setHorizontalAlignment(SwingConstants.RIGHT);
                admin.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
                
                panel1.add(logo, BorderLayout.WEST);
                panel1.add(admin, BorderLayout.CENTER);
                panel1.setPreferredSize(new Dimension(250, 100));
                add(panel1, BorderLayout.NORTH);
                
                
                /*SIDE PANEL*/
                
            JPanel sidepanel = new JPanel();
            sidepanel.setLayout(new BoxLayout(sidepanel, BoxLayout.Y_AXIS));
            sidepanel.setBackground(new Color(30, 41, 59));
            sidepanel.setPreferredSize(new Dimension(220, 250));

            String[] menuItems = {"Dashboard","Lost Items","Found Items","Reports","Logout"};

        for (String item : menuItems) {
            JButton button = new JButton(item);
            
            button.setFont(new Font("Raleway", Font.PLAIN, 16));
            button.setForeground(Color.WHITE);
            button.setBackground(new Color(30, 41, 59));
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            button.setFocusPainted(false);
            button.setBorderPainted(true);
            
            button.setActionCommand(item);
            button.addActionListener(this);
            
            sidepanel.add(button);
            }

            add(sidepanel, BorderLayout.WEST);

            
            /*MAIN CONTENT AREA*/
            JPanel mainContent = new JPanel();
            mainContent.setBackground(new Color(245, 247, 250));
            mainContent.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
            
            
                
            //SUMMARY CARDS//
            JPanel summary = new JPanel();
            summary.setLayout(new GridLayout(1,3,15,0)); //1 row, 3 column, equal sized cell with 10px gap
            summary.setBackground(new Color(245, 247, 250));
            
            totalUsersCount=new JLabel("0");
            lostReportsCount=new JLabel("0");
            foundReportsCount=new JLabel("0");
            
            JPanel userscard=createCard("Total Users", totalUsersCount);
            JPanel lostItemsCard=createCard("Lost Reports",lostReportsCount);
            JPanel foundItemsCard=createCard("Found Reports", foundReportsCount);
            
            summary.add(userscard);
            summary.add(lostItemsCard);
            summary.add(foundItemsCard);
            
            
            mainContent.setLayout(new BorderLayout(10, 20));

        //Create Refresh button
            refreshBtn = new JButton("Refresh");
            refreshBtn.setFont(new Font("Raleway", Font.BOLD, 14));
            refreshBtn.setBackground(new Color(91, 72, 157));
            refreshBtn.setForeground(Color.WHITE);
            refreshBtn.setFocusPainted(false);
            refreshBtn.addActionListener(e -> refreshDashboard());

         //Create the top panel
            JPanel topPanel = new JPanel(new BorderLayout(10, 10));
            topPanel.setBackground(new Color(245, 247, 250));
            topPanel.add(summary, BorderLayout.CENTER);

            //Place Refresh button on the right
            JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            refreshPanel.setBackground(new Color(245, 247, 250));
            refreshPanel.add(refreshBtn);

            topPanel.add(refreshPanel, BorderLayout.SOUTH);

            //Add the complete panel to the dashboard
            mainContent.add(topPanel, BorderLayout.NORTH);

            
            /*REFRESH BUTTON*/
            refreshBtn = new JButton("Refresh");
            refreshBtn.setFont(new Font("Raleway", Font.BOLD, 14));
            refreshBtn.setBackground(new Color(91, 72, 157));
            refreshBtn.setForeground(Color.WHITE);
            refreshBtn.setFocusPainted(false);

            refreshBtn.addActionListener(e -> refreshDashboard());

            /*TABLE*/
            
        userTable = createTable(new String[]{"Name", "Student ID", "Email", "Phone"});

        lostItemsTable = createTable(new String[]{"Item ID", "Item Name", "Category", "Description","Location", "Reported By", "Date Reported","Time Reported", "Unique Identification Features", "Status"});

        foundItemsTable = createTable(new String[]{"Item ID", "Item Name", "Category", "Description","Location", "Reported By", "Date Reported","Time Reported", "Status"});
        
        reportTable = createTable(new String[]{"Report ID", "Reported By", "Issue Type","Subject", "Description", "Date Reported", "Status"});
            
            
            JScrollPane scrollPane = new JScrollPane(userTable);
            
            
            JPanel tablePanel = new JPanel(new BorderLayout());
            tablePanel.setBackground(Color.WHITE);
            tablePanel.setBorder(BorderFactory.createTitledBorder("Registered Users"));
            
            tablePanel.add(scrollPane, BorderLayout.CENTER);
            mainContent.add(tablePanel,BorderLayout.CENTER);
            
            loadUsers();
            loadDashboardCounts();
            loadItems("LOST", lostItemsTable);
            loadItems("FOUND", foundItemsTable);
            loadAdminReports();

                JPanel lostItemsPanel =createItemsPanel("Lost Items", lostItemsTable);

                JPanel foundItemsPanel =createItemsPanel("Found Items", foundItemsTable);

                JPanel reportsPanel = createItemsPanel("Reports", reportTable);
                
                JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

                statusBox = new JComboBox<>(new String[]{"OPEN", "IN_PROGRESS", "RESOLVED"});

                updateStatusBtn = new JButton("Update Status");
                updateStatusBtn.addActionListener(e -> updateReportStatus());

                statusPanel.add(new JLabel("Set Status:"));
                statusPanel.add(statusBox);
                statusPanel.add(updateStatusBtn);

                reportsPanel.add(statusPanel, BorderLayout.SOUTH);
                

                //CONTENT CARDS
                cardLayout = new CardLayout();

                contentCards = new JPanel(cardLayout);
                contentCards.setBackground(new Color(245, 247, 250));
                contentCards.add(mainContent, "Dashboard");
                contentCards.add(lostItemsPanel, "Lost Items");
                contentCards.add(foundItemsPanel, "Found Items");
                contentCards.add(reportsPanel, "Reports");

                add(contentCards, BorderLayout.CENTER);
                
                setSize(900,900);
                setLocationRelativeTo(null);
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                setVisible(true);
    }
            
    private JPanel createItemsPanel(String title,JTable table){
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Raleway", Font.BOLD, 26));
        heading.setForeground(new Color(30, 41, 59));

        panel.add(heading, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createCard(String title,JLabel counttxt){
        JPanel card=new JPanel(new GridLayout(2,1,5,5));
        card.setBackground(Color.WHITE);
        
        JLabel titletxt = new JLabel(title);
        titletxt.setFont(new Font("Raleway",Font.BOLD,16));
        titletxt.setForeground(Color.GRAY);
                
        counttxt.setFont(new Font("RAleway",Font.BOLD,26));
        counttxt.setForeground(new Color(30,41,59));
        
        card.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        
        card.add(titletxt);
        card.add(counttxt);
        
        return card;
    }
    
    private JTable createTable(String[] columns) {

    JTable table = new JTable(new DefaultTableModel(columns,0));
    
    table.setRowHeight(30);
    
    table.setFont(new Font("Raleway",Font.BOLD,14));
    table.getTableHeader().setFont(new Font("Raleway",Font.BOLD,14));

    return table;
}
    private void loadUsers(){
        try{
            Conn c= new Conn();
            
            String query="SELECT name,student_id,email,phone FROM accountdetails";
            ResultSet rs=c.s.executeQuery(query);
            
            DefaultTableModel model =(DefaultTableModel)userTable.getModel();

        model.setRowCount(0);

        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getString("name"),
                rs.getString("student_id"),
                rs.getString("email"),
                rs.getString("phone")
            });
        }

        rs.close();
        }
        catch(Exception e){
            System.out.println(e);
        }
    }

    
    private void loadDashboardCounts(){
        try{
            Conn c= new Conn();
            
            String query2= "SELECT COUNT(*) AS total FROM accountdetails";
            ResultSet rs= c.s.executeQuery(query2);
            
            if (rs.next()) {
            totalUsersCount.setText(rs.getString("total"));
            }
            rs.close();
            
            String lostQuery =
            "SELECT COUNT(*) AS total FROM items " +
            "WHERE report_type = 'LOST'";

        rs = c.s.executeQuery(lostQuery);

        if (rs.next()) {
            lostReportsCount.setText(rs.getString("total"));
        }
        rs.close();
        
        String foundQuery =
            "SELECT COUNT(*) AS total FROM items " +
            "WHERE report_type = 'FOUND'";

        rs = c.s.executeQuery(foundQuery);

        if (rs.next()) {
            foundReportsCount.setText(rs.getString("total"));
        }
        rs.close();
        }
        catch(Exception e){
            System.out.println(e);
        }
    }
    
    private void loadItems(String reportType, JTable targetTable) {
    String query =
        "SELECT i.item_id, i.item_name, i.category, i.description, " +
        "i.location, a.name AS reporter, i.date_occurred, " +
        "i.time_occurred, i.unique_details, i.status " +
        "FROM items i " +
        "LEFT JOIN accountdetails a ON i.user_id = a.user_id " +
        "WHERE i.report_type = ?";

    try {
        Conn c = new Conn();

        PreparedStatement ps = c.c.prepareStatement(query);
        ps.setString(1, reportType);

        ResultSet rs = ps.executeQuery();

        DefaultTableModel model =
            (DefaultTableModel) targetTable.getModel();

        model.setRowCount(0);

        while (rs.next()) {
            if ("LOST".equals(reportType)) {
                model.addRow(new Object[]{
                    rs.getInt("item_id"),
                    rs.getString("item_name"),
                    rs.getString("category"),
                    rs.getString("description"),
                    rs.getString("location"),
                    rs.getString("reporter"),
                    rs.getDate("date_occurred"),
                    rs.getString("time_occurred"),
                    rs.getString("unique_details"),
                    rs.getString("status")
                });
            } else {
                model.addRow(new Object[]{
                    rs.getInt("item_id"),
                    rs.getString("item_name"),
                    rs.getString("category"),
                    rs.getString("description"),
                    rs.getString("location"),
                    rs.getString("reporter"),
                    rs.getDate("date_occurred"),
                    rs.getString("time_occurred"),
                    rs.getString("status")
                });
            }
        }

        rs.close();
        ps.close();
        c.c.close();

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(
            this,
            "Could not load item records: " + e.getMessage()
        );
    }
}
    
    
private void loadAdminReports() {
    String query =
        "SELECT r.report_id, a.name AS reporter, " +
        "r.issue_type, r.subject, r.description, " +
        "r.created_at, r.status " +
        "FROM admin_reports r " +
        "LEFT JOIN accountdetails a ON r.user_id = a.user_id " +
        "ORDER BY r.created_at DESC";

    try {
        Conn c = new Conn();

        try (PreparedStatement ps = c.c.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            DefaultTableModel model =
                (DefaultTableModel) reportTable.getModel();

            model.setRowCount(0);

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("report_id"),
                    rs.getString("reporter"),
                    rs.getString("issue_type"),
                    rs.getString("subject"),
                    rs.getString("description"),
                    rs.getTimestamp("created_at"),
                    rs.getString("status")
                });
            }
        } finally {
            if (c.c != null) {
                c.c.close();
            }
        }

    } catch (SQLException e) {
        e.printStackTrace();

        JOptionPane.showMessageDialog(
            this,
            "Could not load admin reports: " + e.getMessage(),
            "Database Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
}

private void updateReportStatus() {
    int selectedRow = reportTable.getSelectedRow();

    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(
            this,
            "Please select a report first."
        );
        return;
    }

    // Convert the selected view row to its actual model row.
    int modelRow = reportTable.convertRowIndexToModel(selectedRow);

    int reportId = Integer.parseInt(
        reportTable.getModel().getValueAt(modelRow, 0).toString()
    );

    String newStatus = (String) statusBox.getSelectedItem();

    String query =
        "UPDATE admin_reports SET status = ? WHERE report_id = ?";

    try {
        Conn c = new Conn();

        try (PreparedStatement ps = c.c.prepareStatement(query)) {
            ps.setString(1, newStatus);
            ps.setInt(2, reportId);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(
                    this,
                    "Report status updated to " + newStatus + "."
                );

                loadAdminReports();
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Report not found."
                );
            }
        } finally {
            if (c.c != null) {
                c.c.close();
            }
        }

    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(
            this,
            "Could not update report status: " + ex.getMessage(),
            "Database Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
}
private void refreshDashboard() {
    loadUsers();
    loadDashboardCounts();
    loadItems("LOST", lostItemsTable);
    loadItems("FOUND", foundItemsTable);
    loadAdminReports();

    JOptionPane.showMessageDialog(
        this,
        "Dashboard refreshed successfully!"
    );
}
    
    public void actionPerformed(ActionEvent ae){
        String command = ae.getActionCommand();
        
        switch (command){
            case "Dashboard":
                cardLayout.show(contentCards, "Dashboard");
                break;
                
            case "Lost Items":
                cardLayout.show(contentCards, "Lost Items");
                break;
            case "Found Items":
                cardLayout.show(contentCards, "Found Items");
                break;
                
            case "Reports":
                    cardLayout.show(contentCards, "Reports");
                    break;
                    
            case "Logout":
                int choice = JOptionPane.showConfirmDialog(
                   this,
                    "Are you sure you want to logout?",
                    "Logout",
                    JOptionPane.YES_NO_OPTION);

                    if (choice == JOptionPane.YES_OPTION) {
                        setVisible(false);
                        new Login().setVisible(true);
                        dispose();
                    }
                break;
                    
        }
    }
    
    
    public static void main(String[] args) {
        new AdminDashboard();
    }
}

