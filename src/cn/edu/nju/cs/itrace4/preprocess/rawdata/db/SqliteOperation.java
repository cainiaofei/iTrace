package cn.edu.nju.cs.itrace4.preprocess.rawdata.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SqliteOperation implements DBOperationInterface{
	
	private Connection con;
	
	@Override
	public void buildConnection(String driver, String dbPath) {
		 try {
			Class.forName("org.sqlite.JDBC");
			con = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean closeConnection() {
		try {
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public void executeSql(String sql) {
		if(con==null) {
			System.out.println("you should first execute buildConnection method");
			System.exit(-1);
		}
		else {
			try {
				Statement stat = con.createStatement();
				stat.execute(sql);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	
	@SuppressWarnings("finally")
	@Override
	public ResultSet executeQuery(String sql) {
		ResultSet rs = null;
		if(con==null) {
			System.out.println("you should first execute buildConnection method");
			return null;
		}
		else {
			try {
				Statement stat = con.createStatement();
				rs = stat.executeQuery(sql);
			} catch (SQLException e) {
				e.printStackTrace();
			}finally {
				return rs;
			}
		}
	}

}
