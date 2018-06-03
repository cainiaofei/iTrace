package cn.edu.nju.cs.tool.cpyDB;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.edu.nju.cs.itrace4.preprocess.rawdata.db.SqliteOperation;

public class CpyTableWithBatchInsert {
	private String originDBPath ;
	private String originTable ;
	
	private String targetDBPath;
	private String targetTable ;
	
	private String propertyPath;
	
	private String driver = "org.sqlite.JDBC";
	private SqliteOperation originDBOperate;
	private SqliteOperation targetDBOperate;
	
	
	public CpyTableWithBatchInsert(String originDBPath,String originTable,String targetDBPath,
			String targetTable,String propertyPath) {
		this.originDBPath = originDBPath;
		this.originTable = originTable;
		this.targetDBPath = targetDBPath;
		this.targetTable = targetTable;
		this.originDBOperate = new SqliteOperation();
		this.originDBOperate.buildConnection(driver, originDBPath);
		
		this.propertyPath = propertyPath;
		
		this.targetDBOperate = new SqliteOperation();
		this.targetDBOperate.buildConnection(driver, targetDBPath);
//		this.targetDBOperate.setCommit(false);//batch insert
	}
	
	public void cpy() throws SQLException, IOException {
		int count = 0;
		String[] cols = parser(propertyPath);
		String query = "select * from " + originTable;
		ResultSet rs = originDBOperate.executeQuery(query);
		PreparedStatement preparedStatement;

		String compiledQuery = generateInsertSql(targetTable,targetTable,cols);
		preparedStatement = targetDBOperate.prepareStatement(compiledQuery);

		while(rs.next()) {
			count++;
			for(int number = 0; number < cols.length;number++) {
				preparedStatement.setString(number+1, rs.getString(cols[number]));
			}
			
			preparedStatement.addBatch();

			if(count%1000000==0) {
				preparedStatement.executeBatch();
				System.out.println("insert count:"+count);
			}
		}
	}
	
	/**
	 * @author zzf
	 * @date 2018.6.1
	 * @description the order of the fields in the data table should be consistent with
	 * 			 the order in the file 
	 */ 
	private String generateInsertSql(String targetTable,String tableName,String[] cols) {
		//NSERT INTO TESTDB.EMPLOYEE(EMPNO, EMPNM, DEPT, RANK, USERNAME)" +
        //" VALUES" + "(?, ?, ?, ?, ?)
		StringBuilder insertSql = new StringBuilder();
		String baseSql = "insert into " + tableName+"("; 
		insertSql.append(baseSql);
		
		for(int i = 0; i < cols.length;i++) {
			insertSql.append(cols[i]);
			if(i!=cols.length-1) {
				insertSql.append(",");
			}
		}
		insertSql.append(") values(");
		
		for(int i = 0;i<cols.length;i++) {
			insertSql.append("?");
			if(i!=cols.length-1) {
				insertSql.append(",");
			}
		}
		insertSql.append(")");
		return insertSql.toString();
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
	
	public static void main(String[] args) {
		String originDBPath = "/home/zzf/sqliteOutput/test3.db";
		String originTable = "pp";
		String targetDBPath = "/home/zzf/newDB/test3.db";
		String targetTable = "pp";
		String propertyPath = "resource/sql/pp.property";
		CpyTableWithBatchInsert tool = new CpyTableWithBatchInsert(originDBPath,originTable,targetDBPath,
				targetTable,propertyPath);
		try {
			tool.cpy();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	   
	
}
