package cn.edu.nju.cs.itrace4.core.algo.region.outerVertex;

import java.util.Map;

import cn.edu.nju.cs.itrace4.relation.graph.CodeEdge;

public class OuterVertexProcess {
	Map<Integer,String> vertexIdNameMap;
	public OuterVertexProcess(Map<Integer, String> vertexIdNameMap) {
		this.vertexIdNameMap = vertexIdNameMap;
	}
	
	public double[][] describeGraphWithMatrix(Map<CodeEdge, Double> edgeScoreMap, int size) {
		double[][] matrix = new double[size+1][size+1];
		for(CodeEdge edge:edgeScoreMap.keySet()){
			int callerId = edge.getSource().getId();
			int calleeId = edge.getTarget().getId();
			double score = edgeScoreMap.get(edge);
			matrix[callerId][calleeId] = score;
			matrix[calleeId][callerId] = score;
		}
		return matrix;
	}

}
