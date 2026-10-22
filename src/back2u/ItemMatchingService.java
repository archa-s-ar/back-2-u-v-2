
package back2u;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ItemMatchingService {

    // Stores one possible lost-found match.
    public static class MatchResult {
        public int lostItemId;
        public int foundItemId;
        public String lostItemName;
        public String foundItemName;
        public String category;
        public String description;
        public String location;
        public LocalDate date;
        public int score;

        public MatchResult(
                int lostItemId,
                int foundItemId,
                String lostItemName,
                String foundItemName,
                String category,
                String description,
                String location,
                LocalDate date,
                int score) {

            this.lostItemId = lostItemId;
            this.foundItemId = foundItemId;
            this.lostItemName = lostItemName;
            this.foundItemName = foundItemName;
            this.category = category;
            this.description = description;
            this.location = location;
            this.date = date;
            this.score = score;
        }
    }

    // Represents an item fetched from the database.
    private static class Item {
        int id;
        String name;
        String category;
        String description;
        String location;
        String uniqueDetails;
        LocalDate date;

        Item(ResultSet rs) throws SQLException {
            id = rs.getInt("item_id");
            name = rs.getString("item_name");
            category = rs.getString("category");
            description = rs.getString("description");
            location = rs.getString("location");
            uniqueDetails = rs.getString("unique_details");

            Date sqlDate = rs.getDate("date_occurred");

            date = sqlDate == null
                    ? null : sqlDate.toLocalDate();
        }
    }

    // Fetch all open reports and compare LOST against FOUND.
    public static List<MatchResult> findAllMatches()
            throws SQLException {

        List<Item> lostItems = new ArrayList<>();
        List<Item> foundItems = new ArrayList<>();
        List<MatchResult> matches = new ArrayList<>();

        String sql =
            "SELECT item_id, item_name, category, description, " +
            "location, date_occurred, unique_details, " +
            "report_type, status " +
            "FROM items " +
            "WHERE report_type IN ('LOST', 'FOUND')";

        try (Connection con = new Conn().c;
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                String type = rs.getString("report_type");
                String status = rs.getString("status");

                if (!"OPEN".equalsIgnoreCase(status)) {
                    continue;
                }

                Item item = new Item(rs);

                if ("LOST".equalsIgnoreCase(type)) {
                    lostItems.add(item);
                } else if ("FOUND".equalsIgnoreCase(type)) {
                    foundItems.add(item);
                }
            }
        }

        // Compare every lost report with every found report.
        for (Item lost : lostItems) {
            for (Item found : foundItems) {

                int score = ItemMatcher.calculateScore(
                    lost.name,
                    found.name,
                    lost.description,
                    found.description,
                    lost.location,
                    found.location,
                    lost.date,
                    found.date,
                    lost.category,
                    found.category,
                    lost.uniqueDetails,
                    found.uniqueDetails
                );

                if (score >= 30) {
    matches.add(new MatchResult(
        lost.id, found.id,
        lost.name, found.name,
        found.category, found.description,
        found.location, found.date, score
    ));
}
            }
        }

        // Group by lost item, with its strongest matches first.
        matches.sort(
            Comparator.comparingInt(
                (MatchResult m) -> m.lostItemId
            ).thenComparing(
                Comparator.comparingInt(
                    (MatchResult m) -> m.score
                ).reversed()
            )
        );

        return matches;
    }

    // Find possible matches for one particular lost report.
    public static List<MatchResult> findMatches(int lostItemId)
            throws SQLException {

        List<MatchResult> results = new ArrayList<>();

        for (MatchResult match : findAllMatches()) {
            if (match.lostItemId == lostItemId) {
                results.add(match);
            }
        }

        return results;
    }
}
