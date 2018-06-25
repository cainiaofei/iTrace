package cn.edu.nju.cs.itrace4.core.algo.region.closeness;

public class Edge {
	private int vertexA;
	private int vertexB;
	private double closeness;
	
	public Edge(int vertexA,int vertexB,double closeness) {
		this.vertexA = vertexA;
		this.vertexB = vertexB;
		this.closeness = closeness;
	}
	
	public int getVertexA() {
		return vertexA;
	}
	
	public void setVertexA(int vertexA) {
		this.vertexA = vertexA;
	}
	
	public int getVertexB() {
		return vertexB;
	}
	
	public void setVertexB(int vertexB) {
		this.vertexB = vertexB;
	}
	
	public double getCloseness() {
		return closeness;
	}
	
	public void setCloseness(double closeness) {
		this.closeness = closeness;
	}
}
