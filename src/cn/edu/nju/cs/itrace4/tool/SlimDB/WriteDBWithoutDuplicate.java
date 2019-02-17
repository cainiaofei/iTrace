package cn.edu.nju.cs.itrace4.tool.SlimDB;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.preprocess.rawdata.db.SqliteOperation;

/**
 * @date 2018.1.20
 * @author zzf
 * @description copy database to new database table which don't exist duplicate. 
 */
public class WriteDBWithoutDuplicate {
	private SqliteOperation readOperate;
	private SqliteOperation writeOperate;
	private String driver = "org.sqlite.JDBC";
	
	public WriteDBWithoutDuplicate() {
		this.readOperate = new SqliteOperation();
		this.writeOperate = new SqliteOperation();
	}
	
	/**
	 * the two table may be at a same database. 
	 * @throws SQLException 
	 */
	public void copyWithoutDuplicate(String dbPath,String originTable,
			String newTable,String[] cols) throws SQLException {
		readOperate.buildConnection(driver, dbPath);
		writeOperate.buildConnection(driver, dbPath);
		writeOperate.setCommit(false);
		String query = "select *  from " + originTable;
		String base = "insert into " + newTable;
		ResultSet rs = readOperate.executeQuery(query);
		while(rs.next()) {
			String insertSql = buildInsertSql(base,rs,cols);
			writeOperate.executeSql(insertSql);
		}
		writeOperate.commit();
		
		readOperate.closeConnection();
		writeOperate.closeConnection();
	}
	
	
	private String buildInsertSql(String base, ResultSet rs, String[] fields) throws SQLException {
		StringBuilder sb = new StringBuilder(base);
		// tableName(a,b,c) 
		sb.append("(");
		for(String field:fields) {
			sb.append(field+",");
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append(")");
		sb.append(" ");
		
		//values(aa,bb,cc)
		sb.append("values");
		sb.append("(");
		for(String field:fields) {
			sb.append("'"+rs.getString(field)+"'"+",");
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append(")");
		return sb.toString();
	}
	
	
	public void copyWithMap(String dbPath,String originTable,
			String newTable,String[] cols) throws SQLException {
		Map<String,Set<String>> map = new HashMap<String,Set<String>>();
		storeInMap(dbPath,readOperate,originTable,cols,map);
		
		writeOperate.buildConnection(driver, dbPath);
		writeOperate.setCommit(false);
		
		for(String caller:map.keySet()) {
			for(String callee:map.get(caller)) {
				String insertSql = "insert into " + newTable +
						"(caller,callee) values('"+caller+"', '"+callee+"');";
				writeOperate.executeSql(insertSql);
			}
		}
		
		writeOperate.commit();
		writeOperate.closeConnection();
		
	}
	
	private void storeInMap(String dbPath,SqliteOperation readOperate, String originTable, String[] cols,
			Map<String, Set<String>> map) throws SQLException {
		readOperate.buildConnection(driver, dbPath);
		String query = "select * from " + originTable;
		ResultSet rs = readOperate.executeQuery(query);
		while(rs.next()) {
			String caller = rs.getString(cols[0]);
			String callee = rs.getString(cols[1]);
			if(!map.containsKey(caller)) {
				map.put(caller, new HashSet<String>());
			}
			map.get(caller).add(callee);
		}
		readOperate.closeConnection();
	}

	public static void main(String[] args) throws SQLException {
		String[] cols = {"caller","callee"};
		WriteDBWithoutDuplicate tool = new WriteDBWithoutDuplicate();
		String dbPath = "data/exp/Derby/relation/call.db";
		String originTable = "callGraphExistDuplicate";
		String newTable = "callGraph";
		//tool.copyWithoutDuplicate(dbPath, originTable, newTable, cols);
		tool.copyWithMap(dbPath, originTable, newTable, cols);
	}
}
