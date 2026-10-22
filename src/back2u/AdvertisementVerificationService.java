
package back2u;

import java.sql.*;

public class AdvertisementVerificationService {

    private static final int MAX_RATING = 5;

    /**
     * Submit or update a credibility rating.
     * Each user can have only one rating per item.
     */
    public boolean submitRating(int itemId, int userId, int rating)
            throws SQLException {

        if (rating < 1 || rating > MAX_RATING) {
            throw new IllegalArgumentException(
                    "Rating must be between 1 and 5.");
        }

        String sql =
                "INSERT INTO advertisement_ratings "
                + "(item_id, user_id, rating) VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + "rating = VALUES(rating), "
                + "created_at = CURRENT_TIMESTAMP";

        Conn conn = new Conn();

        if (conn.c == null) {
            throw new SQLException("Database connection failed.");
        }

        try (PreparedStatement ps = conn.c.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            ps.setInt(2, userId);
            ps.setInt(3, rating);

            return ps.executeUpdate() > 0;

        } finally {
            closeConnection(conn);
        }
    }

    /**
     * Get the average user rating for an item.
     * Returns -1 if nobody has rated it yet.
     */
    public double getAverageRating(int itemId) throws SQLException {

        String sql =
                "SELECT AVG(rating) AS average_rating "
                + "FROM advertisement_ratings WHERE item_id = ?";

        Conn conn = new Conn();

        if (conn.c == null) {
            throw new SQLException("Database connection failed.");
        }

        try (PreparedStatement ps = conn.c.prepareStatement(sql)) {
            ps.setInt(1, itemId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double average = rs.getDouble("average_rating");

                    if (rs.wasNull()) {
                        return -1;
                    }

                    return average;
                }

                return -1;
            }

        } finally {
            closeConnection(conn);
        }
    }

    /**
     * Convert the average rating to a percentage.
     * Returns -1 when the item has no ratings.
     */
    public int calculateRatingTrustScore(int itemId)
            throws SQLException {

        double average = getAverageRating(itemId);

        if (average < 0) {
            return -1;
        }

        return (int) Math.round((average / MAX_RATING) * 100);
    }

    /**
     * Report a suspicious advertisement.
     */
    public boolean reportAdvertisement(
            int itemId,
            int reporterUserId,
            String reason,
            String details) throws SQLException {

        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select a report reason.");
        }

        String sql =
                "INSERT INTO advertisement_reports "
                + "(item_id, reporter_user_id, reason, details) "
                + "VALUES (?, ?, ?, ?)";

        Conn conn = new Conn();

        if (conn.c == null) {
            throw new SQLException("Database connection failed.");
        }

        try (PreparedStatement ps = conn.c.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            ps.setInt(2, reporterUserId);
            ps.setString(3, reason.trim());

            if (details == null || details.trim().isEmpty()) {
                ps.setNull(4, Types.VARCHAR);
            } else {
                ps.setString(4, details.trim());
            }

            return ps.executeUpdate() > 0;

        } finally {
            closeConnection(conn);
        }
    }

    /**
     * Flag an advertisement for administrator review.
     * This does not automatically prove that the ad is fraudulent.
     */
    public boolean flagAdvertisement(int itemId)
            throws SQLException {

        String sql =
                "UPDATE items SET ad_review_status = 'PENDING_REVIEW' "
                + "WHERE item_id = ?";

        Conn conn = new Conn();

        if (conn.c == null) {
            throw new SQLException("Database connection failed.");
        }

        try (PreparedStatement ps = conn.c.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            return ps.executeUpdate() > 0;

        } finally {
            closeConnection(conn);
        }
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
}
