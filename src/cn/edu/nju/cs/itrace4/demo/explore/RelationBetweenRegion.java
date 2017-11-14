package cn.edu.nju.cs.itrace4.demo.explore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.demo.algo.SortBySubGraph;
import cn.edu.nju.cs.itrace4.demo.algo.SortVertexByScore;
import cn.edu.nju.cs.itrace4.demo.relation.StoreCallSubGraph;
import cn.edu.nju.cs.itrace4.demo.relation.StoreDataSubGraph;
import cn.edu.nju.cs.itrace4.demo.relation.SubGraph;
import cn.edu.nju.cs.itrace4.relation.CallDataRelationGraph;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.relation.graph.CodeEdge;

/**
 * @author zzf
 * @date 2017.11.13
 * @description get relation between valid region and valid region or no valid region
 * 		step1: get region based on call/data threshold
 *      step2: find the first region
 *      step3: calculate the distance of from this valid region and other valid region or no valid region
 */
public class RelationBetweenRegion {
	
	private double[][] callGraphs;
	private double[][] dataGraphs;
	
	private List<SubGraph> callSubGraphList;
	private List<SubGraph> dataSubGraphList;
	private List<SubGraph> callDataSubGraphList;
	
	protected Map<Integer, String> vertexIdNameMap;
	
	private int countThreshold = 2;
	private int routerLen = 1;
	
	public RelationBetweenRegion(RelationInfo ri) {
		callSubGraphList = new StoreCallSubGraph().getSubGraphs(ri);
		dataSubGraphList = new StoreDataSubGraph().getSubGraphs(ri);
		callDataSubGraphList = mergeSubGraphList(callSubGraphList,dataSubGraphList);
		
		callGraphs = describeCallGraphWithMatrix(new CallDataRelationGraph(ri).callEdgeScoreMap,ri.getVertexes().size());
		dataGraphs = describeDataGraphWithMatrix(new CallDataRelationGraph(ri).dataEdgeScoreMap,ri.getVertexes().size());
		vertexIdNameMap = ri.getVertexIdNameMap();
	}
	
	
	public void getRelationBetweenRegion(SimilarityMatrix matrix, TextDataset textDataset){
		 SimilarityMatrix oracle = textDataset.getRtm();
		 //get all target artifacts
		 removeLoneVertexList(callDataSubGraphList);
		 for(String req:textDataset.getSourceCollection().keySet()){
			//it will get maxId for every subGraph after sort.
			Collections.sort(callDataSubGraphList,new SortBySubGraph(vertexIdNameMap,matrix,req));
			int firstValidIndex = getFirstValidRegionIndex(req,callDataSubGraphList,oracle);
			System.out.println("*****************"+req+"******************* "+firstValidIndex);
			if(firstValidIndex==-1) {
				continue;
			}
			
			SubGraph target = callDataSubGraphList.get(firstValidIndex);
			for(int i = firstValidIndex+1; i<callDataSubGraphList.size(); i++) {
				SubGraph subGraph = callDataSubGraphList.get(i);
				boolean valid = isValidRegion(req,subGraph,oracle);
				double distance = distanceBetweenRegion(subGraph,target);
				if(distance!=0) {
					System.out.println(valid+" : "+distance);
				}
			}
		 }
		
	}
	
	private double distanceBetweenRegion(SubGraph subGraph, SubGraph target) {
		double maxValue = -1;
		for(int id:subGraph.getVertexList()) {
			double curValue = giveBonusForLonePointBasedCallGraph(callGraphs,target,id,1);
			maxValue = Math.max(maxValue, curValue);
		}
		return maxValue;
	}

	private double giveBonusForLonePointBasedCallGraph(double[][] graphs, SubGraph subGraph, int loneVertex,
			double diffBetweenTopAndCur) {
		double maxBonus = 0;
		for (int vertex : subGraph.getVertexList()) {
			List<List<Integer>> allRoutes = new LinkedList<List<Integer>>();
			List<Integer> curRoute = new LinkedList<Integer>();
			Set<Integer> vertexInGraph = new HashSet<Integer>(subGraph.getVertexList());
			Set<Integer> visited = new HashSet<Integer>();
			visited.add(loneVertex);
			curRoute.add(loneVertex);
			getAllRoutesFromOuterToInnerByDfs(graphs, loneVertex, curRoute, allRoutes, vertexInGraph, visited, vertex);
			getAllRoutesFromInnerToOuterByDfs(graphs, loneVertex, curRoute, allRoutes, vertexInGraph, visited, vertex);
			double curMaxBonus = 0;
			for (List<Integer> route : allRoutes) {
				double geometryMean = geometricMean(graphs, route);//
				curMaxBonus = Math.max(curMaxBonus, geometryMean);
			}

			maxBonus = Math.max(maxBonus, curMaxBonus);
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
	
	

	private boolean isValidRegion(String req, SubGraph subGraph, SimilarityMatrix oracle) {
		int localMaxId = subGraph.getMaxId();
		String className = vertexIdNameMap.get(localMaxId);
		if(oracle.isLinkAboveThreshold(req, className)) {
			return true;
		}
		else {
			return false;
		}
	}


	private int getFirstValidRegionIndex(String req,List<SubGraph> callDataSubGraphList, SimilarityMatrix oracle) {
		for(int i = 0; i < callDataSubGraphList.size();i++) {
			SubGraph subGraph = callDataSubGraphList.get(i);
			int curMaxId = subGraph.getMaxId();
			String curName = vertexIdNameMap.get(curMaxId);
			if(oracle.isLinkAboveThreshold(req, curName)) {
				return i;
			}
		}
		return -1;
	}

	private void removeLoneVertexList(List<SubGraph> callDataSubGraphList) {
		Iterator<SubGraph> ite = callDataSubGraphList.iterator();
		while(ite.hasNext()) {
			SubGraph subGraph = ite.next();
			if(subGraph.getVertexList().size()<countThreshold) {
				ite.remove();
			}
		}
	}

	private List<SubGraph> mergeSubGraphList(List<SubGraph> callSubGraphList,
			List<SubGraph> dataSubGraphList) {
		List<SubGraph> callDataSubGraphList = new LinkedList<SubGraph>();
		callDataSubGraphList.addAll(callSubGraphList);
		callDataSubGraphList.addAll(dataSubGraphList);
		return callDataSubGraphList;
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
}
