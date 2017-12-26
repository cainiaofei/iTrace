package cn.edu.nju.cs.tool.test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class Demo {
	
//	public void process() {
//		String driver = "org.sqlite.JDBC";
//		String dbPath = "data/exp/Infinispan/rtm/Infinispan-req.db";
//		try {
//			Class.forName(driver);
//			Connection con = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
//			Statement stmt = con.createStatement();
//			for(int i = 1; i < 100;i++) {
//				String sql = "insert into temp(id) values("+i+")";
//				stmt.execute(sql);
//			}
//			con.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	
	public void process() {
		String driver = "org.sqlite.JDBC";
		String dbPath = "data/exp/Infinispan/rtm/Infinispan-req.db";
		try {
			Class.forName(driver);
			Connection con = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
			con.setAutoCommit(false);
			String sql = "insert into temp(id) values(?)";
			PreparedStatement stmt = con.prepareStatement(sql);
			for(int i = 1; i < 100;i++) {
				stmt.setInt(1, i);
				stmt.executeUpdate();
			}
			con.commit();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public String getClassName(String str) {
		String temp = str.substring(0, str.indexOf("("));
		return temp.substring(0,temp.lastIndexOf('.'));
	}
	
	public static void main(String[] args){
		Demo demo = new Demo();
		//demo.process();
		String str = "xx.YY.cc(dd).xxx";
		System.out.println(demo.getClassName(str));
	}
}

/**
 * 
 * try sql
 */
