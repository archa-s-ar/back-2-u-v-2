
package back2u;

import java.sql.*;
import java.util.*;
import java.util.List;

public class MatchNotificationService {

    
public static class Notification {
    public String ownerResponse;
    public int matchId;
    public int lostItemId;
    public int foundItemId;
    public int score;
    public String lostName;
    public String foundName;
    public String location;
    public String status;
    public String role;
    public String myResponse;
    public String finderResponse;
    
    public Notification(
        int matchId, int lostItemId, int foundItemId,
        int score, String lostName, String foundName,
        String location, String status, String role,
        String myResponse, String ownerResponse,
        String finderResponse) {

    this.matchId = matchId;
    this.lostItemId = lostItemId;
    this.foundItemId = foundItemId;
    this.score = score;
    this.lostName = lostName;
    this.foundName = foundName;
    this.location = location;
    this.status = status;
    this.role = role;
    this.myResponse = myResponse;
    this.ownerResponse = ownerResponse;
    this.finderResponse = finderResponse;
}
}


    // Generate candidate pairs and store them only once.
    public static void refreshMatches() throws SQLException {
        List<ItemMatchingService.MatchResult> matches =
                ItemMatchingService.findAllMatches();

        String sql =
            "INSERT IGNORE INTO item_matches " +
            "(lost_item_id, found_item_id, match_score) " +
            "VALUES (?, ?, ?)";

        try (Connection con = new Conn().c;
             PreparedStatement ps = con.prepareStatement(sql)) {

            for (ItemMatchingService.MatchResult m : matches) {
                ps.setInt(1, m.lostItemId);
                ps.setInt(2, m.foundItemId);
                ps.setInt(3, m.score);
                ps.addBatch();
            }

            ps.executeBatch();
        }
    }

    // Return matches involving items reported by this user.
    public static List<Notification> getNotifications(int userId)
            throws SQLException {

        refreshMatches();

        List<Notification> result = new ArrayList<>();

        String sql =
            "SELECT im.match_id, im.lost_item_id, im.found_item_id, " +
            "im.match_score, im.match_status, " +
            "im.owner_response, im.finder_response, " +
            "lost.item_name AS lost_name, " +
            "found.item_name AS found_name, " +
            "found.location AS found_location, " +
            "lost.user_id AS owner_id, " +
            "found.user_id AS finder_id " +
            "FROM item_matches im " +
            "JOIN items lost ON lost.item_id = im.lost_item_id " +
            "JOIN items found ON found.item_id = im.found_item_id " +
            "WHERE lost.user_id = ? OR found.user_id = ? " +
            "ORDER BY im.created_at DESC";

        try (Connection con = new Conn().c;
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    boolean isOwner =
                            rs.getInt("owner_id") == userId;

                    String role = isOwner ? "OWNER" : "FINDER";
                    String response = isOwner
                            ? rs.getString("owner_response")
                            : rs.getString("finder_response");

                    result.add(new Notification(
                            
                    rs.getInt("match_id"),
                    rs.getInt("lost_item_id"),
                    rs.getInt("found_item_id"),
                    rs.getInt("match_score"),
                    rs.getString("lost_name"),
                    rs.getString("found_name"),
                    rs.getString("found_location"),
                    rs.getString("match_status"),
                    role,
                    response,
                    rs.getString("owner_response"),
                    rs.getString("finder_response")
                    ));
                }
            }
        }

        return result;
    }

    // Only the finder can retrieve the owner's unique details.
    public static String getOwnerUniqueDetails(
            int matchId, int finderUserId) throws SQLException {

        String sql =
            "SELECT lost.unique_details " +
            "FROM item_matches im " +
            "JOIN items lost ON lost.item_id = im.lost_item_id " +
            "JOIN items found ON found.item_id = im.found_item_id " +
            "WHERE im.match_id = ? " +
            "AND found.user_id = ? " +
            "AND im.match_status NOT IN ('REJECTED', 'COMPLETED')";

        try (Connection con = new Conn().c;
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, matchId);
            ps.setInt(2, finderUserId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String details = rs.getString("unique_details");
                    return details == null || details.trim().isEmpty()
                            ? "No unique details were provided."
                            : details;
                }
            }
        }

        throw new SQLException(
            "Details unavailable. This notification may not belong to you."
        );
    }

    // Save the response from the logged-in owner or finder.
    public static void respond(
        int matchId, int userId, boolean confirmed)
        throws SQLException {

    String response = confirmed ? "CONFIRMED" : "REJECTED";

    String sql =
        "UPDATE item_matches im " +
        "JOIN items lost ON lost.item_id = im.lost_item_id " +
        "JOIN items found ON found.item_id = im.found_item_id " +
        "SET " +
        "im.owner_response = CASE " +
        "WHEN lost.user_id = ? THEN ? " +
        "ELSE im.owner_response END, " +
        "im.finder_response = CASE " +
        "WHEN found.user_id = ? THEN ? " +
        "ELSE im.finder_response END, " +
        "im.match_status = CASE " +
        "WHEN ? = 'REJECTED' THEN 'REJECTED' " +
        "ELSE im.match_status END " +
        "WHERE im.match_id = ? " +
        "AND (lost.user_id = ? OR found.user_id = ?) " +
        "AND im.match_status NOT IN ('REJECTED', 'COMPLETED')";

    try (Connection con = new Conn().c;
         PreparedStatement ps = con.prepareStatement(sql)) {

        int i = 1;
        ps.setInt(i++, userId);
        ps.setString(i++, response);
        ps.setInt(i++, userId);
        ps.setString(i++, response);
        ps.setString(i++, response);
        ps.setInt(i++, matchId);
        ps.setInt(i++, userId);
        ps.setInt(i, userId);

        int updated = ps.executeUpdate();

        if (updated == 0) {
            throw new SQLException(
                "Response not saved. Check the notification or its status."
            );
        }

        // Check the saved responses and update both item statuses.
        updateMatchStatus(matchId, con);
    }
}
    
    private static void updateMatchStatus(
        int matchId, Connection con) throws SQLException {

    String checkSql =
        "SELECT owner_response, finder_response " +
        "FROM item_matches WHERE match_id = ?";

    String ownerResponse;
    String finderResponse;

    try (PreparedStatement ps = con.prepareStatement(checkSql)) {
        ps.setInt(1, matchId);

        try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                throw new SQLException("Match not found.");
            }

            ownerResponse = rs.getString("owner_response");
            finderResponse = rs.getString("finder_response");
        }
    }

    if ("CONFIRMED".equals(ownerResponse)
            && "CONFIRMED".equals(finderResponse)) {

        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE item_matches SET match_status = 'VERIFICATION' " +
                "WHERE match_id = ?")) {
            ps.setInt(1, matchId);
            ps.executeUpdate();
        }

        updateItemStatuses(matchId, con);
    }
}
    
    private static void updateItemStatuses(
        int matchId, Connection con) throws SQLException {

    String sql =
        "UPDATE items i " +
        "JOIN item_matches im ON " +
        "(i.item_id = im.lost_item_id OR " +
        "i.item_id = im.found_item_id) " +
        "SET i.status = 'MATCHED' " +
        "WHERE im.match_id = ? " +
        "AND im.owner_response = 'CONFIRMED' " +
        "AND im.finder_response = 'CONFIRMED'";

    try (PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, matchId);

        int updated = ps.executeUpdate();

        System.out.println(
            "Item records updated to MATCHED: " + updated);
    }
}
}
