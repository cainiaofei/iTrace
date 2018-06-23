package cn.edu.nju.cs.itrace4.demo.algo.outerVertex;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.algo.region.relation.SubGraph;
import cn.edu.nju.cs.itrace4.core.algo.region.util.sort.SortBySubGraph;

public class MeanAllocateWithCallSubGraph extends 
	OuterVertexProcessWithCallSubGraph{
	private Map<Integer,String> vertexIdNameMap;
	private List<SubGraph> callSubGraphList;
	//store have judge in order to calculate rate which need to judge by user
	private double[][] graphs;
	private Set<Integer> loneVertexSet;
	
	public MeanAllocateWithCallSubGraph(Map<Integer,String> vertexIdNameMap,List<SubGraph> callSubGraphList,
			Map<String,Set<String>> judged,double[][] graphs){
		super(vertexIdNameMap,graphs);
		this.vertexIdNameMap = vertexIdNameMap;
		this.callSubGraphList = callSubGraphList;
		this.graphs = graphs;
	}
	
	@Override
	public void processOuterVertexWithDataSubGraph(SimilarityMatrix matrix,SimilarityMatrix matrix_ud){
		for(String req:matrix.sourceArtifactsIds()){//outer for
			Collections.sort(callSubGraphList,new SortBySubGraph(vertexIdNameMap,matrix,req));
			int maxId = callSubGraphList.get(0).getMaxId();
			double maxScore = matrix.getScoreForLink(req, vertexIdNameMap.get(maxId));
			for(SubGraph subGraph:callSubGraphList){///subGraph
				if(subGraph.isValidWithThisReq(req)){
					int outerSize = getOuterSizeConnectWithInner(loneVertexSet,subGraph);
					int totalSize = outerSize + subGraph.getVertexList().size();
					for(int loneVertexId:loneVertexSet){
						double closeness = this.getBonusForLonePoint(graphs, subGraph, loneVertexId, 1);
						if(closeness!=0){
							double bonus = maxScore/totalSize;
							String targetName = vertexIdNameMap.get(loneVertexId);
							double newValueThisLink = Math.min(maxScore, getNewValueForThisThink(
									matrix,matrix_ud,targetName,bonus,req));
							matrix_ud.setScoreForLink(req, targetName, newValueThisLink);
						}
					}
				}
				else{
					continue;
				}
			}///subGraph
		}//outer for
	}
	
	private double getNewValueForThisThink(SimilarityMatrix matrix, SimilarityMatrix matrix_ud, String targetName,
			double bonus,String req) {
		double originValue = matrix.getScoreForLink(req, targetName);
		double curValue = matrix_ud.getScoreForLink(req, targetName);
		return Math.max(curValue, originValue+bonus);
	}
}
