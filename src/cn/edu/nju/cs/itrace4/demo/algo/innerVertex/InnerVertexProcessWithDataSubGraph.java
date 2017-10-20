package cn.edu.nju.cs.itrace4.demo.algo.innerVertex;

import java.util.Map;

import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.demo.relation.SubGraph;

public class InnerVertexProcessWithDataSubGraph extends InnerVertexProcess{

	public InnerVertexProcessWithDataSubGraph(Map<Integer, String> vertexIdNameMap) {
		super(vertexIdNameMap);
	}
	
	public void processInnerVertex(SimilarityMatrix matrix,SimilarityMatrix matrix_ud,
			TextDataset textDataset){}
	
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
}
