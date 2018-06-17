package cn.edu.nju.cs.itrace4.tool.SlimDB;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import cn.edu.nju.cs.itrace4.preprocess.rawdata.db.SqliteOperation;

/**
 * @author zzf 
 * @date 2018.1.15
 * @description remove some record which fHashcode is 'null'、'static' or 'primitive' through sql. 
 */

public class FilterDB {
	private String dbPath = "/home/zzf/sqliteOutput/test3.db";
	private SqliteOperation sqlOperate;
	private String driver = "org.sqlite.JDBC";
	
	public FilterDB() {
		sqlOperate = new SqliteOperation();
		sqlOperate.buildConnection(driver, dbPath);
	}
	
	public void filter(String filterSqlPath) {
		String sql = null;
		try {
			sql = read(filterSqlPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		sqlOperate.executeSql(sql);
	}

	private String read(String filterSqlPath) throws IOException {
		StringBuilder sql = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(filterSqlPath)));
			String line = null;
			while((line=br.readLine())!=null) {
				sql.append(line);
				sql.append("\n");
			}
			br.close();
			return sql.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return sql.toString();
	}
	
	public static void main(String[] args) {
		FilterDB tool = new FilterDB();
		String filterSqlPath = "resource/sql/buildPP.sql";
		tool.filter(filterSqlPath);
	}
	
}
