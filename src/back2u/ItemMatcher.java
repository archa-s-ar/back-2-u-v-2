
package back2u;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ItemMatcher {

    public static int calculateScore(
            String lostName,
            String foundName,
            String lostDescription,
            String foundDescription,
            String lostLocation,
            String foundLocation,
            LocalDate lostDate,
            LocalDate foundDate,
            String lostCategory,
            String foundCategory,
            String lostUniqueDetails,
            String foundUniqueDetails) {

        int score = 0;

        // 1. Item name: 20 points
        score += textScore(lostName, foundName, 20);

        // 2. Description: 25 points
        score += textScore(
            lostDescription, foundDescription, 25
        );

        // 3. Location: 25 points
        score += locationScore(
            lostLocation, foundLocation
        );

        // 4. Date: 15 points
        score += dateScore(lostDate, foundDate);

        // 5. Category: 10 points
        if (sameText(lostCategory, foundCategory)) {
            score += 10;
        }

        // 6. Unique details: 5 points
        score += textScore(
            lostUniqueDetails, foundUniqueDetails, 5
        );

        return Math.min(score, 100);
    }

    private static int textScore(
            String a, String b, int maxPoints) {

        Set<String> wordsA = words(a);
        Set<String> wordsB = words(b);

        if (wordsA.isEmpty() || wordsB.isEmpty()) {
            return 0;
        }

        Set<String> common = new HashSet<>(wordsA);
        common.retainAll(wordsB);

        Set<String> all = new HashSet<>(wordsA);
        all.addAll(wordsB);

        double similarity =
            (double) common.size() / all.size();

        return (int) Math.round(similarity * maxPoints);
    }

    private static Set<String> words(String text) {

        Set<String> result = new HashSet<>();

        if (text == null || text.trim().isEmpty()) {
            return result;
        }

        String cleaned = text.toLowerCase()
            .replaceAll("[^a-z0-9]+", " ")
            .trim();

        if (!cleaned.isEmpty()) {
            result.addAll(
                Arrays.asList(cleaned.split("\\s+"))
            );
        }

        return result;
    }

    private static int locationScore(String a, String b) {

        if (a == null || b == null
                || a.trim().isEmpty()
                || b.trim().isEmpty()) {
            return 0;
        }

        String x = a.trim().toLowerCase();
        String y = b.trim().toLowerCase();

        if (x.equals(y)) {
            return 25;
        }

        if (x.contains(y) || y.contains(x)) {
            return 15;
        }

        return 0;
    }

    private static int dateScore(
            LocalDate lost, LocalDate found) {

        if (lost == null || found == null) {
            return 0;
        }

        long days = Math.abs(
            ChronoUnit.DAYS.between(lost, found)
        );

        if (days == 0) return 15;
        if (days <= 1) return 12;
        if (days <= 3) return 8;
        if (days <= 7) return 4;

        return 0;
    }

    private static boolean sameText(String a, String b) {
        return a != null && b != null
            && !a.trim().isEmpty()
            && a.trim().equalsIgnoreCase(b.trim());
    }
}
