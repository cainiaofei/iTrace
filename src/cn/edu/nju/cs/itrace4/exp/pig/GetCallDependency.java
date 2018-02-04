package cn.edu.nju.cs.itrace4.exp.pig;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.edu.nju.cs.itrace4.exp.tool.getCallerDB.GetCallDependencyThisThread;

public class GetCallDependency {
	private String sqlDriver = "org.sqlite.JDBC";
	private String dbPath = "data/exp/Pig_Run/relation/CallGraph.db";
	private String tableName = "callGraph";

	public void getCallDependency() {
		Set<String> callRelationSet = Collections.synchronizedSet(new HashSet<String>());
		List<Thread> threads = new ArrayList<Thread>();
		for(String threadID:getThreadIDSet()) {//for
			System.out.println(threadID);
			if(threads.size()==8) {//8
				waitThread(threads);
				threads.clear();
			}
			else {
				Thread thread = new Thread(new GetCallDependencyThisThread(sqlDriver,dbPath,
						tableName,threadID,callRelationSet));
				thread.start();
				threads.add(thread);
			}
		}//for
		/*
		 *@date: 2017.12.25
		 *@description: it must wait all threads stop.
		 */ 
		for(Thread thread:threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			serialize(callRelationSet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		writeDB(callRelationSet);
	}
	
	
	private void writeDB(Set<String> callRelationSet) {
		try {
			Class.forName(sqlDriver);
			Connection con = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
			Statement stmt = con.createStatement();
			String clearSql = "delete from graph";
			stmt.execute(clearSql);
			for(String callRelation:callRelationSet) {
				String[] strs = callRelation.split("#");
				String caller = strs[0].replace("/", ".");
				String callee = strs[1].replace("/", ".");
				String insertSql = "insert into graph(caller,callee) values('"+caller+"' , '"+callee+"')";
//				System.out.println(insertSql);
				stmt.execute(insertSql);
			}
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void serialize(Set<String> callRelationSet) throws FileNotFoundException, IOException {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("."
				+ "/callRelationSet.ser"));  
        out.writeObject(callRelationSet);  
        out.close();  
	}

	private void waitThread(List<Thread> threads) {
		for(Thread thread:threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private Set<String> getThreadIDSet(){
		Set<String> threadSet = new HashSet<String>();
		try {
			Class.forName(sqlDriver);
			Connection con = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
			Statement stmt = con.createStatement();
			String sql = "select distinct threadID from " + tableName;
			ResultSet rs = stmt.executeQuery(sql);
			while(rs.next()) {
				threadSet.add(rs.getString("threadID"));
			}
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return threadSet;
	}
	
	public static void main(String[] args) {
		GetCallDependency tool = new GetCallDependency();
		tool.getCallDependency();
	}
}
