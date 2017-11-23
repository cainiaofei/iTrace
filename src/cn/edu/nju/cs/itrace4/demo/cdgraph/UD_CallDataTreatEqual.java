package cn.edu.nju.cs.itrace4.demo.cdgraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.core.algo.CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.demo.algo.SortBySubGraph;
import cn.edu.nju.cs.itrace4.demo.algo.SortVertexByScore;
import cn.edu.nju.cs.itrace4.demo.relation.StoreDataSubGraphRemoveEdge;
import cn.edu.nju.cs.itrace4.demo.relation.SubGraph;
import cn.edu.nju.cs.itrace4.relation.CallDataRelationGraph;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.relation.graph.CodeEdge;
import javafx.util.Pair;

/**
 * @author zzf
 * @date 2017.11.17
 * @description treat data/call edge equal. There exist this case that a region may contain both
 * 	call edge and data edge.
 * @step  
 * 	step1: get all regions through class <code>StoreDataSubGraphRemoveEdge</code>
 *  step2: the lone vertex will change because we define the lonevetex is vertex we has not verify.
 */
public class UD_CallDataTreatEqual implements CSTI{
	
	private int callRouterLen = 6;
	private int dataRouterLen = 2;
	private double[][] callGraphs;
	private double[][] dataGraphs;
	
	
	private List<SubGraph> regionList;
	
	protected Map<Integer, String> vertexIdNameMap;
	private Map<String,Set<String>> valid;
	private double percent;
	private Set<Integer> allVertexIdList = new HashSet<Integer>();
	private boolean hidden = true;
	
	private double callThreshold;
	private double dataThreshold;
	
	private Map<Integer,Map<Integer,Double>> callRouterCache = new HashMap<Integer,Map<Integer,Double>>();
	private int countThreshold = 2;
	
	
	public UD_CallDataTreatEqual(RelationInfo ri,double callThreshold,double dataThreshold,
			double percent,Map<String,Set<String>> valid){
		allVertexIdList = ri.getVertexIdNameMap().keySet();
		this.callThreshold = callThreshold;
		this.dataThreshold = dataThreshold;
		
		regionList = new StoreDataSubGraphRemoveEdge().getSubGraphs(ri);
		
		callGraphs = describeCallGraphWithMatrix(new CallDataRelationGraph(ri).callEdgeScoreMap,ri.getVertexes().size());
		dataGraphs = describeDataGraphWithMatrix(new CallDataRelationGraph(ri).dataEdgeScoreMap,ri.getVertexes().size());
		
		vertexIdNameMap = ri.getVertexIdNameMap();
		this.valid = valid;
		this.percent = percent;
	}
	
	
	public SimilarityMatrix optimizeIRMatrix(SimilarityMatrix matrix, TextDataset textDataset) {
		 
		 SimilarityMatrix oracle = textDataset.getRtm();
		 
		 int loneVertexSize = getRegionCountWhichHasOnlyOneVertex(regionList);
		 
		 for(String req:matrix.sourceArtifactsIds()){
			List<SubGraph> curRegionList = new ArrayList<SubGraph>(regionList);
			//it will get maxId for every subGraph after sort.
			Collections.sort(curRegionList,new SortBySubGraph(vertexIdNameMap,matrix,req));
			int maxId = curRegionList.get(0).getMaxId();
			double maxScore = matrix.getScoreForLink(req, vertexIdNameMap.get(maxId));
			
			int index = 1;
			//the count of region which has more than one vertex.
			int subGraphAmount = curRegionList.size() - loneVertexSize;
			Set<Integer> hasVisitedRegion = new HashSet<Integer>();
			//for(SubGraph subGraph:callDataSubGraphList){//subGraph
			while(curRegionList.size() != 0) {
				Collections.sort(curRegionList,new SortBySubGraph(vertexIdNameMap,matrix,req));
				SubGraph subGraph = curRegionList.get(0);
				List<Integer> vertexList = subGraph.getVertexList();
				Collections.sort(vertexList, new SortVertexByScore(vertexIdNameMap,matrix,req));
				if(vertexList.size()<countThreshold){
					curRegionList.remove(0);
					continue;
				}
				//regard the max score in this subGraph as represent
				int localMaxId = subGraph.getMaxId();
				String represent = vertexIdNameMap.get(localMaxId);
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
				if(oracle.isLinkAboveThreshold(req,represent) && index<subGraphAmount*percent){//if start
					subGraph.addReq(req);
					
					for(int vertexId:vertexList) {
						String vertexName = vertexIdNameMap.get(vertexId);
						double curValue = matrix.getScoreForLink(req, vertexName);
						//double preValue = curValue;
						if(!vertexName.equals(represent)){
							int graphSize = subGraph.getVertexList().size();
							//
							//curValue = Math.min(maxScore*0.9999, curValue+maxScore/(graphSize-1));
							curValue = Math.min(maxScore*0.9999, curValue+maxScore/(graphSize));
						}
						matrix.setScoreForLink(req, vertexName, curValue);
					}
					Set<Integer> curLoneVertexList = fillLoneVertex(subGraph,
							hasVisitedRegion,hidden);
					giveBonusForLoneNotInThisRegion(matrix, subGraph,curLoneVertexList,req);
					hasVisitedRegion.addAll(subGraph.getVertexList());
				}//if end
				index++;
				curRegionList.remove(0);
			}///
		}//req
		
		LinksList allLinks = matrix.allLinks();
		Collections.sort(allLinks, Collections.reverseOrder());
		SimilarityMatrix res = new SimilarityMatrix();
		for(SingleLink link:allLinks){
			res.addLink(link.getSourceArtifactId(), link.getTargetArtifactId(),link.getScore());
		}
		
		double rate = allSize(valid)*1.0/res.allLinks().size(); 
		System.out.println("ud_CallDataTreatEqual:"+rate);
		System.setProperty("rate", rate+"");
		return res;
	}
	
