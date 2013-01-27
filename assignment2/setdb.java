import java.io.*;
import java.util.*;
import java.util.regex.*;
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

	try {
	    BufferedReader br = new BufferedReader(new FileReader("data/schema.txt"));
	    String str = null;
	    Vector<String> tables = new Vector<String>();
	    Set<String> intset = new HashSet<String>();
	    Vector<String> commands1 = new Vector<String>();
	    Vector<String> commands2 = new Vector<String>();
	    while((str=br.readLine())!=null) {
		Pattern pattern = Pattern.compile("^CREATE\\sTABLE\\s(.*)\\s\\(");
		Matcher matcher = pattern.matcher(str);
		if(matcher.matches()) {
		    tables.add(matcher.group(1));
		    String command1 = "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='"+matcher.group(1)+"'";
		    commands1.add(command1);
		    String command2 = "DROP TABLE "+matcher.group(1);
		    commands2.add(command2);
		}
		pattern = Pattern.compile("^\\s+(.*)\\sinteger.*");
		matcher = pattern.matcher(str);
		if(matcher.matches()) {
		    intset.add(matcher.group(1));
		}
	    }

	    // if the tables already exist, then drop them
	    for(int i=commands1.size()-1; i>=0; i--) {
		String command1 = commands1.get(i);
		String command2 = commands2.get(i);
		try {
		    System.out.println("Issue command: "+command1);
		    ResultSet table = stmt.executeQuery(command1);
		    if(table.next()) {
			System.out.println("Issue command: "+command2);
			stmt.executeUpdate(command2);
		    }
		} catch (SQLException e) {
		    e.printStackTrace(System.out);
		}
	    }

	    // create the tables
	    br = new BufferedReader(new FileReader("data/schema.txt"));
	    String command = new String();
	    while((str=br.readLine())!=null) {
		command = command + str;
		if(str.equals(")")) {
		    System.out.println("Issue command: "+command);
		    try {
			stmt.executeUpdate(command);
		    } catch(SQLException e) {
			e.printStackTrace(System.out);
		    }
		    command = new String();
		}
	    }

	    // insert data into the tables
	    for(int i=0; i<tables.size(); i++) {
		String table = tables.get(i);

		// read data from file
		br = new BufferedReader(new FileReader("data/"+table+".csv"));
		Vector<Vector<String>> tabvec = new Vector<Vector<String>>();
		while((str=br.readLine())!=null) {
		    Vector<String> linevec = new Vector<String>();
		    Pattern pattern = Pattern.compile("\"([^,]+)\",?");
		    Matcher matcher = pattern.matcher(str);
		    while(matcher.find()) {
			// System.out.println(matcher.group(1));
			linevec.add(matcher.group(1));
		    }
		    tabvec.add(linevec);
		}

		// begin import data
		System.out.println("Import table: "+table);
		Vector<String> linevec0 = tabvec.get(0);
		String commandpre = "INSERT INTO "+table+"("+linevec0.get(0);
		for(int k=1; k<linevec0.size(); k++) {
		    commandpre = commandpre + "," + linevec0.get(k);
		}
		commandpre = commandpre + ") ";
		for(int k=1; k<tabvec.size(); k++) {
		    Vector<String> linevecn = tabvec.get(k);
		    command = commandpre+"VALUES (";
		    String title = linevec0.get(0);
		    if(linevecn.get(0).equals("\\N")) {
			command = command + "NULL";
		    } else if(intset.contains(title)) {
			command = command + linevecn.get(0);
		    } else {
			command = command + "'" + linevecn.get(0) + "'";
		    }
		    for(int l=1; l<linevecn.size(); l++) {
		        title = linevec0.get(l);
			if(linevecn.get(l).equals("\\N")) {
			    command = command + "," + "NULL";
			} else if(intset.contains(title)) {
			    command = command + "," + linevecn.get(l);
			} else {
			    command = command + ",'" + linevecn.get(l) + "'";
			}
		    }
		    command = command + ")";

		    // insert one line of data
		    System.out.println("Issue command: "+command);
		    try {
			stmt.executeUpdate(command);
		    } catch(SQLException e) {
			e.printStackTrace(System.out);
		    }
		}
	    }
	} catch (FileNotFoundException e) {
	    e.printStackTrace(System.out);
	} catch (IOException e) {
	    e.printStackTrace(System.out);
	}
    }
}