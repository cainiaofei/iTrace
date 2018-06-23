package cn.edu.nju.cs.itrace4.demo.algo.outerVertex;

import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.core.algo.region.relation.SubGraph;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;

public class OuterVertexProcessWithDataSubGraph extends OuterVertexProcess{
	private Map<Integer,String> vertexIdNameMap; 
	private double[][] graphs;
	public OuterVertexProcessWithDataSubGraph(Map<Integer, String> vertexIdNameMap,double[][] graphs) {
		super(vertexIdNameMap);
		this.vertexIdNameMap = vertexIdNameMap;
		this.graphs = graphs;
	}
	
	public double getBonusForLonePoint(double[][] graphs, SubGraph subGraph, 
			int loneVertex, double diffBetweenTopAndCur) {
		double max = 0;
		for(int innerPoint:subGraph.getVertexList()){
			if(graphs[innerPoint][loneVertex]!=0){
				max = Math.max(max, graphs[loneVertex][innerPoint]);
			}
		}
		return max*diffBetweenTopAndCur;
	}
	
	public int getOuterSizeConnectWithInner(Set<Integer> loneVertexSet, SubGraph subGraph) {
		int outerSize = 0;
		for(int loneVertex:loneVertexSet){
			double bonus = giveBonusForLonePoint(graphs,subGraph,loneVertex,1);
			if(bonus!=0){
				outerSize++;
			}
		}
		return outerSize;
	}
	
	public double giveBonusForLonePoint(double[][] graphs, SubGraph subGraph, 
			int loneVertex, double diffBetweenTopAndCur) {
		double max = 0;
		for(int innerPoint:subGraph.getVertexList()){
			if(graphs[innerPoint][loneVertex]!=0){
				max = Math.max(max, graphs[loneVertex][innerPoint]);
			}
		}
		return max*diffBetweenTopAndCur;
	}
	
	
	public void processOuterVertexWithDataSubGraph(SimilarityMatrix matrix,SimilarityMatrix matrix_ud){
	}
}
