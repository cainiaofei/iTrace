package cn.edu.nju.cs.itrace4.demo.algo.outerVertex.process;

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
import cn.edu.nju.cs.itrace4.demo.algo.SortBySubGraph;
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
	private SimilarityMatrix originMatrix;
	private double percent;
	
	public UD_DataSubGraphWithBonusForLone(RelationInfo ri,Map<String,Set<String>> valid,
			SimilarityMatrix originMatrix,double percent){
		dataSubGraphList = new StoreDataSubGraph().getSubGraphs(ri);
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
	
	@Override
	public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset,
			SimilarityMatrix similarityMatrix) {
		return null;
	}

	/**
	 * it will use this method
	 * */
	public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset,
			MethodTypeProcessLone methodType) {
		if(methodType.equals(MethodTypeProcessLone.ConsiderAllArea)){
			return processLoneVertexConsiderAllArea(matrix,textDataset);
		}
		else if(methodType.equals(MethodTypeProcessLone.LikeInner)){
			return processLoneVertexLikeInner(matrix,textDataset);
		}
		else if(methodType.equals(MethodTypeProcessLone.WeightAverageForInner)){
			return processInnerVertexUseWeightedAverage(matrix,textDataset);
		}
		else if(methodType.equals(MethodTypeProcessLone.LikeInnerIgnoreDirection)){
			return processLoneVertexLikeInnerIgnoreDirection(matrix,textDataset);
		}
		else if(methodType.equals(MethodTypeProcessLone.LikeInnerWithNewEquationForLone)){
			return processLoneVertexLikeInnerWithNewEquationForLone(matrix,textDataset);
		}
		else if(methodType.equals(MethodTypeProcessLone.CallAddDataAdd)){
			return processLoneVertexCallAddDataAdd(matrix, textDataset);
		}
		else if(methodType.equals(MethodTypeProcessLone.InnerMean)){
			return processLoneVertexInnerMean(matrix, textDataset);
		}
		else if(methodType.equals(MethodTypeProcessLone.InnerMeanMaxCallData)){
			return processLoneVertexInnerMeanMaxCallData(matrix,textDataset);
		}
		else{
			return null;
		}
	}
	
	
	private SimilarityMatrix processLoneVertexInnerMeanMaxCallData(SimilarityMatrix matrix, TextDataset textDataset) {
		SimilarityMatrix oracle = textDataset.getRtm();
		 SimilarityMatrix matrix_ud = new SimilarityMatrix();
		 //get all target artifacts
		 Set<String> targetArtifacts = matrix.targetArtifactsIds();
		 //remove target artifacts which not corresponding with any source artifacts.
		 filterSubGraphsList(targetArtifacts);
		 fillLoneVertex(loneVertexSet,dataSubGraphList);
		 for(String req:matrix.sourceArtifactsIds()){
			//it will get maxId for every subGraph after sort.
			Collections.sort(dataSubGraphList,new SortBySubGraph(vertexIdNameMap,matrix,req));
			int maxId = dataSubGraphList.get(0).getMaxId();
			double maxScore = matrix.getScoreForLink(req, vertexIdNameMap.get(maxId));
			int index = 1;
			int subGraphAmount = dataSubGraphList.size()-loneVertexSet.size();
			for(SubGraph subGraph:dataSubGraphList){
				List<Integer> vertexList = subGraph.getVertexList();
				if(vertexList.size()==1){///////only process subGraph which has only one vertex 
					continue;
				}
				//regard the max score in this subGraph as represent
				String represent = vertexIdNameMap.get(subGraph.getMaxId());
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
					Map<String,Double> vertexMapWeight = new HashMap<String,Double>();
					giveBonusForNeighbor(subGraph,subGraph.getMaxId(),vertexMapWeight);
					
					double allWeight = allWeight(vertexMapWeight);
					
					for(String vertexName:vertexMapWeight.keySet()){
						double curValue = matrix.getScoreForLink(req, vertexName);
						double curWeight = vertexMapWeight.get(vertexName);
						double originValue = originMatrix.getScoreForLink(req, vertexName);
						if(!vertexName.equals(represent)){
							//double nowValue = Math.min(maxScore, originValue+maxScore/allWeight*curWeight);
							curValue = Math.max(curValue, originValue+maxScore/subGraph.getVertexList().size());
							curValue = Math.min(maxScore, curValue);
							maxScoreInThisSubGraph = Math.max(maxScoreInThisSubGraph, curValue);
						}
						matrix_ud.addLink(req, vertexName,curValue);
					}
					
					matrix_ud.addLink(req, represent, representValue);
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
		 
		// giveBonusForLoneVertexListUsingNewEquation(matrix, matrix_ud, dataSubGraphList);
		giveBonusForLoneVertexList(matrix,matrix_ud,dataSubGraphList); 
		LinksList allLinks = matrix_ud.allLinks();
		Collections.sort(allLinks, Collections.reverseOrder());
		SimilarityMatrix res = new SimilarityMatrix();
		for(SingleLink link:allLinks){
			res.addLink(link.getSourceArtifactId(), link.getTargetArtifactId(),link.getScore());
		}
		return res;
	}

	private SimilarityMatrix processLoneVertexLikeInnerWithNewEquationForLone(SimilarityMatrix matrix,
			TextDataset textDataset) {
		
		SimilarityMatrix oracle = textDataset.getRtm();
		SimilarityMatrix matrix_ud = new SimilarityMatrix();
		//get all target artifacts
		Set<String> targetArtifacts = matrix.targetArtifactsIds();
		//remove target artifacts which not corresponding with any source artifacts.
		filterSubGraphsList(targetArtifacts);
		fillLoneVertex(loneVertexSet,dataSubGraphList);
		for(String req:matrix.sourceArtifactsIds()){
			//it will get maxId for every subGraph after sort.
			Collections.sort(dataSubGraphList,new SortBySubGraph(vertexIdNameMap,matrix,req));
			int maxId = dataSubGraphList.get(0).getMaxId();
			double maxScore = matrix.getScoreForLink(req, vertexIdNameMap.get(maxId));
			int index = 1;
			int subGraphAmount = dataSubGraphList.size()-loneVertexSet.size();
			for(SubGraph subGraph:dataSubGraphList){
				List<Integer> vertexList = subGraph.getVertexList();
				
//				if(vertexList.size()==1&&!hasContainedThisLink(matrix_ud, req, vertexList.get(0))){
//					matrix_ud.addLink(req, vertexIdNameMap.get(vertexList.get(0)),
//							matrix.getScoreForLink(req, vertexIdNameMap.get(vertexList.get(0))));
//					continue;
//				}
				if(vertexList.size()==1){///////only process subGraph which has only one vertex 
					continue;
				}
				//regard the max score in this subGraph as represent
				String represent = vertexIdNameMap.get(subGraph.getMaxId());
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
					//there may exist more than one max point
					subGraph.addReq(req);
					boolean hasIgnoreRepresent = false;
					for(int id:vertexList){
						double curValue = matrix.getScoreForLink(req, vertexIdNameMap.get(id));
						if(curValue!=representValue||hasIgnoreRepresent){
							curValue = Math.min(maxScore, curValue+maxScore/vertexList.size());
							maxScoreInThisSubGraph = Math.max(maxScoreInThisSubGraph, curValue);
						}
						else{
							hasIgnoreRepresent = true;
						}
						matrix_ud.addLink(req, vertexIdNameMap.get(id),curValue);
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
		giveBonusForLoneVertexListUsingNewEquation(matrix, matrix_ud,dataSubGraphList);
		LinksList allLinks = matrix_ud.allLinks();
		Collections.sort(allLinks, Collections.reverseOrder());
		SimilarityMatrix res = new SimilarityMatrix();
		for(SingleLink link:allLinks){
			res.addLink(link.getSourceArtifactId(), link.getTargetArtifactId(),link.getScore());
		}
		return res;
	}
	
	
	
	/**
	 * 09:39 2017/6/8
	 * give bonus for lone vertex using new equation. (a/a+b+c+d)*(max/n) 
	 */
	private void giveBonusForLoneVertexListUsingNewEquation(SimilarityMatrix matrix, SimilarityMatrix matrix_ud,
			List<SubGraph> subGraphList){
		for(String req:matrix.sourceArtifactsIds()){
			Collections.sort(dataSubGraphList,new SortBySubGraph(vertexIdNameMap,matrix,req));
			int maxId = dataSubGraphList.get(0).getMaxId();
			double maxScore = matrix.getScoreForLink(req, vertexIdNameMap.get(maxId));
			
			for(int loneVertex:loneVertexSet){
				String loneVertexName = vertexIdNameMap.get(loneVertex);
				for(SubGraph subGraph:subGraphList){///subGraph
					if(subGraph.getVertexList().size()==1||!subGraph.isVisited(req)){
						continue;
					}
					double bonus = giveBonusForLonePoint(graphs,subGraph,loneVertex,1);
					subGraph.getMap().put(loneVertexName, bonus);
				}///subGraph
				//calculate a+b+c+d...
				double geometricMeanSum = getGeometricMeanSumAboutThisLone(subGraphList,loneVertexName); 
				double originValue = matrix.getScoreForLink(req, loneVertexName);
				double nowValue = originValue;
				if(geometricMeanSum==0){
					matrix_ud.addLink(req, loneVertexName, originValue);
				}
				else{
					for(SubGraph subGraph:subGraphList){///for
						List<Integer> vertexList = subGraph.getVertexList();
						if(subGraph.isValidWithThisReq(req)){
							int outerSize = getOuterSizeConnectWithInner(loneVertexSet,subGraph);
							double curMaxScore = matrix_ud.getScoreForLink(req, vertexIdNameMap.get(subGraph.getMaxId()));
							nowValue += subGraph.getMap().get(loneVertexName)/geometricMeanSum*
									maxScore/(vertexList.size());//这个可以调整
						}
					}///
					nowValue = Math.min(nowValue, maxScore);
					matrix_ud.addLink(req, loneVertexName, nowValue);
				}
			} ///for lone vertex
		}
		
	}
	
	private double getGeometricMeanSumAboutThisLone(List<SubGraph> subGraphList, String loneVertexName) {
		double res = 0;
		for(SubGraph subGraph:subGraphList){
			if(subGraph.getMap().containsKey(loneVertexName)){
				res += subGraph.getMap().get(loneVertexName);
			}
		}
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
				double validValueSum = 0;
				for(SubGraph subGraph:subGraphList){///subGraph
					if(subGraph.getVertexList().size()==1){
						continue;
					}
					
					double bonus = giveBonusForLonePoint(graphs,subGraph,loneVertex,1);
					if(subGraph.isVisited(req)){
						sum += bonus;
					}
					if(subGraph.isValidWithThisReq(req)){
						validSum += bonus;
						validValueSum += matrix_ud.getScoreForLink(req, vertexIdNameMap.get(subGraph.getMaxId()))
								*bonus;
					}
				}///subGraph
				String loneVertexName = vertexIdNameMap.get(loneVertex);
				double originValue = originMatrix.getScoreForLink(req, loneVertexName);
				double curValue = matrix.getScoreForLink(req, loneVertexName);
				if(sum==0){
					matrix_ud.addLink(req, loneVertexName, curValue);
				}
				else{
					double nowValue = originValue + validSum/sum*validValueSum;
					
					nowValue = Math.min(Math.max(nowValue,curValue), maxScore);
					matrix_ud.addLink(req, loneVertexName, nowValue);
				}
			} 
		}
		
	}
	
	/**
	 * maybe give bonus two times.  
	 */
	private void giveBonusForLoneVertexListWithCallAndData(SimilarityMatrix matrix, SimilarityMatrix matrix_ud,
			List<SubGraph> subGraphList) {
		for(String req:matrix.sourceArtifactsIds()){
			
			Collections.sort(dataSubGraphList,new SortBySubGraph(vertexIdNameMap,matrix,req));
			
			for(int loneVertex:loneVertexSet){
				double sum = 0;
				double validSum = 0;
				double validValueSum = 0;
				for(SubGraph subGraph:subGraphList){///subGraph
					if(subGraph.getVertexList().size()==1){
						continue;
					}
					
					double bonus = giveBonusForLonePoint(graphs,subGraph,loneVertex,1);
					if(subGraph.isVisited(req)){
						sum += bonus;
					}
					if(subGraph.isValidWithThisReq(req)){
						validSum += bonus;
						validValueSum += matrix_ud.getScoreForLink(req, vertexIdNameMap.get(subGraph.getMaxId()))
								*bonus;
					}
				}///subGraph
				String loneVertexName = vertexIdNameMap.get(loneVertex);
				double curValue = matrix.getScoreForLink(req, loneVertexName);
				if(sum==0){
					matrix_ud.addLink(req, loneVertexName, curValue);
				}
				else{
					curValue = curValue + validSum/sum*validValueSum;////maybe exist trouble
					matrix_ud.addLink(req, loneVertexName, curValue);
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
		
		int totalSize = vertexList.size() + outerSize;////zhiqian shi +
		
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

	/**
	 * process inner vertex using the weight average
	 * */
	public SimilarityMatrix processInnerVertexUseWeightedAverage(SimilarityMatrix matrix,
			TextDataset textDataset){
		return processLoneVertexLikeInner(matrix,textDataset);
	}
	
	
	/**
	 * process lone vertex like inner, use the simple formula
	 */
	public SimilarityMatrix processLoneVertexLikeInner(SimilarityMatrix matrix, TextDataset textDataset){
		SimilarityMatrix oracle = textDataset.getRtm();
		 SimilarityMatrix matrix_ud = new SimilarityMatrix();
		 //get all target artifacts
		 Set<String> targetArtifacts = matrix.targetArtifactsIds();
		 //remove target artifacts which not corresponding with any source artifacts.
		 filterSubGraphsList(targetArtifacts);
		 fillLoneVertex(loneVertexSet,dataSubGraphList);
		 for(String req:matrix.sourceArtifactsIds()){
			//it will get maxId for every subGraph after sort.
			Collections.sort(dataSubGraphList,new SortBySubGraph(vertexIdNameMap,matrix,req));
			int maxId = dataSubGraphList.get(0).getMaxId();
			double maxScore = matrix.getScoreForLink(req, vertexIdNameMap.get(maxId));
			int index = 1;
			int subGraphAmount = dataSubGraphList.size()-loneVertexSet.size();
			for(SubGraph subGraph:dataSubGraphList){
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
				String represent = vertexIdNameMap.get(subGraph.getMaxId());
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
				}
				if(oracle.isLinkAboveThreshold(req,represent)&&index<subGraphAmount*percent){
					//there may exist more than one max point
					boolean hasIgnoreRepresent = false;
					for(int id:vertexList){
						double curValue = matrix.getScoreForLink(req, vertexIdNameMap.get(id));
						if(curValue!=representValue||hasIgnoreRepresent){
							curValue = Math.min(maxScore, curValue+maxScore/vertexList.size());
							maxScoreInThisSubGraph = Math.max(maxScoreInThisSubGraph, curValue);
						}
						else{
							hasIgnoreRepresent = true;
						}
						matrix_ud.addLink(req, vertexIdNameMap.get(id),curValue);
					}
					
					/**
					 * process the lone vertex, encapsulate them into a function
					 **/
					giveBonusForLone(matrix,loneVertexSet,matrix_ud,maxScore,
							maxScoreInThisSubGraph,req,subGraph);
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
		LinksList allLinks = matrix_ud.allLinks();
		Collections.sort(allLinks, Collections.reverseOrder());
		SimilarityMatrix res = new SimilarityMatrix();
		for(SingleLink link:allLinks){
			res.addLink(link.getSourceArtifactId(), link.getTargetArtifactId(),link.getScore());
		}
		
		double rate = allSize(valid)*1.0/matrix.allLinks().size(); 
		return res;
	}
	
	
	/**
	 * process lone vertex like inner, use the simple formula
	 */
	public SimilarityMatrix processLoneVertexLikeInnerIgnoreDirection(SimilarityMatrix matrix, TextDataset textDataset){
		SimilarityMatrix oracle = textDataset.getRtm();
		 SimilarityMatrix matrix_ud = new SimilarityMatrix();
		 //get all target artifacts
		 Set<String> targetArtifacts = matrix.targetArtifactsIds();
		 //remove target artifacts which not corresponding with any source artifacts.
		 filterSubGraphsList(targetArtifacts);
		 fillLoneVertex(loneVertexSet,dataSubGraphList);
		 for(String req:matrix.sourceArtifactsIds()){
			//it will get maxId for every subGraph after sort.
			Collections.sort(dataSubGraphList,new SortBySubGraph(vertexIdNameMap,matrix,req));
			int maxId = dataSubGraphList.get(0).getMaxId();
			double maxScore = matrix.getScoreForLink(req, vertexIdNameMap.get(maxId));
			int index = 1;
			int subGraphAmount = dataSubGraphList.size()-loneVertexSet.size();
			for(SubGraph subGraph:dataSubGraphList){
				List<Integer> vertexList = subGraph.getVertexList();
				
//				if(vertexList.size()==1&&!hasContainedThisLink(matrix_ud, req, vertexList.get(0))){
//					matrix_ud.addLink(req, vertexIdNameMap.get(vertexList.get(0)),
//							matrix.getScoreForLink(req, vertexIdNameMap.get(vertexList.get(0))));
//					continue;
//				}
				if(vertexList.size()==1){///////only process subGraph which has only one vertex 
					continue;
				}
				//regard the max score in this subGraph as represent
				String represent = vertexIdNameMap.get(subGraph.getMaxId());
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
					//there may exist more than one max point
					subGraph.addReq(req);
					boolean hasIgnoreRepresent = false;
					for(int id:vertexList){
						double curValue = matrix.getScoreForLink(req, vertexIdNameMap.get(id));
						if(curValue!=representValue||hasIgnoreRepresent){
							curValue = Math.min(maxScore, curValue+maxScore/vertexList.size());
							maxScoreInThisSubGraph = Math.max(maxScoreInThisSubGraph, curValue);
						}
						else{
							hasIgnoreRepresent = true;
						}
						matrix_ud.addLink(req, vertexIdNameMap.get(id),curValue);
					}
					
					/**
					 * process the lone vertex, encapsulate them into a function
					 **/
//					giveBonusForLone(matrix,loneVertexSet,matrix_ud,maxScore,
//							maxScoreInThisSubGraph,req,subGraph);
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
		 
		giveBonusForLoneVertexList(matrix,matrix_ud,dataSubGraphList); 
		
		LinksList allLinks = matrix_ud.allLinks();
		Collections.sort(allLinks, Collections.reverseOrder());
		SimilarityMatrix res = new SimilarityMatrix();
		for(SingleLink link:allLinks){
			res.addLink(link.getSourceArtifactId(), link.getTargetArtifactId(),link.getScore());
		}
		
		double rate = allSize(valid)*1.0/matrix.allLinks().size(); 
		return res;
	} 
	
	/**
	 * consider all area and base on percent
	 * */
	public SimilarityMatrix processLoneVertexConsiderAllArea(SimilarityMatrix matrix, TextDataset textDataset){
		SimilarityMatrix oracle = textDataset.getRtm();
		 SimilarityMatrix matrix_ud = new SimilarityMatrix();
		 //get all target artifacts
		 Set<String> targetArtifacts = matrix.targetArtifactsIds();
		 //remove target artifacts which not corresponding with any source artifacts.
		 filterSubGraphsList(targetArtifacts);
		 fillLoneVertex(loneVertexSet,dataSubGraphList);
		 for(String req:matrix.sourceArtifactsIds()){
			//it will get maxId for every subGraph after sort.
			Collections.sort(dataSubGraphList,new SortBySubGraph(vertexIdNameMap,matrix,req));
			int maxId = dataSubGraphList.get(0).getMaxId();
			double maxScore = matrix.getScoreForLink(req, vertexIdNameMap.get(maxId));
			int index = 1;
			int subGraphAmount = dataSubGraphList.size()-loneVertexSet.size();
			for(SubGraph subGraph:dataSubGraphList){
				List<Integer> vertexList = subGraph.getVertexList();
				if(vertexList.size()==1){///////only process subGraph which has only one vertex 
					continue;
				}
				//regard the max score in this subGraph as represent
				String represent = vertexIdNameMap.get(subGraph.getMaxId());
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
					Map<String,Double> vertexMapWeight = new HashMap<String,Double>();
					giveBonusForNeighbor(subGraph,subGraph.getMaxId(),vertexMapWeight);
					
					double allWeight = allWeight(vertexMapWeight);
					
					for(String vertexName:vertexMapWeight.keySet()){
						double curValue = matrix.getScoreForLink(req, vertexName);
						double curWeight = vertexMapWeight.get(vertexName);
						if(!vertexName.equals(represent)){
							//curValue = Math.min(maxScore, curValue+maxScore/allWeight*curWeight);
							double originValue = originMatrix.getScoreForLink(req, vertexName);
							curValue = Math.min(maxScore, Math.max(originValue+maxScore/allWeight*curWeight,
									curValue));
							maxScoreInThisSubGraph = Math.max(maxScoreInThisSubGraph, curValue);
						}
						matrix_ud.addLink(req, vertexName,curValue);
					}
					
					matrix_ud.addLink(req, represent, representValue);
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
		 
		// giveBonusForLoneVertexListUsingNewEquation(matrix, matrix_ud, dataSubGraphList);
		giveBonusForLoneVertexList(matrix,matrix_ud,dataSubGraphList); 
		LinksList allLinks = matrix_ud.allLinks();
		Collections.sort(allLinks, Collections.reverseOrder());
		SimilarityMatrix res = new SimilarityMatrix();
		for(SingleLink link:allLinks){
			res.addLink(link.getSourceArtifactId(), link.getTargetArtifactId(),link.getScore());
		}
		return res;
	}
	
	public SimilarityMatrix processLoneVertexInnerMean (SimilarityMatrix matrix, TextDataset textDataset){
		SimilarityMatrix oracle = textDataset.getRtm();
		 SimilarityMatrix matrix_ud = new SimilarityMatrix();
		 //get all target artifacts
		 Set<String> targetArtifacts = matrix.targetArtifactsIds();
		 //remove target artifacts which not corresponding with any source artifacts.
		 filterSubGraphsList(targetArtifacts);
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
				String represent = vertexIdNameMap.get(subGraph.getMaxId());
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
					Map<String,Double> vertexMapWeight = new HashMap<String,Double>();
					giveBonusForNeighbor(subGraph,subGraph.getMaxId(),vertexMapWeight);
					for(String vertexName:vertexMapWeight.keySet()){
						double curValue = matrix.getScoreForLink(req, vertexName);
						if(!vertexName.equals(represent)){
							curValue = Math.min(maxScore, curValue+maxScore/subGraph.getVertexList().size());
							maxScoreInThisSubGraph = Math.max(maxScoreInThisSubGraph, curValue);
						}
						matrix_ud.addLink(req, vertexName,curValue);
					}
					
					matrix_ud.addLink(req, represent, representValue);
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
	
	
	/*
	 * code in 2017/6/14
	 */
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
 
	
	/**
	 * call add then data add
	 */
	public SimilarityMatrix processLoneVertexCallAddDataAdd(SimilarityMatrix matrix, TextDataset textDataset){
		SimilarityMatrix oracle = textDataset.getRtm();
		 SimilarityMatrix matrix_ud = new SimilarityMatrix();
		 //get all target artifacts
		 Set<String> targetArtifacts = matrix.targetArtifactsIds();
		 //remove target artifacts which not corresponding with any source artifacts.
		 filterSubGraphsList(targetArtifacts);
		 fillLoneVertex(loneVertexSet,dataSubGraphList);
		 for(String req:matrix.sourceArtifactsIds()){
			//it will get maxId for every subGraph after sort.
			Collections.sort(dataSubGraphList,new SortBySubGraph(vertexIdNameMap,matrix,req));
			int maxId = dataSubGraphList.get(0).getMaxId();
			double maxScore = matrix.getScoreForLink(req, vertexIdNameMap.get(maxId));
			int index = 1;
			int subGraphAmount = dataSubGraphList.size()-loneVertexSet.size();
			for(SubGraph subGraph:dataSubGraphList){
				List<Integer> vertexList = subGraph.getVertexList();
				if(vertexList.size()==1){///////only process subGraph which has only one vertex 
					continue;
				}
				//regard the max score in this subGraph as represent
				String represent = vertexIdNameMap.get(subGraph.getMaxId());
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
					Map<String,Double> vertexMapWeight = new HashMap<String,Double>();
					giveBonusForNeighbor(subGraph,subGraph.getMaxId(),vertexMapWeight);
					
					double allWeight = allWeight(vertexMapWeight);
					
					for(String vertexName:vertexMapWeight.keySet()){
						double curValue = matrix.getScoreForLink(req, vertexName);
						double curWeight = vertexMapWeight.get(vertexName);
						if(!vertexName.equals(represent)){
							//curValue = Math.min(maxScore, curValue+maxScore/allWeight*curWeight);
							curValue = Math.min(maxScore, curValue+maxScore/allWeight*curWeight);
							maxScoreInThisSubGraph = Math.max(maxScoreInThisSubGraph, curValue);
						}
						matrix_ud.addLink(req, vertexName,curValue);
					}
					
					matrix_ud.addLink(req, represent, representValue);
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
		 
		// giveBonusForLoneVertexListUsingNewEquation(matrix, matrix_ud, dataSubGraphList);
		giveBonusForLoneVertexListWithCallAndData(matrix,matrix_ud,dataSubGraphList); 
		LinksList allLinks = matrix_ud.allLinks();
		Collections.sort(allLinks, Collections.reverseOrder());
		SimilarityMatrix res = new SimilarityMatrix();
		for(SingleLink link:allLinks){
			res.addLink(link.getSourceArtifactId(), link.getTargetArtifactId(),link.getScore());
		}
		return res;
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
