package cn.edu.nju.cs.itrace4.demo.algo.relationBetweenSubGraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.demo.algo.outerVertex.process.CodeRegionClosenessType;
import cn.edu.nju.cs.itrace4.demo.datastruct.GraphNode;
import cn.edu.nju.cs.itrace4.demo.relation.StoreDataSubGraphRemoveEdge;
import cn.edu.nju.cs.itrace4.demo.relation.SubGraph;
import cn.edu.nju.cs.itrace4.relation.CallDataRelationGraph;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.relation.graph.CodeEdge;

/**
 * @date 2018.5.28
 * @author tiaozhanzhe668@163.com
 * @description 1. calculate the Closeness between vertex.
 * 				2. show the portion of valid valid code element and no-valid code element. 
 */
public class CodeRegionInfo {
	private int routerLenThreshold = 17;
	private RelationInfo ri;
	private CodeRegionClosenessType codeRegionClosenessType;
	private TextDataset textDataset;
	protected Map<Integer, String> vertexIdNameMap;
	private double[][] closenessBetweenRegionDP;
	
	public CodeRegionInfo(TextDataset textDataset,RelationInfo ri,
			CodeRegionClosenessType codeRegionClosenessType) {
		this.textDataset = textDataset;
		this.ri = ri;
		this.codeRegionClosenessType = codeRegionClosenessType;
		init();
	}
	
	private void init() {
		vertexIdNameMap = ri.getVertexIdNameMap();
		closenessBetweenRegionDP = new double[ri.getVertexes().size()+1][ri.getVertexes().size()+1];
	}

	/**
	 * @author tiaozhanzhe668@163.com 
	 * @date 2018.5.28
	 * @description 1. get code region through ri
	 * 				2. get the Closeness between code region based on codeRegionClosenessType.
	 * 				3. data display.
	 */
	public void showClosenessBetweenRegion() {
		List<SubGraph> codeRegionList = new StoreDataSubGraphRemoveEdge().getSubGraphs(ri);
		int countThreshold = 1;
		selectRegionWithMoreThanOneVertex(codeRegionList,countThreshold);
		double[][] closenessBetweenCodeRegion = getClosenessBetweenCodeRegion(codeRegionList);
		displayCodeRegionInfo(closenessBetweenCodeRegion);
	}
	 
	/**
	 * @date 2018.5.29
	 * @author zzf
	 * @description remove region the code element number of which no more than countThreshold.
	 */
	private void selectRegionWithMoreThanOneVertex(List<SubGraph> codeRegionList, int countThreshold) {
		Iterator<SubGraph> ite = codeRegionList.iterator();
		while(ite.hasNext()) {
			SubGraph subGraph = ite.next();
			if(subGraph.getVertexList().size()<=countThreshold) {
				ite.remove();
			}
		}
	}

	private void displayCodeRegionInfo(double[][] closenessBetweenCodeRegion) {
		
	}

	/**
	 * @description employ different strategy base on codeRegionClosenessType.
	 */
	private double[][] getClosenessBetweenCodeRegion(List<SubGraph> codeRegionList) {
		int regionCount = codeRegionList.size();
		double[][] closenessBetweenCodeRegion = new double[regionCount][regionCount];
		for(int i = 0; i < codeRegionList.size();i++) {
			for(int j = i+1;j<codeRegionList.size();j++) {
				double closeness = getClosenessBetweenThem(codeRegionList.get(i),codeRegionList.get(j));
				closenessBetweenCodeRegion[i][j] = closeness;
				System.out.println(i+"--->"+j+":"+closeness);
			}
		}
		return closenessBetweenCodeRegion;
	}

	/**
	 * @date 2018.5.29
	 * @author zzf
	 * @description there are kinds of way to calculate Closeness between code region based on different
	 * 		codeRegionClosenessType.  
	 */
	private double getClosenessBetweenThem(SubGraph codeRegionA, SubGraph codeRegionB) {
		if(codeRegionClosenessType==CodeRegionClosenessType.closenessBetweenRepresent) {
			return getClosenessBetweenThemBasedOnRepresent(codeRegionA,codeRegionB);
		}
		else if(codeRegionClosenessType==CodeRegionClosenessType.maxClosenessBetweenEvery) {
			return getClosenessBetweenThemBasedOnMaxDist(codeRegionA,codeRegionB);
		}
		return 0;
	}

