
package back2u;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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

    /**
     * Backward-compatible method.
     * Existing calls retain the original lost-item scoring.
     */
    public int calculateScore(
            String itemName,
            String category,
            String description,
            String date,
            String time,
            String location,
            String uniqueDetails) {

        return calculateLostScore(
                itemName, category, description,
                date, time, location, uniqueDetails
        );
    }

    public int calculateLostScore(
            String itemName,
            String category,
            String description,
            String date,
            String time,
            String location,
            String uniqueDetails) {

        int score = 0;

        score += scale(scoreItemName(itemName), 20, 20);
        score += scale(scoreDescription(description), 25, 25);
        score += scoreUniqueDetails(uniqueDetails);
        score += scale(scoreLocation(location), 15, 15);
        score += scale(scoreDateAndTime(date, time), 10, 10);
        score += scale(
                scoreCategory(itemName, category, description),
                10, 10
        );

        return clamp(score);
    }

    public int calculateFoundScore(
            String itemName,
            String category,
            String description,
            String date,
            String time,
            String location) {

        int score = 0;

        score += scale(scoreItemName(itemName), 20, 25);
        score += scale(scoreDescription(description), 25, 30);
        score += scale(scoreLocation(location), 15, 20);
        score += scale(scoreDateAndTime(date, time), 10, 15);
        score += scoreCategory(itemName, category, description);

        return clamp(score);
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

        return 20;
    }

    private int scoreDescription(String description) {

        if (isBlank(description)) {
            return 0;
        }

        String value = description.trim().toLowerCase(Locale.ROOT);
        int words = countWords(value);
        int length = value.length();

        if (value.matches(
                "(human|homo sapiens|unknown|nothing|none|"
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

        String value = normalize(location);

        if (value.matches(
                "(unknown|somewhere|anywhere|not known|n a|na)")) {
            return 2;
        }

        if (value.length() < 4) {
            return 3;
        }

        return countWords(value) == 1 ? 9 : 15;
    }

    private int scoreDateAndTime(String date, String time) {

        int score = 0;

        if (!isBlank(date)) {
            try {
                LocalDate parsedDate = LocalDate.parse(date.trim());

                if (!parsedDate.isAfter(LocalDate.now())) {
                    score += 5;
                }
            } catch (DateTimeParseException ex) {
                // Invalid date earns no points.
            }
        }

        if (!isBlank(time) && isValidTime(time.trim())) {
            score += 5;
        }

        return score;
    }

    private boolean isValidTime(String value) {

        DateTimeFormatter[] formats = {
            DateTimeFormatter.ofPattern("H:mm", Locale.ROOT),
            DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT),
            DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("h a", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("hh a", Locale.ENGLISH)
        };

        for (DateTimeFormatter format : formats) {
            try {
                LocalTime.parse(
                        value.toUpperCase(Locale.ROOT), format);
                return true;
            } catch (DateTimeParseException ex) {
                // Try the next accepted format.
            }
        }

        return false;
    }

    private int scoreCategory(
            String itemName,
            String category,
            String description) {

        if (isBlank(category)) {
            return 0;
        }

        String cat = normalize(category);

        if (cat.equals("other") || cat.equals("others")) {
            return 5;
        }

        String text = normalize(
                (itemName == null ? "" : itemName) + " "
                + (description == null ? "" : description)
        );

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
            case "books stationery":
            case "books and stationery":
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
                return 5;
        }

        return matches ? 10 : 3;
    }

    private boolean containsAny(String text, String... keywords) {

        Set<String> words = new HashSet<>(
                Arrays.asList(text.split("\\s+")));

        for (String keyword : keywords) {
            String normalized = normalize(keyword);

            if (normalized.contains(" ")) {
                if ((" " + text + " ").contains(
                        " " + normalized + " ")) {
                    return true;
                }
            } else if (words.contains(normalized)) {
                return true;
            }
        }

        return false;
    }

    private int scale(int rawScore, int oldMaximum, int newMaximum) {
        return (int) Math.round(
                rawScore * (double) newMaximum / oldMaximum);
    }

    private int clamp(int score) {
        return Math.max(0, Math.min(score, 100));
    }

    private String normalize(String value) {
        return value == null
                ? ""
                : value.toLowerCase(Locale.ROOT)
                        .replaceAll("[^a-z0-9]+", " ")
                        .trim();
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
