import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.sql.*;

class hw2jdbc {
    public static void usage() {
	System.out.println("hw2jdbc - setup the database for homework 2");
	System.out.println("usage: java hw2jdbc");
	System.out.println("    or java hw2jdbc username password");
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
		//System.out.println("original search path: " + res.getString(1));
	    }

	    // change the schema search path to public
	    // ALTER USER myuser SET search_path = scott, new_schema, public;
	    stmt.executeUpdate("SET search_path=public");

	    res = stmt.executeQuery("show search_path");
	    while(res.next()) {
		//System.out.println("new search path: " + res.getString(1));
	    }
	} catch (ClassNotFoundException e) {
	    e.printStackTrace(System.out);
	} catch (SQLException e) {
	    e.printStackTrace(System.out);
	}
	return stmt;
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
		System.out.println();
	    } catch (IOException e) {
		e.printStackTrace(System.out);
	    }
	} else {
	    usage();
	    return;
	}
	String url = "jdbc:postgresql://131.252.208.122:5432/"+username;

	hw2jdbc s = new hw2jdbc();
	Statement stmt = s.getStatement(url,username,password);

	try {
	    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	    System.out.println("Input the mission please: ");
	    String mission = br.readLine();

	    System.out.println();

	    // get agent whose 
	    String command = "SELECT agent.agent_id,agent.first,agent.last,agent.clearance_id,mission.access_id ";
	    command = command + "FROM mission,teamrel,agent ";
	    command = command + "WHERE mission.mission_id="+mission+" AND mission.team_id=teamrel.team_id AND teamrel.agent_id=agent.agent_id AND mission.access_id<agent.clearance_id";
	    //System.out.println("Issue command: "+command);
	    ResultSet query = stmt.executeQuery(command);
	    Vector<String> commands = new Vector<String>();
	    System.out.println("The agents whose access_id need change:");
	    System.out.printf("  Agent ID    First Name    Last Name    Old Clearance ID    New Clearance ID\n");
	    System.out.printf("----------  ------------  -----------  ------------------  ------------------\n");
	    while(query.next()) {
		//System.out.println(query.getInt(1)+","+query.getString(2)+","+query.getString(3)+","+query.getInt(4)+","+query.getInt(5));
		System.out.printf("%10d  %12s  %11s  %18s  %18s\n", query.getInt(1), query.getString(2), query.getString(3), query.getInt(4), query.getInt(5));
		command = "UPDATE agent SET clearance_id="+query.getInt(5)+" WHERE agent_id="+query.getInt(1);
		commands.add(command);
	    }
	    for(String com : commands) {
		//System.out.println("Issue command: "+com);
		stmt.executeUpdate(com);
	    }

	    System.out.println();

	    // get and print the list of languages
	    command = "SELECT DISTINCT language.language\n";
	    command = command + "FROM mission,teamrel,languagerel,language\n";
	    command = command + "WHERE mission.mission_id="+mission+" AND mission.team_id=teamrel.team_id AND teamrel.agent_id=languagerel.agent_id AND languagerel.lang_id=language.lang_id";
	    //System.out.println("Issue command: "+command);
	    query = stmt.executeQuery(command);
	    System.out.println("The list of languages:");
	    System.out.printf("  Language\n");
	    System.out.printf("----------\n");
	    while(query.next()) {
		//System.out.println(query.getString(1));
		System.out.printf("%10s\n",query.getString(1));
	    }

	    System.out.println();
	} catch (FileNotFoundException e) {
	    e.printStackTrace(System.out);
	} catch (IOException e) {
	    e.printStackTrace(System.out);
	} catch (SQLException e) {
	    e.printStackTrace(System.out);
	}
    }
}