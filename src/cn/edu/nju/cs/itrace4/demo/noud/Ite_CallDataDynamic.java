package cn.edu.nju.cs.itrace4.demo.noud;

import java.util.ArrayList;
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
import cn.edu.nju.cs.itrace4.demo.algo.SortVertexByScore;
import cn.edu.nju.cs.itrace4.demo.relation.StoreCallSubGraph;
import cn.edu.nju.cs.itrace4.demo.relation.StoreDataSubGraph;
import cn.edu.nju.cs.itrace4.demo.relation.SubGraph;
import cn.edu.nju.cs.itrace4.relation.CallDataRelationGraph;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.relation.graph.CodeEdge;
import javafx.util.Pair;

/**
 * @author zzf
 * @date 2017.11.08
 * @description step1: generate call region and data region based on call/data threshold. 
 *              step2: merge call/data region. 
 *              step3: based on the highest IR link, give bonus for remaining about every
 *              requirement.
 *              step4: record visited code element. 
 *              step5: end, we will end iterate when the bonus is very small.
 */

public class Ite_CallDataDynamic implements CSTI {
	private int routerLen;
	private double[][] callGraphs;
	private double[][] dataGraphs;

	private List<SubGraph> callSubGraphList;
	private List<SubGraph> dataSubGraphList;
	private List<SubGraph> callDataSubGraphList;

	protected Map<Integer, String> vertexIdNameMap;
	private Set<Integer> allVertexIdList = new HashSet<Integer>();
	private boolean hidden = true;
	private Set<Integer> absoluteLoneVertexSet = new HashSet<Integer>();

	private double callThreshold;
	private double dataThreshold;

	private Map<Integer, Map<Integer, Double>> callRouterCache = new HashMap<Integer, Map<Integer, Double>>();
	private Map<Integer, Map<Integer, Double>> dataRouterCache = new HashMap<Integer, Map<Integer, Double>>();
	private int countThreshold = 2;

	public Ite_CallDataDynamic(RelationInfo ri, double callThreshold, double dataThreshold) {
		this.callThreshold = callThreshold;
		this.dataThreshold = dataThreshold;

		callSubGraphList = new StoreCallSubGraph().getSubGraphs(ri);
		dataSubGraphList = new StoreDataSubGraph().getSubGraphs(ri);

		buildTagForSubGraph(callSubGraphList, "call");
		buildTagForSubGraph(dataSubGraphList, "data");
		callDataSubGraphList = mergeSubGraphList(callSubGraphList, dataSubGraphList);

		callGraphs = describeCallGraphWithMatrix(new CallDataRelationGraph(ri).callEdgeScoreMap,
				ri.getVertexes().size());
		dataGraphs = describeDataGraphWithMatrix(new CallDataRelationGraph(ri).dataEdgeScoreMap,
				ri.getVertexes().size());
		vertexIdNameMap = ri.getVertexIdNameMap();
	}

	private void buildTagForSubGraph(List<SubGraph> subGraphList, String type) {
		for (SubGraph subGraph : subGraphList) {
			subGraph.SetType(type);
		}
	}

	private List<SubGraph> mergeSubGraphList(List<SubGraph> callSubGraphList, List<SubGraph> dataSubGraphList) {
		List<SubGraph> callDataSubGraphList = new LinkedList<SubGraph>();
		callDataSubGraphList.addAll(callSubGraphList);
		callDataSubGraphList.addAll(dataSubGraphList);
		return callDataSubGraphList;
	}

	private double[][] describeDataGraphWithMatrix(Map<CodeEdge, Double> dataEdgeScoreMap, int size) {
		double[][] matrix = new double[size + 1][size + 1];
		for (CodeEdge edge : dataEdgeScoreMap.keySet()) {
			int callerId = edge.getSource().getId();
			int calleeId = edge.getTarget().getId();
			double score = dataEdgeScoreMap.get(edge);
			matrix[callerId][calleeId] = score;
			matrix[calleeId][callerId] = score;
		}
		return matrix;
	}

	private double[][] describeCallGraphWithMatrix(Map<CodeEdge, Double> callEdgeScoreMap, int size) {
		double[][] matrix = new double[size + 1][size + 1];
		for (CodeEdge edge : callEdgeScoreMap.keySet()) {
			int callerId = edge.getSource().getId();
			int calleeId = edge.getTarget().getId();
			double score = callEdgeScoreMap.get(edge);
			matrix[callerId][calleeId] = score;
		}
		return matrix;
	}

