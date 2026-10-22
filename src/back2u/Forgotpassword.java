
package back2u;

import javax.swing.*;
import java.sql.*;
import java.awt.*;
import java.awt.event.*;
public class Forgotpassword extends JFrame implements ActionListener {
    JTextField l1text;
    JPasswordField l2pass, confirm;
    JButton reset, back;
    Forgotpassword(){
        setTitle("Forgot Password");
        setSize(600,800);
        setLayout(null);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(245,242,255));
        
        JLabel title=new JLabel("Forgot Password");
        title.setBounds(130,20,240,30);
        title.setFont(new Font("Raleway",Font.BOLD,22));
        add(title);
        
        //email label
        JLabel l1=new JLabel("Email:");
        l1.setBounds(40,70,100,30);
        l1.setFont(new Font("Raleway",Font.BOLD,16));
        add(l1);
        
        //email textfield
        l1text=new JTextField();
        l1text.setBounds(220,70,220,30);
        add(l1text);
        
        //password label
        JLabel l2=new JLabel("New Password:");
        l2.setBounds(40,120,200,30);
        l2.setFont(new Font("Raleway",Font.BOLD,16));
        add(l2);
        
        //passwordfield
        l2pass=new JPasswordField();
        l2pass.setBounds(220,120,220,30);
        add(l2pass);
        
        //confirmpassword
        JLabel l3=new JLabel("Confirm password:");
        l3.setBounds(40,160,200,30);
        l3.setFont(new Font("Raleway",Font.BOLD,16));
        add(l3);
        
        //confirmapassword passwordfield
        confirm=new JPasswordField();
        confirm.setBounds(220,160,220,30);
        add(confirm);
        
        reset =new JButton("Reset password");
        reset.setBounds(280,220,200,30);
        reset.setFont(new Font("Raleway",Font.BOLD,16));
        reset.addActionListener(this);
        add(reset);
        
        back =new JButton("Back to login");
        back.setBounds(50,220,200,30);
        back.setFont(new Font("Raleway",Font.BOLD,16));
        back.addActionListener(this);
        add(back);
     
        
        setVisible(true);
        
    }
    public void actionPerformed(ActionEvent et){
        if(et.getSource()==reset){
            String em=l1text.getText().trim();
            String p1=new String(l2pass.getPassword());
            String p2=new String(confirm.getPassword());
            if(em.isEmpty()||p1.isEmpty()||p2.isEmpty()){
                JOptionPane.showMessageDialog(this, "Please fill all fields");
            }
            else if(!p1.equals(p2)){
                JOptionPane.showMessageDialog(this, "Password do not match");
                
            }
            else {
                try{
                    Conn con=new Conn();
                    String query="UPDATE accountdetails SET password=? WHERE email=?";
                    PreparedStatement ps=con.c.prepareStatement(query);
                    ps.setString(1,p1);
                    ps.setString(2,em);
                    int rows=ps.executeUpdate();
                    if(rows>0){
                        JOptionPane.showMessageDialog(this,"Password reset successful!");
                        
                    }
                    else{
                        JOptionPane.showMessageDialog(this,"Email not registered");
                    }
                    ps.close();
                    con.c.close();
                    
                }
                catch(Exception ex){
                    JOptionPane.showMessageDialog(this,"Error:"+ex.getMessage());
                    
                }
            }
            
        }
        else if(et.getSource()==back){
            dispose();
            new Login();
        }
        
    }
    
public static void main(String args[]){
    new Forgotpassword();
 }
}
