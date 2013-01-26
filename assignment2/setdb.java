import java.io.*;
import java.sql.*;

class setdb {
    public static void usage() {
	System.out.println("setdb - setup the database for homework 2");
	System.out.println("usage: setdb username password");
    }
    public Statement getStatement(String url, String username, String password) {
	Connection db = null;
	Statement stmt = null;

	//Class.forName("com.mysql.jdbc.Driver");
	try {
	    // download the driver from: http://jdbc.postgresql.org/
	    // http://jdbc.postgresql.org/download/postgresql-9.2-1002.jdbc4.jar
	    Class.forName("org.postgresql.Driver");
	    db = DriverManager.getConnection(url,username,password);

	    stmt = db.createStatement();

	    ResultSet res = stmt.executeQuery("show search_path");
	    while(res.next()) {
		System.out.println("original search path: " + res.getString(1));
	    }

	    // change the schema search path to public
	    // ALTER USER myuser SET search_path = scott, new_schema, public;
	    stmt.executeUpdate("SET search_path=public");

	    res = stmt.executeQuery("show search_path");
	    while(res.next()) {
		System.out.println("new search path: " + res.getString(1));
	    }
	} catch (ClassNotFoundException e) {
	    e.printStackTrace(System.out);
	} catch (SQLException e) {
	    e.printStackTrace(System.out);
	}
	return stmt;
    }
    public void set(Statement execStat) {
	//Class.forName("com.mysql.jdbc.Driver");
	try {
	    ResultSet res = execStat.executeQuery("SELECT * FROM student");
	    while(res.next()) {
		System.out.println(res.getInt(1)+","+res.getString(2)+","+res.getInt(3));
	    }
	} catch (SQLException e) {
	    e.printStackTrace(System.out);
	}
    }
    public static void main(String[] args) {
	String username = null;
	String password = null;
	if(args.length == 2) {
	    username = args[0];
	    password = args[1];
	} else if(args.length == 0) {
	    // BufferedReader br = new BufferedReader(new FileReader(filename));
	    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	    try {
		System.out.print("username: ");
		username = br.readLine();
		//System.out.print("password: ");
		//password = br.readLine();
		char[] passwd;
		Console cons;
		if((cons=System.console())!=null && (passwd=cons.readPassword("%s","password: "))!=null) {
		    password = new String(passwd);
		}
	    } catch (IOException e) {
		e.printStackTrace(System.out);
	    }
	} else {
	    usage();
	    return;
	}
	String url = "jdbc:postgresql://131.252.208.122:5432/"+username;

	setdb s = new setdb();
	Statement stmt = s.getStatement(url,username,password);
	s.set(stmt);
    }
}