	/**
	 * @date 2018.5.29
	 * @author zzf
	 * @description  
	 */
	private double getClosenessBetweenThemBasedOnMaxDist(SubGraph codeRegionA, SubGraph codeRegionB) {
		double[][] graphs = getCodeDependencyGraph();
		double maxClosenessBetweenCodeRegion = 0;
		for(int sourceId:codeRegionA.getVertexList()) {
			Set<Integer> visited = new HashSet<Integer>();
			visited.add(sourceId);
			for(int targetId:codeRegionB.getVertexList()) {
				double maxUtilNow = 0;
				double curDist = 1.0;
				double curClossness = getClosenessBetweenTwoVertex(sourceId, targetId, graphs, maxUtilNow, curDist, visited);
				maxClosenessBetweenCodeRegion = Math.max(curClossness, maxClosenessBetweenCodeRegion);
			}
		}
		return maxClosenessBetweenCodeRegion;
	}

	/**
	 * @description the representative is vertex which in-degree is zero and has longest call router if
	 * 		there are more than one vertex which in-degree is zero. 
	 *      1. get the graph and ignore direction.
	 *      2. get representative code element based on graph.
	 *      3. calculate the Closeness between them  
	 */
	private double getClosenessBetweenThemBasedOnRepresent(SubGraph codeRegionA, SubGraph codeRegionB) {
		double[][] graphs = getCodeDependencyGraph();
		int representIdInCodeRegionA = getRepresentIdInSubGraph(codeRegionA);
		int representIdInCodeRegionB = getRepresentIdInSubGraph(codeRegionB);
		
		double maxUtilNow = 0;
		double curDist = 1.0;
		Set<Integer> visited = new HashSet<Integer>();
		visited.add(representIdInCodeRegionA);
		
//		double closeness = getClosenessBetweenTwoVertex(representIdInCodeRegionA,representIdInCodeRegionB,
//				graphs,maxUtilNow,curDist,visited);
		double closeness = getGeometricMeanClosenessBetweenTwoVertex(representIdInCodeRegionA,representIdInCodeRegionB,
				graphs,maxUtilNow,curDist,visited);
		
		closenessBetweenRegionDP[representIdInCodeRegionA][representIdInCodeRegionB] = closeness;
		return closeness;
	}
	
	/**
	 * @date 2018.5.29
	 * @description  return all valid router between two vertex.
	 */
	private double getClosenessBetweenTwoVertex(int source,int target,double[][] graphs,
			double maxUtilNow,double curDist,Set<Integer> visited) {
		 if(visited.size()>10) {
			 return 0;
		 }
		 if(closenessBetweenRegionDP[source][target]>0) {
			 return Math.max(maxUtilNow, curDist*closenessBetweenRegionDP[source][target]);
		 }
		 else if(maxUtilNow>curDist) {
			 return maxUtilNow;
		 }
		 else {
			 if(source==target) {
				 return Math.max(maxUtilNow,curDist);
			 }
			 else {
				 double localMax = 0.0;
				 for(int i = 0; i < graphs.length;i++) {
					 if(graphs[source][i]>0 && !visited.contains(i) && i!=source) {
						 visited.add(i);
						 curDist = curDist * graphs[source][i];
						 localMax = Math.max(localMax, getClosenessBetweenTwoVertex(i,target,graphs,
								 maxUtilNow,curDist,visited));
						 maxUtilNow = Math.max(maxUtilNow, localMax);
						 visited.remove(i);
					 }
				 }
				 return Math.max(localMax, maxUtilNow);
			 }
		 }
	}
	
	
	/**
	 * @date 2018.5.29
	 * @description geometric mean 
	 */
	private double getGeometricMeanClosenessBetweenTwoVertex(int source,int target,double[][] graphs,
			double maxUtilNow,double curDist,Set<Integer> visited) {
		if(visited.size()>routerLenThreshold) {
			 return 0;
		 }
		 if(maxUtilNow>curDist) {
			 return maxUtilNow;
		 }
		 else {
			 if(source==target) {
				 curDist = Math.pow(curDist,1.0/(visited.size()-1));
				 return Math.max(maxUtilNow,curDist);
			 }
			 else {
				 double localMax = 0.0;
				 for(int i = 0; i < graphs.length;i++) {
					 if(graphs[source][i]>0 && !visited.contains(i) && i!=source) {
						 visited.add(i);
						 curDist = curDist * graphs[source][i];
						 localMax = Math.max(localMax, getGeometricMeanClosenessBetweenTwoVertex(i,target,graphs,
								 maxUtilNow,curDist,visited));
						 maxUtilNow = Math.max(maxUtilNow, localMax);
						 visited.remove(i);
					 }
				 }
				 return Math.max(localMax, maxUtilNow);
			 }
		 }
	}
	
	
	/**
	 * @date 2018.5.29
	 * @description  get represent vertex in code region based on code structure (call dependency graph)
	 */
	private int getRepresentIdInSubGraph(SubGraph codeRegionA) {
		double[][] callGraphs = describeCallGraphWithMatrix(new CallDataRelationGraph(ri,false).callEdgeScoreMap,
				ri.getVertexes().size());
		GraphNode representNode = getRepresentativeElement(codeRegionA,callGraphs);
		return representNode.getId();
	}
	
