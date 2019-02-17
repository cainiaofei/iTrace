package cn.edu.nju.cs.itrace4.core.algo.region.relation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SubGraph {
	//顶点集合
	private String type;
	private List<Integer> vertexList;
	private int maxId;
	private Set<String> set;
	boolean isVisited = false;
	private Map<String,Double> map;
	private Set<String> visited;
	//2017/8/12 增加一个属性  为了使用新方法 这里的距离和传统意义的距离还不一样 越大越紧密
	private double closenessDistanceFromMaxSubGraph;
	//2018.4.18 增加一个属性，记录这个域的名字
	private String regionName;
	
	/*
	 * @date 2018/1/13
	 * @description add a property used to store the max bonus.
	 */
	private double maxBonus;
	
	public void setMaxBonus(double maxBonus) {
		this.maxBonus = maxBonus;
	}
	
	public double getMaxBonus() {
		return maxBonus;
	}
	
	public void SetType(String type) {
		this.type = type;
	}
	
	public String getType() {
		return type;
	}
	
	public SubGraph(List<Integer> vertexList){
		this.vertexList = vertexList;
		set = new HashSet<String>();
		map = new HashMap<String,Double>();
		visited = new HashSet<String>();
	}
	
	public Map<String,Double> getMap(){
		return map;
	}
	
	public void setVisited(String req){
		visited.add(req);
	}
	
	public boolean isVisited(String req){
		return visited.contains(req);
	}
	
	public void addReq(String req){
		set.add(req);
	}
	
	public boolean isValidWithThisReq(String req){
		return set.contains(req);
	}
	
	public List<Integer> getVertexList(){
		return vertexList;
	}
	
	public void setVertexList(List<Integer> vertexList){
		this.vertexList = vertexList;
	}
	
	
	public void setMaxId(int id){
		maxId = id;
	}
	
	public int getMaxId(){
		return maxId;
	}
	
	public double getClosenessDistanceFromMaxSubGraph() {
		return closenessDistanceFromMaxSubGraph;
	}

	public void setClosenessDistanceFromMaxSubGraph(double closenessDistanceFromMaxSubGraph) {
		this.closenessDistanceFromMaxSubGraph = closenessDistanceFromMaxSubGraph;
	}
	
	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}
	 
	public String getRegionName() {
		return regionName;
	}
}
