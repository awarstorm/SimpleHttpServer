package com.eaglecrk.awarstorm.SimpleHttpServer;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class DatabaseHandler {

	public static String createNewDatabase(String path) {
		String url = "jdbc:sqlite:"+path;
		try {
			File file = new File(path);
			file.getParentFile().mkdirs();
			Class.forName("org.sqlite.JDBC");
			
			try(Connection conn = DriverManager.getConnection(url)){
				if ( conn != null ) {
					DatabaseMetaData meta = conn.getMetaData();
					System.out.println("The driver name is " + meta.getDriverName());
					System.out.println("A new database has been created.");
				}
				
			} catch (SQLException e) {
				System.out.println(e.getMessage());
			}
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}			
		return url;
	}
	
	public static void createTableIfNotExists(Connection conn) {
	    // SQL statement for creating a new table
	    String sql = "CREATE TABLE IF NOT EXISTS sumHistory (\n"
	            + "	id integer PRIMARY KEY,\n"
	            + "	input1 integer,\n"
	            + "	input2 integer,\n"
	            + "	sum integer\n"
	            + ");";
	    
	    try {
	            Statement stmt = conn.createStatement();
	        // create a new table
	        stmt.execute(sql);
	    } catch (SQLException e) {
	        System.out.println(e.getMessage());
	    }	
	}
	
    public static Connection connect(String databaseString) {
        Connection conn = null;
        try {
            // create a connection to the database
        	Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(databaseString);
            System.out.println("Connection to SQLite has been established.");
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }
    
	public static void insertSum(Connection conn, int input1, int input2, int sum) {
	    String sql = "INSERT INTO sumHistory(input1,input2,sum) VALUES(?,?,?)";
	
	    try { 
	        PreparedStatement pstmt = conn.prepareStatement(sql);
	        pstmt.setInt(1, input1);
	        pstmt.setInt(2, input2);
	        pstmt.setInt(3, sum);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        System.out.println(e.getMessage());
	    }
	}
	
	public static ResultSet getList( Connection conn, int maxCount) {
		String sql = "";
		ResultSet rs = null;
		if ( maxCount > 0 ) {
			sql = "SELECT id, input1, input2, sum FROM sumHistory LIMIT 10 OFFSET (SELECT COUNT(*) FROM sumHistory)-10;";
		}
		else {
			sql = "SELECT id, input1, input2, sum FROM sumHistory;";
		}
		try {
			Statement stmt  = conn.createStatement();
            rs    = stmt.executeQuery(sql);
	    } catch (SQLException e) {
	        System.out.println(e.getMessage());
	    }
		return rs;
	}
}
