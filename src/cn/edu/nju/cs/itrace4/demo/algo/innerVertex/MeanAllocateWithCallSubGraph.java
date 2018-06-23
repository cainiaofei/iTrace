package cn.edu.nju.cs.itrace4.demo.algo.innerVertex;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.algo.region.relation.SubGraph;
import cn.edu.nju.cs.itrace4.core.algo.region.util.sort.SortBySubGraph;

public class MeanAllocateWithCallSubGraph extends InnerVertexProcessWithCallSubGraph{
	private Map<Integer,String> vertexIdNameMap;
	private List<SubGraph> callSubGraphList;
	private int amountNeedToJudge;
	//store have judge in order to calculate rate which need to judge by user
	private Map<String,Set<String>> judged;
	
	public MeanAllocateWithCallSubGraph(Map<Integer,String> vertexIdNameMap,List<SubGraph> callSubGraphList,
			int amountNeedToJudge,Map<String,Set<String>> judged){
		super(vertexIdNameMap);
		this.vertexIdNameMap = vertexIdNameMap;
		this.callSubGraphList = callSubGraphList;
		this.amountNeedToJudge = amountNeedToJudge;
		this.judged = judged;
	}
	
	@Override
	public void processInnerVertexWithCallSubGraph(SimilarityMatrix matrix,SimilarityMatrix matrix_ud,
			TextDataset textDataset){
		 SimilarityMatrix oracle = textDataset.getRtm();
		 for(String req:matrix.sourceArtifactsIds()){
			Collections.sort(callSubGraphList,new SortBySubGraph(vertexIdNameMap,matrix,req));
			int maxId = callSubGraphList.get(0).getMaxId();
			double maxScore = matrix.getScoreForLink(req, vertexIdNameMap.get(maxId));
			int index = 1;
			for(SubGraph subGraph:callSubGraphList){
				List<Integer> vertexList = subGraph.getVertexList();
				
				if(vertexList.size()==1&&!hasContainedThisLink(matrix_ud, req, vertexList.get(0))){
					matrix_ud.addLink(req, vertexIdNameMap.get(vertexList.get(0)),
							matrix.getScoreForLink(req, vertexIdNameMap.get(vertexList.get(0))));
					continue;
				}
				if(vertexList.size()==1){///////only process subGraph which has only one vertex 
					continue;
				}
				//regard the max score in this subGraph as represent
				String representName = vertexIdNameMap.get(subGraph.getMaxId());
				double representValue = matrix.getScoreForLink(req, representName);
				double maxScoreInThisSubGraph = representValue;
				//anyway we will store the link that has been judged.
				if(index<amountNeedToJudge){
					if(judged.containsKey(req)){
						judged.get(req).add(representName);
					}
					else{
						judged.put(req, new HashSet<String>());
						judged.get(req).add(representName);
					}
					subGraph.setVisited(req);
				}
				if(index<amountNeedToJudge&&oracle.isLinkAboveThreshold(req,representName)){
					subGraph.addReq(req);
					for(int id:vertexList){
						String curName = vertexIdNameMap.get(id);
						double curValue = matrix.getScoreForLink(req, vertexIdNameMap.get(id));
						if(!curName.equals(representName)){
							curValue = Math.min(maxScore, curValue+maxScore/vertexList.size());
							maxScoreInThisSubGraph = Math.max(maxScoreInThisSubGraph, curValue);
						}
						//any way we will add this link into matrix_ud. the matter is curValue.
						matrix_ud.addLink(req, vertexIdNameMap.get(id),curValue);
					}
				}
				else{///this subGraph is not valid with requirement or it more than amountNeedJudged
					for(int id:vertexList){
						double curValue = matrix.getScoreForLink(req, vertexIdNameMap.get(id));
						matrix_ud.addLink(req, vertexIdNameMap.get(id),curValue);//
					}
				}
				index++;
			}///
		}//req
	}
}
