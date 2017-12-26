package cn.edu.nju.cs.itrace4.exp.pig;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Set;

public class WriteGraphDataFromSer {
	private String sqlDriver = "org.sqlite.JDBC";
	private String dbPath = "data/exp/Pig/relation/CallGraph.db";
	private String serPath = "callRelationSet.ser";
	
	public void writeDataToGraphDB() throws IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(serPath));
		Set<String> callRelationSet = (Set<String>)in.readObject();
		in.close();
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
	
	public static void main(String[] args) {
		WriteGraphDataFromSer tool = new WriteGraphDataFromSer();
		try {
			tool.writeDataToGraphDB();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
