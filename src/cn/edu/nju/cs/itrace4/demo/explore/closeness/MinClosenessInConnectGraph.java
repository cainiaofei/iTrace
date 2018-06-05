package cn.edu.nju.cs.itrace4.demo.explore.closeness;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.relation.CallDataRelationGraph;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.relation.graph.CodeEdge;
import cn.edu.nju.cs.refactor.exception.FileException;
import cn.edu.nju.cs.refactor.util.FileProcess;
import cn.edu.nju.cs.refactor.util.FileProcessTool;

public class MinClosenessInConnectGraph {
	private double[][] graphs;
	private RelationInfo ri;
	private TextDataset textDataset;
	private Map<String,Integer> nameIdMap;
	private String rtmPath = "data/exp/iTrust/rtm/RTM_CLASS.txt";
	private Map<String,Set<String>> reqClassRTM; 
	private FileProcess fileProcess = new FileProcessTool();
	
	public MinClosenessInConnectGraph(RelationInfo ri,TextDataset textDataset) {
		this.ri = ri;
		this.textDataset = textDataset;
		this.graphs = getCodeDependencyGraph();
		this.nameIdMap = getNameIdMap(ri);
		this.reqClassRTM = getReqClassRTM();
	}
	
	private Map<String, Set<String>> getReqClassRTM() {
		Map<String,Set<String>> reqClassRTM = new HashMap<String,Set<String>>();
		String content = null;
		try {
			content = fileProcess.getFileConent(rtmPath);
		} catch (FileException | IOException e) {
			e.printStackTrace();
		}
		String[] reqClassRowArray = content.split("/n");
		for(String reqClassRow:reqClassRowArray) {
			String[] strs = reqClassRow.split("/s+"); 
			if(!reqClassRTM.containsKey(strs[0])) {
				reqClassRTM.put(strs[0], new HashSet<String>());
			}
			reqClassRTM.get(strs[0]).add(strs[1]);
		}
		return reqClassRTM;
	}

	private Map<String, Integer> getNameIdMap(RelationInfo ri) {
		Map<Integer,String> idNameMap = ri.getVertexIdNameMap();
		Map<String,Integer> nameIdMap = new HashMap<String,Integer>();
		for(int id:idNameMap.keySet()) {
			nameIdMap.put(idNameMap.get(id), id);
		}
		return nameIdMap;
	}

	
	/**
	 * entrance 
	 */
	public void showMinClosenessInConnectedArea() {
		Map<String,Double> minClosessnessForReq = findMinClosenessInConnectedArea();
		for(String req:minClosessnessForReq.keySet()) {
			System.out.println("req:"+minClosessnessForReq.get(req));
		}
	}
	
	/**
	 * @date 2018.6.5
	 * @author zzf
	 * @description use cloned graph for every *requirement* because some class achieve several requirements. And if remove edge 
	 * 		directly between them, it may cause a bug. 
	 */
	private Map<String,Double> findMinClosenessInConnectedArea(){
		double[][] codeDepGraphs = cpy(graphs);
		Map<String,Double> minClosessnessForReq = new HashMap<String,Double>();
		for(String req:reqClassRTM.keySet()) {
			Set<Integer> classIDSet = getCorrespondClass(req);
			Edge preClosessnessEdge = null; 
			Edge minClosessnessEdge = findMinClosenessEdge(codeDepGraphs,classIDSet);
			while(isConnectedArea(codeDepGraphs, classIDSet)) {
				int vertexA = minClosessnessEdge.getVertexA();
				int vertexB = minClosessnessEdge.getVertexB();
				//remove min closeness edge
				codeDepGraphs[vertexA][vertexB] = 0;
				preClosessnessEdge = minClosessnessEdge;
				minClosessnessEdge = findMinClosenessEdge(codeDepGraphs,classIDSet);
			}
			minClosessnessForReq.put(req, preClosessnessEdge.getCloseness());
		}
		return minClosessnessForReq;
	}
	
	
	private Edge findMinClosenessEdge(double[][] codeDepGraphs, Set<Integer> classIDSet) {
		Edge edge = null;
		int[] classIDArray = transferArrayFromSet(classIDSet);
		for(int i = 0; i < classIDArray.length;i++) {
			for(int j = i+1;j < classIDArray.length;j++) {
				int vertexA = classIDArray[i];
				int vertexB = classIDArray[j];
				if(codeDepGraphs[vertexA][vertexB]==0) {
					continue; // there no exist edge.
				}
				if(edge==null || edge.getCloseness()>codeDepGraphs[vertexA][vertexB]) {
					edge = new Edge(vertexA,vertexB,codeDepGraphs[vertexA][vertexB]);
				}
			}
		}
		return edge;
	}

	private int[] transferArrayFromSet(Set<Integer> classIDSet) {
		int[] classIDArray = new int[classIDSet.size()];
		int number = 0;
		for(int classID:classIDSet) {
			classIDArray[number++] = classID;
		}
		return classIDArray;
	}

	private double[][] cpy(double[][] graphs) {
		double[][] codeDepGraphs = new double[graphs.length][graphs.length];
		for(int i = 0; i < graphs.length;i++) {
			codeDepGraphs[i] = Arrays.copyOf(graphs[i], graphs[i].length);
		}
		return codeDepGraphs;
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
	
	private Set<Integer> getCorrespondClass(String req){
		Set<Integer> classIDSet = new HashSet<Integer>();
		Set<String> classNameSet = reqClassRTM.get(req);
		for(String className:classNameSet) {
			classIDSet.add(nameIdMap.get(className));
		}
		return classIDSet;
	}
	
	/**
	 * this is the core algorithm in this class. Judge whether this area is connected. 
	 * algo: dfs from one vertex, if it can traverse all vertex through edge between vertex in 
	 * this set called classInArea.
 	 */
	private boolean isConnectedArea(double[][] graphs,Set<Integer> classInArea) {
		int seed = getSeedID(classInArea);
		Set<Integer> hasBeenVisitedClass = new HashSet<Integer>();
		hasBeenVisitedClass.add(seed);
		Set<Integer> allReachedClassInArea = getAllReachedClassInArea(seed,classInArea,hasBeenVisitedClass);
		return allReachedClassInArea.size()==classInArea.size();
	}


	//dfs
	private Set<Integer> getAllReachedClassInArea(int seed, Set<Integer> classInArea,
			Set<Integer> hasBeenVisitedClass) {
		Set<Integer> allReachedClassInArea = new HashSet<Integer>();
		allReachedClassInArea.add(seed);
		if(allReachedClassInArea.size()==classInArea.size()) {
			return allReachedClassInArea;
		}
		else {
			for(int i = 0; i < graphs.length;i++) {
				if(classInArea.contains(i) && !hasBeenVisitedClass.contains(i) 
						&& graphs[seed][i]>0) {
					hasBeenVisitedClass.add(i);
					Set<Integer> reachedClassFromCur = getAllReachedClassInArea(i,classInArea,hasBeenVisitedClass);
					allReachedClassInArea.addAll(reachedClassFromCur);
				}
			}
		}
		return allReachedClassInArea;
	}

	/**
	 * random
	 */
	private int getSeedID(Set<Integer> classInArea) {
		for(int classID:classInArea) {
			return classID;
		}
		return -1;
	}
	
}
