
package back2u;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class AdvertisementRatingPanel extends JPanel {

    private final int itemId;
    private final int userId;

    private final JCheckBox[] ratingBoxes = new JCheckBox[5];
    private final JLabel ratingLabel = new JLabel("Choose a rating");
    private final JButton submitButton = new JButton("Submit Rating");

    private int selectedRating = 0;

    public AdvertisementRatingPanel(int itemId, int userId) {

        this.itemId = itemId;
        this.userId = userId;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder(
                "Rate this advertisement"
        ));

        JPanel checkboxPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT)
        );

        for (int i = 0; i < 5; i++) {

            final int rating = i + 1;

            ratingBoxes[i] = new JCheckBox(
                    String.valueOf(rating)
            );

            ratingBoxes[i].addActionListener(e ->
                    selectRating(rating)
            );

            checkboxPanel.add(ratingBoxes[i]);
        }

        JPanel bottomPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT)
        );

        bottomPanel.add(ratingLabel);
        bottomPanel.add(submitButton);

        add(new JLabel("Rating (1 = low, 5 = high):"),
                BorderLayout.NORTH);
        add(checkboxPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        submitButton.addActionListener(e -> submitRating());
    }

    private void selectRating(int rating) {

        selectedRating = rating;

        // Checkbox scale: selecting 4 checks 1, 2, 3 and 4.
        for (int i = 0; i < ratingBoxes.length; i++) {
            ratingBoxes[i].setSelected(i < rating);
        }

        ratingLabel.setText(
                "Selected rating: " + rating + "/5"
        );
    }

    private void submitRating() {

        if (selectedRating == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a rating from 1 to 5."
            );
            return;
        }

        try {
            AdvertisementRatingService.submitRating(
                    itemId, userId, selectedRating
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Rating submitted successfully!\n"
                    + "Your rating: " + selectedRating + "/5"
            );

            submitButton.setEnabled(false);

        } catch (SQLException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Could not submit rating: " + ex.getMessage(),
                    "Rating Error",
                    JOptionPane.ERROR_MESSAGE
            );

            ex.printStackTrace();

        } catch (IllegalArgumentException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Invalid Rating",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }
}
