package cn.edu.nju.cs.itrace4.demo.algo.outerVertex.process;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.core.algo.region.relation.StoreDataSubGraphNoTrans;
import cn.edu.nju.cs.itrace4.core.algo.region.relation.SubGraph;
import cn.edu.nju.cs.itrace4.core.algo.region.util.sort.SortBySubGraph;
import cn.edu.nju.cs.itrace4.relation.CallDataRelationGraph;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.relation.graph.CodeEdge;
import javafx.util.Pair;

public class UD_DataSubGraphWithBonusForLoneNoTrans implements CSTI{
	private Map<String,Set<String>> valid;
	private double[][] graphs;
	private List<SubGraph> dataSubGraphList;
	protected Map<Integer, String> vertexIdNameMap;
	private Set<Integer> loneVertexSet = new HashSet<Integer>();
	private SimilarityMatrix originMatrix;
	private double percent;
	private Map<Integer,Set<Integer>> vertexMapDataRelated;
	
	
	public UD_DataSubGraphWithBonusForLoneNoTrans(RelationInfo ri,Map<String,Set<String>> valid,
			SimilarityMatrix originMatrix,double percent){
		vertexMapDataRelated = new HashMap<Integer,Set<Integer>>();
		dataSubGraphList = new StoreDataSubGraphNoTrans().getSubGraphs(ri,vertexMapDataRelated);
		graphs = describeGraphWithMatrix(new CallDataRelationGraph(ri).dataEdgeScoreMap,ri.getVertexes().size());
		vertexIdNameMap = ri.getVertexIdNameMap();
		this.valid = valid;
		this.originMatrix = originMatrix;
		this.percent = percent;
		this.percent = percent;
	}
	
	private double[][] describeGraphWithMatrix(Map<CodeEdge, Double> dataEdgeScoreMap, int size) {
		double[][] matrix = new double[size+1][size+1];
		for(CodeEdge edge:dataEdgeScoreMap.keySet()){
			int callerId = edge.getSource().getId();
			int calleeId = edge.getTarget().getId();
			double score = dataEdgeScoreMap.get(edge);
			matrix[callerId][calleeId] = score;
			matrix[calleeId][callerId] = score;
		}
		return matrix;
	}

	private void fillLoneVertex(Set<Integer> loneVertexSet, List<SubGraph> callSubGraphList) {
		for(SubGraph subGraph:dataSubGraphList){
			if(subGraph.getVertexList().size()==1){
				loneVertexSet.add(subGraph.getVertexList().get(0));
			}
		}
	}
	
	public SimilarityMatrix processLoneVertexInnerMean (SimilarityMatrix matrix, TextDataset textDataset){
		SimilarityMatrix oracle = textDataset.getRtm();
		 SimilarityMatrix matrix_ud = new SimilarityMatrix();
		 //get all target artifacts 
		 Set<String> targetArtifacts = matrix.targetArtifactsIds();
		 //remove target artifacts which not corresponding with any source artifacts.
		 filterSubGraphsList(targetArtifacts);
		 filterVertexMapDataRelated(targetArtifacts);
		 fillLoneVertex(loneVertexSet,dataSubGraphList);
		 int loneVertexSize = loneVertexSet.size();
		 
		 for(String req:matrix.sourceArtifactsIds()){
			//it will get maxId for every subGraph after sort.
			Collections.sort(dataSubGraphList,new SortBySubGraph(vertexIdNameMap,matrix,req));
			
			int maxId = dataSubGraphList.get(0).getMaxId();
			double maxScore = matrix.getScoreForLink(req, vertexIdNameMap.get(maxId));
			int index = 1;
			int subGraphAmount = dataSubGraphList.size()-loneVertexSize;
			for(SubGraph subGraph:dataSubGraphList){
				List<Integer> vertexList = subGraph.getVertexList();
				if(vertexList.size()==1){///////only process subGraph which has only one vertex 
					continue;
				}
				//regard the max score in this subGraph as represent
				int curMaxId = subGraph.getMaxId();
				String represent = vertexIdNameMap.get(curMaxId);
				double representValue = matrix.getScoreForLink(req, represent);
				double maxScoreInThisSubGraph = representValue;
				if(index<subGraphAmount*percent){
					if(valid.containsKey(req)){
						valid.get(req).add(represent);
					}
					else{
						valid.put(req, new HashSet<String>());
						valid.get(req).add(represent);
					}
					subGraph.setVisited(req);
				}
				if(oracle.isLinkAboveThreshold(req,represent)&&index<subGraphAmount*percent){
					subGraph.addReq(req);
					Set<Integer> relatedDataVertex = vertexMapDataRelated.get(curMaxId); 
					for(int vertexId:relatedDataVertex){
						String vertexName = vertexIdNameMap.get(vertexId);
						double curValue = matrix.getScoreForLink(req, vertexName);
						if(hasContainedThisLink(matrix_ud, req, vertexId)){
							curValue = matrix_ud.getScoreForLink(req, vertexName);
						}
						if(!vertexName.equals(represent)){
							curValue = Math.min(maxScore, curValue+maxScore/subGraph.getVertexList().size());
							maxScoreInThisSubGraph = Math.max(maxScoreInThisSubGraph, curValue);
						}
						
						if(hasContainedThisLink(matrix_ud, req, vertexId)){
							matrix_ud.setScoreForLink(req, vertexName, representValue);
						}
						else{
							matrix_ud.addLink(req, vertexName,curValue);
						}
						
					}
					
					if(hasContainedThisLink(matrix_ud, req, curMaxId)){
						;
					}
					else{
						matrix_ud.addLink(req, represent, representValue);
					}
				}
				else{
					for(int id:vertexList){
						double curValue = matrix.getScoreForLink(req, vertexIdNameMap.get(id));
						matrix_ud.addLink(req, vertexIdNameMap.get(id),curValue);//
					}
				}
				index++;
			}///
		}//req
		 System.out.println("no trans 145");
		// giveBonusForLoneVertexListUsingNewEquation(matrix, matrix_ud, dataSubGraphList);
		giveBonusForLoneRelativeVertexList(matrix, matrix_ud, dataSubGraphList); 
		LinksList allLinks = matrix_ud.allLinks();
		Collections.sort(allLinks, Collections.reverseOrder());
		SimilarityMatrix res = new SimilarityMatrix();
		for(SingleLink link:allLinks){
			res.addLink(link.getSourceArtifactId(), link.getTargetArtifactId(),link.getScore());
		}
		
		double rate = allSize(valid)*1.0/res.allLinks().size(); 
		return res;
	}
	
