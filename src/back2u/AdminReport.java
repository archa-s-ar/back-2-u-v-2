
package back2u;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AdminReport extends JFrame implements ActionListener{
    private JComboBox issuebox;
    private JTextField subtxt;
    private JTextArea descri;
    private JButton submit;
    private final int userId;

    AdminReport(int userId){
        this.userId = userId;
        
        setLayout(null);
        JLabel head = new JLabel("Report an Issue");
        head.setFont(new Font("Raleway", Font.BOLD,32));
        head.setBounds(150,100,500,30);
        add(head);
        
        JLabel subhead= new JLabel("Back2U - User Support");
        subhead.setFont(new Font("Raleway", Font.BOLD,14));
        subhead.setBounds(150,135,500,30);
        add(subhead);
        
        JLabel subdescri = new JLabel("Facing a problem? Tell our admin team what happened so we can help");
        subdescri.setFont(new Font("Raleway", Font.BOLD,14));
        subdescri.setBounds(150,165,500,30);
        add(subdescri);
        
        JLabel issuetype=new JLabel("Issue Type");
        issuetype.setFont(new Font("Raleway", Font.BOLD,16));
        issuetype.setBounds(150,200,500,30);
        add(issuetype);
        
        
        String valissue[]={"Technical Issue","Login/Registration Problem","Lost/Found Problem","Incorrect Information","User Account Issue","Other"};
        issuebox = new JComboBox(valissue);
        issuebox.setBounds(150,240,500,30);
        add(issuebox);
        
        JLabel sub=new JLabel("Subject");
        sub.setFont(new Font("Raleway", Font.BOLD,16));
        sub.setBounds(150,280,500,30);
        add(sub);
        
        subtxt = new JTextField();
        subtxt.setBounds(150,320,500,30);
        add(subtxt);
        
        JLabel descrilab= new JLabel("Description");
        descrilab.setBounds(150,360,500,30);
        descrilab.setFont(new Font("Raleway", Font.BOLD,16));
        add(descrilab);
        
        
        descri = new JTextArea();
        descri.setBounds(150,400,500,160);
        add(descri);
        
        submit = new JButton("Submit Report");
        submit.setBounds(300,600,150,30);
        submit.addActionListener(this);
        add(submit);
        
        setLocation(300,0);
        setSize(900,900);
        setVisible(true);
        
    }
    
    public void actionPerformed(ActionEvent ae) {
    if (ae.getSource() == submit) {

        String issue = (String) issuebox.getSelectedItem();
        String subj = subtxt.getText().trim();
        String descrip = descri.getText().trim();

        // Validate the fields
        if (subj.isEmpty() || descrip.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Please enter both the subject and description."
            );
            return;
        }

        String query = "INSERT INTO admin_reports "
                + "(user_id, issue_type, subject, description) "
                + "VALUES (?, ?, ?, ?)";

        try {
            Conn c = new Conn();

            try (java.sql.PreparedStatement ps =
                    c.c.prepareStatement(query)) {

                ps.setInt(1, userId);
                ps.setString(2, issue);
                ps.setString(3, subj);
                ps.setString(4, descrip);

                int rows = ps.executeUpdate();

                if (rows > 0) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Your report has been submitted successfully!"
                    );

                    subtxt.setText("");
                    descri.setText("");
                    issuebox.setSelectedIndex(0);
                }
            } finally {
                if (c.c != null) {
                    c.c.close();
                }
            }

        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Failed to submit report: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
    
    public static void main(String[] args){
        new AdminReport(1);
    }
}
