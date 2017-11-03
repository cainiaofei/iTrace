package cn.edu.nju.cs.itrace4.exp.infinispan.preprocess;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.edu.nju.cs.itrace4.exp.infinispan.tool.RTMProcess;
import cn.edu.nju.cs.itrace4.preprocess.rawdata.db.SqliteOperation;

/**
 * @author zzf
 * @date 2017.11.3
 * @description build rtm through db. 
 */
public class GenerateRTM {
	private SqliteOperation sqlOperate;
	private RTMProcess generateFinalRTM;
	private String dbPath;
	private String dbProperty;
	private String driver;
	private String sqlFile;
	
	public GenerateRTM(String dbPath) {
		this.dbPath = dbPath;
		driver = "org.sqlite.JDBC";
		dbProperty = "resource/infinispanDB.property";
		sqlFile = "resource/sql/buildRTMForInfinispan.sql";
		generateFinalRTM = new RTMProcess();
		sqlOperate = new SqliteOperation();
		sqlOperate.buildConnection(driver, dbPath);
	}
	
	
	
	private void clean() {
			if(tableExist("rtm")) {
				removeTable("rtm");
			}
			else {
				sqlOperate.executeSql("create table rtm(request text, file_path text)");
			}
			if(tableExist("init_rtm")) {
				removeTable("init_rtm");
			}
	}

	private void removeTable(String table) {
		sqlOperate.executeSql("drop table "+table);
	}

	/**
	 * @author zzf
	 * @date 2017.11.3
	 * SELECT name FROM sqlite_master WHERE type='table' 
	 */
	public boolean tableExist(String table) {
		String sql = "select name from sqlite_master where type='table'";
		ResultSet rs = sqlOperate.executeQuery(sql);
		try {
			while(rs.next()) {
				String tableName = rs.getString("name");
				if(table.equals(tableName)) {
					rs.close();
					return true;
				}
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void buildRTMTable() {
		clean();
		String sql;
		try {
			sql = getSqlTXT(sqlFile);
			sqlOperate.executeSql(sql);
			//merge based on issue_link
			generateFinalRTM.generateFinalRTM(dbPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getSqlTXT(String sqlFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(sqlFile));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while((line=br.readLine())!=null) {
			sb.append(line+" ");
		}
		br.close();
		return sb.toString();
	}
	
//	private boolean tableExist(String table) throws SQLException {
//		String sql = "pragma table_info(" + table + ")";
//		ResultSet rs = sqlOperate.executeQuery(sql);
//		boolean res = rs != null && rs.next();
//
//		if (rs != null) {
//			rs.close();
//		}
//
//		return res;
//	}

}
