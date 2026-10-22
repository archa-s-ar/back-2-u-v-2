
package back2u;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.sql.*;

public class Notifications extends JFrame {

    
    private final int userId;
    private JTable table;
    private DefaultTableModel model;
    private List<MatchNotificationService.Notification> notifications;

    public Notifications(int userId) {
        this.userId = userId;

        setTitle("Back2U - Notifications");
        setSize(950, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JLabel heading = new JLabel(
                "Notifications - Possible Item Matches");
        heading.setFont(new Font("Raleway", Font.BOLD, 22));
        heading.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        add(heading, BorderLayout.NORTH);

        model = new DefaultTableModel(
    new String[] {"Lost Item", "Found Item", "Location","Score", "Your Role", "Your Response","Match Status", "Notification Message"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

        table = new JTable(model);
        table.setRowHeight(27);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton viewButton = new JButton("View Details");
        JButton matchButton = new JButton("Match");
        JButton rejectButton = new JButton("Not a Match");
        JButton refreshButton = new JButton("Refresh");
        JButton closeButton = new JButton("Close");
        JButton checkStatusButton = new JButton("Check Status");

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 12));
        buttons.add(viewButton);
        buttons.add(matchButton);
        buttons.add(rejectButton);
        buttons.add(checkStatusButton);
        buttons.add(refreshButton);
        buttons.add(closeButton);

        add(buttons, BorderLayout.SOUTH);

        viewButton.addActionListener(e -> viewDetails());
        matchButton.addActionListener(e -> respond(true));
        rejectButton.addActionListener(e -> respond(false));
        refreshButton.addActionListener(e -> loadNotifications());
        closeButton.addActionListener(e -> dispose());
        checkStatusButton.addActionListener(e -> checkStatus());

        loadNotifications();
    }

    private MatchNotificationService.Notification selectedNotification() {
        int row = table.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(
                this, "Please select a notification first.");
            return null;
        }

        row = table.convertRowIndexToModel(row);
        return notifications.get(row);
    }

    private void viewDetails() {
        MatchNotificationService.Notification n =
                selectedNotification();

        if (n == null) return;

        if ("REJECTED".equals(n.status)
                || "COMPLETED".equals(n.status)) {
            JOptionPane.showMessageDialog(
                this, "This match is no longer active.");
            return;
        }

        String message =
            "Lost item: " + n.lostName +
            "\nFound item: " + n.foundName +
            "\nLocation: " + n.location +
            "\nMatch score: " + n.score + "/100" +
            "\nYour role: " + n.role +
            "\nYour response: " + n.myResponse +
            "\nMatch status: " + n.status;

        if ("FINDER".equals(n.role)) {
            try {
                String details =
                    MatchNotificationService.getOwnerUniqueDetails(
                        n.matchId, userId);

                message += "\n\nOwner's unique details:\n" + details;

            } catch (SQLException ex) {
                showError(ex);
                return;
            }
        } else {
            message +=
                "\n\nThe finder's report details can be reviewed " +
                "separately during verification.";
        }

        JTextArea area = new JTextArea(message, 12, 42);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);

        JOptionPane.showMessageDialog(
            this, new JScrollPane(area),
            "Possible Match Details",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void respond(boolean confirmed) {
        MatchNotificationService.Notification n =
                selectedNotification();

        if (n == null) return;

        if ("REJECTED".equals(n.status)
                || "COMPLETED".equals(n.status)) {
            JOptionPane.showMessageDialog(
                this, "This match is no longer active.");
            return;
        }

        if (!"PENDING".equals(n.myResponse)) {
            JOptionPane.showMessageDialog(
                this, "You have already responded to this match.");
            return;
        }

        String action = confirmed ? "confirm this possible match"
                                  : "mark this as not a match";

        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to " + action + "?",
            "Confirm Response",
            JOptionPane.YES_NO_OPTION);

        if (choice != JOptionPane.YES_OPTION) return;

        try {
            MatchNotificationService.respond(
                n.matchId, userId, confirmed);

            JOptionPane.showMessageDialog(
                this,
                confirmed
                    ? "Your response was saved. The match will proceed " +
                      "to verification when both parties confirm."
                    : "Your response was saved. This match has been rejected.");

            loadNotifications();

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void loadNotifications() {
    model.setRowCount(0);

    try {
        notifications =
            MatchNotificationService.getNotifications(userId);

        for (MatchNotificationService.Notification n : notifications) {

            String message;

            if ("REJECTED".equals(n.status)) {
    message = "This match was rejected.";

} else if ("COMPLETED".equals(n.status)) {
    message = "This match is completed.";

} else if ("CONFIRMED".equals(n.ownerResponse)
        && "CONFIRMED".equals(n.finderResponse)) {
    message = "Both users confirmed! Match verified.";

} else if ("OWNER".equals(n.role)
        && "CONFIRMED".equals(n.myResponse)) {
    message = "You confirmed. Waiting for finder.";

} else if ("FINDER".equals(n.role)
        && "CONFIRMED".equals(n.myResponse)) {
    message = "You confirmed. Waiting for owner.";

} else {
    message = "A possible match needs your review.";
}

            model.addRow(new Object[] {
                n.lostName,
                n.foundName,
                n.location,
                n.score + "/100",
                n.role,
                n.myResponse,
                n.status,
                message
            });
        }

        if (notifications.isEmpty()) {
            setTitle("Back2U - Notifications (No matches yet)");
        } else {
            setTitle("Back2U - Notifications");
        }

    } catch (SQLException ex) {
        showError(ex);
    }
}
    

    private void showError(Exception ex) {
        ex.printStackTrace();

        JOptionPane.showMessageDialog(
            this,
            "Unable to complete the operation.\n" + ex.getMessage(),
            "Back2U Error",
            JOptionPane.ERROR_MESSAGE);
    }
    
    private void checkStatus() {
    MatchNotificationService.Notification n =
            selectedNotification();

    if (n == null) {
        return;
    }

    String sql =
            "SELECT lost.status AS lost_status, " +
            "found.status AS found_status " +
            "FROM items lost " +
            "JOIN items found ON found.item_id = ? " +
            "WHERE lost.item_id = ? " +
            "AND lost.report_type = 'LOST' " +
            "AND found.report_type = 'FOUND'";

    try (Connection con = new Conn().c;
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, n.foundItemId);
        ps.setInt(2, n.lostItemId);

        try (ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                String lostStatus = rs.getString("lost_status");
                String foundStatus = rs.getString("found_status");

                if ("MATCHED".equalsIgnoreCase(lostStatus)
                        && "MATCHED".equalsIgnoreCase(foundStatus)) {

                    JOptionPane.showMessageDialog(
                            this,
                            "Both items are matched! Opening details...",
                            "Match Confirmed",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    if ("OWNER".equalsIgnoreCase(n.role)) {

                    new DetailsMatch(n.lostItemId,n.foundItemId,n.matchId,userId);

                } else if ("FINDER".equalsIgnoreCase(n.role)) {

                new handover(n.matchId).setVisible(true);
                }

                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Item not matched yet.\n" +
                            "Both the owner and finder must confirm the match.",
                            "Match Status",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }

            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Unable to find these item records.",
                        "Match Status",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        }

    } catch (SQLException ex) {
        showError(ex);
    }
}
}
