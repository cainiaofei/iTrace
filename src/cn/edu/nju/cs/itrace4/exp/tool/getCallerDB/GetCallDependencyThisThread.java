package cn.edu.nju.cs.itrace4.exp.tool.getCallerDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.Stack;

/**
 * @author zzf
 * @date 2017.11.18
 * @description  use multi-thread to get caller table, then get code dependency.
 */
public class GetCallDependencyThisThread implements Runnable{
	private String tableName;
	private String threadID;
	private Set<String> callRelationSet;
	private Connection con;
	
	public GetCallDependencyThisThread(String sqlDriver,String dbPath,String tableName,
			String threadID,Set<String> callRelationSet) {
		this.tableName = tableName;
		this.threadID = threadID;
		this.callRelationSet = callRelationSet;
		dbConnect(sqlDriver,dbPath);
	}
	
	@Override
	public void run() {
		getCallRelation();
	}
	
	public void getCallRelation() {
		String sql = "select * from " + tableName + " where threadID = '" + threadID + "'";
		Statement stmt;
		ResultSet rs = null;
		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(sql);
			getCallRelationByStack(rs);
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void getCallRelationByStack(ResultSet rs) throws SQLException {
		Stack<String> stack = new Stack<String>();
		while(rs.next()) {//while
			String callFlag = rs.getString("callFlag");
			String classSignature = rs.getString("classSignature");
			String methodName = rs.getString("methodName");
			String methodSignature = rs.getString("methodSignature");
			//remove ';'  example: xxx; ===> xxx
			classSignature = classSignature.substring(0,classSignature.length()-1);
			methodName = replaceAngleBracketWithBar(methodName);
			String identify = classSignature + "." + methodName + methodSignature;
			if(callFlag.equals("E")) {//entrance
				stack.push(identify);
			}
			else {//exit
				stack.pop();
				if(!stack.isEmpty()) {
					callRelationSet.add(stack.peek()+"#"+identify);
				}
			}
		}//while
		rs.close();
	}

	private String replaceAngleBracketWithBar(String methodName) {
		if(methodName.equals("<init>")) {
			return "-init-";
		}
		else if(methodName.equals("<clinit>")) {
			return "-clinit-";
		}
		else {
			return methodName;
		}
	}
	
	private void dbConnect(String driver, String dbPath) {
		try {
			Class.forName(driver);
			con = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
