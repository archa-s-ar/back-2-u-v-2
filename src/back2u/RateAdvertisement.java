
package back2u;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class RateAdvertisement extends JDialog {

    private final int itemId;
    private final int userId;
    private final JComboBox<Integer> ratingBox;
    private final JLabel statusLabel;

    public RateAdvertisement(
            JFrame parent, int itemId, int userId,
            String itemName) {

        super(parent, "Rate Advertisement", true);

        this.itemId = itemId;
        this.userId = userId;

        setSize(380, 240);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JLabel heading = new JLabel(
                "Rate: " + itemName, SwingConstants.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 17));
        heading.setBorder(
                BorderFactory.createEmptyBorder(15, 10, 5, 10));
        add(heading, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(2, 1, 5, 5));

        JPanel ratingPanel = new JPanel();
        ratingPanel.add(new JLabel("Your rating:"));

        ratingBox = new JComboBox<>(
                new Integer[]{1, 2, 3, 4, 5});
        ratingBox.setSelectedItem(5);
        ratingPanel.add(ratingBox);

        center.add(ratingPanel);

        statusLabel = new JLabel("1 = Poor, 5 = Excellent",
                SwingConstants.CENTER);
        center.add(statusLabel);

        add(center, BorderLayout.CENTER);

        JButton submitButton = new JButton("Submit Rating");
        JButton cancelButton = new JButton("Cancel");

        JPanel buttons = new JPanel();
        buttons.add(submitButton);
        buttons.add(cancelButton);
        add(buttons, BorderLayout.SOUTH);

        submitButton.addActionListener(e -> submitRating());
        cancelButton.addActionListener(e -> dispose());

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void submitRating() {

        int rating = (Integer) ratingBox.getSelectedItem();

        try {
            AdvertisementRatingService.submitRating(
                    itemId, userId, rating);

            JOptionPane.showMessageDialog(
                    this,
                    "Rating submitted successfully!",
                    "Back2U",
                    JOptionPane.INFORMATION_MESSAGE);

            dispose();

        } catch (SQLException | IllegalArgumentException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Unable to Submit Rating",
                    JOptionPane.WARNING_MESSAGE);
        }
    }
}