	private void giveBonusForLoneRelativeVertexList(SimilarityMatrix matrix, SimilarityMatrix matrix_ud,
			List<SubGraph> subGraphList) {
		for(String req:matrix.sourceArtifactsIds()){
			
			Collections.sort(dataSubGraphList,new SortBySubGraph(vertexIdNameMap,matrix,req));
			int maxId = dataSubGraphList.get(0).getMaxId();
			double maxScore = matrix.getScoreForLink(req, vertexIdNameMap.get(maxId));
			
			for(int loneVertex:loneVertexSet){
				String loneVertexName = vertexIdNameMap.get(loneVertex);
				if(hasContainedThisLink(matrix_ud, req, loneVertex)){///////the relative lone vertex is change.
					continue;
				}
				double sum = 0;
				double validSum = 0;
				double validValueSum = 0;
				for(SubGraph subGraph:subGraphList){///subGraph
					if(subGraph.getVertexList().size()==1||!subGraph.isVisited(req)){
						continue;
					}
					double bonus = giveBonusForLonePoint(graphs,subGraph,loneVertex,1);
					if(subGraph.isVisited(req)){
						sum += bonus;
					}
					if(subGraph.isValidWithThisReq(req)){
						validSum += bonus;
						validValueSum += matrix_ud.getScoreForLink(req, vertexIdNameMap.get(subGraph.getMaxId()))
								* bonus;
					}
				}///subGraph
				double curValue = matrix.getScoreForLink(req, loneVertexName);
				if(sum==0){
					matrix_ud.addLink(req, loneVertexName, curValue);
				}
				else{
					//double nowValue = curValue + validSum/sum*validValueSum;////maybe exist trouble
					double nowValue = curValue + validValueSum;
					nowValue = Math.min(nowValue, maxScore);
					matrix_ud.addLink(req, loneVertexName, nowValue);
				}
			} 
		}
	}
	
	
	private double giveBonusForLonePoint(double[][] graphs, SubGraph subGraph, 
			int loneVertex, double diffBetweenTopAndCur) {
		double max = 0;
		for(int innerPoint:subGraph.getVertexList()){
			if(graphs[innerPoint][loneVertex]!=0){
				max = Math.max(max, graphs[loneVertex][innerPoint]);
			}
		}
		return max*diffBetweenTopAndCur;
	}
	
	protected boolean hasContainedThisLink(SimilarityMatrix matrix, String req, int id) {
		String codeTarget = vertexIdNameMap.get(id);
		if(matrix.sourceArtifactsIds().contains(req)&&
				matrix.getLinksForSourceId(req).containsKey(codeTarget)){
			return true;
		}
		return false;
	}
	
	public void filterVertexMapDataRelated(Set<String> set){
		Iterator<Integer> ite = vertexMapDataRelated.keySet().iterator();
		while(ite.hasNext()){//while
			int keyId = ite.next();
			String keyName = vertexIdNameMap.get(keyId);
			if(!set.contains(keyName)){
				ite.remove();
			}
			else{
				Iterator<Integer> valueIte = vertexMapDataRelated.get(keyId).iterator();
				while(valueIte.hasNext()){
					int valueId = valueIte.next();
					String valueName = vertexIdNameMap.get(valueId);
					if(!set.contains(valueName)){
						valueIte.remove();
					}
				}
			}
		}//while
	}
	
	public void filterSubGraphsList(Set<String> set){
		for(SubGraph subGraph:dataSubGraphList){
			List<Integer> vertexList = subGraph.getVertexList();
			Iterator<Integer> ite = vertexList.iterator();
			while(ite.hasNext()){
				if(!set.contains(vertexIdNameMap.get(ite.next()))){
					ite.remove();
				}
			}
		}
		
		Iterator<SubGraph> subGraphIte = dataSubGraphList.iterator();
		while(subGraphIte.hasNext()){
			if(subGraphIte.next().getVertexList().size()==0){
				subGraphIte.remove();
			}
		}
	}
	
	private int allSize(Map<String, Set<String>> valid) {
		int amount = 0;
		for(String key:valid.keySet()){
			amount += valid.get(key).size();
		}
		return amount;
	}
	
	private double allWeight(Map<String, Double> vertexMapWeight) {
		double result = 0;
		for(String vertex:vertexMapWeight.keySet()){
			result += vertexMapWeight.get(vertex);
		}
		return result;
	}
	
	/**
	 * it will use this method
	 * */
	public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset,
			MethodTypeProcessLone methodType) {
		return processLoneVertexInnerMean(matrix, textDataset);
	}
	
	@Override
	public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset,
			SimilarityMatrix similarityMatrix) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAlgorithmName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, String>> getAlgorithmParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDetails() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getCorrectImprovedTargetsList() {
		// TODO Auto-generated method stub
		return null;
	}

}
