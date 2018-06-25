package cn.edu.nju.cs.tool.cpyDB;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.edu.nju.cs.itrace4.preprocess.rawdata.db.SqliteOperation;

public class CopyDBThroughSQL {
	private String originDBPath ;
	private String originTable ;
	
	private String targetDBPath;
	private String targetTable ;
	
	private String propertyPath;
	
	private String driver = "org.sqlite.JDBC";
	private SqliteOperation originDBOperate;
	private SqliteOperation targetDBOperate;
	
	public CopyDBThroughSQL(String originDBPath,String originTable,String targetDBPath,
			String targetTable,String propertyPath) {
		this.originDBPath = originDBPath;
		this.originTable = originTable;
		this.targetDBPath = targetDBPath;
		this.targetTable = targetTable;
		this.originDBOperate = new SqliteOperation();
		this.originDBOperate.buildConnection(driver, this.originDBPath);
		
		this.propertyPath = propertyPath;
		
		this.targetDBOperate = new SqliteOperation();
		this.targetDBOperate.buildConnection(driver, this.targetDBPath);
		this.targetDBOperate.setCommit(false);//batch insert
	}
	
	public void transfer() throws IOException, SQLException {
		int count = 0;
		String[] cols = parser(propertyPath);
		String query = "select * from " + originTable;
		ResultSet rs = originDBOperate.executeQuery(query);
		String base = "insert into " + targetTable;
		
	}
	
	public String[] parser(String fieldsPath) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(fieldsPath)));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while((line=br.readLine())!=null) {
			sb.append(line);
		}
		br.close();
		String[] fields = sb.toString().split(",");
		for(int i = 0; i < fields.length;i++) {
			fields[i] = fields[i].trim();
		}
		return fields;
	}
}
