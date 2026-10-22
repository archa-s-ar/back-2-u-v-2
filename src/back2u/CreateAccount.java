
package back2u;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CreateAccount extends JFrame implements ActionListener{
    JTextField nametextfield,mailtextfield,phntextfield,stud_idTextfield;
    JPasswordField password,rpassword;
    JCheckBox terms;
    JButton create,back;
    
    CreateAccount(){
          
        setTitle("CREATE ACCOUNT");
        
        setLayout(null);
        getContentPane().setBackground(new Color(245,242,255));
        
        JLabel head = new JLabel("CREATE ACCOUNT");
        head.setBounds(300,100,300,30);
        head.setFont(new Font("Raleway", Font.BOLD, 32));
        add(head);
        
        JLabel name = new JLabel("Full Name: ");
        name.setBounds(200,190,150,30);
        name.setFont(new Font("Raleway",Font.BOLD,18));
        add(name);
        
        nametextfield = new JTextField();
        nametextfield.setBounds(360,190,250,30);
        add(nametextfield);
        
        JLabel stud_id = new JLabel("Student ID ");
        stud_id.setBounds(200,230,150,30);
        stud_id.setFont(new Font("Raleway",Font.BOLD,18));
        add(stud_id);
        
        stud_idTextfield = new JTextField();
        stud_idTextfield.setBounds(360,230,250,30);
        add(stud_idTextfield);
        
        JLabel mail = new JLabel("E-mail id: ");
        mail.setBounds(200,270,150,30);
        mail.setFont(new Font("Raleway",Font.BOLD,18));
        add(mail);
        
        mailtextfield = new JTextField();
        mailtextfield.setBounds(360,270,250,30);
        add(mailtextfield);
        
        JLabel phn = new JLabel("Phone No: ");
        phn.setBounds(200,310,150,30);
        phn.setFont(new Font("Raleway",Font.BOLD,18));
        add(phn);
        
        phntextfield = new JTextField();
        phntextfield.setBounds(360,310,250,30);
        add(phntextfield);
        
        JLabel pass = new JLabel("Password: ");
        pass.setFont(new Font("Raleway",Font.BOLD,18));
        pass.setBounds(200,350,250,30);
        add(pass);
        
        password = new JPasswordField();
        password.setBounds(360,350,250,30);
        add(password);
        
        JLabel rpass = new JLabel("Confirm Password: ");
        rpass.setFont(new Font("Raleway",Font.BOLD,18));
        rpass.setBounds(200,390,250,30);
        add(rpass);
        
        rpassword = new JPasswordField();
        rpassword.setBounds(380,390,250,30);
        add(rpassword);
        
        terms = new JCheckBox("I agree to the Terms and Conditions");
        terms.setFont(new Font("Raleway",Font.BOLD,12));
        terms.setBounds(150,430,250,30);
        terms.setBackground(Color.WHITE);
        add(terms);
        
        create =new JButton("Create Account");
        create.setFont(new Font("Raleway",Font.BOLD,16));
        create.setBounds(240,490,200,30);
        create.addActionListener(this);
        add(create);
        
        back =new JButton("Back");
        back.setFont(new Font("Raleway",Font.BOLD,16));
        back.setBounds(470,490,200,30);
        back.addActionListener(this);
        add(back);
        
        
        
        setSize(900,900);
        setLocation(300,0);
        setVisible(true);
    }
    public void actionPerformed(ActionEvent ae){
        if(ae.getSource()==create){
            
         String passwordl = new String(password.getPassword());
         String fname= nametextfield.getText();
         String email= mailtextfield.getText();
         String phone=phntextfield.getText();
         String stud_idno=stud_idTextfield.getText();
         String confpass = new String(rpassword.getPassword());
         
            if (fname.isEmpty()||email.isEmpty()||phone.isEmpty()||passwordl.isEmpty()||stud_idno.isEmpty()){
                JOptionPane.showMessageDialog(null, "Field empty");
                return;
            }
         
            if (!phone.matches("[0-9]{10}")){
                JOptionPane.showMessageDialog(null, "Enter a valid phone number");
                return;
            }
            
            if(!passwordl.equals(confpass)){
                JOptionPane.showMessageDialog(null, "Password mismatch");
                return;
            }
            
            if (!terms.isSelected()){
                JOptionPane.showMessageDialog(null, "Accept the Terms and Conditions to continue");
                return;
            }
            
            
            
            if(passwordl.length()<8){
             JOptionPane.showMessageDialog(this,"Password must contain 8 characters");
            }
            else if(!passwordl.matches(".*[0-9].*")){
             JOptionPane.showMessageDialog( this,"Password must contain a digit");
            }
            else{
                try{
                 Conn c= new Conn();
                 String query = "INSERT INTO accountdetails (name, student_id, email, phone, password) VALUES ('"
                                    + fname + "','" + stud_idno + "','" + email + "','"
                                    + phone + "','" + passwordl + "')";
                 
                 c.s.executeUpdate(query);
                 JOptionPane.showMessageDialog( null,"Account created successfully");
                }
                catch(Exception e){
                 System.out.println(e);
                }
             }
         }
        else if(ae.getSource()==back){
        }
            setVisible(false);
            new Login().setVisible(true);
        }
    
    public static void main (String args[]){
        
        new CreateAccount();
        
    }
    
}
