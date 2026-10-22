
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

        if (itemId <= 0 || userId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid advertisement or user ID.");
        }

        if (rating < 1 || rating > MAX_RATING) {
            throw new IllegalArgumentException(
                    "Rating must be between 1 and 5.");
        }

        String checkSql =
                "SELECT user_id, ad_review_status "
                + "FROM items WHERE item_id = ?";

        String ratingSql =
                "INSERT INTO advertisement_ratings "
                + "(item_id, user_id, rating) VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + "rating = VALUES(rating), "
                + "created_at = CURRENT_TIMESTAMP";

        Conn conn = new Conn();

        if (conn.c == null) {
            throw new SQLException("Database connection failed.");
        }

        try {
            try (PreparedStatement ps =
                         conn.c.prepareStatement(checkSql)) {

                ps.setInt(1, itemId);

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        throw new SQLException(
                                "Advertisement does not exist.");
                    }

                    if (rs.getInt("user_id") == userId) {
                        throw new IllegalArgumentException(
                                "You cannot rate your own advertisement.");
                    }

                    if (!"APPROVED".equalsIgnoreCase(
                            rs.getString("ad_review_status"))) {
                        throw new SQLException(
                                "Only approved advertisements can be rated.");
                    }
                }
            }

            try (PreparedStatement ps =
                         conn.c.prepareStatement(ratingSql)) {

                ps.setInt(1, itemId);
                ps.setInt(2, userId);
                ps.setInt(3, rating);

                return ps.executeUpdate() > 0;
            }

        } finally {
            closeConnection(conn);
        }
    }

    /**
     * Get the average user rating.
     * Returns -1 if there are no ratings.
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

                    return rs.wasNull() ? -1 : average;
                }

                return -1;
            }

        } finally {
            closeConnection(conn);
        }
    }

    /**
     * Convert the average rating to a percentage.
     * Returns -1 when there are no ratings.
     */
    public int calculateRatingTrustScore(int itemId)
            throws SQLException {

        double average = getAverageRating(itemId);

        if (average < 0) {
            return -1;
        }

        return (int) Math.round(
                (average / MAX_RATING) * 100.0);
    }

    /**
     * Report a suspicious advertisement.
     * Reporting does not automatically prove fraud.
     */
    public boolean reportAdvertisement(
            int itemId,
            int reporterUserId,
            String reason,
            String details) throws SQLException {

        if (itemId <= 0 || reporterUserId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid advertisement or reporter ID.");
        }

        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select a report reason.");
        }

        String sql =
                "INSERT INTO advertisement_reports "
                + "(item_id, reporter_user_id, reason, details) "
                + "SELECT item_id, ?, ?, ? FROM items "
                + "WHERE item_id = ?";

        Conn conn = new Conn();

        if (conn.c == null) {
            throw new SQLException("Database connection failed.");
        }

        try (PreparedStatement ps = conn.c.prepareStatement(sql)) {

            ps.setInt(1, reporterUserId);
            ps.setString(2, reason.trim());

            if (details == null || details.trim().isEmpty()) {
                ps.setNull(3, Types.VARCHAR);
            } else {
                ps.setString(3, details.trim());
            }

            ps.setInt(4, itemId);

            int rows = ps.executeUpdate();

            if (rows == 0) {
                throw new SQLException(
                        "Advertisement does not exist.");
            }

            return true;

        } finally {
            closeConnection(conn);
        }
    }

    /**
     * Flag an advertisement for administrator review.
     * Never overwrites a rejected advertisement's status.
     */
    public boolean flagAdvertisement(int itemId)
            throws SQLException {

        if (itemId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid advertisement ID.");
        }

        String sql =
                "UPDATE items "
                + "SET ad_review_status = 'PENDING_REVIEW' "
                + "WHERE item_id = ? "
                + "AND ad_review_status <> 'REJECTED'";

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
