import java.io.*;
import java.sql.*;

class hw2jdbc {
    public static void main(String[] args) {
	System.out.println("hw2jdbc");
	//Class.forName("com.mysql.jdbc.Driver");
	try {
	    // download the driver from: http://jdbc.postgresql.org/
	    // http://jdbc.postgresql.org/download/postgresql-9.2-1002.jdbc4.jar
	    Class.forName("org.postgresql.Driver");
	    String url = "jdbc:postgresql://131.252.208.122:5432/w13db10";
	    String username = "w13db10";
	    String password = "Derek.QIAN";
	    Connection db = DriverManager.getConnection(url,username,password);

	    Statement execStat = db.createStatement();

	    ResultSet res = execStat.executeQuery("show search_path");
	    while(res.next()) {
		System.out.println(res.getString(1));
	    }

	    // change the schema search path to public
	    // ALTER USER myuser SET search_path = scott, new_schema, public;
	    execStat.executeUpdate("SET search_path=public");

	    res = execStat.executeQuery("show search_path");
	    while(res.next()) {
		System.out.println(res.getString(1));
	    }
	    res = execStat.executeQuery("SELECT * FROM student");
	    while(res.next()) {
		System.out.println(res.getInt(1)+","+res.getString(2)+","+res.getInt(3));
	    }
	} catch (ClassNotFoundException e) {
	    e.printStackTrace(System.out);
	} catch (SQLException e) {
	    e.printStackTrace(System.out);
	}
    }
}