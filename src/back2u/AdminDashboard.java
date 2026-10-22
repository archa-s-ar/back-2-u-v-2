
package back2u;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class AdminDashboard extends JFrame {

    private JTable userTable;
    private JTable lostItemsTable;
    private JTable foundItemsTable;
    private JTable reportTable;
    private JTable advertisementReportsTable;
    private JTable pendingAdvertisementsTable;

    private JLabel totalUsersCount;
    private JLabel lostReportsCount;
    private JLabel foundReportsCount;

    private CardLayout cardLayout;
    private JPanel contentCards;

    private JComboBox<String> statusBox;

    private static final Color NAVY = new Color(30, 41, 59);
    private static final Color BACKGROUND = new Color(245, 247, 250);
    private static final Color PURPLE = new Color(91, 72, 157);

    public AdminDashboard() {

        setTitle("Back2U - Admin Dashboard");
        setLayout(new BorderLayout(10, 5));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ================= HEADER =================

        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setBackground(NAVY);

        JLabel logo = new JLabel("Back2U");
        logo.setFont(new Font("Raleway", Font.BOLD, 26));
        logo.setForeground(Color.WHITE);
        logo.setBorder(
                BorderFactory.createEmptyBorder(0, 20, 0, 0)
        );

        JLabel adminTitle = new JLabel("Admin Dashboard");
        adminTitle.setFont(new Font("Raleway", Font.BOLD, 26));
        adminTitle.setForeground(Color.WHITE);
        adminTitle.setHorizontalAlignment(SwingConstants.RIGHT);
        adminTitle.setBorder(
                BorderFactory.createEmptyBorder(0, 0, 0, 20)
        );

        header.add(logo, BorderLayout.WEST);
        header.add(adminTitle, BorderLayout.CENTER);
        header.setPreferredSize(new Dimension(250, 80));

        add(header, BorderLayout.NORTH);

        // ================= TABLES =================

        userTable = createTable(new String[]{
            "Name", "Student ID", "Email", "Phone"
        });

        lostItemsTable = createTable(new String[]{
            "Item ID", "Item Name", "Category", "Description",
            "Location", "Reported By", "Date Reported",
            "Time Reported", "Unique Identification Features",
            "Status"
        });

        foundItemsTable = createTable(new String[]{
            "Item ID", "Item Name", "Category", "Description",
            "Location", "Reported By", "Date Reported",
            "Time Reported", "Status"
        });

        reportTable = createTable(new String[]{
            "Report ID", "Reported By", "Issue Type",
            "Subject", "Description", "Date Reported", "Status"
        });

        advertisementReportsTable = createTable(new String[]{
            "Report ID", "Item ID", "Reporter", "Reason",
            "Details", "Report Date", "Report Status"
        });

        pendingAdvertisementsTable = createTable(new String[]{
            "Item ID", "Type", "Item Name", "Category",
            "Description", "Location", "Score", "Review Status",
            "Date Reported"
        });

        // ================= DASHBOARD SUMMARY =================

        JPanel mainContent = new JPanel(new BorderLayout(10, 20));
        mainContent.setBackground(BACKGROUND);
        mainContent.setBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        );

        JPanel summary = new JPanel(new GridLayout(1, 3, 15, 0));
        summary.setBackground(BACKGROUND);

        totalUsersCount = new JLabel("0");
        lostReportsCount = new JLabel("0");
        foundReportsCount = new JLabel("0");

        summary.add(createCard("Total Users", totalUsersCount));
        summary.add(createCard("Lost Reports", lostReportsCount));
        summary.add(createCard("Found Reports", foundReportsCount));

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setFont(new Font("Raleway", Font.BOLD, 14));
        refreshButton.setBackground(PURPLE);
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(e -> refreshDashboard());

        JPanel refreshPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );
        refreshPanel.setBackground(BACKGROUND);
        refreshPanel.add(refreshButton);

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(BACKGROUND);
        topPanel.add(summary, BorderLayout.CENTER);
        topPanel.add(refreshPanel, BorderLayout.SOUTH);

        mainContent.add(topPanel, BorderLayout.NORTH);

        JPanel usersPanel = createItemsPanel(
                "Registered Users", userTable
        );

        mainContent.add(usersPanel, BorderLayout.CENTER);

        // ================= LOST / FOUND PANELS =================

        JPanel lostItemsPanel = createItemsPanel(
                "Lost Items", lostItemsTable
        );

        JPanel foundItemsPanel = createItemsPanel(
                "Found Items", foundItemsTable
        );

        // ================= SUPPORT REPORTS =================

        JPanel reportsPanel = createItemsPanel(
                "Support Reports", reportTable
        );

        JPanel supportActionPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT)
        );

        statusBox = new JComboBox<>(new String[]{
            "OPEN", "IN_PROGRESS", "RESOLVED"
        });

        JButton updateStatusButton = new JButton("Update Status");
        updateStatusButton.addActionListener(
                e -> updateReportStatus()
        );

        supportActionPanel.add(new JLabel("Set Status:"));
        supportActionPanel.add(statusBox);
        supportActionPanel.add(updateStatusButton);

        reportsPanel.add(
                supportActionPanel, BorderLayout.SOUTH
        );

        // ================= ADVERTISEMENT REVIEW =================

        JPanel advertisementPanel = createItemsPanel(
                "Advertisement Verification",
                advertisementReportsTable
        );

        JPanel advertisementActions = new JPanel(
                new FlowLayout(FlowLayout.LEFT)
        );

        JButton approveButton =
                new JButton("Approve Advertisement");

        JButton rejectButton =
                new JButton("Reject Advertisement");

        approveButton.addActionListener(
                e -> reviewAdvertisement("APPROVED")
        );

        rejectButton.addActionListener(
                e -> reviewAdvertisement("REJECTED")
        );

        advertisementActions.add(approveButton);
        advertisementActions.add(rejectButton);

        advertisementPanel.add(
                advertisementActions, BorderLayout.SOUTH
        );

        // ================= PENDING ADVERTISEMENTS =================

        JPanel pendingAdvertisementsPanel = createItemsPanel(
                "Pending Advertisements - Admin Decision Required",
                pendingAdvertisementsTable
        );

        JPanel pendingActions = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton approveAndPublishButton = new JButton("Approve & Publish");
        JButton removeListingButton = new JButton("Remove Listing");
        JButton refreshPendingButton = new JButton("Refresh Queue");

        approveAndPublishButton.addActionListener(
                e -> reviewPendingAdvertisement("APPROVED")
        );
        removeListingButton.addActionListener(
                e -> reviewPendingAdvertisement("REJECTED")
        );
        refreshPendingButton.addActionListener(
                e -> loadPendingAdvertisements()
        );

        pendingActions.add(approveAndPublishButton);
        pendingActions.add(removeListingButton);
        pendingActions.add(refreshPendingButton);
        pendingAdvertisementsPanel.add(pendingActions, BorderLayout.SOUTH);

        // ================= CARD LAYOUT =================

        cardLayout = new CardLayout();
        contentCards = new JPanel(cardLayout);
        contentCards.setBackground(BACKGROUND);

        contentCards.add(mainContent, "Dashboard");
        contentCards.add(lostItemsPanel, "Lost Items");
        contentCards.add(foundItemsPanel, "Found Items");
        contentCards.add(reportsPanel, "Reports");
        contentCards.add(
                advertisementPanel, "Advertisement Review"
        );
        contentCards.add(
                pendingAdvertisementsPanel, "Pending Advertisements"
        );

        add(contentCards, BorderLayout.CENTER);

        // ================= SIDEBAR =================

        JPanel sidebar = new JPanel();
        sidebar.setLayout(
                new BoxLayout(sidebar, BoxLayout.Y_AXIS)
        );
        sidebar.setBackground(NAVY);
        sidebar.setPreferredSize(new Dimension(220, 300));

        String[] menuItems = {
            "Dashboard",
            "Lost Items",
            "Found Items",
            "Reports",
            "Advertisement Review",
            "Pending Advertisements",
            "Logout"
        };

        for (String item : menuItems) {

            JButton button = new JButton(item);
            button.setFont(new Font("Raleway", Font.PLAIN, 15));
            button.setForeground(Color.WHITE);
            button.setBackground(NAVY);
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.setMaximumSize(
                    new Dimension(Integer.MAX_VALUE, 50)
            );
            button.setFocusPainted(false);
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setBorder(
                    BorderFactory.createEmptyBorder(0, 20, 0, 5)
            );

            button.addActionListener(e -> {
                if ("Logout".equals(item)) {
                    logout();
                } else {
                    cardLayout.show(contentCards, item);

                    if ("Advertisement Review".equals(item)) {
                        loadAdvertisementReports();
                    } else if ("Pending Advertisements".equals(item)) {
                        loadPendingAdvertisements();
                    } else if ("Reports".equals(item)) {
                        loadAdminReports();
                    }
                }
            });

            sidebar.add(button);
        }

        add(sidebar, BorderLayout.WEST);

        // ================= INITIAL DATA =================

        setSize(1200, 850);
        setLocationRelativeTo(null);

        loadUsers();
        loadDashboardCounts();
        loadItems("LOST", lostItemsTable);
        loadItems("FOUND", foundItemsTable);
        loadAdminReports();
        loadAdvertisementReports();
        loadPendingAdvertisements();

        setVisible(true);
    }

    // =========================================================
    // UI HELPERS
    // =========================================================

    private JTable createTable(String[] columns) {

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Raleway", Font.PLAIN, 13));
        table.getTableHeader().setFont(
                new Font("Raleway", Font.BOLD, 13)
        );
        table.setAutoCreateRowSorter(true);

        return table;
    }

    private JPanel createItemsPanel(String title, JTable table) {

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND);
        panel.setBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        );

        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Raleway", Font.BOLD, 25));
        heading.setForeground(NAVY);

        panel.add(heading, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCard(String title, JLabel countLabel) {

        JPanel card = new JPanel(new GridLayout(2, 1, 5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Raleway", Font.BOLD, 16));
        titleLabel.setForeground(Color.GRAY);

        countLabel.setFont(new Font("Raleway", Font.BOLD, 26));
        countLabel.setForeground(NAVY);

        card.add(titleLabel);
        card.add(countLabel);

        return card;
    }

    // =========================================================
    // DATABASE HELPERS
    // =========================================================

    private Conn openConnection() throws SQLException {

        Conn conn = new Conn();

        if (conn.c == null) {
            throw new SQLException(
                    "Database connection failed. Check Conn.java "
                    + "and back2u.properties."
            );
        }

        return conn;
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

    private void showDatabaseError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    // =========================================================
    // LOAD USERS
    // =========================================================

    private void loadUsers() {

        String sql =
                "SELECT name, student_id, email, phone "
                + "FROM accountdetails";

        Conn conn = null;

        try {
            conn = openConnection();

            try (PreparedStatement ps =
                         conn.c.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                DefaultTableModel model =
                        (DefaultTableModel) userTable.getModel();

                model.setRowCount(0);

                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("name"),
                        rs.getString("student_id"),
                        rs.getString("email"),
                        rs.getString("phone")
                    });
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            showDatabaseError(
                    "Could not load users: " + ex.getMessage()
            );

        } finally {
            closeConnection(conn);
        }
    }

    // =========================================================
    // DASHBOARD COUNTS
    // =========================================================

    private void loadDashboardCounts() {

        String usersSql =
                "SELECT COUNT(*) FROM accountdetails";

        String lostSql =
                "SELECT COUNT(*) FROM items "
                + "WHERE report_type = 'LOST'";

        String foundSql =
                "SELECT COUNT(*) FROM items "
                + "WHERE report_type = 'FOUND'";

        Conn conn = null;

        try {
            conn = openConnection();

            totalUsersCount.setText(
                    getCount(conn.c, usersSql)
            );

            lostReportsCount.setText(
                    getCount(conn.c, lostSql)
            );

            foundReportsCount.setText(
                    getCount(conn.c, foundSql)
            );

        } catch (SQLException ex) {
            ex.printStackTrace();
            showDatabaseError(
                    "Could not load dashboard counts: "
                    + ex.getMessage()
            );

        } finally {
            closeConnection(conn);
        }
    }

    private String getCount(Connection connection, String sql)
            throws SQLException {

        try (PreparedStatement ps =
                     connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getString(1);
            }

            return "0";
        }
    }

    // =========================================================
    // LOAD LOST / FOUND ITEMS
    // =========================================================

    private void loadItems(String reportType, JTable targetTable) {

        String sql =
                "SELECT i.item_id, i.item_name, i.category, "
                + "i.description, i.location, "
                + "a.name AS reporter, i.date_occurred, "
                + "i.time_occurred, i.unique_details, i.status "
                + "FROM items i "
                + "LEFT JOIN accountdetails a "
                + "ON i.user_id = a.user_id "
                + "WHERE i.report_type = ?";

        Conn conn = null;

        try {
            conn = openConnection();

            try (PreparedStatement ps =
                         conn.c.prepareStatement(sql)) {

                ps.setString(1, reportType);

                try (ResultSet rs = ps.executeQuery()) {

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
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            showDatabaseError(
                    "Could not load item records: " + ex.getMessage()
            );

        } finally {
            closeConnection(conn);
        }
    }

    // =========================================================
    // LOAD SUPPORT REPORTS
    // =========================================================

    private void loadAdminReports() {

        String sql =
                "SELECT r.report_id, a.name AS reporter, "
                + "r.issue_type, r.subject, r.description, "
                + "r.created_at, r.status "
                + "FROM admin_reports r "
                + "LEFT JOIN accountdetails a "
                + "ON r.user_id = a.user_id "
                + "ORDER BY r.created_at DESC";

        Conn conn = null;

        try {
            conn = openConnection();

            try (PreparedStatement ps =
                         conn.c.prepareStatement(sql);
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
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            showDatabaseError(
                    "Could not load support reports: "
                    + ex.getMessage()
            );

        } finally {
            closeConnection(conn);
        }
    }

    private void updateReportStatus() {

        int selectedRow = reportTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this, "Please select a support report first."
            );
            return;
        }

        int modelRow =
                reportTable.convertRowIndexToModel(selectedRow);

        int reportId = Integer.parseInt(
                reportTable.getModel()
                        .getValueAt(modelRow, 0).toString()
        );

        String newStatus =
                (String) statusBox.getSelectedItem();

        String sql =
                "UPDATE admin_reports SET status = ? "
                + "WHERE report_id = ?";

        Conn conn = null;

        try {
            conn = openConnection();

            try (PreparedStatement ps =
                         conn.c.prepareStatement(sql)) {

                ps.setString(1, newStatus);
                ps.setInt(2, reportId);

                if (ps.executeUpdate() > 0) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Support report status updated."
                    );
                    loadAdminReports();

                } else {
                    JOptionPane.showMessageDialog(
                            this, "Support report not found."
                    );
                }
            }

        } catch (SQLException ex) {
            showDatabaseError(
                    "Could not update support report: "
                    + ex.getMessage()
            );

        } finally {
            closeConnection(conn);
        }
    }

    // =========================================================
    // LOAD SUSPICIOUS ADVERTISEMENT REPORTS
    // =========================================================

    private void loadAdvertisementReports() {

        String sql =
                "SELECT r.report_id, r.item_id, "
                + "a.name AS reporter, r.reason, r.details, "
                + "r.created_at, r.report_status "
                + "FROM advertisement_reports r "
                + "LEFT JOIN accountdetails a "
                + "ON r.reporter_user_id = a.user_id "
                + "ORDER BY r.created_at DESC";

        Conn conn = null;

        try {
            conn = openConnection();

            try (PreparedStatement ps =
                         conn.c.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                DefaultTableModel model =
                        (DefaultTableModel)
                                advertisementReportsTable.getModel();

                model.setRowCount(0);

                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("report_id"),
                        rs.getInt("item_id"),
                        rs.getString("reporter"),
                        rs.getString("reason"),
                        rs.getString("details"),
                        rs.getTimestamp("created_at"),
                        rs.getString("report_status")
                    });
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            showDatabaseError(
                    "Could not load advertisement reports: "
                    + ex.getMessage()
            );

        } finally {
            closeConnection(conn);
        }
    }

    // =========================================================
    // LOAD PENDING ADVERTISEMENTS
    // =========================================================

    private void loadPendingAdvertisements() {

        String sql =
                "SELECT item_id, report_type, item_name, category, "
                + "description, location, trust_score, "
                + "ad_review_status, date_occurred "
                + "FROM items "
                + "WHERE ad_review_status = 'PENDING_REVIEW' "
                + "ORDER BY created_at DESC";

        Conn conn = null;

        try {
            conn = openConnection();

            try (PreparedStatement ps = conn.c.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                DefaultTableModel model =
                        (DefaultTableModel) pendingAdvertisementsTable.getModel();
                model.setRowCount(0);

                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("item_id"),
                        rs.getString("report_type"),
                        rs.getString("item_name"),
                        rs.getString("category"),
                        rs.getString("description"),
                        rs.getString("location"),
                        rs.getInt("trust_score"),
                        rs.getString("ad_review_status"),
                        rs.getDate("date_occurred")
                    });
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            showDatabaseError(
                    "Could not load pending advertisements: " + ex.getMessage()
            );
        } finally {
            closeConnection(conn);
        }
    }

    // =========================================================
    // APPROVE & PUBLISH / REMOVE PENDING ADVERTISEMENT
    // =========================================================

    private void reviewPendingAdvertisement(String decision) {

        int selectedRow = pendingAdvertisementsTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this, "Please select a pending listing first."
            );
            return;
        }

        int modelRow = pendingAdvertisementsTable.convertRowIndexToModel(selectedRow);
        int itemId = Integer.parseInt(
                pendingAdvertisementsTable.getModel().getValueAt(modelRow, 0).toString()
        );
        String itemName = String.valueOf(
                pendingAdvertisementsTable.getModel().getValueAt(modelRow, 2)
        );

        boolean approving = "APPROVED".equals(decision);
        String actionText = approving ? "approve and publish" : "remove";

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to " + actionText + " this listing?\n"
                + "Item ID: " + itemId + "\n"
                + "Item: " + itemName + "\n\n"
                + (approving
                    ? "The listing will become eligible for matching."
                    : "The listing will be rejected and hidden from users."),
                "Confirm Listing Review",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        String sql =
                "UPDATE items SET ad_review_status = ? "
                + "WHERE item_id = ? AND ad_review_status = 'PENDING_REVIEW'";

        Conn conn = null;

        try {
            conn = openConnection();

            try (PreparedStatement ps = conn.c.prepareStatement(sql)) {
                ps.setString(1, decision);
                ps.setInt(2, itemId);

                int rows = ps.executeUpdate();

                if (rows == 0) {
                    JOptionPane.showMessageDialog(
                            this,
                            "This listing is no longer pending review. Refreshing queue."
                    );
                    loadPendingAdvertisements();
                    return;
                }
            }

            JOptionPane.showMessageDialog(
                    this,
                    approving
                        ? "Listing approved and published for matching."
                        : "Listing rejected and removed from user-facing listings."
            );

            loadPendingAdvertisements();
            loadItems("LOST", lostItemsTable);
            loadItems("FOUND", foundItemsTable);

        } catch (SQLException ex) {
            ex.printStackTrace();
            showDatabaseError(
                    "Could not review pending listing: " + ex.getMessage()
            );
        } finally {
            closeConnection(conn);
        }
    }

    // =========================================================
    // APPROVE / REJECT ADVERTISEMENT
    // =========================================================

    private void reviewAdvertisement(String decision) {

        int selectedRow =
                advertisementReportsTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Select an advertisement report first."
            );
            return;
        }

        int row = advertisementReportsTable
                .convertRowIndexToModel(selectedRow);

        int reportId = Integer.parseInt(
                advertisementReportsTable.getModel()
                        .getValueAt(row, 0).toString()
        );

        int itemId = Integer.parseInt(
                advertisementReportsTable.getModel()
                        .getValueAt(row, 1).toString()
        );

        String currentReportStatus = String.valueOf(
                advertisementReportsTable.getModel()
                        .getValueAt(row, 6)
        );

        if (!"PENDING".equalsIgnoreCase(currentReportStatus)) {
            JOptionPane.showMessageDialog(
                    this,
                    "This advertisement report has already been reviewed."
            );
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to " + decision
                + " advertisement #" + itemId + "?\n"
                + "This will update its advertisement review status.",
                "Confirm Advertisement Review",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        Conn conn = null;

        try {
            conn = openConnection();
            conn.c.setAutoCommit(false);

            // Only process the selected report if it is still pending.
            String reportSql =
                    "UPDATE advertisement_reports "
                    + "SET report_status = 'REVIEWED' "
                    + "WHERE report_id = ? "
                    + "AND item_id = ? "
                    + "AND report_status = 'PENDING'";

            try (PreparedStatement reportPs =
                         conn.c.prepareStatement(reportSql)) {

                reportPs.setInt(1, reportId);
                reportPs.setInt(2, itemId);

                if (reportPs.executeUpdate() == 0) {
                    conn.c.rollback();

                    JOptionPane.showMessageDialog(
                            this,
                            "This report was already processed "
                            + "or no longer exists."
                    );
                    return;
                }
            }

            // Change the advertisement's review status.
            String itemSql =
                    "UPDATE items SET ad_review_status = ? "
                    + "WHERE item_id = ?";

            try (PreparedStatement itemPs =
                         conn.c.prepareStatement(itemSql)) {

                itemPs.setString(1, decision);
                itemPs.setInt(2, itemId);

                if (itemPs.executeUpdate() == 0) {
                    conn.c.rollback();

                    JOptionPane.showMessageDialog(
                            this, "Advertisement not found."
                    );
                    return;
                }
            }

            conn.c.commit();

            JOptionPane.showMessageDialog(
                    this,
                    "Advertisement review completed.\n"
                    + "Item ID: " + itemId + "\n"
                    + "Review status: " + decision
            );

            loadAdvertisementReports();
            loadItems("LOST", lostItemsTable);
            loadItems("FOUND", foundItemsTable);

        } catch (SQLException ex) {

            if (conn != null && conn.c != null) {
                try {
                    conn.c.rollback();
                } catch (SQLException rollbackEx) {
                    ex.addSuppressed(rollbackEx);
                }
            }

            ex.printStackTrace();
            showDatabaseError(
                    "Could not complete advertisement review: "
                    + ex.getMessage()
            );

        } finally {

            if (conn != null && conn.c != null) {
                try {
                    conn.c.setAutoCommit(true);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }

            closeConnection(conn);
        }
    }

    // =========================================================
    // REFRESH / LOGOUT
    // =========================================================

    private void refreshDashboard() {

        loadUsers();
        loadDashboardCounts();
        loadItems("LOST", lostItemsTable);
        loadItems("FOUND", foundItemsTable);
        loadAdminReports();
        loadAdvertisementReports();
        loadPendingAdvertisements();

        JOptionPane.showMessageDialog(
                this, "Dashboard refreshed successfully!"
        );
    }

    private void logout() {

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Logout",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            new Login().setVisible(true);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdminDashboard::new);
    }
}
