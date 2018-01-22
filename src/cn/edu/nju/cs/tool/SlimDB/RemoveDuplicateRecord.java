package cn.edu.nju.cs.tool.SlimDB;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import cn.edu.nju.cs.itrace4.preprocess.rawdata.db.SqliteOperation;


/**
 * @author zzf
 * @date 2018.1.15
 * @description:
 * 			   1. build empty database
 *             2. insert record with no duplicate.
 */
public class RemoveDuplicateRecord {
	private String dbPath = "/home/zzf/geek/Pig_cluster/newDB/test3.db";
	private String fieldsPath = "resource/sql/pp.property";
	private String originTable = "pp";
	private String targetTable = "parameterPass";
	private SqliteOperation sqlOperate;
	private String driver = "org.sqlite.JDBC";
	
	public RemoveDuplicateRecord() {
		sqlOperate = new SqliteOperation();
		sqlOperate.buildConnection(driver, dbPath);
		sqlOperate.setCommit(false);
	}
	
	public void buildTableWithNoDuplicate() throws SQLException, IOException {
		int count = 0;
		String querySql = "select * from " + originTable;
		//String emptyDBSql = "delete from " + targetTable;
		String base = "insert into " + targetTable;
		Set<String> set = new HashSet<String>();
		String[] fields = readFields(fieldsPath);
		//sqlOperate.executeSql(emptyDBSql);
		ResultSet rs = sqlOperate.executeQuery(querySql);
		while(rs.next()) {
			String mcSignature = rs.getString("McSignature");
			String fHashcode = rs.getString("fHashcode");
			if(!set.contains(mcSignature+fHashcode)) {
				count++;
				System.out.println("count:"+count);
				set.add(mcSignature+fHashcode);
				String insertSql = buildInsertSql(base,rs,fields);
				sqlOperate.executeSql(insertSql);
				if(count%200000==0) {
					sqlOperate.commit();
				}
			}
		}
		sqlOperate.closeConnection();
		System.out.println("insert count:"+count);
	}
	
	public String[] readFields(String fieldsPath) throws IOException {
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
			System.out.println("string:"+sb.toString()+"--field:"+rs.getString(field));
			sb.append("'"+rs.getString(field)+"'"+",");
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append(")");
		return sb.toString();
	}

	public static void main(String[] args) throws SQLException, IOException {
		RemoveDuplicateRecord tool = new RemoveDuplicateRecord();
		tool.buildTableWithNoDuplicate();
	}
}





