package cn.edu.nju.cs.itrace4.cluster;

import java.sql.ResultSet;
import java.sql.SQLException;

import cn.edu.nju.cs.itrace4.exp.tool.GetUC;
import cn.edu.nju.cs.itrace4.preprocess.TextPreprocessor;
import cn.edu.nju.cs.itrace4.preprocess.rawdata.db.SqliteOperation;
import cn.edu.nju.cs.itrace4.util.FileWrite;
import cn.edu.nju.cs.itrace4.util.FileWriterImp;

public class GetReqInIssueDB {
	private FileWrite fileWrite;
	private String driver = "org.sqlite.JDBC";
	private SqliteOperation sqlOperate;
	private GetUC getUC ;
	
	
	public GetReqInIssueDB() {
		this.sqlOperate = new SqliteOperation();
		this.getUC = new GetUC();
		this.fileWrite = new FileWriterImp();
	}
	
	public void getUCText(String dbPath,String tableName,String[] cols,String target)
			throws SQLException {
		StringBuilder res = new StringBuilder();
		sqlOperate.buildConnection(driver,dbPath);
		String query = "select * from " + tableName;
		ResultSet rs = sqlOperate.executeQuery(query);
		while(rs.next()) {
			StringBuilder sb = new StringBuilder();
			for(int i = 1; i < cols.length;i++) {
				String col = cols[i];
				String line = rs.getString(col);
				line = line.replaceAll("\n", " ");
				sb.append(line+" ");
			}
			String content = sb.toString();
			content = getUC.filter(content);
			TextPreprocessor tp = new TextPreprocessor(content);
			content = tp.doJavaFileProcess();
			res.append(rs.getString(cols[0])+":"+content+"\n");
			//System.out.println("content:"+content);
		}
		fileWrite.createFile(target);
		fileWrite.writeContent(res.toString());
		fileWrite.close();
	}
	
	public static void main(String[] args) {
		GetReqInIssueDB tool = new GetReqInIssueDB();
		String targetName = "cluster/maven.txt";
		String dbPath = "data/exp/Maven/rtm/Maven-req.db";
		String tableName = "issue";
		String[] cols = {"issue_id","summary","description"};
		try {
			tool.getUCText(dbPath, tableName, cols, targetName);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
