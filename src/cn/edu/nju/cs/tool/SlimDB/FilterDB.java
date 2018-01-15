package cn.edu.nju.cs.tool.SlimDB;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import cn.edu.nju.cs.itrace4.preprocess.rawdata.db.SqliteOperation;

public class FilterDB {
	private String dbPath = "/home/zzf/geek/Pig_cluster/relation/test2.db";
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
		String filterSqlPath = "resource/sql/buildFM.sql";
		tool.filter(filterSqlPath);
	}
	
}
