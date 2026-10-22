package back2u;

import java.sql.*; // for databade jdbc
import java.io.*;
import java.util.Properties;


public class Conn {
    // for step 2, connection define this Connection c;
    Connection c;
    Statement s; //for step 3 
    
    public Conn(){
        try{
      
            Properties prop = new Properties();

            FileInputStream fis = new FileInputStream("back2u.properties");
            prop.load(fis);
         

            String url = prop.getProperty("db.url");
            String username = prop.getProperty("db.username");
            String password = prop.getProperty("db.password");

            //to register Class.static method(forname)(driver name);
            //Class.forName(com.mysql.cj.jdbc.Driver); /*No need of this line, bcz library can pick on its own*/
            //import library
            //Create Connection
            
                    //url=jdbc:mysql://localhost:3306/.... but default is localhost so ignore
                    //url,username,password
            
            c = DriverManager.getConnection(url, username, password);
            s = c.createStatement(); //create statement
            
            
            
        }
        catch (Exception e){
            System.out.println(e);
        }
    }
}