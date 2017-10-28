package cn.edu.nju.cs.itrace4.demo.algo;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.core.algo.CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.core.document.StringHashSet;
import cn.edu.nju.cs.itrace4.demo.relation.StoreDataSubGraph;
import cn.edu.nju.cs.itrace4.demo.relation.SubGraph;
import cn.edu.nju.cs.itrace4.relation.CallDataRelationGraph;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.relation.graph.CodeEdge;
import javafx.util.Pair;

public class UD_DataSubGraphWithBonusForLone implements CSTI{

	private Map<String,Set<String>> valid;
	private double[][] graphs;
	private List<SubGraph> dataSubGraphList;
	protected Map<Integer, String> vertexIdNameMap;
	private Set<Integer> loneVertexSet = new HashSet<Integer>();
	
	
	public UD_DataSubGraphWithBonusForLone(RelationInfo ri,Map<String,Set<String>> valid){
		dataSubGraphList = new StoreDataSubGraph().getSubGraphs(ri);
		graphs = describeGraphWithMatrix(new CallDataRelationGraph(ri).dataEdgeScoreMap,ri.getVertexes().size());
		vertexIdNameMap = ri.getVertexIdNameMap();
		this.valid = valid;
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
	
	@Override
	public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset,
			SimilarityMatrix similarityMatrix) {
		 SimilarityMatrix oracle = textDataset.getRtm();
		 SimilarityMatrix matrix_ud = new SimilarityMatrix();
		//get all target artifacts
		 Set<String> targetArtifacts = matrix.targetArtifactsIds();
		 filterSubGraphsList(targetArtifacts);
		 fillLoneVertex(loneVertexSet,dataSubGraphList);
		 int verifyNumber = 0;
		 for(String req:matrix.sourceArtifactsIds()){
			Collections.sort(dataSubGraphList,new SortBySubGraph(vertexIdNameMap,matrix,req));
			int maxId = dataSubGraphList.get(0).getMaxId();
			double maxScore = matrix.getScoreForLink(req, vertexIdNameMap.get(maxId));
			int index = 1;
			double percent = 0.7;
			int subGraphAmount = dataSubGraphList.size()-loneVertexSet.size();
			for(SubGraph subGraph:dataSubGraphList){
				List<Integer> vertexList = subGraph.getVertexList();
				/*if(vertexList.size()==1&&!hasContainedThisLink(matrix_ud, req, vertexList.get(0))){
					matrix_ud.addLink(req, vertexIdNameMap.get(vertexList.get(0)),
							matrix.getScoreForLink(req, vertexIdNameMap.get(vertexList.get(0))));
					continue;
				}*/
				if(vertexList.size()==1){///////
					continue;
				}
				//find max in 
				String represent = vertexIdNameMap.get(subGraph.getMaxId());
				double representValue = matrix.getScoreForLink(req, represent);
				double maxScoreInThisSubGraph = representValue;
				if(index<subGraphAmount*percent){
					if(valid.containsKey(req)&&valid.get(req).contains(represent)){
						;
					}
					else{
						verifyNumber++;
					}
				}
				if(oracle.isLinkAboveThreshold(req,represent)&&index<subGraphAmount*percent){
					Map<String,Double> vertexMapWeight = new HashMap<String,Double>();
					giveBonusForNeighbor(subGraph,subGraph.getMaxId(),vertexMapWeight);
					
					double allWeight = allWeight(vertexMapWeight);
					
					for(String vertexName:vertexMapWeight.keySet()){
						double curValue = matrix.getScoreForLink(req, vertexName);
						double curWeight = vertexMapWeight.get(vertexName);
						if(!vertexName.equals(represent)){
							curValue = Math.min(maxScore, curValue+maxScore/allWeight*curWeight);
							maxScoreInThisSubGraph = Math.max(maxScoreInThisSubGraph, curValue);
						}
						matrix_ud.addLink(req, vertexName,curValue);
					}
					
					matrix_ud.addLink(req, represent, representValue);
					/**
					 * process the lone vertex, encapsulate them into a function
					 * now process loneVertex at the end
					 **/
				}
				else{
					for(int id:vertexList){
						double curValue = matrix.getScoreForLink(req, vertexIdNameMap.get(id));
						matrix_ud.addLink(req, vertexIdNameMap.get(id),curValue);//
					}
				}
				index++;
			}///
		}//for
		giveBonusForLoneVertexList(matrix, matrix_ud,dataSubGraphList);
		LinksList allLinks = matrix_ud.allLinks();
		Collections.sort(allLinks, Collections.reverseOrder());
		SimilarityMatrix res = new SimilarityMatrix();
		for(SingleLink link:allLinks){
			res.addLink(link.getSourceArtifactId(), link.getTargetArtifactId(),link.getScore());
		}
		double rate = (verifyNumber + allSize(valid))*1.0/allLinks.size();
		return res;
	}