	private void giveBonusForLoneNotInThisRegion(SimilarityMatrix matrix, SubGraph subGraph,
			Set<Integer> curLoneVertexList,String req) {
		giveBonusBasedCallGraph(matrix, subGraph, curLoneVertexList,req);
		giveBonusBasedDataGraph(matrix, subGraph, curLoneVertexList,req);
	}
	
	private double giveBonusForLonePointBasedCallGraph(double[][] graphs, SubGraph subGraph,
			int loneVertex,double diffBetweenTopAndCur) {
		 double maxBonus = 0;
		 for(int vertex:subGraph.getVertexList()){
			 if(callRouterCache.containsKey(vertex) && 
					 callRouterCache.get(vertex).containsKey(loneVertex)) {
				 double geometryMean = callRouterCache.get(vertex).get(loneVertex);
				 maxBonus = Math.max(maxBonus,geometryMean);
			 }
			 else {
				 List<List<Integer>> allRoutes = new LinkedList<List<Integer>>();
	             List<Integer> curRoute = new LinkedList<Integer>();
	             Set<Integer> vertexInGraph = new HashSet<Integer>(subGraph.getVertexList());
	             Set<Integer> visited = new HashSet<Integer>();
	             visited.add(loneVertex);
	             curRoute.add(loneVertex);
	             getAllRoutesFromOuterToInnerByDfs(graphs,loneVertex,curRoute,allRoutes,vertexInGraph,
	            		 visited,vertex,callRouterLen); 
	             getAllRoutesFromInnerToOuterByDfs(graphs,loneVertex,curRoute,allRoutes,vertexInGraph,
	            		 visited,vertex,callRouterLen);
	             double curMaxBonus = 0;
	             for(List<Integer> route:allRoutes){
	            	 double geometryMean = geometricMean(graphs,route);//
	            	 curMaxBonus = Math.max(curMaxBonus, geometryMean);
	             }
            	 if(!callRouterCache.containsKey(vertex)) {
            		 callRouterCache.put(vertex, new HashMap<Integer,Double>());
            	 }
            	 if(!callRouterCache.containsKey(loneVertex)) {
            		 callRouterCache.put(loneVertex, new HashMap<Integer,Double>());
            	 }
            	 callRouterCache.get(vertex).put(loneVertex, curMaxBonus);
            	 callRouterCache.get(loneVertex).put(vertex, curMaxBonus);
            	 
            	 maxBonus = Math.max(maxBonus,curMaxBonus);
			 }
        }
		return maxBonus;
	}
	
