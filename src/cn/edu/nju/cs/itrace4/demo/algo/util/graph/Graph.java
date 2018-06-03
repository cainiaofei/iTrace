package cn.edu.nju.cs.itrace4.demo.algo.util.graph;

import java.util.List;

public interface Graph {
	//routers between vertexA and vertexB
	public List<List<Integer>> getAllRoutersBetweenTwoVertex(int vertexA,int vertexB);
}
