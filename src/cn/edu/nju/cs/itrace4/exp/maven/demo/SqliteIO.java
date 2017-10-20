package cn.edu.nju.cs.itrace4.exp.maven.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import cn.edu.nju.cs.itrace4.relation.info.CallRelation;

/**
 * @author zzf <tiaozhanzhe668@163.com> 
 * @date 2017/10/11
 * @description test sqlite io
 */

public class SqliteIO {
	
	
	public void writeDATA() throws Exception {
		String callDBPath = "data\\exp\\Infinispan\\rtm\\Infinispan-req.db";
		Connection con;
		Statement stmt;
		Class.forName("org.sqlite.JDBC");
        con = DriverManager.getConnection("jdbc:sqlite:" + callDBPath);
        con.setAutoCommit(true);
        stmt = con.createStatement();
        
        String sql = "SELECT * FROM issue;";
        ResultSet rs = stmt.executeQuery(sql);
        Set<String> classSet = new HashSet<String>();
        
        while (rs.next()) {
        	String callerInDB = rs.getString("issue_id").trim();
            String calleeInDB = rs.getString("type").trim();

//            String callerMethod = sqlFormatToIdFormatInCallDB(callerInDB);
////            System.out.println(" callerMethod = " + callerMethod );
//            String calleeMethod = sqlFormatToIdFormatInCallDB(calleeInDB);
//            String callerClass = callerMethod.split("#")[0];
//            String calleeClass = calleeMethod.split("#")[0];
//            classSet.add(callerClass);
//            classSet.add(calleeClass);
            System.out.println(callerInDB);
        }
        storeClassSet(classSet);
	}
	
	private void storeClassSet(Set<String> classSet) throws IOException {
		File file = new File("D:\\workspace\\eclipse-workspace\\iTrace4\\data\\"
				+ "exp\\Maven\\relation\\classSet.out");
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
		out.writeObject(classSet);
		out.close();
	}

	public void process() throws Exception {
		String callDBPath = "D:\\workspace\\eclipse-workspace\\iTrace4\\data\\"
				+ "exp\\Maven\\relation\\Maven_CDGraph.db";
		Connection con;
		Statement stmt;
		Class.forName("org.sqlite.JDBC");
        con = DriverManager.getConnection("jdbc:sqlite:" + callDBPath);
        con.setAutoCommit(false);
        System.out.println("Opened %s successfully\n");
        stmt = con.createStatement();

        String sql = "SELECT * FROM callGraph;";
        ResultSet rs = stmt.executeQuery(sql);
        Set<String> classSet = new HashSet<String>();
        while (rs.next()) {
        	String callerInDB = rs.getString("caller").trim();
            String calleeInDB = rs.getString("callee").trim();

            String callerMethod = sqlFormatToIdFormatInCallDB(callerInDB);
//            System.out.println(" callerMethod = " + callerMethod );
            String calleeMethod = sqlFormatToIdFormatInCallDB(calleeInDB);
            String callerClass = callerMethod.split("#")[0];
            String calleeClass = calleeMethod.split("#")[0];
//            System.out.println(cr);
           System.out.println(callerClass+"---"+calleeClass);
           classSet.add(callerClass);
           classSet.add(calleeClass);
        }
        storeClassSet(classSet);
	}
	
	  private static String sqlFormatToIdFormatInCallDB(String sqlFormat) {

	        String[] tokens = sqlFormat.split("\\.");

	        // find Java method
	        for (int i = 0; i < tokens.length; i++) {
	            if (tokens[i].startsWith("MPEGecoder")) continue;
	            if (Character.isUpperCase(tokens[i].charAt(0))) {
	                StringBuilder sb = new StringBuilder();

//	                String s = tokens[i];
//	                sb.append(tokens[i]);
	                sb.append(tokens[i].split("\\$")[0]);
	                sb.append("#");
	                sb.append(tokens[i + 1].split("\\(")[0]);
	                return sb.toString();
	            }
	        }

//	        find JSP method
	        StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < tokens.length; i++) {
	            if (tokens[i].equals("jsp")) {

	                for (int j = i; j < tokens.length; j++) {
	                    if (tokens[j].endsWith("_jsp")) {

	                        for (int k = i + 1; k <= j; k++) {
	                            sb.append(tokens[k]);
	                            if (k != j) {
	                                sb.append(".");
	                            }
	                        }
	                        sb.append("#");
	                        sb.append(tokens[j + 1].split("\\(")[0]);
	                    }
	                }
	            }
	        }
	        // [keng]
	        return sb.toString().replace("_002d", "-");
	    }
	
	public static void main(String[] args) throws Exception {
		SqliteIO test = new SqliteIO();
//		test.process();
		test.writeDATA();
	}
}