	/**
	 * @author zzf
	 * @description choose the code element which has not been called anyone in region
	 * as the representative element. 
	 */
	private GraphNode getRepresentativeElement(SubGraph subGraph,double[][] callGraphs) {
		int localMaxId = subGraph.getVertexList().get(0);
		String representClassName = vertexIdNameMap.get(localMaxId);
		GraphNode representNode = new GraphNode(localMaxId,representClassName);
		
		List<Integer> vertexList = subGraph.getVertexList();
		List<GraphNode> entranceNodeList = new ArrayList<GraphNode>();
		for(int id:vertexList) {
			GraphNode graphNode = new GraphNode(id,vertexIdNameMap.get(id));
			neighborWithGraphNode(graphNode,vertexList,callGraphs);
			if(graphNode.getCallerList().size()==0) {
				entranceNodeList.add(graphNode);
			}
		}
		int maxLen = 1;
		for(GraphNode curNode:entranceNodeList) {
			int maxLenStartFromCurNode = getMaxCallRouterLength(subGraph,curNode.getId(),
					new HashSet<Integer>(),callGraphs);
			if(maxLenStartFromCurNode>maxLen) {
				representNode = curNode;
				maxLen = Math.max(maxLen, maxLenStartFromCurNode);
			}
		}
		return representNode;
	}

	/**
	 * @author zzf
	 * @description get the max length of call router which start from curId in region 
	 */
	private int getMaxCallRouterLength(SubGraph region,int curId,Set<Integer> visited,
			double[][] callGraphs) {
		List<Integer> neighborList = region.getVertexList();
		int maxLen = 1;
		for(int neighbor:neighborList) {
			if(callGraphs[curId][neighbor]>0 && !visited.contains(neighbor)) {
				visited.add(neighbor);
				int curLen = 1 + getMaxCallRouterLength(region,neighbor,visited,callGraphs);
				visited.remove(neighbor);
				maxLen = Math.max(maxLen, curLen);
			}
		}
		return maxLen;
	}
	
	/**
	 * @author zzf
	 * @description calculate the caller class and callee class which are in same region with graphNode
	 */
	private void neighborWithGraphNode(GraphNode graphNode, List<Integer> vertexList,double[][] callGraphs) {
		int curId = graphNode.getId();
		for(int neighborId:vertexList) {
			if(callGraphs[neighborId][curId]>0) {
				graphNode.addCallerGraphNode(new GraphNode(neighborId,vertexIdNameMap.get(neighborId)));
			}
			if(callGraphs[curId][neighborId]>0) {
				graphNode.addCalleeGraphNode(new GraphNode(neighborId,vertexIdNameMap.get(neighborId)));
			}
		}
	}


	/**
	 * @description the call data graph, if there exist call and data dependency simultaneously, choose max closeness.
	 */
	private double[][] getCodeDependencyGraph(){
		double[][] graphs = new double[ri.getVertexes().size()+1][ri.getVertexes().size()+1];
		CallDataRelationGraph callDataRelationGraph = new CallDataRelationGraph(ri,false);
		for(CodeEdge edge:callDataRelationGraph.callEdgeScoreMap.keySet()){
			int callerId = edge.getSource().getId();
			int calleeId = edge.getTarget().getId();
			double score = callDataRelationGraph.callEdgeScoreMap.get(edge);
			graphs[callerId][calleeId] = score;
			graphs[calleeId][callerId] = score;
		}
		for(CodeEdge edge:callDataRelationGraph.dataEdgeScoreMap.keySet()){
			int callerId = edge.getSource().getId();
			int calleeId = edge.getTarget().getId();
			double score = callDataRelationGraph.dataEdgeScoreMap.get(edge);
			graphs[callerId][calleeId] = Math.max(graphs[callerId][calleeId],score);
			graphs[calleeId][callerId] = graphs[callerId][calleeId];
		}
		return graphs;
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
