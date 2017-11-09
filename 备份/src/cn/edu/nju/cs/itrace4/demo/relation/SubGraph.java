package cn.edu.nju.cs.itrace4.demo.relation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SubGraph {
	//顶点集合
	private List<Integer> vertexList;
	private int maxId;
	private Set<String> set;
	boolean isVisited = false;
	private Map<String,Double> map;
	private Set<String> visited;
	//2017/8/12 增加一个属性  为了使用新方法 这里的距离和传统意义的距离还不一样 越大越紧密
	private double closenessDistanceFromMaxSubGraph;
	
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
}
