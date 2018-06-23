package cn.edu.nju.cs.itrace4.preprocess.rtm.issue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.edu.nju.cs.itrace4.exp.tool.GetUC;
import cn.edu.nju.cs.itrace4.preprocess.rawdata.db.SqliteOperation;

/**
 * @author zzf
 * @date 2018.1.6
 * @description generate issue text through issue table in database. 
 */
public class PrepareIssueTXT {
	private SqliteOperation sqlOperate;
	private String issueTablePath;
	private String tableName;
	private String issueTXTPath;
	private String[] params;
	private String driverName;
	//use it to remove some special character.
	
	public void init(String driverName,String issueTablePath,String tableName,
			String[] params,String issueTXTPath) {
		this.sqlOperate = new SqliteOperation();
		this.issueTablePath = issueTablePath;
		this.tableName = tableName;
		this.issueTXTPath = issueTXTPath;
		this.params = params;
		this.driverName = driverName;
		sqlOperate.buildConnection(driverName, issueTablePath);
	}
	
	public void generateIssueTXT(String driverName,String issueTablePath,String tableName,
			String[] params,String issueTXTPath) throws SQLException, IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(issueTXTPath+File.separator+
				"result_issue_type_infinispan.txt")));
		init(driverName,issueTablePath,tableName,params,issueTXTPath);
		String sql = buildSelectSql(params,tableName);
		ResultSet rs = sqlOperate.executeQuery(sql);
		while(rs.next()) {
			StringBuilder sb = new StringBuilder();
			for(String param:params) {
				String value = rs.getString(param);
				sb.append(value+" ");
			}
			bw.write(filter(sb.toString()));
			bw.newLine();
		}
		bw.close();
	}

	/**
	 * @date 2018.1.6
	 * @author zzf
	 * @description remove special character and '\n' 
	 */
	private String filter(String text) {
		text = text.toLowerCase();
		GetUC tool = new GetUC();
		text = tool.filter(text);
		String[] specifyCh = {"\n"}; 
		for(String str:specifyCh) {
			text = text.replaceAll(str, " ");
		}
		return text;
	}

	private String buildSelectSql(String[] params,String tableName) {
		StringBuilder sb = new StringBuilder();
		sb.append("select ");
		for(int i = 0; i < params.length;i++) {
			if(i==params.length-1) {
				sb.append(params[i]+" ");
			}
			else {
				sb.append(params[i]+",");
			}
		}
		sb.append("from "+tableName);
		return sb.toString();
	}
	
	public static void main(String[] args) throws SQLException, IOException {
		PrepareIssueTXT prepareIssueTXT = new PrepareIssueTXT();
		String driverName = "org.sqlite.JDBC";
		String issueTablePath = "data/exp/Infinispan/rtm/Infinispan-req.db";
		String tableName = "issue";
		String[] params = {"issue_type","summary","description"};
		String issueTXTPath = "../cluster_python/code/data";
		prepareIssueTXT.generateIssueTXT(driverName, issueTablePath, tableName, 
				params, issueTXTPath);
	}
}
