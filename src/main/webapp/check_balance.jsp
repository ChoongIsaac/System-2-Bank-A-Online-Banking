<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    <%@ page import="java.sql.*" %>
    
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>BankA2U</title>
</head>
<h1>Online Banking System</h1>
<h2>Check Balance</h2>
<body>
<form method="POST">
      Account: <input type="text" id="account" name="account" /><br>
      PIN: <input type="text" id="pin" name="pin" />
      <input type="submit" value="Check Balance" />
    </form>
<% 
try{
String acc = request.getParameter("account");
String pin = request.getParameter("pin");
Class.forName("oracle.jdbc.OracleDriver");
Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@fsktmdbora.upm.edu.my:1521:fsktm", "nky", "nky");
PreparedStatement stmt = conn.prepareStatement("SELECT id, name, balance, online_pin FROM accounts where id=? and online_pin=?");
stmt.setString(1, acc);
stmt.setString(2, pin);
ResultSet rs = stmt.executeQuery();

	%>
	<table>
    <tr>
    	<th>Acc</th>
        <th>Name</th>
        <th>Balance</th>
      
    </tr>
    <%
    while (rs.next()) {
    %>
    <tr>
    	<td><%= rs.getString("id") %></td>
        <td><%= rs.getString("name") %></td>
        <td><%= rs.getString("balance") %></td>
 
    </tr>
    <%
    }
    
}catch(Exception e){
	e.printStackTrace();
	out.println("Couldnt connect to database");
}
    %>
</table>
</body>
</html>