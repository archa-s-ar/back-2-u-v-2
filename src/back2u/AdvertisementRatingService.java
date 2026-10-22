
package back2u;

import java.sql.*;

public class AdvertisementRatingService {

    private static final int REVIEW_THRESHOLD = 50;
    private static final int MAX_RATING = 5;

    /**
     * Submit a rating or update the user's existing rating.
     * Only approved advertisements can be rated.
     */
    public static void submitRating(
            int itemId, int userId, int rating) throws SQLException {

        if (itemId <= 0 || userId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid advertisement or user ID.");
        }

        if (rating < 1 || rating > MAX_RATING) {
            throw new IllegalArgumentException(
                    "Rating must be between 1 and 5.");
        }

        Conn db = new Conn();

        if (db.c == null) {
            throw new SQLException("Database connection failed.");
        }

        Connection con = db.c;

        try {
            con.setAutoCommit(false);

            int ownerId;
            String reviewStatus;

            String checkSql =
                    "SELECT user_id, ad_review_status "
                    + "FROM items WHERE item_id = ? FOR UPDATE";

            try (PreparedStatement ps =
                         con.prepareStatement(checkSql)) {

                ps.setInt(1, itemId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException(
                                "Advertisement not found.");
                    }

                    ownerId = rs.getInt("user_id");
                    reviewStatus = rs.getString("ad_review_status");
                }
            }

            if (ownerId == userId) {
                throw new SQLException(
                        "You cannot rate your own advertisement.");
            }

            if (!"APPROVED".equalsIgnoreCase(reviewStatus)) {
                throw new SQLException(
                        "Only approved advertisements can be rated.");
            }

            String ratingSql =
                    "INSERT INTO advertisement_ratings "
                    + "(item_id, user_id, rating) VALUES (?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE "
                    + "rating = VALUES(rating), "
                    + "created_at = CURRENT_TIMESTAMP";

            try (PreparedStatement ps =
                         con.prepareStatement(ratingSql)) {

                ps.setInt(1, itemId);
                ps.setInt(2, userId);
                ps.setInt(3, rating);
                ps.executeUpdate();
            }

            recalculateTrustScore(itemId, con);

            con.commit();

        } catch (SQLException | RuntimeException ex) {

            try {
                con.rollback();
            } catch (SQLException rollbackEx) {
                ex.addSuppressed(rollbackEx);
            }

            throw ex;

        } finally {

            try {
                con.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            try {
                if (db.s != null) {
                    db.s.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            try {
                con.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Recalculate trust score using:
     * 70% initial listing score + 30% average rating score.
     *
     * Pending advertisements remain pending.
     * Rejected advertisements remain rejected.
     */
    private static void recalculateTrustScore(
            int itemId, Connection con) throws SQLException {

        String sql =
                "SELECT i.initial_trust_score, "
                + "i.ad_review_status, "
                + "AVG(r.rating) AS average_rating "
                + "FROM items i "
                + "LEFT JOIN advertisement_ratings r "
                + "ON i.item_id = r.item_id "
                + "WHERE i.item_id = ? "
                + "GROUP BY i.item_id, i.initial_trust_score, "
                + "i.ad_review_status";

        int initialScore;
        String reviewStatus;
        double averageRating;

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, itemId);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new SQLException("Listing not found.");
                }

                initialScore = rs.getInt("initial_trust_score");
                reviewStatus = rs.getString("ad_review_status");

                averageRating = rs.getDouble("average_rating");

                if (rs.wasNull()) {
                    // No ratings: preserve the initial score.
                    updateScoreOnly(itemId, initialScore, con);
                    return;
                }
            }
        }

        double ratingScore =
                (averageRating / MAX_RATING) * 100.0;

        int newScore = (int) Math.round(
                0.70 * initialScore + 0.30 * ratingScore);

        newScore = Math.max(0, Math.min(100, newScore));

        String newStatus = reviewStatus;

        // Only an already-approved listing can be moved
        // back to review by a falling score.
        if (newScore < REVIEW_THRESHOLD
                && "APPROVED".equalsIgnoreCase(reviewStatus)) {

            newStatus = "PENDING_REVIEW";
        }

        // This condition protects rejected listings.
        String updateSql =
                "UPDATE items SET trust_score = ?, "
                + "ad_review_status = ? "
                + "WHERE item_id = ? "
                + "AND ad_review_status <> 'REJECTED'";

        try (PreparedStatement ps =
                     con.prepareStatement(updateSql)) {

            ps.setInt(1, newScore);
            ps.setString(2, newStatus);
            ps.setInt(3, itemId);
            ps.executeUpdate();
        }
    }

    private static void updateScoreOnly(
            int itemId, int score, Connection con)
            throws SQLException {

        String sql =
                "UPDATE items SET trust_score = ? "
                + "WHERE item_id = ? "
                + "AND ad_review_status <> 'REJECTED'";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, score);
            ps.setInt(2, itemId);
            ps.executeUpdate();
        }
    }

    /**
     * Returns the average rating, or 0.0 if no ratings exist.
     */
    public static double getAverageRating(int itemId)
            throws SQLException {

        if (itemId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid advertisement ID.");
        }

        Conn db = new Conn();

        if (db.c == null) {
            throw new SQLException("Database connection failed.");
        }

        try (PreparedStatement ps = db.c.prepareStatement(
                "SELECT AVG(rating) AS avg_rating "
                + "FROM advertisement_ratings "
                + "WHERE item_id = ?")) {

            ps.setInt(1, itemId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double average = rs.getDouble("avg_rating");

                    return rs.wasNull() ? 0.0 : average;
                }
            }

        } finally {

            try {
                if (db.s != null) {
                    db.s.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            try {
                if (db.c != null) {
                    db.c.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        return 0.0;
    }
}
