
package back2u;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class ReportFounditem extends JFrame implements ActionListener{
    JComboBox<String>category;
    JTextField Itemtext, DateText, TimeText, Locationtext;
    JTextArea Descriptiontext, Uniquetext;
    JButton Cancel, Submit;
    private final int userId;
    
    ReportFounditem(int userId){
        this.userId=userId;
        
        setTitle("Report Found Item");
        setSize(900,820);
        setLocation(300,0);
        setLayout(null);
        JLabel heading=new JLabel("Report Found Item");
        heading.setFont(new Font("Arial",Font.BOLD,26));
        heading.setHorizontalAlignment(SwingConstants.CENTER);
        heading.setBounds(40,20,440,40);
        add(heading);
        
        JLabel subheading=new JLabel("Fill in the details below to report the item you found. ");
        subheading.setBounds(40,60,550,25);
        add(subheading);
        
        JLabel section1 =new JLabel("Item information");
        section1.setFont(new Font("raleway",Font.BOLD,20));
        section1.setBounds(40,140,250,30);
        add(section1);
        
        JLabel categoryLabel = new JLabel("Item Category");
        categoryLabel.setFont(new Font("raleway",Font.BOLD,16));
        categoryLabel.setBounds(40, 200, 110, 25);
        add(categoryLabel);
        
            category=new JComboBox<>(new String[]{
            "Select category","electronics","Bags","Books","Clothing","Accessories","Others"});
            category.setBounds(150,200,280,30);
            add(category);
            
            JLabel ItemName=new JLabel("Item name:");
            ItemName.setFont(new Font("raleway",Font.BOLD,16));
            ItemName.setBounds(40,270,100,30);
            add(ItemName);
            
            Itemtext=new JTextField();
            Itemtext.setBounds(150,270,280,30);
            add(Itemtext);
            
            JLabel Description=new JLabel("Description:");
            Description.setFont(new Font("raleway",Font.BOLD,16));
            Description.setBounds(40,320,100,30);
            add(Description);
            
            Descriptiontext=new JTextArea();
            Descriptiontext.setLineWrap(true);
            Descriptiontext.setWrapStyleWord(true);
            
            JScrollPane descScroll=new JScrollPane(Descriptiontext);
            descScroll.setBounds(150,320,280,70);
            add(descScroll);
            
            JLabel Section2=new JLabel("Found Details");
            Section2.setFont(new Font("raleway",Font.BOLD,20));
            Section2.setBounds(40,410,300,30);
            add(Section2);
            
            JLabel Datefound=new JLabel("Date found:");
            Datefound.setFont(new Font("raleway",Font.BOLD,16));
            Datefound.setBounds(40,450,300,30);
            add(Datefound);
            
            DateText=new JTextField();
            DateText.setToolTipText("Enter the date as yyyy-mm-dd");
            DateText.setBounds(150,450,300,30);
            add(DateText);
            
            JLabel Time=new JLabel("Time:");
            Time.setFont(new Font("raleway",Font.BOLD,16));
            Time.setBounds(40,490,300,30);
            add(Time);
            
             TimeText=new JTextField();
             TimeText.setBounds(150,490,300,30);
             add(TimeText);
             
             JLabel Location=new JLabel("Found Location:");
             Location.setFont(new Font("raleway",Font.BOLD,16));
             Location.setBounds(40,540,300,30);
             add(Location);
             
             Locationtext=new JTextField();
             Locationtext.setBounds(170,540,300,30);
             add(Locationtext);
             
             JLabel Additional=new JLabel("Additional information");
             Additional.setFont(new Font("raleway",Font.BOLD,20));
             Additional.setBounds(40,610,300,30);
             add(Additional);
             
             JLabel Unique=new JLabel("<html>Additional details <br>like how it was kept:</html>"
                     );
             Unique.setFont(new Font("raleway",Font.BOLD,16));
             Unique.setBounds(40,650,165,40);
             add(Unique);
             
             Uniquetext=new JTextArea();
             Uniquetext.setLineWrap(true);
             Uniquetext.setWrapStyleWord(true);
             
             JScrollPane UniqueScroll=new JScrollPane(Uniquetext);
            UniqueScroll.setBounds(210,650,250,70);
            add(UniqueScroll);
             
             
             Cancel=new JButton("Cancel");
             Cancel.setBounds(210,730,110,35);
             Cancel.addActionListener(this);
             add(Cancel);
             
             Submit=new JButton("Submit");
             Submit.setBounds(340,730,120,35);
             Submit.addActionListener(this);
             add(Submit);
                     
  
            
 setVisible(true);
    }
    public void actionPerformed(ActionEvent e){
        if(e.getSource()==Cancel){
            int choice=JOptionPane.showConfirmDialog(
            this,"Do you want to clear the form and close it?",
            "Confirm cancel",
            JOptionPane.YES_NO_OPTION);
            if(choice==JOptionPane.YES_OPTION){
            Itemtext.setText("");
            Descriptiontext.setText("");
            DateText.setText("");
            TimeText.setText("");
            Locationtext.setText("");
            Uniquetext.setText("");
            category.setSelectedIndex(0);
            JOptionPane.showMessageDialog(this,"Form Cleared Successfully");
            dispose();
            }
        }
       else if (e.getSource() == Submit) {

            // Validate required fields
            if (category.getSelectedIndex() == 0
                    || Itemtext.getText().trim().isEmpty()
                    || Descriptiontext.getText().trim().isEmpty()
                    || DateText.getText().trim().isEmpty()
                    || TimeText.getText().trim().isEmpty()
                    || Locationtext.getText().trim().isEmpty()) {

                JOptionPane.showMessageDialog(
                        this, "Please fill in required fields");
            }

            else {

                try {
                    // Get values from the form
                    String itemName = Itemtext.getText().trim();
                    String itemCategory =
                            category.getSelectedItem().toString();
                    String description =
                            Descriptiontext.getText().trim();
                    String time = TimeText.getText().trim();
                    String location = Locationtext.getText().trim();
                    String uniqueDetails = Uniquetext.getText().trim();

                    // Convert yyyy-MM-dd text into a date
                    DateTimeFormatter formatter =
                            DateTimeFormatter.ofPattern("yyyy-MM-dd");

                    LocalDate foundDate =
                            LocalDate.parse(
                                    DateText.getText().trim(), formatter);

                    // Connect to the database
                    Conn con = new Conn();

                    if (con.c == null) {
                        throw new Exception(
                                "Database connection failed. Check Conn.java.");
                    }

                    // Insert the report into the existing items table
                    String query =
        "INSERT INTO items " +
        "(user_id, report_type, item_name, category, description, " +
        "date_occurred, time_occurred, location, status, unique_details) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                    try (PreparedStatement ps = con.c.prepareStatement(query)) {

                    ps.setInt(1, userId);
                    ps.setString(2, "FOUND");
                    ps.setString(3, itemName);
                     ps.setString(4, itemCategory);
                    ps.setString(5, description);
                    ps.setDate(6, java.sql.Date.valueOf(foundDate));
                    ps.setString(7, time);
                    ps.setString(8, location);
                    ps.setString(9, "OPEN");
                    ps.setString(10, uniqueDetails);

                    int rows = ps.executeUpdate();

                if (rows > 0) {JOptionPane.showMessageDialog(this,"Found item reported successfully!");

        Itemtext.setText("");
        Descriptiontext.setText("");
        DateText.setText("");
        TimeText.setText("");
        Locationtext.setText("");
        Uniquetext.setText("");
        category.setSelectedIndex(0);
    }
}

                    con.c.close();

                } catch (java.time.format.DateTimeParseException ex) {

                    JOptionPane.showMessageDialog(
                            this,
                            "Enter a valid date in yyyy-MM-dd format.\n"
                            + "Example: 2026-10-20");

                } catch (Exception ex) {

                    JOptionPane.showMessageDialog(
                            this,
                            "Could not save report: " + ex.getMessage());

                    ex.printStackTrace();
                }
            }
        }
    }

     public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> new ReportFounditem(1));
}
}
