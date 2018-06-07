package cn.edu.nju.cs.itrace4.core.algo.region.calldata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.core.document.StringHashSet;
import cn.edu.nju.cs.itrace4.core.algo.region.util.sort.SortBySubGraph;
import cn.edu.nju.cs.itrace4.core.algo.region.util.sort.SortVertexByScore;
import cn.edu.nju.cs.itrace4.core.algo.region.util.sort.SortBySubGraph;
import cn.edu.nju.cs.itrace4.demo.algo.outerVertex.process.MethodTypeProcessLone;
import cn.edu.nju.cs.itrace4.demo.relation.StoreCallSubGraph;
import cn.edu.nju.cs.itrace4.demo.relation.SubGraph;
import cn.edu.nju.cs.itrace4.relation.CallDataRelationGraph;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.relation.graph.CodeEdge;
import javafx.util.Pair;

public class UD_CallExtendData implements CSTI{
	private int routerLen;
	private double[][] callGraphs;
	private double[][] dataGraphs;
	
	private List<SubGraph> callSubGraphList;
	
	protected Map<Integer, String> vertexIdNameMap;
	private Set<Integer> loneVertexSet = new HashSet<Integer>();
	private Map<String,Set<String>> valid;
	private double percent;
	private Map<String,Set<Integer>> reqMapLoneVertex = new HashMap<String,Set<Integer>>();
	
	private RelationInfo ri;
	
	public UD_CallExtendData(RelationInfo ri,Map<String,Set<String>> valid,double percent){
		this.ri = ri;
		callSubGraphList = new StoreCallSubGraph().getSubGraphs(ri);
		//callDataSubGraphList = new StoreCallSubGraph().getSubGraphs(ri);
		callGraphs = describeCallGraphWithMatrix(new CallDataRelationGraph(ri).callEdgeScoreMap,ri.getVertexes().size());
		dataGraphs = describeDataGraphWithMatrix(new CallDataRelationGraph(ri).dataEdgeScoreMap,ri.getVertexes().size());
		
		vertexIdNameMap = ri.getVertexIdNameMap();
		this.valid = valid;
		this.percent = percent;
		this.routerLen = Integer.valueOf(System.getProperty("routerLen"));
		if(routerLen==0) {
			System.err.println("---err---");
			System.exit(-1);
		}
	}
	
	
	private double[][] describeDataGraphWithMatrix(Map<CodeEdge, Double> dataEdgeScoreMap, int size) {
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


	private double[][] describeCallGraphWithMatrix(Map<CodeEdge, Double> callEdgeScoreMap, int size) {
		double[][] matrix = new double[size+1][size+1];
		for(CodeEdge edge:callEdgeScoreMap.keySet()){
			int callerId = edge.getSource().getId();
			int calleeId = edge.getTarget().getId();
			double score = callEdgeScoreMap.get(edge);
			matrix[callerId][calleeId] = score;
		}
		return matrix;
	}


	private void fillLoneVertex(Set<Integer> loneVertexSet, List<SubGraph> callDataSubGraphList) {
		for(SubGraph subGraph:callDataSubGraphList){
			for(int ele:subGraph.getVertexList()) {
				loneVertexSet.add(ele);
			}
		}
		
		for(SubGraph subGraph:callDataSubGraphList) {
			List<Integer> curList = subGraph.getVertexList();
			if(curList.size()>1) {
				for(int ele:curList) {
					if(loneVertexSet.contains(ele)) {
						loneVertexSet.remove(ele);
					}
				}
			}
		}
	}

	@Override
	public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset,
			SimilarityMatrix similarityMatrix) {
		return null;
	}
	
