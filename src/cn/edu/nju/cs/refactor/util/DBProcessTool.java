package cn.edu.nju.cs.refactor.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author zzf
 * @date 2018.1.22
 * @description some common operation of database. 
 */
public class DBProcessTool implements DBProcess{
	private Connection con;
	
	@Override
	public void setCommit(boolean flag) {
		try {
			con.setAutoCommit(flag);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void commit() {
		try {
			con.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void buildConnection(String driver, String dbPath) {
		 try {
			Class.forName(driver);
			con = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean closeConnection() {
		try {
			if(con.getAutoCommit()==false) {
				con.commit();
			}
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

	@Override
	public String description() {
		StringBuilder sb = new StringBuilder();
		sb.append("build/close database connection\n");
		sb.append("execute query and other sql \n");
		sb.append("set the mode of connection");
		return sb.toString();
	}
}
