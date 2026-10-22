
package back2u;

import java.sql.*;

public class AdvertisementRatingService {

    private static final int REVIEW_THRESHOLD = 50;

    public static void submitRating(
            int itemId, int userId, int rating) throws SQLException {

        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException(
                    "Rating must be between 1 and 5."
            );
        }

        Conn db = new Conn();

        if (db.c == null) {
            throw new SQLException("Database connection failed.");
        }

        Connection con = db.c;

        try {
            con.setAutoCommit(false);

            // Only approved listings can be rated.
            String checkSql =
                    "SELECT item_id FROM items "
                    + "WHERE item_id = ? "
                    + "AND ad_review_status = 'APPROVED' "
                    + "FOR UPDATE";

            try (PreparedStatement ps =
                         con.prepareStatement(checkSql)) {

                ps.setInt(1, itemId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException(
                                "This listing is not available for rating."
                        );
                    }
                }
            }

            // Do not allow users to rate their own listings.
            String ownerSql =
                    "SELECT user_id FROM items WHERE item_id = ?";

            try (PreparedStatement ps =
                         con.prepareStatement(ownerSql)) {

                ps.setInt(1, itemId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt("user_id") == userId) {
                        throw new SQLException(
                                "You cannot rate your own listing."
                        );
                    }
                }
            }

            // Unique(item_id, user_id) prevents duplicate ratings.
            String insertSql =
                    "INSERT INTO advertisement_ratings "
                    + "(item_id, user_id, rating) VALUES (?, ?, ?)";

            try (PreparedStatement ps =
                         con.prepareStatement(insertSql)) {

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
                if (db.s != null) db.s.close();
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

                double average = rs.getDouble("average_rating");

                if (rs.wasNull()) {
                    // No ratings: retain the initial score.
                    updateScoreOnly(itemId, initialScore, con);
                    return;
                }

                averageRating = average;
            }
        }

        double ratingScore = (averageRating / 5.0) * 100.0;

        int newScore = (int) Math.round(
                0.70 * initialScore + 0.30 * ratingScore
        );

        newScore = Math.max(0, Math.min(100, newScore));

        String newStatus = reviewStatus;

        // A previously approved listing below the threshold
        // must return to admin review.
        if (newScore < REVIEW_THRESHOLD
                && "APPROVED".equals(reviewStatus)) {

            newStatus = "PENDING_REVIEW";
        }

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

    public static double getAverageRating(int itemId)
            throws SQLException {

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

                    if (rs.wasNull()) {
                        return 0.0;
                    }

                    return average;
                }
            }

        } finally {
            try {
                if (db.s != null) db.s.close();
                if (db.c != null) db.c.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        return 0.0;
    }
}
