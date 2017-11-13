package cn.edu.nju.cs.itrace4.exp.tool;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.io._;

/**
 * @author zzf
 * @date 2017/10/16
 * @description do some merge operations based on <code>issue_link</code>. 
 */

public class RTMProcess {
	
	private Map<String,Integer> nameMapId = new HashMap<String,Integer>();
	private Map<Integer,String> idMapName = new HashMap<Integer,String>();
	private Set<String> mergeStringSet = new HashSet<String>();
	
	public RTMProcess() {
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
	}
	
	public void generateFinalRTM(String dbPath) throws SQLException {
		initMap(dbPath);
		int[][] graphs = buildGraphs(dbPath);
		List<List<Integer>> subGraphList = getSubGraphList(graphs);
		buildFinalRtm(dbPath,subGraphList);
	}
	
	/**
	 * @author zzf
	 * @date 2017/10/16
	 * @description first write merged data in table, then other data. 
	 */
	public void buildFinalRtm(String dbPath,List<List<Integer>> subGraphList) throws SQLException {
		int count_log = 0;
		Connection con = getDBConn(dbPath);
		String sql = "select * from init_rtm where issue_id=";
		Set<String> visited = new HashSet<String>();
		Statement stmt = con.createStatement();
		con.setAutoCommit(false);
		for(List<Integer> subGraph:subGraphList) {
			StringBuilder request = new StringBuilder();
			StringBuilder code = new StringBuilder();
			for(int id:subGraph) {
				String issueId = idMapName.get(id);
				ResultSet rs = stmt.executeQuery(sql+"'"+issueId+"'");
				/**
				 * issueId may not in init_rtm 
				 */
				if(!rs.next()) {
					continue;
				}
				visited.add(rs.getString("issue_id").trim());
				request.append(rs.getString("request")+" ");
				code.append(rs.getString("file_path")+"和");
			}//inner for loop
			if(request.length()>0) {
				request = filter(request.toString().toCharArray());
				String insertSql = "insert into rtm (request,file_path) values (" + "'" + 
	            		request.toString() + "',"+"'"+code.toString() + "')";
				//System.out.println("insert:"+insertSql);
		        stmt.executeUpdate(insertSql);
		        count_log++;
			}
		}
		
		// remain isolated record.
		sql = "select * from init_rtm";
		ResultSet rs = stmt.executeQuery(sql);
		while(rs.next()) {
			String issueId = rs.getString("issue_id").trim();
			if(visited.contains(issueId)) {
				continue;
			}
			else {
				visited.add(issueId);
				String request = rs.getString("request");
				request = filter(request.toCharArray()).toString();
				String code = rs.getString("file_path");
				String insertSql = "insert into rtm (request,file_path) values (" + "'" + 
	            		request + "',"+"'"+code + "')";
				//Statement stmt = con.createStatement();
		        stmt.executeUpdate(insertSql);
		        count_log++;
			}
		}
		con.commit();
		con.close();
		System.out.println("the insert count is:" + count_log);
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

	public int[][] buildGraphs(String dbPath) throws SQLException{
		int[][] graphs = new int[nameMapId.size()][nameMapId.size()];
		Connection con = getDBConn(dbPath);
		Statement stat = con.createStatement();
		String sql = "select * from issue_link";
		ResultSet rs = stat.executeQuery(sql);
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
		con.close();
		return graphs;
	}
	
	
	public void initMap(String dbPath) throws SQLException {
		Connection con = getDBConn(dbPath);
		Statement stat = con.createStatement();
		String sql = "select * from issue_link";
		ResultSet rs = stat.executeQuery(sql);
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
		con.close();
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
	
	private Connection getDBConn(String dbPath)  {
		File dbFile = new File(dbPath);
        if (!dbFile.exists()) {
            _.abort("DB file doesn't exist");
        }

        Connection con = null;
        try {
			Class.forName("org.sqlite.JDBC");
			con = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
        return con;  
	}
	

	public static void main(String[] args) throws SQLException {
		RTMProcess rtmProcess = new RTMProcess();
		String dbPath = "data\\exp\\Infinispan\\test\\Infinispan-req.db";
		rtmProcess.generateFinalRTM(dbPath);
	}
}