	/*
	 * 21:13 2017/6/1
	 * change the way of outer vertex bonus calculate.
	 * */
	private void giveBonusForLoneVertexList(SimilarityMatrix matrix, SimilarityMatrix matrix_ud,
			List<SubGraph> subGraphList) {
		for(String req:matrix.sourceArtifactsIds()){
			
			Collections.sort(dataSubGraphList,new SortBySubGraph(vertexIdNameMap,matrix,req));
			int maxId = dataSubGraphList.get(0).getMaxId();
			double maxScore = matrix.getScoreForLink(req, vertexIdNameMap.get(maxId));
			
			for(int loneVertex:loneVertexSet){
				double sum = 0;
				double validSum = 0;
				int totalSize = 0;
				for(SubGraph subGraph:subGraphList){///subGraph
					if(subGraph.getVertexList().size()==1){
						continue;
					}
					
					int outerSize = getOuterSizeConnectWithInner(loneVertexSet,subGraph);
					int curTotalSize = subGraph.getVertexList().size() + outerSize;
					totalSize = Math.max(totalSize, curTotalSize);
					
					double bonus = giveBonusForLonePoint(graphs,subGraph,loneVertex,1);
					sum += bonus;
					if(subGraph.isValidWithThisReq(req)){
						validSum += bonus;
					}
				}///subGraph
				String loneVertexName = vertexIdNameMap.get(loneVertex);
				double originValue = matrix.getScoreForLink(req, loneVertexName);
				if(sum==0){
					matrix_ud.addLink(req, loneVertexName, originValue);
				}
				else{
					//System.out.println(sum);
					double nowValue = originValue + validSum/sum*maxScore/totalSize;////maybe exist trouble
					nowValue = Math.min(nowValue, maxScore);
					matrix_ud.addLink(req, loneVertexName, nowValue);
				}
			} 
		}
		
	}

	
	private double allWeight(Map<String, Double> vertexMapWeight) {
		double result = 0;
		for(String vertex:vertexMapWeight.keySet()){
			result += vertexMapWeight.get(vertex);
		}
		return result;
	}
	
	private int allSize(Map<String, Set<String>> valid) {
		int amount = 0;
		for(String key:valid.keySet()){
			amount += valid.get(key).size();
		}
		return amount;
	}

	/**
	 * try the new method
	 * use closeness when give bonus for neighbor.
	 * */
	private void giveBonusForNeighbor(SubGraph subGraph,int represent,Map<String,Double> vertexMapWeight){
		for(int vertexId:subGraph.getVertexList()){
			if(vertexId!=represent){
				List<Integer> list = new LinkedList<Integer>();
				list.add(represent);
				/**
				 * it seemed not sensible use a graph to represent a vertex, but in order to 
				 * use the previous code, I have to do as such.
				 */
				SubGraph centreGraph = new SubGraph(list);
				
				double weight = giveBonusForLonePoint(graphs,centreGraph,vertexId,1);
				vertexMapWeight.put(vertexIdNameMap.get(vertexId), weight);
			}
		}
	}
	
	
	/*
	 * step1: calculate the total size, which contains origin size and outer size.
	 * step2: alike the inner vertex, give bonus for outer vertex. 
	 * step3: don't consider increase more than one time.
	 */
	private void giveBonusForLone(SimilarityMatrix matrix, Set<Integer> loneVertexSet, 
			SimilarityMatrix matrix_ud,double maxScore,double maxScoreInThisSubGraph,
			String req,SubGraph subGraph) {
		
int outerSize = getOuterSizeConnectWithInner(loneVertexSet,subGraph);
		
		List<Integer> vertexList = subGraph.getVertexList();
		
		int totalSize = vertexList.size() + outerSize;
		
		for(int loneVertex:loneVertexSet){////
			String targetArtifact = vertexIdNameMap.get(loneVertex);
			double curValue = matrix.getScoreForLink(req, vertexIdNameMap.get(loneVertex));
			double bonus = giveBonusForLonePoint(graphs,subGraph,loneVertex,maxScoreInThisSubGraph-curValue);
			if(bonus!=0){
				bonus = maxScore;
			}
			
			double preValue = matrix.getScoreForLink(req, vertexIdNameMap.get(loneVertex));
			if(hasContainedThisLink(matrix_ud, req, loneVertex)){
				preValue = matrix_ud.getScoreForLink(req, vertexIdNameMap.get(loneVertex));
			}
			
			double nowValue = Math.min(maxScore,Math.max(preValue, curValue + bonus/totalSize));
			
			if(hasContainedThisLink(matrix_ud, req, loneVertex)){
				matrix_ud.setScoreForLink(req, targetArtifact, nowValue);
			}
			else{
				matrix_ud.addLink(req, targetArtifact, nowValue);
			}
		}
	}

	
	
	private int getOuterSizeConnectWithInner(Set<Integer> loneVertexSet, SubGraph subGraph) {
		int outerSize = 0;
		for(int loneVertex:loneVertexSet){
			double bonus = giveBonusForLonePoint(graphs,subGraph,loneVertex,1);
			if(bonus!=0){
				outerSize++;
			}
		}
		return outerSize;
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
	
	protected boolean hasContainedThisLink(SimilarityMatrix matrix, String req, int id) {
		String codeTarget = vertexIdNameMap.get(id);
		if(matrix.sourceArtifactsIds().contains(req)&&
				matrix.getLinksForSourceId(req).containsKey(codeTarget)){
			return true;
		}
		return false;
	}
	
	protected String getFirstKey(StringHashSet sourceArtifactsIds) {
		Iterator<String> ite = sourceArtifactsIds.iterator();
		return ite.next();
	}

	
	@Override
	public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAlgorithmName() {
		// TODO Auto-generated method stub
		return "UD_DataSubGraphWithBonusForLone";
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
