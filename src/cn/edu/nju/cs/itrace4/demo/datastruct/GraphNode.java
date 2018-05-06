package cn.edu.nju.cs.itrace4.demo.datastruct;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zzf
 * @date 2018.4.22
 * @description in order to find the node which has not been called by node in region, build this new class.   
 */

public class GraphNode {
	private List<GraphNode> callerList = new ArrayList<GraphNode>();
	private List<GraphNode> calleeList = new ArrayList<GraphNode>();
	private int id;
	private String className;
	
	public int getId() {
		return id;
	}

	public void setId(int id,String className) {
		this.id = id;
		this.className = className;
	}

	public GraphNode(String className) {
		this.className = className;
	}
	
	public GraphNode(int id,String className) {
		this.id = id;
		this.className = className;
	}
	
	public void addCallerGraphNode(GraphNode callerNode) {
		callerList.add(callerNode);
	}
	
	public void addCalleeGraphNode(GraphNode calleeNode) {
		calleeList.add(calleeNode);
	}
	
	
	public List<GraphNode> getCallerList() {
		return callerList;
	}

	public void setCallerList(List<GraphNode> callerList) {
		this.callerList = callerList;
	}

	public List<GraphNode> getCalleeList() {
		return calleeList;
	}

	public void setCalleeList(List<GraphNode> calleeList) {
		this.calleeList = calleeList;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}
	
}
