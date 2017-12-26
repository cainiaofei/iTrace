package cn.edu.nju.cs.itrace4.preprocess.rawdata.db;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.exp.infinispan.tool.RTMProcess;
import cn.edu.nju.cs.itrace4.preprocess.rawdata.db.SqliteOperation;

/**
 * @author zzf
 * @date 2017.11.3
 * @description build rtm through db. 
 */
public class GenerateRTM {
	private SqliteOperation sqlOperate;
	private String dbPath;
	private String dbProperty;
	private String driver;
	private String sqlFile;
	
	private Map<String,Integer> nameMapId = new HashMap<String,Integer>();
	private Map<Integer,String> idMapName = new HashMap<Integer,String>();
	private Set<String> mergeStringSet = new HashSet<String>();
	
	public GenerateRTM(String dbPath, String dbProperty,String sqlFile) {
		this.dbPath = dbPath;
		driver = "org.sqlite.JDBC";
		this.dbProperty = dbProperty;
		this.sqlFile = sqlFile;
		//sqlFile = "resource/sql/buildRTMForInfinispan.sql";
		sqlOperate = new SqliteOperation();
		sqlOperate.buildConnection(driver, dbPath);
		fillMergeSet();
	}
	
	public GenerateRTM() {}
	
	private void fillMergeSet() {
		mergeStringSet.add("Duplicate");
		mergeStringSet.add("Supercedes");
		mergeStringSet.add("Part-of");
		//新加的几个类型 2017/10/17
		mergeStringSet.add("Container");
		mergeStringSet.add("Cloners");
		mergeStringSet.add("Incorporates");
		//mergeStringSet.add("Related");
		//Infinispan中的几个新类型
		mergeStringSet.add("Superset");
		mergeStringSet.add("Cloners (old)");
		//Pig太大了增加几个新类型
		mergeStringSet.add("Required");
		mergeStringSet.add("Regression");
	}
	
	private void clean() {
		if(tableExist("rtm")) {
			removeTable("rtm");
		}
		sqlOperate.executeSql("create table rtm(request text, file_path text)");
		if(tableExist("init_rtm")) {
			removeTable("init_rtm");
		}
	}

	private void removeTable(String table) {
		sqlOperate.executeSql("drop table "+table);
	}

