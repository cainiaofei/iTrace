package cn.edu.nju.cs.itrace4.demo.rtm;

import java.util.List;

public interface BuildRTM {
	public List<String> filterIssueList(String dbPath,String tableName,String sql);
	public void writeRTMDB(String dbPath,String tableName);
}