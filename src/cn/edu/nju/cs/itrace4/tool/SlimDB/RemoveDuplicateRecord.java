package cn.edu.nju.cs.itrace4.tool.SlimDB;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import cn.edu.nju.cs.itrace4.preprocess.rawdata.db.SqliteOperation;
import cn.edu.nju.cs.itrace4.util.FileProcess;
import cn.edu.nju.cs.itrace4.util.FileProcessTool;


/**
 * @author zzf
 * @date 2018.1.15
 * @description:
 * 			   1. build empty database
 *             2. insert record with no duplicate.
 */
public class RemoveDuplicateRecord {
	private FileProcess fp = new FileProcessTool();
	private String baseURL = "logs"
			+ "";
	
	private String target = "data/exp/Derby/relation/test3.db";
	private String dbPath = "/home/zzf/sqliteOutput_Derby/test3.db";
	
	private String fieldsPath = "resource/sql/pp.property";
	private String tableName = "parameterPass";
	private SqliteOperation sqlOperate;
	private SqliteOperation targetOperate;
	private String driver = "org.sqlite.JDBC";
	
	public RemoveDuplicateRecord() {
		sqlOperate = new SqliteOperation();
		sqlOperate.buildConnection(driver, dbPath);
		sqlOperate.setCommit(false);
		
		targetOperate = new SqliteOperation();
		targetOperate.buildConnection(driver, target);
		targetOperate.setCommit(false);
		
	}
	
	public void buildTableWithNoDuplicate() throws SQLException, IOException {
		int count = 0;
		String querySql = "select * from " + tableName;
		String emptyDBSql = "delete from " + tableName;
		String base = "insert into " + tableName;
		Set<String> set = new HashSet<String>();
		String[] fields = readFields(fieldsPath);
		
		targetOperate.executeSql(emptyDBSql);
		
		ResultSet rs = sqlOperate.executeQuery(querySql);
		while(rs.next()) {
			String mcSignature = rs.getString("McSignature");
			String fHashcode = rs.getString("fHashcode");
			if(!set.contains(mcSignature+fHashcode)) {
				count++;
				//System.out.println("count:"+count);
				
				set.add(mcSignature+fHashcode);
				String insertSql = buildInsertSql(base,rs,fields);
				targetOperate.executeSql(insertSql);
				if(count%200000==0) {
					targetOperate.commit();
				}
			}
			if(set.size()%10000==0) {
				System.out.println("the size of set:"+set.size());
				fp.writeFile(baseURL+File.separator+set.size()+".txt",
						set.size()+"");
			}
		}
		sqlOperate.closeConnection();
		targetOperate.closeConnection();
		//System.out.println("insert count:"+count);
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
			//System.out.println("string:"+sb.toString()+"--field:"+rs.getString(field));
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





