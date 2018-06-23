package cn.edu.nju.cs.itrace4.demo.algo.outerVertex;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.algo.region.relation.SubGraph;
import cn.edu.nju.cs.itrace4.core.algo.region.util.sort.SortBySubGraph;

public class WeightAverageWithCallSubGraph extends OuterVertexProcessWithCallSubGraph{
	private Map<Integer,String> vertexIdNameMap;
	private List<SubGraph> callSubGraphList;
	//store have judge in order to calculate rate which need to judge by user
	private double[][] graphs;
	private Set<Integer> loneVertexSet;
	private Map<String,Set<String>> judged;
	
	public WeightAverageWithCallSubGraph(Map<Integer,String> vertexIdNameMap,List<SubGraph> callSubGraphList,
			Map<String,Set<String>> judged,double[][] graphs){
		super(vertexIdNameMap,graphs);
		this.vertexIdNameMap = vertexIdNameMap;
		this.callSubGraphList = callSubGraphList;
		this.graphs = graphs;
		this.judged = judged;
	}
	
	@Override
	public void processOuterVertexWithDataSubGraph(SimilarityMatrix matrix,SimilarityMatrix matrix_ud){
		for(String req:matrix.sourceArtifactsIds()){
			Collections.sort(callSubGraphList,new SortBySubGraph(vertexIdNameMap,matrix,req));
			int maxId = callSubGraphList.get(0).getMaxId();
			double maxScore = matrix.getScoreForLink(req, vertexIdNameMap.get(maxId));
			
			for(int loneVertex:loneVertexSet){
				double sum = 0;
				double validSum = 0;
				double validValueSum = 0;
				for(SubGraph subGraph:callSubGraphList){///subGraph
					if(subGraph.getVertexList().size()==1||!subGraph.isVisited(req)){
						continue;
					}
					double bonus = giveBonusForLonePoint(graphs,subGraph,loneVertex,1);
					sum += bonus;
					if(subGraph.isValidWithThisReq(req)){
						validSum += bonus;
						validValueSum += matrix_ud.getScoreForLink(req, vertexIdNameMap.get(subGraph.getMaxId()))
								* bonus;
					}
				}///subGraph
				String loneVertexName = vertexIdNameMap.get(loneVertex);
				double originValue = matrix.getScoreForLink(req, loneVertexName);
				if(sum==0){
					matrix_ud.setScoreForLink(req, loneVertexName, originValue);
				}
				else{
					double nowValue = originValue + validSum/sum*validValueSum;////maybe exist trouble
					nowValue = Math.min(nowValue, maxScore);
					matrix_ud.setScoreForLink(req, loneVertexName, nowValue);
				}
			} 
		}
	}
}