	public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset,
			MethodTypeProcessLone methodType) {
			return processLoneVertexInnerMean(matrix,textDataset);
	}
	
	
	/**
	 * @author zzf
	 * @date 2017.10.26
	 * @description use map to arrange req and relevant lone vertex, Because it has different vertext list
	 * 	about different req. 
	 */
	public SimilarityMatrix processLoneVertexInnerMean(SimilarityMatrix matrix, TextDataset textDataset){
		 SimilarityMatrix oracle = textDataset.getRtm();
		 SimilarityMatrix matrix_ud = new SimilarityMatrix();
		 //get all target artifacts
		 Set<String> targetArtifacts = matrix.targetArtifactsIds();
		 //remove target artifacts which not corresponding with any source artifacts.
		 
		 for(String req:matrix.sourceArtifactsIds()){
			callSubGraphList = new StoreCallSubGraph().getSubGraphs(ri);
			filterSubGraphsList(targetArtifacts,callSubGraphList);
			//it will get maxId for every subGraph after sort.
			Collections.sort(callSubGraphList,new SortBySubGraph(vertexIdNameMap,matrix,req));
			int maxId = callSubGraphList.get(0).getMaxId();
			double maxScore = matrix.getScoreForLink(req, vertexIdNameMap.get(maxId));
			
			int rank = 1;
			extendsByDataEdge(callSubGraphList,dataGraphs,rank);
			filterSubGraphsList(targetArtifacts,callSubGraphList);
			loneVertexSet.clear();
			fillLoneVertex(loneVertexSet,callSubGraphList);
			int loneVertexSize = loneVertexSet.size();
			/**
			 * @date 2017/10.27 
			 */
			//maxScore = 0;
			
			
			int index = 1;
			int subGraphAmount = callSubGraphList.size() - loneVertexSize;
			/**
			 * @author zzf
			 * @date 2017.10.26
			 * @description new add lonevertex 
			 */
			for(SubGraph subGraph:callSubGraphList){
				List<Integer> vertexList = subGraph.getVertexList();
				Collections.sort(vertexList, new SortVertexByScore(vertexIdNameMap,matrix,req));
				if(vertexList.size()==1){
					continue;
				}
				//regard the max score in this subGraph as represent
				int localMaxId = subGraph.getMaxId();
				String represent = vertexIdNameMap.get(localMaxId);
				double representValue = matrix.getScoreForLink(req, represent);
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
				/**
				 * @author zzf 
				 * @date 2017.10.26
				 * @description regard more than percent as lone vertex. 
				 */
				else {
					for(int id:vertexList) {
						loneVertexSet.add(id);
					}
					index++;
					continue;
				}
				if(oracle.isLinkAboveThreshold(req,represent)&&index<subGraphAmount*percent){
					subGraph.addReq(req);
					
					/**
					 * @author zzf
					 * @date 2017/10/25
					 * @description stable sort 
					 */
					for(int vertexId:vertexList) {
						String vertexName = vertexIdNameMap.get(vertexId);
						double curValue = matrix.getScoreForLink(req, vertexName);
						double preValue = curValue;
						if(hasContainedThisLink(matrix_ud,req,vertexId)) {
							preValue = matrix_ud.getScoreForLink(req, vertexName);
							curValue = preValue;
						}
						if(!vertexName.equals(represent)){
							int graphSize = subGraph.getVertexList().size();
							curValue = Math.min(maxScore*0.9999, curValue+maxScore/(graphSize-1));
						}
						if(hasContainedThisLink(matrix_ud,req,vertexId)) {
							//matrix_ud.setScoreForLink(req, vertexName, Math.max(preValue, curValue));
							matrix_ud.setScoreForLink(req, vertexName,curValue);
						}
						else{
							matrix_ud.addLink(req, vertexName, curValue);
						}
					}
				}
				else{
					for(int id:vertexList){
						String vertexName = vertexIdNameMap.get(id);
						double curValue = matrix.getScoreForLink(req,vertexName);
						if(!hasContainedThisLink(matrix_ud,req,id)) {
							matrix_ud.addLink(req, vertexName,curValue);//
						}
					}
				}
				index++;
			}///
			reqMapLoneVertex.put(req, new HashSet<Integer>(loneVertexSet));
		}//req
		giveBonusForLoneRelativeVertexListBasedOnCallData(matrix, matrix_ud, callSubGraphList);
		
//		routerLen = 6;
//		giveBonusForOnlyInDataLoneRelativeVertexList(matrix, matrix_ud, callSubGraphList,onlyInData);
//		routerLen = 2;
//		giveBonusForOnlyInCallLoneRelativeVertexList(matrix, matrix_ud, dataSubGraphList,onlyInCall);
		
		LinksList allLinks = matrix_ud.allLinks();
		Collections.sort(allLinks, Collections.reverseOrder());
		SimilarityMatrix res = new SimilarityMatrix();
		for(SingleLink link:allLinks){
			res.addLink(link.getSourceArtifactId(), link.getTargetArtifactId(),link.getScore());
		}
		
		double rate = allSize(valid)*1.0/res.allLinks().size(); 
		System.out.println(rate);
		System.setProperty("rate", rate+"");
		return res;
	}
	
	
	/**
	 * @author zzf <tiaozhanzhe668@163.com>
	 * @date 2017.10.27
	 * @description find relevant class based on data edge. 
	 */
	private void extendsByDataEdge(List<SubGraph> subGraphList, double[][] dataGraphs,int rank) {
		
		for(SubGraph subGraph:subGraphList) {
			if(subGraph.getVertexList().size()==1) {
				continue;
			}
			/**
			 * 
			 * */
			rank = subGraph.getVertexList().size();
			List<Integer> newClass = getRelatedClassByDataEdge(subGraph,dataGraphs,rank);
			subGraph.getVertexList().addAll(newClass);
		}
	}


	private List<Integer> getRelatedClassByDataEdge(SubGraph subGraph, double[][] dataGraphs, int rank) {
		List<Integer> newAddNodeList = new ArrayList<Integer>();
		Set<Integer> set = new HashSet<Integer>();
		List<Integer> vertexList = subGraph.getVertexList();
		set.addAll(vertexList);
		int size = subGraph.getVertexList().size()/3;
		for(int curNodeId = 0; curNodeId < Math.min(size,rank);curNodeId++) {
			//
			int curNode = vertexList.get(curNodeId);
			for(int dataNode = 0; dataNode < dataGraphs.length;dataNode++) {
				if(!set.contains(dataNode) && dataGraphs[curNode][dataNode]!=0.0) {
					newAddNodeList.add(dataNode);
				}
			}
		}
		return newAddNodeList;
	}


	private void filterSubGraphsList(Set<String> set, List<SubGraph> graphList) {
		for(SubGraph subGraph:graphList){
			List<Integer> vertexList = subGraph.getVertexList();
			Iterator<Integer> ite = vertexList.iterator();
			while(ite.hasNext()){
				if(!set.contains(vertexIdNameMap.get(ite.next()))){
					ite.remove();
				}
			}
		}
		
		Iterator<SubGraph> graphIte = graphList.iterator();
		while(graphIte.hasNext()){
			if(graphIte.next().getVertexList().size()==0){
				graphIte.remove();
			}
		}
		
	}


	private void giveBonusForOnlyInCallLoneRelativeVertexList(SimilarityMatrix matrix, SimilarityMatrix matrix_ud,
			List<SubGraph> dataSubGraphList, Set<Integer> onlyInCall) {
		for(String req:matrix.sourceArtifactsIds()){
			Collections.sort(dataSubGraphList,new SortBySubGraph(vertexIdNameMap,matrix,req));
			int maxId = dataSubGraphList.get(0).getMaxId();
			double maxScore = matrix.getScoreForLink(req, vertexIdNameMap.get(maxId));
			
			List<Integer> loneVertexList = fillWithLoneSet(reqMapLoneVertex.get(req));
			Collections.sort(loneVertexList,new SortVertexByScore(vertexIdNameMap,matrix,req));
			
			for(int loneVertex:loneVertexList){
				String loneVertexName = vertexIdNameMap.get(loneVertex);
				if(hasContainedThisLink(matrix_ud, req, loneVertex)){///////the relative lone vertex is change.
					continue;
				}
				double sum = 0;
				double validSum = 0;
				double validValueSum = 0;
				for(SubGraph subGraph:dataSubGraphList){///subGraph
					if(subGraph.getVertexList().size()==1||!subGraph.isVisited(req)){
						continue;
					}
					double bonus = giveBonusForLonePoint(dataGraphs,subGraph,loneVertex,1);
					
					/**
					 * @date 2017.10.27
					 * outBonus 
					 */
					//bonus = 0;
					
					
					if(subGraph.isVisited(req)){
						sum += bonus;
					}
					if(subGraph.isValidWithThisReq(req)){
						validSum += bonus;
						/**
						 * @date 2017.10.27 
						 */
						double localMaxScore = matrix_ud.getScoreForLink(req, vertexIdNameMap.get(subGraph.getMaxId()));
						localMaxScore = maxScore;
						
						validValueSum += (localMaxScore-matrix.getScoreForLink(req, loneVertexName))
								* bonus;
					}
				}///subGraph
				double originValue = matrix.getScoreForLink(req, loneVertexName);
				if(sum==0){
					matrix_ud.setScoreForLink(req, loneVertexName, originValue);
				}
				else{
					//double nowValue = originValue + validSum/sum*validValueSum;////maybe exist trouble
					double nowValue = originValue + validValueSum;
					nowValue = Math.min(nowValue, maxScore);
					matrix_ud.setScoreForLink(req, loneVertexName, nowValue);
				}
			} 
		}
	}


	private void giveBonusForOnlyInDataLoneRelativeVertexList(SimilarityMatrix matrix,
			SimilarityMatrix matrix_ud,
			List<SubGraph> callSubGraphList, Set<Integer> loneVertexSet) {
		
		for(String req:matrix.sourceArtifactsIds()){
			Collections.sort(callSubGraphList,new SortBySubGraph(vertexIdNameMap,matrix,req));
			int maxId = callSubGraphList.get(0).getMaxId();
			double maxScore = matrix.getScoreForLink(req, vertexIdNameMap.get(maxId));
			
			List<Integer> loneVertexList = fillWithLoneSet(loneVertexSet);
			Collections.sort(loneVertexList,new SortVertexByScore(vertexIdNameMap,matrix,req));
			
			for(int loneVertex:loneVertexList){
				String loneVertexName = vertexIdNameMap.get(loneVertex);
				if(hasContainedThisLink(matrix_ud, req, loneVertex)){///////the relative lone vertex is change.
					continue;
				}
				double sum = 0;
				double validSum = 0;
				double validValueSum = 0;
				for(SubGraph subGraph:callSubGraphList){///subGraph
					if(subGraph.getVertexList().size()==1||!subGraph.isVisited(req)){
						continue;
					}
					double bonus = giveBonusForLonePoint(callGraphs,subGraph,loneVertex,1);
					
					/**
					 * @date 2017.10.27
					 * outBonus 
					 */
					//bonus = 0;
					
					if(subGraph.isVisited(req)){
						sum += bonus;
					}
					if(subGraph.isValidWithThisReq(req)){
						validSum += bonus;
						
						/**
						 * @date 2017.10.27
						 */
						double localMaxScore = matrix_ud.getScoreForLink(req, vertexIdNameMap.get(subGraph.getMaxId()));
						localMaxScore = maxScore;
						
						validValueSum += (localMaxScore-matrix.getScoreForLink(req, loneVertexName))
								* bonus;
					}
				}///subGraph
				double originValue = matrix.getScoreForLink(req, loneVertexName);
				if(sum==0){
					matrix_ud.setScoreForLink(req, loneVertexName, originValue);
				}
				else{
					//double nowValue = originValue + validSum/sum*validValueSum;////maybe exist trouble
					double nowValue = originValue + validValueSum;
					nowValue = Math.min(nowValue, maxScore);
					matrix_ud.setScoreForLink(req, loneVertexName, nowValue);
				}
			} 
		}
	}


	private Set<Integer> getInDataButNotCall(List<SubGraph> dataSubGraphList, List<SubGraph> callSubGraphList) {
		Set<Integer> inData = new HashSet<Integer>();
		for(SubGraph subGraph:dataSubGraphList) {
			for(int ele:subGraph.getVertexList()) {
				inData.add(ele);
			}
		}
		
		for(SubGraph subGraph:callSubGraphList) {
			for(int ele:subGraph.getVertexList()) {
				if(inData.contains(ele)) {
					inData.remove(ele);
				}
			}
		}
		return inData;
	}


	private Set<Integer> getInCallButNotData(List<SubGraph> callSubGraphList, 
			List<SubGraph> dataSubGraphList) {
		Set<Integer> inCall = new HashSet<Integer>();
		for(SubGraph subGraph:callSubGraphList) {
			for(int ele:subGraph.getVertexList()) {
				inCall.add(ele);
			}
		}
		
		for(SubGraph subGraph:dataSubGraphList) {
			for(int ele:subGraph.getVertexList()) {
				if(inCall.contains(ele)) {
					inCall.remove(ele);
				}
			}
		}
		return inCall;
	}


	private int allSize(Map<String, Set<String>> valid) {
		int amount = 0;
		for(String key:valid.keySet()){
			amount += valid.get(key).size();
		}
		return amount;
	}
	

	private void giveBonusForLoneRelativeVertexListBasedOnCallData(SimilarityMatrix matrix, SimilarityMatrix matrix_ud,
			List<SubGraph> callSubGraphList) {
		for(String req:matrix.sourceArtifactsIds()){
			Collections.sort(callSubGraphList,new SortBySubGraph(vertexIdNameMap,matrix,req));
			int maxId = callSubGraphList.get(0).getMaxId();
			double maxScore = matrix.getScoreForLink(req, vertexIdNameMap.get(maxId));
			
			List<Integer> loneVertexList = fillWithLoneSet(reqMapLoneVertex.get(req));
			Collections.sort(loneVertexList,new SortVertexByScore(vertexIdNameMap,matrix,req));
			
			for(int loneVertex:loneVertexList){
				String loneVertexName = vertexIdNameMap.get(loneVertex);
				if(hasContainedThisLink(matrix_ud, req, loneVertex)){///////the relative lone vertex is change.
					continue;
				}
				double sum = 0;
				double validValueSum = 0;
				for(SubGraph subGraph:callSubGraphList){///subGraph
					if(subGraph.getVertexList().size()==1||!subGraph.isVisited(req)){
						continue;
					}
					//callBonus
					routerLen = 6;
					double callBonus = giveBonusForLonePoint(callGraphs,subGraph,loneVertex,1);
					//dataBonus
					routerLen = 2;
					double dataBonus = giveBonusForLonePoint(dataGraphs,subGraph,loneVertex,1);
					
					if(callBonus!=0 && dataBonus!=0) {
						System.out.println();
					}
					
					double bonus1 = Math.max(callBonus, dataBonus);
					double bonus = callBonus + dataBonus;
					
					/**
					 * @date 2017.10.27
					 * outBonus 
					 */
					//bonus = 0;
					
					if(subGraph.isVisited(req)){
						sum += bonus;
					}
					if(subGraph.isValidWithThisReq(req)){
						/**
						 * @date 2017.10.27
						 */
//						if(callBonus!=0 && dataBonus!=0) {
//							System.out.println(dataBonus+":"+callBonus);
//						}
						//System.out.println(dataBonus+":"+callBonus);
						double localMaxScore = matrix_ud.getScoreForLink(req, vertexIdNameMap.get(subGraph.getMaxId()));
						localMaxScore = maxScore;
						validValueSum += (localMaxScore - matrix.getScoreForLink(req, loneVertexName))
								* bonus;
						double diff = (localMaxScore - matrix.getScoreForLink(req, loneVertexName))*(bonus-bonus1);
						if(diff<0) {
							System.out.println(localMaxScore+"-->"+matrix.getScoreForLink(req, loneVertexName));
						}
					}
				}///subGraph
				double originValue = matrix.getScoreForLink(req, loneVertexName);
				if(sum==0){
					matrix_ud.addLink(req, loneVertexName, originValue);
				}
				else{
					//double nowValue = originValue + validSum/sum*validValueSum;////maybe exist trouble
					double nowValue = originValue + validValueSum;
					nowValue = Math.min(nowValue, maxScore);
					matrix_ud.addLink(req, loneVertexName, nowValue);
				}
			} 
		}
		
	}
	
	private List<Integer> fillWithLoneSet(Set<Integer> loneVertexSet) {
		List<Integer> list = new LinkedList<Integer>();
		for(int ele:loneVertexSet) {
			list.add(ele);
		}
		return list;
	}

	
	private double giveBonusForLonePoint(double[][] graphs, SubGraph subGraph, int loneVertex,double diffBetweenTopAndCur) {
		 double maxBonus = 0;
		 for(int vertex:subGraph.getVertexList()){
             List<List<Integer>> allRoutes = new LinkedList<List<Integer>>();
             List<Integer> curRoute = new LinkedList<Integer>();
             Set<Integer> vertexInGraph = new HashSet<Integer>(subGraph.getVertexList());
             Set<Integer> visited = new HashSet<Integer>();
             
             visited.add(loneVertex);
             curRoute.add(loneVertex);
             getAllRoutesFromOuterToInnerByDfs(graphs,loneVertex,curRoute,allRoutes,vertexInGraph,visited,vertex); 
             getAllRoutesFromInnerToOuterByDfs(graphs,loneVertex,curRoute,allRoutes,vertexInGraph,visited,vertex); 
             for(List<Integer> route:allRoutes){
            	 double geometryMean = geometricMean(graphs,route);//
            	 maxBonus = Math.max(maxBonus, /*diffBetweenTopAndCur*/geometryMean);
             }
        }
		return maxBonus;
	}


	private double geometricMean(double[][] graphs, List<Integer> route) {
		if(route.size()==0){
			return 0;
		}
		
		double res = 1;
		
		int[] routes = new int[route.size()];
		int index = 0;
		for(int ele:route){
			routes[index++] = ele;
		}
		for(int i = 1; i < routes.length;i++){
			int one = routes[i];
			int other = routes[i-1];
			if(graphs[one][other]!=0){
				//base += 1.0/graphs[one][other];
				res *= graphs[one][other];
			}
			else{
				//base += 1.0/graphs[other][one];
				res *= graphs[other][one];
			}
		}
		//double geometryMean = Math.pow(res, 1.0/(routes.length-1));
		double geometryMean = Math.pow(res, 1.0/(1));
		//double geometryMean = count / base;
		return geometryMean;
	}


	private void getAllRoutesFromOuterToInnerByDfs(double[][] graphs, int curVertex, List<Integer> curRoute,List<List<Integer>> allRoutes,
			Set<Integer> vertexInGraph, Set<Integer> visited, int target) {
		 if(curVertex==target){
	            allRoutes.add(new LinkedList<Integer>(curRoute));
	     }
	     else if(vertexInGraph.contains(curVertex)||curRoute.size()==routerLen){
	            return ;
	     }
	     else{
	        	//from outer to inner
	            for(int i = 1; i < graphs.length;i++){
	            	if(graphs[curVertex][i]==0||visited.contains(i)){
	            		continue;
	            	}
	                visited.add(i);
	                curRoute.add(i);
	                getAllRoutesFromOuterToInnerByDfs(graphs,i,curRoute,allRoutes,vertexInGraph,visited,target);
	                curRoute.remove(curRoute.size()-1);
	                visited.remove(i);
	            }
	     }
	}
	
	
	private void getAllRoutesFromInnerToOuterByDfs(double[][] graphs, int curVertex, List<Integer> curRoute,List<List<Integer>> allRoutes,
			Set<Integer> vertexInGraph, Set<Integer> visited, int target) {
		 if(curVertex==target){
	            allRoutes.add(new LinkedList<Integer>(curRoute));
	     }
	     else if(vertexInGraph.contains(curVertex)||curRoute.size()==routerLen){
	            return ;
	     }
	     else{
	        	//from outer to inner
	            for(int i = 1; i < graphs.length;i++){
	            	if(graphs[i][curVertex]==0||visited.contains(i)){
	            		continue;
	            	}
	                visited.add(i);
	                curRoute.add(i);
	                getAllRoutesFromInnerToOuterByDfs(graphs,i,curRoute,allRoutes,vertexInGraph,visited,target);
	                curRoute.remove(curRoute.size()-1);
	                visited.remove(i);
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
	
	public void filterSubGraphsList(Set<String> set){
		for(SubGraph subGraph:callSubGraphList){
			List<Integer> vertexList = subGraph.getVertexList();
			Iterator<Integer> ite = vertexList.iterator();
			while(ite.hasNext()){
				if(!set.contains(vertexIdNameMap.get(ite.next()))){
					ite.remove();
				}
			}
		}
		
		Iterator<SubGraph> subGraphIte = callSubGraphList.iterator();
		while(subGraphIte.hasNext()){
			if(subGraphIte.next().getVertexList().size()==0){
				subGraphIte.remove();
			}
		}
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