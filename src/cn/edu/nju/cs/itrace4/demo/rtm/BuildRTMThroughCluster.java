package cn.edu.nju.cs.itrace4.demo.rtm;

import java.util.List;

public class BuildRTMThroughCluster implements BuildRTM{

	private String dbPath;
	
	//public List<String> 
	
	@Override
	public List<String> filterIssueList(String dbPath, String tableName, String sql) {
		return null;
	}

	@Override
	public void writeRTMDB(String dbPath, String tableName) {
	}
	
}