	/**
	 * @author zzf
	 * @date 2017.11.3
	 * SELECT name FROM sqlite_master WHERE type='table' 
	 */
	public boolean tableExist(String table) {
		String sql = "select name from sqlite_master where type='table'";
		ResultSet rs = sqlOperate.executeQuery(sql);
		try {
			while(rs.next()) {
				String tableName = rs.getString("name");
				if(table.equals(tableName)) {
					rs.close();
					return true;
				}
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void buildRTMTable() {
		clean();
		String sql;
		try {
			sql = getSqlTXT(sqlFile);
			sqlOperate.executeSql(sql);
			//merge based on issue_link
			generateFinalRTM(dbPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getSqlTXT(String sqlFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(sqlFile));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while((line=br.readLine())!=null) {
			sb.append(line+" ");
		}
		br.close();
		return sb.toString();
	}
	
	private void generateFinalRTM(String dbPath) throws SQLException {
		initMap(dbPath);
		int[][] graphs = buildGraphs(dbPath);
		List<List<Integer>> subGraphList = getSubGraphList(graphs);
		buildFinalRtm(dbPath,subGraphList);
	}
	
	protected void buildFinalRtm(String dbPath,List<List<Integer>> subGraphList) throws SQLException {
		int count_log = 0;
		String sql = "select * from init_rtm where issue_id=";
		Set<String> visited = new HashSet<String>();
		
		for(List<Integer> subGraph:subGraphList) {
			StringBuilder request = new StringBuilder();
			StringBuilder summary = new StringBuilder();
			StringBuilder description = new StringBuilder();
			StringBuilder code = new StringBuilder();
			int actualCount = 0;
			
			for(int id:subGraph) {
				String issueId = idMapName.get(id);
				ResultSet rs = sqlOperate.executeQuery(sql+"'"+issueId+"'");
				if(!rs.next()) {//there no exist this issue in init_rtm, may be filter by some case contraint.
					continue;
				}
				actualCount++;
				visited.add(rs.getString("issue_id").trim());
				summary.append(rs.getString("summary")+" ");
				description.append(rs.getString("description"));
				code.append(rs.getString("file_path")+"和");
			}//inner for loop
			
			if(actualCount<2 && description.length()<8) {
				continue;
			}
			if(code.length()>0) {
				request.append(summary+" "+description);
				request = filter(request.toString().toCharArray());
				String insertSql = "insert into rtm (request,file_path) values (" + "'" + 
	            		request.toString() + "',"+"'"+code.toString() + "')";
				//System.out.println("insert:"+insertSql);
				sqlOperate.executeSql(insertSql);
		        count_log++;
			}
		}
		
		// remain isolated record.
		sql = "select * from init_rtm";
		ResultSet rs = sqlOperate.executeQuery(sql);
		while(rs.next()) {
			String issueId = rs.getString("issue_id").trim();
			if(visited.contains(issueId)) {
				continue;
			}
			else {
				visited.add(issueId);
				String summary = rs.getString("summary");
				String description = rs.getString("description");
				if(description==null||description.length()<3) {
					continue;
				}
				String request = summary + " " + description;
				request = filter(request.toCharArray()).toString();
				String code = rs.getString("file_path");
				String insertSql = "insert into rtm (request,file_path) values (" + "'" + 
	            		request + "',"+"'"+code + "')";
				//Statement stmt = con.createStatement();
				sqlOperate.executeSql(insertSql);
		        count_log++;
			}
		}
		System.out.println("the insert count is:" + count_log);
	}
	
	
	private void initMap(String dbPath) throws SQLException {
		String sql = "select * from issue_link";
		ResultSet rs = sqlOperate.executeQuery(sql);
		while(rs.next()) {
			String source = rs.getString("source_issue_id").trim();
			String target = rs.getString("target_issue_id").trim();
			String name = rs.getString("name").trim();
			if(!mergeStringSet.contains(name)) {
				continue;
			}
			else {
				if(!nameMapId.containsKey(source)) {
					nameMapId.put(source, nameMapId.size());
					idMapName.put(idMapName.size(), source);
				}
				if(!nameMapId.containsKey(target)) {
					nameMapId.put(target, nameMapId.size());
					idMapName.put(nameMapId.size(), target);
				}
			}
		}
	}
	
	public List<List<Integer>> getSubGraphList(int[][] graphs){
		List<List<Integer>> res = new LinkedList<List<Integer>>();
		char[] visited = new char[graphs.length];
		for(int i = 0; i < graphs.length;i++) {// as for every node.
			if(visited[i]=='X') {
				continue;
			}
			else {
				List<Integer> cur = dfs(graphs,i,visited);
				res.add(cur);
			}
		}
		return res;
	}
	
	private List<Integer> dfs(int[][] graphs, int pos, char[] visited) {
		List<Integer> list = new LinkedList<Integer>();
		list.add(pos);
		visited[pos] = 'X';
		for(int i = 0; i < graphs.length;i++) {
			if(graphs[i][pos]==1 && visited[i]!='X') {
				List<Integer> cur = dfs(graphs,i,visited);
				list.addAll(cur);
			}
		}
		return list;
	}
	
	private int[][] buildGraphs(String dbPath) throws SQLException{
		int[][] graphs = new int[nameMapId.size()][nameMapId.size()];
		String sql = "select * from issue_link";
		ResultSet rs = sqlOperate.executeQuery(sql);
		while(rs.next()) {
			String source = rs.getString("source_issue_id").trim();
			String target = rs.getString("target_issue_id").trim();
			String name = rs.getString("name").trim();
			if(!mergeStringSet.contains(name)) {
				continue;
			}
			else {
				int sourceId = nameMapId.get(source);
				int targetId = nameMapId.get(target);
				graphs[sourceId][targetId] = 1;
				graphs[targetId][sourceId] = 1;
			}
		}
		return graphs;
	}
	
	private StringBuilder filter(char[] chs) {
		StringBuilder sb = new StringBuilder();
		for(char ch:chs) {
			if(ch=='\'' || ch=='\"' ){
				sb.append(" ");
			}
			else {
				sb.append(ch);
			}
		}
		return sb;
	}
	
//	private boolean tableExist(String table) throws SQLException {
//		String sql = "pragma table_info(" + table + ")";
//		ResultSet rs = sqlOperate.executeQuery(sql);
//		boolean res = rs != null && rs.next();
//
//		if (rs != null) {
//			rs.close();
//		}
//
//		return res;
//	}

}
