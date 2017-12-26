package cn.edu.nju.cs.itrace4.preprocess.rawdata.db;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GenerateRTMThroughCluster extends GenerateRTM{
	private SqliteOperation sqlOperate;
	private String clusterFilePath;
	private Map<Integer,List<Integer>> classMapEleSet = new HashMap<Integer,List<Integer>>();
	private Map<Integer,String> idMapName = new HashMap<Integer,String>();
	private String dbPath;
	private String driver;
	
	public GenerateRTMThroughCluster(String dbPath, String dbProperty, String sqlFile) {
		super();
		this.dbPath = dbPath;
		driver = "org.sqlite.JDBC";
		sqlOperate = new SqliteOperation();
		sqlOperate.buildConnection(driver, dbPath);
	}
	
	private void generateFinalRTM(String dbPath) throws SQLException {
		initMap();
		List<List<Integer>> subGraphList = null;
		try {
			subGraphList = getSubGraphList(clusterFilePath);//filePath
		} catch (IOException e) {
			e.printStackTrace();
		}
		buildFinalRtm(dbPath,subGraphList);
	}
	
	private List<List<Integer>> getSubGraphList(String clusterFilePath) throws IOException{
		List<List<Integer>> res = new ArrayList<List<Integer>>();
		BufferedReader br = new BufferedReader(new FileReader(clusterFilePath));
		String line = null;
		int number = 1;
		try {
			while((line=br.readLine())!=null) {
				int classId = Integer.valueOf(line);
				if(!classMapEleSet.containsKey(classId)) {
					classMapEleSet.put(classId, new ArrayList<Integer>());
				}
				classMapEleSet.get(classId).add(number);
				number++;
			}
			br.close();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Iterator<Integer> ite = classMapEleSet.keySet().iterator();
		while(ite.hasNext()) {
			res.add(classMapEleSet.get(ite.next()));
		}
		return res;
	}
	
	/**
	 * @author zzf
	 * @date 2017.12.27
	 * @description  build link from issue_id to number 
	 */
	private void initMap() {
		String sql = "select * from issue";
		ResultSet rs = sqlOperate.executeQuery(sql);
		int number = 1;
		try {
			while(rs.next()) {
				String issueId = rs.getString("issue_id");
				idMapName.put(number, issueId);
				number++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
