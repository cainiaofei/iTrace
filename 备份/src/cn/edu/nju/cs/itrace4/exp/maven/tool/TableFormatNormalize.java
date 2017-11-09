//package cn.edu.nju.cs.itrace4.exp.maven.tool;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.ResultSet;
//import java.sql.Statement;
//
///**
// * @author zzf
// * @date 2017/10/11
// * @description in order to reuse the code of niejia, generate a data table named callGraph containing caller and callee from table graph
// * provided by hongyu kuang.
// * note:execute is different from executeQuery, remember commit 
// */
//public class TableFormatNormalize {
//	
//	public void generateFormatTable(String callDBPath) throws Exception {
//		Connection con;
//		Statement stmt;
//		Class.forName("org.sqlite.JDBC");
//        con = DriverManager.getConnection("jdbc:sqlite:" + callDBPath);
//        con.setAutoCommit(false);
//        
//        System.out.println("Opened db successfully\n");
//        stmt = con.createStatement();
//        stmt.execute("delete from callGraph");
//        
//        String sql = "select source as caller, sink as callee from graph where edgeData='call'";
//        ResultSet rs = stmt.executeQuery(sql);
//        while (rs.next()) {
//            String callerInDB = rs.getString("caller").trim();
//            String calleeInDB = rs.getString("callee").trim();
//            String insertSql = "insert into callGraph (caller,callee) values (" + "'" + 
//            		callerInDB + "',"+"'"+calleeInDB + "')";
//            System.out.println(insertSql);
//            stmt = con.createStatement();
//            stmt.executeUpdate(insertSql);
//        }
//        con.commit();
//	}
//	
//	public static void main(String[] args) throws Exception {
//		TableFormatNormalize tool = new TableFormatNormalize();
//		tool.generateFormatTable("");
//	}
//}
