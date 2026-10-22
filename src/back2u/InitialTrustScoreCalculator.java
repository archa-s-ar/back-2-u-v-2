
package back2u;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class InitialTrustScoreCalculator {

    private static final Set<String> GENERIC_NAMES =
            new HashSet<>(Arrays.asList(
                    "item", "thing", "stuff", "object",
                    "unknown", "misc", "something", "human"
            ));

    public int calculateScore(
            String itemName,
            String category,
            String description,
            String date,
            String time,
            String location,
            String uniqueDetails) {

        int score = 0;

        score += scoreItemName(itemName);           // 20
        score += scoreDescription(description);    // 25
        score += scoreUniqueDetails(uniqueDetails);// 20
        score += scoreLocation(location);          // 15
        score += scoreDateAndTime(date, time);     // 10
        score += scoreCategory(itemName, category,
                               description);      // 10

        return Math.max(0, Math.min(score, 100));
    }

    private int scoreItemName(String name) {

        if (isBlank(name)) {
            return 0;
        }

        String value = name.trim().toLowerCase(Locale.ROOT);

        if (GENERIC_NAMES.contains(value)) {
            return 2;
        }

        int words = countWords(value);

        if (value.length() < 4) {
            return 5;
        }

        if (words == 1 && value.length() < 7) {
            return 12;
        }

        if (words >= 2 || value.length() >= 7) {
            return 20;
        }

        return 12;
    }

    private int scoreDescription(String description) {

        if (isBlank(description)) {
            return 0;
        }

        String value = description.trim().toLowerCase(Locale.ROOT);
        int words = countWords(value);
        int length = value.length();

        // Avoid giving high scores to generic descriptions.
        if (value.matches(
                "(?i)(human|homo sapiens|unknown|nothing|none|"
                + "not known|item|object|something)[.! ]*")) {
            return 1;
        }

        if (length < 15 || words < 3) {
            return 4;
        }

        if (length < 35 || words < 6) {
            return 10;
        }

        if (length < 70 || words < 10) {
            return 17;
        }

        if (length < 120 || words < 16) {
            return 22;
        }

        return 25;
    }

    private int scoreUniqueDetails(String details) {

        if (isBlank(details)) {
            return 0;
        }

        int words = countWords(details);
        int length = details.trim().length();

        if (length < 8 || words < 2) {
            return 4;
        }

        if (length < 20 || words < 4) {
            return 10;
        }

        if (length < 40 || words < 7) {
            return 15;
        }

        return 20;
    }

    private int scoreLocation(String location) {

        if (isBlank(location)) {
            return 0;
        }

        String value = location.trim().toLowerCase(Locale.ROOT);

        if (value.matches(
                "(unknown|somewhere|anywhere|not known|n/a|na)")) {
            return 2;
        }

        int words = countWords(value);

        if (value.length() < 4) {
            return 3;
        }

        if (words == 1) {
            return 9;
        }

        return 15;
    }

    private int scoreDateAndTime(String date, String time) {

        int score = 0;

        if (!isBlank(date)) {
            try {
                LocalDate parsed = LocalDate.parse(date.trim());

                if (!parsed.isAfter(LocalDate.now())) {
                    score += 5;
                }
            } catch (DateTimeParseException ex) {
                // Invalid dates earn no date points.
            }
        }

        if (!isBlank(time)) {
            String value = time.trim();

            // Accept examples such as 10:30 AM, 14:30, or 10 AM.
            boolean validTime = value.matches(
                    "(?i)(\\d{1,2}:\\d{2}\\s*(AM|PM)?"
                    + "|\\d{1,2}\\s*(AM|PM))"
            );

            if (validTime) {
                score += 5;
            } else if (value.length() >= 3) {
                score += 2;
            }
        }

        return score;
    }

    private int scoreCategory(
            String itemName,
            String category,
            String description) {

        if (isBlank(category)) {
            return 0;
        }

        String cat = category.trim().toLowerCase(Locale.ROOT);

        if (cat.equals("other") || cat.equals("others")) {
            // "Other" is a valid category, but offers less specificity.
            return 5;
        }

        String text = (
                (itemName == null ? "" : itemName) + " "
                + (description == null ? "" : description)
        ).toLowerCase(Locale.ROOT);

        boolean matches;

        switch (cat) {
            case "electronics":
                matches = containsAny(text,
                        "phone", "laptop", "tablet", "computer",
                        "charger", "headphone", "earphone",
                        "watch", "camera", "electronic");
                break;

            case "bags":
                matches = containsAny(text,
                        "bag", "backpack", "purse", "suitcase");
                break;

            case "books":
            case "books & stationery":
                matches = containsAny(text,
                        "book", "notebook", "pen", "pencil",
                        "stationery", "textbook");
                break;

            case "clothing":
                matches = containsAny(text,
                        "shirt", "jacket", "dress", "coat",
                        "uniform", "clothing", "shoe");
                break;

            case "accessories":
                matches = containsAny(text,
                        "watch", "ring", "bracelet", "necklace",
                        "glasses", "accessory");
                break;

            case "id cards":
                matches = containsAny(text,
                        "id card", "identity card", "student card",
                        "college card", "identification card");
                break;

            case "keys":
                matches = containsAny(text,
                        "key", "keys", "keychain");
                break;

            case "wallets":
                matches = containsAny(text,
                        "wallet", "purse", "card holder");
                break;

            default:
                // Avoid assuming unfamiliar categories are invalid.
                return 5;
        }

        return matches ? 10 : 3;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private int countWords(String text) {
        if (isBlank(text)) {
            return 0;
        }

        return text.trim().split("\\s+").length;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