	private void getAllRoutesFromOuterToInnerByDfs(double[][] graphs, int curVertex, List<Integer> curRoute,List<List<Integer>> allRoutes,
			Set<Integer> vertexInGraph, Set<Integer> visited, int target,int routerLen) {
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
	                getAllRoutesFromOuterToInnerByDfs(graphs,i,curRoute,allRoutes,vertexInGraph,visited,target,routerLen);
	                curRoute.remove(curRoute.size()-1);
	                visited.remove(i);
	            }
	     }
	}
	
	private void giveBonusBasedCallGraph(SimilarityMatrix matrix, SubGraph subGraph,
			Set<Integer> curLoneVertexList, String req) {

		Collections.sort(regionList, new SortBySubGraph(vertexIdNameMap, matrix, req));
		int maxId = regionList.get(0).getMaxId();
		double maxScore = matrix.getScoreForLink(req, vertexIdNameMap.get(maxId));

		List<Integer> loneVertexList = fillWithLoneSet(curLoneVertexList);
		Collections.sort(loneVertexList, new SortVertexByScore(vertexIdNameMap, matrix, req));
		for (int loneVertex : loneVertexList) {
			String loneVertexName = vertexIdNameMap.get(loneVertex);
			
			double bonus = giveBonusForLonePointBasedCallGraph(callGraphs, subGraph, loneVertex, 1);

			double localMaxScore = matrix.getScoreForLink(req, vertexIdNameMap.get(subGraph.getMaxId()));

			double validValueSum = maxScore * bonus;
			//double validValueSum = (localMaxScore) * bonus;
			double originValue = matrix.getScoreForLink(req, loneVertexName);
			double nowValue = originValue + validValueSum;
			nowValue = Math.min(nowValue, maxScore);
			matrix.setScoreForLink(req, loneVertexName, nowValue);
		}
	}
	
	private int allSize(Map<String, Set<String>> valid) {
		int amount = 0;
		for(String key:valid.keySet()){
			amount += valid.get(key).size();
		}
		return amount;
	}
	
	private int getRegionCountWhichHasOnlyOneVertex(List<SubGraph> regionList) {
		int bachelorCount = 0;
		for(SubGraph region:regionList) {
			if(region.getVertexList().size()==1) {
				bachelorCount++;
			}
		}
		return bachelorCount;
	}

	private Set<Integer> fillLoneVertex(SubGraph subGraph, Set<Integer> hasVisited,boolean hidden) {
		Set<Integer> regionSet = new HashSet<Integer>();
		regionSet.addAll(subGraph.getVertexList());
		if(hidden) {
			regionSet.addAll(hasVisited);
		}
		Set<Integer> loneVertexSet = new HashSet<Integer>();
		for(int ele:allVertexIdList) {
			if(!regionSet.contains(ele)) {
				loneVertexSet.add(ele);
			}
		}
		return loneVertexSet;
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
	
	private void giveBonusBasedDataGraph(SimilarityMatrix matrix, SubGraph subGraph,
			Set<Integer> curLoneVertexList, String req) {
		Collections.sort(regionList, new SortBySubGraph(vertexIdNameMap, matrix, req));
		int maxId = regionList.get(0).getMaxId();
		double maxScore = matrix.getScoreForLink(req, vertexIdNameMap.get(maxId));

		List<Integer> loneVertexList = fillWithLoneSet(curLoneVertexList);
		Collections.sort(loneVertexList, new SortVertexByScore(vertexIdNameMap, matrix, req));
		for (int loneVertex : loneVertexList) {
			String loneVertexName = vertexIdNameMap.get(loneVertex);
			double bonus = giveBonusForLonePointBasedDataGraph(dataGraphs, subGraph, loneVertex, 1);
			double localMaxScore = matrix.getScoreForLink(req, vertexIdNameMap.get(subGraph.getMaxId()));
			//double validValueSum = (localMaxScore) * bonus;
			double validValueSum = maxScore * bonus;
			
			double originValue = matrix.getScoreForLink(req, loneVertexName);
			double nowValue = originValue + validValueSum;
			nowValue = Math.min(nowValue, maxScore);
			matrix.setScoreForLink(req, loneVertexName, nowValue);
		}
		
	}

	private double giveBonusForLonePointBasedDataGraph(double[][] graphs, SubGraph subGraph, int loneVertex,
			double diffBetweenTopAndCur) {
		double maxBonus = 0;
		for (int vertex : subGraph.getVertexList()) {
			List<List<Integer>> allRoutes = new LinkedList<List<Integer>>();
			List<Integer> curRoute = new LinkedList<Integer>();
			Set<Integer> vertexInGraph = new HashSet<Integer>(subGraph.getVertexList());
			Set<Integer> visited = new HashSet<Integer>();
			visited.add(loneVertex);
			curRoute.add(loneVertex);
			getAllRoutesFromOuterToInnerByDfs(graphs, loneVertex, curRoute, allRoutes, 
					vertexInGraph, visited, vertex,dataRouterLen);
			getAllRoutesFromInnerToOuterByDfs(graphs, loneVertex, curRoute, allRoutes, 
					vertexInGraph, visited, vertex,dataRouterLen);
			double curMaxBonus = 0;
			for (List<Integer> route : allRoutes) {
				double geometryMean = geometricMean(graphs, route);//
				curMaxBonus = Math.max(curMaxBonus, geometryMean);
			}

			maxBonus = Math.max(maxBonus, curMaxBonus);
		}
		return maxBonus;
	}
	
	private void getAllRoutesFromInnerToOuterByDfs(double[][] graphs, int curVertex, List<Integer> curRoute,List<List<Integer>> allRoutes,
			Set<Integer> vertexInGraph, Set<Integer> visited, int target,int routerLen) {
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
	                getAllRoutesFromInnerToOuterByDfs(graphs,i,curRoute,allRoutes,vertexInGraph,visited,target,routerLen);
	                curRoute.remove(curRoute.size()-1);
	                visited.remove(i);
	            }
	     }
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
	
	@Override
	public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset,
			SimilarityMatrix similarityMatrix) {
		return improve(matrix,textDataset);
	}

	@Override
	public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset) {
		return optimizeIRMatrix(matrix,textDataset);
	}

	@Override
	public String getAlgorithmName() {
		return "UD_CallDataTreatEqual"+callThreshold+"_"+dataThreshold;
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
	
	private List<Integer> fillWithLoneSet(Set<Integer> loneVertexSet) {
		List<Integer> list = new LinkedList<Integer>();
		for(int ele:loneVertexSet) {
			list.add(ele);
		}
		return list;
	}


}
