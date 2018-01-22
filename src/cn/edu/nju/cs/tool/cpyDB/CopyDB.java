package cn.edu.nju.cs.tool.cpyDB;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.edu.nju.cs.itrace4.preprocess.rawdata.db.SqliteOperation;

/**
 * @date 2018.1.16
 * @author zzf
 * @description copy db
 */
public class CopyDB {
	private String originDBPath ;
	private String originTable ;
	
	private String targetDBPath;
	private String targetTable ;
	
	private String propertyPath;
	
	private String driver = "org.sqlite.JDBC";
	private SqliteOperation originDBOperate;
	private SqliteOperation targetDBOperate;
	
	
	public CopyDB(String originDBPath,String originTable,String targetDBPath,
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
		while(rs.next()) {
			count++;
			if(count<=1920000) {
				System.out.println("count:"+count);
				continue;
			}
			String insertSql = buildInsertSql(base,rs,cols);
			targetDBOperate.executeSql(insertSql);
			if(count%1000000==0) {
				targetDBOperate.commit();
				System.out.println("insert count:"+count);
			}
		}
		System.out.println("the number of record is:"+count);
		targetDBOperate.closeConnection();
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

	public static void main(String[] args) {
		String originDBPath = "/home/zzf/sqliteOutput/test3.db";
		String originTable = "pp";
		String targetDBPath = "/home/zzf/newDB/test3.db";
		String targetTable = "pp";
		String propertyPath = "resource/sql/pp.property";
		CopyDB tool = new CopyDB(originDBPath,originTable,targetDBPath,targetTable,propertyPath);
		try {
			tool.transfer();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}











