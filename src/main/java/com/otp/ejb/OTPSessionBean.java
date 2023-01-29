package com.otp.ejb;
import java.sql.*;
import java.util.Date;
import java.util.Properties;
import java.time.LocalDateTime;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Session Bean implementation class OTPSessionBean
 */
@Stateless
@LocalBean
public class OTPSessionBean implements OTPSessionBeanRemote, OTPSessionBeanLocal {

    /**
     * Default constructor. 
     */
    public OTPSessionBean() {
        // TODO Auto-generated constructor stub
    }

	@Override
	public String getOTPFromCloud() {
		String otp = "";
		try {
			// Create a URL object for the web service endpoint
	        URL url = new URL("https://fastidious-pothos-4bd0f8.netlify.app/.netlify/functions/generateOTP");

	        // Open a connection to the web service
	        HttpURLConnection con = (HttpURLConnection) url.openConnection();

	        // Set the request method to GET
	        con.setRequestMethod("GET");

	        // Read the response
	        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	        String inputLine;
	        StringBuffer content = new StringBuffer();
	        while ((inputLine = in.readLine()) != null) {
	            content.append(inputLine);
	        }
	        in.close();
	        otp = content.toString();
	        // Close the connection
	        con.disconnect();
			}catch(Exception e) {
				System.out.println("Something went wrong!");
			}
		return otp;
	}

	@Override
	public void sendEmail(String id,int otp) {
		String email = "";
		try {
			//Retrieve the email based on the user id
			DBConnection db = new DBConnection();
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection conn = db.createConnection();
			PreparedStatement stmt = conn.prepareStatement("SELECT email FROM accounts where id=?");
			stmt.setString(1, id);
			ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					email =  rs.getString("email");
				}
		}catch(Exception e) {
			e.printStackTrace();
		}
		try {
			//set up properties for email sending
		  Properties properties = new Properties();
		  properties = System.getProperties();
		  // set the port to gmail server port
		  properties.put("mail.smtp.host", "smtp.gmail.com");
		  properties.put("mail.smtp.port", "465");
		  properties.put("mail.smtp.auth", "true");
		  properties.put("mail.smtp.ssl.enable", "true");
	
		  //authenticate the email sender's google acc
		  //if the sender acc's require 2FA, an App password is needed to be used
		  //I have a 2FA, so this app password will need to update every authentication
	        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
	        	   protected PasswordAuthentication getPasswordAuthentication() {
	        		    return new PasswordAuthentication("choongwenjian@gmail.com", 
	        		    		"vxtlqlvycuuqmafq");
	        		   }
	        		  });
	        session.setDebug(true);
	        
	        //Set the message of the email that contain the OTP
	            Message message = new MimeMessage(session);
	            message.setFrom(new InternetAddress("choongwenjian@gmail.com"));
	            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
	            message.setSubject("Top Up Payment");
	            message.setText("Dear customer!This is an verification email"
	            		+ "from Bank A. Your OTP: " + otp);

	            Transport.send(message);
	            System.out.println("Email sent successfully.");
	        } catch (MessagingException e) {
	            throw new RuntimeException(e);
	        }
	}

	@Override
	public void storeOTP(int otp,String id) {
		// TODO Auto-generated method stub
		try {
			DBConnection db = new DBConnection();
			
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection conn = db.createConnection();
			  String insertSQL = "UPDATE accounts SET otp = ? WHERE id = ?";
	            PreparedStatement insertStatement = conn.prepareStatement(insertSQL);

	            // Set the values for the statement's parameters
	            insertStatement.setInt(1, otp);
	            insertStatement.setString(2, id);
	          
	            // Execute the statement
	            insertStatement.executeUpdate();

	            // Close the connection
	            conn.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	@Override
	public boolean verifyOTP(String id,int enteredOTP) {
		int otp_db = 0;
		try {
	     // get OTP from the database
		DBConnection db = new DBConnection();
		Class.forName("oracle.jdbc.driver.OracleDriver");
		Connection conn = db.createConnection();
		PreparedStatement stmt = conn.prepareStatement("SELECT otp FROM accounts where id=?");
		stmt.setString(1, id);
		ResultSet rs = stmt.executeQuery();
		 while (rs.next()) {
			 otp_db =  rs.getInt("otp");
		 }
		
		 // set the expiry time for OTP
		 LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(5);
		 // this is the current time to submit OTP
		 LocalDateTime now = LocalDateTime.now();
		 // if valid return true flag 
	        if (enteredOTP == otp_db && now.isBefore(expiryTime)) {
	            return true;
	        }else {
	        	return false;
	        }
	        
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return false;
		
	}

}
