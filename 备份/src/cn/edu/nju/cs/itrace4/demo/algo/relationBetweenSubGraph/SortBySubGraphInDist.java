package cn.edu.nju.cs.itrace4.demo.algo.relationBetweenSubGraph;

import java.util.Comparator;
import java.util.Map;

import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.demo.relation.SubGraph;

public class SortBySubGraphInDist implements Comparator<SubGraph>{

	private Map<Integer, String> vertexIdNameMap;
	SimilarityMatrix matrix;
	String target;//requirement
	
	public SortBySubGraphInDist(Map<Integer, String> vertexIdNameMap,SimilarityMatrix matrix,
			String target){
		this.vertexIdNameMap = vertexIdNameMap;
		this.matrix = matrix;
		this.target = target;
	}
	
	@Override
	public int compare(SubGraph sub1, SubGraph sub2) {
		double maxIRValue1 = matrix.getScoreForLink(target,vertexIdNameMap.get(sub1.getMaxId()));
		double maxIRValue2 = matrix.getScoreForLink(target,vertexIdNameMap.get(sub2.getMaxId()));
		
		double subValue1 = maxIRValue1 * sub1.getClosenessDistanceFromMaxSubGraph();
		double subValue2 = maxIRValue2 * sub2.getClosenessDistanceFromMaxSubGraph();
		
		double diff = subValue2 - subValue1;
		
		if(diff>0){
			return 1;
		}
		else if(diff<0){
			return -1;
		}
		else{
			return 0;
		}
	}
}