	private Set<Integer> fillLoneVertex(SubGraph subGraph, Set<Integer> hasVisited, boolean hidden) {
		Set<Integer> regionSet = new HashSet<Integer>();
		regionSet.addAll(subGraph.getVertexList());
		if (hidden) {
			regionSet.addAll(hasVisited);
		}
		Set<Integer> loneVertexSet = new HashSet<Integer>();
		for (int ele : allVertexIdList) {
			if (!regionSet.contains(ele)) {
				loneVertexSet.add(ele);
			}
		}
		return loneVertexSet;
	}

	@Override
	public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset,
			SimilarityMatrix similarityMatrix) {
		return processLoneVertexInnerMean(matrix, textDataset);
	}

	/**
	 * @author zzf <tiaozhanzhe668@163.com>
	 * @date 2017.11.09
	 * @param matrix   SimilarityMatrix   the candidate links after IR
	 * @param textDataset   TextDataset   include uc/code rtm artifacts.
	 * @description  alike ud_feedback but we don't need user justify and we recognize highest IR value positive, then update its 
	 * neighborhood.
 	 */
	public SimilarityMatrix processLoneVertexInnerMean(SimilarityMatrix matrix, TextDataset textDataset) {
		// get all target artifacts
		Set<String> targetArtifacts = matrix.targetArtifactsIds();
		// remove target artifacts which not corresponding with any source artifacts.
		filterSubGraphsList(targetArtifacts, callDataSubGraphList);
		buildAllVertexIdList(allVertexIdList, callDataSubGraphList);
		fillAbsoluteLoneVertexSet(absoluteLoneVertexSet, callDataSubGraphList);
		removeLoneVertexList(callDataSubGraphList);

		List<SubGraph> originCallDataSubGraphList = new ArrayList<SubGraph>(callDataSubGraphList);

		for (String req : matrix.sourceArtifactsIds()) {
			callDataSubGraphList = new ArrayList<SubGraph>(originCallDataSubGraphList);
			// it will get maxId for every subGraph after sort.
			Collections.sort(callDataSubGraphList, new SortBySubGraph(vertexIdNameMap, matrix, req));
			int maxId = callDataSubGraphList.get(0).getMaxId();
			double maxScore = matrix.getScoreForLink(req, vertexIdNameMap.get(maxId));

			// int subGraphAmount = callDataSubGraphList.size() - loneVertexSize;
			Set<Integer> hasVisitedRegion = new HashSet<Integer>();
			// for(SubGraph subGraph:callDataSubGraphList){//subGraph
			int index = 0;
			int amount = callDataSubGraphList.size();
			while (!callDataSubGraphList.isEmpty()&&index<1) {
				Collections.sort(callDataSubGraphList, new SortBySubGraph(vertexIdNameMap, matrix, req));
				SubGraph subGraph = callDataSubGraphList.get(0);
				List<Integer> vertexList = subGraph.getVertexList();
				Collections.sort(vertexList, new SortVertexByScore(vertexIdNameMap, matrix, req));
				// regard the max score in this subGraph as represent
				int localMaxId = subGraph.getMaxId();
				String represent = vertexIdNameMap.get(localMaxId);
				
				if(subGraph.getVertexList().size()<countThreshold*1) {
					index++;
					continue;
				}
				
				for (int vertexId : vertexList) {
					String vertexName = vertexIdNameMap.get(vertexId);
					double curValue = matrix.getScoreForLink(req, vertexName);
					// double preValue = curValue;
					if (!vertexName.equals(represent)) {
						int graphSize = subGraph.getVertexList().size();
						curValue = Math.min(maxScore * 0.9999, curValue + maxScore / (graphSize - 1));
					}
					matrix.setScoreForLink(req, vertexName, curValue);
				}
				Set<Integer> curLoneVertexList = fillLoneVertex(subGraph, hasVisitedRegion, hidden);
				giveBonusForLoneNotInThisRegion(matrix, subGraph, curLoneVertexList, req);
				hasVisitedRegion.addAll(subGraph.getVertexList());
				callDataSubGraphList.remove(0);
				
				index++;
			} ///
		} // req

		return matrix;
	}

	private void removeLoneVertexList(List<SubGraph> callDataSubGraphList) {
		Iterator<SubGraph> ite = callDataSubGraphList.iterator();
		while (ite.hasNext()) {
			SubGraph subGraph = ite.next();
			if (subGraph.getVertexList().size() < countThreshold) {
				ite.remove();
			}
		}
	}

	private void giveBonusForLoneNotInThisRegion(SimilarityMatrix matrix, SubGraph subGraph,
			Set<Integer> curLoneVertexList, String req) {
		// giveBonusBasedCallGraph(matrix, subGraph, curLoneVertexList,req);
		// giveBonusBasedDataGraph(matrix, subGraph, curLoneVertexList,req);

		if (subGraph.getType().equals("call")) {
			giveBonusBasedCallGraph(matrix, subGraph, curLoneVertexList, req);
		} else if (subGraph.getType().equals("data")) {
			giveBonusBasedDataGraph(matrix, subGraph, curLoneVertexList, req);
		} else {
			System.out.println("---------giveBonusForLoneNotInThisRegion-----------------");
			System.out.println("-----you may forget init subGraph Type-----");
			System.exit(-1);
		}
	}

	private void giveBonusBasedDataGraph(SimilarityMatrix matrix, SubGraph subGraph, Set<Integer> curLoneVertexList,
			String req) {
		Collections.sort(callDataSubGraphList, new SortBySubGraph(vertexIdNameMap, matrix, req));
		int maxId = callDataSubGraphList.get(0).getMaxId();
		double maxScore = matrix.getScoreForLink(req, vertexIdNameMap.get(maxId));

		List<Integer> loneVertexList = fillWithLoneSet(curLoneVertexList);
		Collections.sort(loneVertexList, new SortVertexByScore(vertexIdNameMap, matrix, req));
		for (int loneVertex : loneVertexList) {
			String loneVertexName = vertexIdNameMap.get(loneVertex);
			routerLen = 2;
			double bonus = giveBonusForLonePointBasedDataGraph(dataGraphs, subGraph, loneVertex, 1);

			double localMaxScore = matrix.getScoreForLink(req, vertexIdNameMap.get(subGraph.getMaxId()));
			// localMaxScore = maxScore;

			double validValueSum = (localMaxScore) * bonus;
			double originValue = matrix.getScoreForLink(req, loneVertexName);
			double nowValue = originValue + validValueSum;
			nowValue = Math.min(nowValue, maxScore);
			matrix.setScoreForLink(req, loneVertexName, nowValue);
		}

	}

	private void giveBonusBasedCallGraph(SimilarityMatrix matrix, SubGraph subGraph, Set<Integer> curLoneVertexList,
			String req) {

		Collections.sort(callDataSubGraphList, new SortBySubGraph(vertexIdNameMap, matrix, req));
		int maxId = callDataSubGraphList.get(0).getMaxId();
		double maxScore = matrix.getScoreForLink(req, vertexIdNameMap.get(maxId));

		List<Integer> loneVertexList = fillWithLoneSet(curLoneVertexList);
		Collections.sort(loneVertexList, new SortVertexByScore(vertexIdNameMap, matrix, req));
		for (int loneVertex : loneVertexList) {
			String loneVertexName = vertexIdNameMap.get(loneVertex);

			// routerLen = 3;
			routerLen = 6;
			double bonus = giveBonusForLonePointBasedCallGraph(callGraphs, subGraph, loneVertex, 1);

			double localMaxScore = matrix.getScoreForLink(req, vertexIdNameMap.get(subGraph.getMaxId()));
			// localMaxScore = maxScore;

			double validValueSum = (localMaxScore) * bonus;
			double originValue = matrix.getScoreForLink(req, loneVertexName);
			double nowValue = originValue + validValueSum;
			nowValue = Math.min(nowValue, maxScore);
			matrix.setScoreForLink(req, loneVertexName, nowValue);
		}
	}

	private void buildAllVertexIdList(Set<Integer> allVertexIdList, List<SubGraph> callDataSubGraphList2) {
		for (SubGraph subGraph : callDataSubGraphList) {
			for (int id : subGraph.getVertexList()) {
				allVertexIdList.add(id);
			}
		}

	}

	private void filterSubGraphsList(Set<String> set, List<SubGraph> graphList) {
		for (SubGraph subGraph : graphList) {
			List<Integer> vertexList = subGraph.getVertexList();
			Iterator<Integer> ite = vertexList.iterator();
			while (ite.hasNext()) {
				if (!set.contains(vertexIdNameMap.get(ite.next()))) {
					System.out.println("-----there shouldn't exist this case-----");
					System.exit(-1);
					ite.remove();
				}
			}
		}

		Iterator<SubGraph> graphIte = graphList.iterator();
		while (graphIte.hasNext()) {
			if (graphIte.next().getVertexList().size() == 0) {
				graphIte.remove();
			}
		}

	}

	private void fillAbsoluteLoneVertexSet(Set<Integer> absoluteLoneVertexSet, List<SubGraph> callDataSubGraphList) {
		for (SubGraph subGraph : callDataSubGraphList) {
			for (int ele : subGraph.getVertexList()) {
				absoluteLoneVertexSet.add(ele);
			}
		}

		for (SubGraph subGraph : callDataSubGraphList) {
			List<Integer> curList = subGraph.getVertexList();
			if (curList.size() >= countThreshold) {
				for (int ele : subGraph.getVertexList()) {
					if (absoluteLoneVertexSet.contains(ele)) {
						absoluteLoneVertexSet.remove(ele);
					}
				}
			}
		}
	}

	private List<Integer> fillWithLoneSet(Set<Integer> loneVertexSet) {
		List<Integer> list = new LinkedList<Integer>();
		for (int ele : loneVertexSet) {
			list.add(ele);
		}
		return list;
	}

	private double giveBonusForLonePointBasedCallGraph(double[][] graphs, SubGraph subGraph, int loneVertex,
			double diffBetweenTopAndCur) {
		double maxBonus = 0;
		for (int vertex : subGraph.getVertexList()) {
			if (callRouterCache.containsKey(vertex) && callRouterCache.get(vertex).containsKey(loneVertex)) {
				double geometryMean = callRouterCache.get(vertex).get(loneVertex);
				maxBonus = Math.max(maxBonus, geometryMean);
			} else {
				List<List<Integer>> allRoutes = new LinkedList<List<Integer>>();
				List<Integer> curRoute = new LinkedList<Integer>();
				Set<Integer> vertexInGraph = new HashSet<Integer>(subGraph.getVertexList());
				Set<Integer> visited = new HashSet<Integer>();
				visited.add(loneVertex);
				curRoute.add(loneVertex);
				getAllRoutesFromOuterToInnerByDfs(graphs, loneVertex, curRoute, allRoutes, vertexInGraph, visited,
						vertex);
				getAllRoutesFromInnerToOuterByDfs(graphs, loneVertex, curRoute, allRoutes, vertexInGraph, visited,
						vertex);
				double curMaxBonus = 0;
				for (List<Integer> route : allRoutes) {
					double geometryMean = geometricMean(graphs, route);//
					curMaxBonus = Math.max(curMaxBonus, geometryMean);
				}
				if (!callRouterCache.containsKey(vertex)) {
					callRouterCache.put(vertex, new HashMap<Integer, Double>());
				}
				if (!callRouterCache.containsKey(loneVertex)) {
					callRouterCache.put(loneVertex, new HashMap<Integer, Double>());
				}
				callRouterCache.get(vertex).put(loneVertex, curMaxBonus);
				callRouterCache.get(loneVertex).put(vertex, curMaxBonus);

				maxBonus = Math.max(maxBonus, curMaxBonus);
			}
		}
		return maxBonus;
	}

	private double giveBonusForLonePointBasedDataGraph(double[][] graphs, SubGraph subGraph, int loneVertex,
			double diffBetweenTopAndCur) {
		double maxBonus = 0;
		for (int vertex : subGraph.getVertexList()) {
			if (dataRouterCache.containsKey(vertex) && dataRouterCache.get(vertex).containsKey(loneVertex)) {
				double geometryMean = dataRouterCache.get(vertex).get(loneVertex);
				maxBonus = Math.max(maxBonus, geometryMean);
			} else {
				List<List<Integer>> allRoutes = new LinkedList<List<Integer>>();
				List<Integer> curRoute = new LinkedList<Integer>();
				Set<Integer> vertexInGraph = new HashSet<Integer>(subGraph.getVertexList());
				Set<Integer> visited = new HashSet<Integer>();
				visited.add(loneVertex);
				curRoute.add(loneVertex);
				getAllRoutesFromOuterToInnerByDfs(graphs, loneVertex, curRoute, allRoutes, vertexInGraph, visited,
						vertex);
				getAllRoutesFromInnerToOuterByDfs(graphs, loneVertex, curRoute, allRoutes, vertexInGraph, visited,
						vertex);
				double curMaxBonus = 0;
				for (List<Integer> route : allRoutes) {
					double geometryMean = geometricMean(graphs, route);//
					curMaxBonus = Math.max(curMaxBonus, geometryMean);
				}
				if (!dataRouterCache.containsKey(vertex)) {
					dataRouterCache.put(vertex, new HashMap<Integer, Double>());
				}
				if (!dataRouterCache.containsKey(loneVertex)) {
					dataRouterCache.put(loneVertex, new HashMap<Integer, Double>());
				}
				dataRouterCache.get(vertex).put(loneVertex, curMaxBonus);
				dataRouterCache.get(loneVertex).put(vertex, curMaxBonus);

				maxBonus = Math.max(maxBonus, curMaxBonus);
			}
		}
		return maxBonus;
	}

	private double geometricMean(double[][] graphs, List<Integer> route) {
		if (route.size() == 0) {
			return 0;
		}
		double res = 1;
		int[] routes = new int[route.size()];
		int index = 0;
		for (int ele : route) {
			routes[index++] = ele;
		}
		for (int i = 1; i < routes.length; i++) {
			int one = routes[i];
			int other = routes[i - 1];
			if (graphs[one][other] != 0) {
				// base += 1.0/graphs[one][other];
				res *= graphs[one][other];
			} else {
				// base += 1.0/graphs[other][one];
				res *= graphs[other][one];
			}
		}
		// double geometryMean = Math.pow(res, 1.0/(routes.length-1));
		double geometryMean = Math.pow(res, 1.0 / (1));
		// double geometryMean = count / base;
		return geometryMean;
	}

	private void getAllRoutesFromOuterToInnerByDfs(double[][] graphs, int curVertex, List<Integer> curRoute,
			List<List<Integer>> allRoutes, Set<Integer> vertexInGraph, Set<Integer> visited, int target) {
		if (curVertex == target) {
			allRoutes.add(new LinkedList<Integer>(curRoute));
		} else if (vertexInGraph.contains(curVertex) || curRoute.size() == routerLen) {
			return;
		} else {
			// from outer to inner
			for (int i = 1; i < graphs.length; i++) {
				if (graphs[curVertex][i] == 0 || visited.contains(i)) {
					continue;
				}
				visited.add(i);
				curRoute.add(i);
				getAllRoutesFromOuterToInnerByDfs(graphs, i, curRoute, allRoutes, vertexInGraph, visited, target);
				curRoute.remove(curRoute.size() - 1);
				visited.remove(i);
			}
		}
	}

	private void getAllRoutesFromInnerToOuterByDfs(double[][] graphs, int curVertex, List<Integer> curRoute,
			List<List<Integer>> allRoutes, Set<Integer> vertexInGraph, Set<Integer> visited, int target) {
		if (curVertex == target) {
			allRoutes.add(new LinkedList<Integer>(curRoute));
		} else if (vertexInGraph.contains(curVertex) || curRoute.size() == routerLen) {
			return;
		} else {
			// from outer to inner
			for (int i = 1; i < graphs.length; i++) {
				if (graphs[i][curVertex] == 0 || visited.contains(i)) {
					continue;
				}
				visited.add(i);
				curRoute.add(i);
				getAllRoutesFromInnerToOuterByDfs(graphs, i, curRoute, allRoutes, vertexInGraph, visited, target);
				curRoute.remove(curRoute.size() - 1);
				visited.remove(i);
			}
		}
	}

	protected boolean hasContainedThisLink(SimilarityMatrix matrix, String req, int id) {
		String codeTarget = vertexIdNameMap.get(id);
		if (matrix.sourceArtifactsIds().contains(req) && matrix.getLinksForSourceId(req).containsKey(codeTarget)) {
			return true;
		}
		return false;
	}

	public void filterSubGraphsList(Set<String> set) {
		for (SubGraph subGraph : callDataSubGraphList) {
			List<Integer> vertexList = subGraph.getVertexList();
			Iterator<Integer> ite = vertexList.iterator();
			while (ite.hasNext()) {
				if (!set.contains(vertexIdNameMap.get(ite.next()))) {
					ite.remove();
				}
			}
		}

		Iterator<SubGraph> subGraphIte = callDataSubGraphList.iterator();
		while (subGraphIte.hasNext()) {
			if (subGraphIte.next().getVertexList().size() == 0) {
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
		return "Ite_CallDataDynamic_" + callThreshold + "_" + dataThreshold;
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
