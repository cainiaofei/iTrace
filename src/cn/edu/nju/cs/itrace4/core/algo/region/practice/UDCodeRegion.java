package cn.edu.nju.cs.itrace4.core.algo.region.practice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.CSTI;
import cn.edu.nju.cs.itrace4.core.algo.region.relation.StoreDataSubGraphRemoveEdge;
import cn.edu.nju.cs.itrace4.core.algo.region.relation.SubGraph;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.relation.CallDataRelationGraph;
import cn.edu.nju.cs.itrace4.relation.RelationGraph;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.relation.graph.CodeEdge;
import cn.edu.nju.cs.itrace4.relation.graph.CodeVertex;
import javafx.util.Pair;

public class UDCodeRegion implements CSTI{
	private int callRouterLen = 4;
	private int dataRouterLen = 2;
	private double[][] callGraphs;
	private double[][] dataGraphs;
	
	private List<SubGraph> regionList;
	
	protected Map<Integer, String> vertexIdNameMap;
	protected Map<String, Integer> vertexNameIdMap = new HashMap<String,Integer>();
	
	private Set<Integer> allVertexIdList = new HashSet<Integer>();
	
	private double callThreshold;
	private double dataThreshold;
	
	private Map<Integer,Map<Integer,Double>> callRouterCache = new HashMap<Integer,Map<Integer,Double>>();
	
	//used to merge text which in a same region.
	List<SubGraph> regions;
	private RelationGraph relationGraph;
	
	public UDCodeRegion(RelationInfo ri,double callThreshold,
			double dataThreshold){
		allVertexIdList = ri.getVertexIdNameMap().keySet();
		this.callThreshold = callThreshold;
		this.dataThreshold = dataThreshold;
		relationGraph = new CallDataRelationGraph(ri);
		vertexIdNameMap = ri.getVertexIdNameMap();
	    reverse(vertexIdNameMap,vertexNameIdMap);
		regionList = new StoreDataSubGraphRemoveEdge().getSubGraphs(ri);
		callGraphs = describeCallGraphWithMatrix(new CallDataRelationGraph(ri,false).callEdgeScoreMap,
				ri.getVertexes().size());
		dataGraphs = describeDataGraphWithMatrix(new CallDataRelationGraph(ri,false).dataEdgeScoreMap,
				ri.getVertexes().size());
	}
	
	
	private void reverse(Map<Integer, String> vertexIdNameMap, Map<String, Integer> vertexNameIdMap) {
		for(int id:vertexIdNameMap.keySet()) {
			vertexNameIdMap.put(vertexIdNameMap.get(id), id);
		}
	}

	public SimilarityMatrix optimizeIRMatrix(SimilarityMatrix matrix, TextDataset textDataset) {
		 SimilarityMatrix oracle = textDataset.getRtm();
		 LinksList resultLinks = new LinksList();
		 LinksList originLinks = matrix.allLinks();
		 int i = originLinks.size();
		 //store max score for every requirement.
		 Map<String,Double> maxScoreForReq = new HashMap<String,Double>();
		 //2018.5.17 represent link by map
		 Map<String,Map<String,Double>> linkMap = matrix.getMatrix();
		 while (originLinks.size() != 0) {////////////////////////
			 SingleLink link = originLinks.get(0);
			 originLinks.remove(0);
			 String source = link.getSourceArtifactId();
			 String target = link.getTargetArtifactId();
			 double score = link.getScore();
			 linkMap.get(source).remove(target);

			 if(!maxScoreForReq.containsKey(source)) {
				 maxScoreForReq.put(source, 0.0);
			 }
			 maxScoreForReq.put(source, Math.max(maxScoreForReq.get(source), score));

			 if (oracle.isLinkAboveThreshold(source, target)) {
				 SubGraph subGraph = getRegionRepresent(source,target,regionList);
				 if(subGraph!=null) {
					 clusterProcess(source,target,subGraph,matrix,maxScoreForReq.get(source));
				 }
				 else {
					 List<Integer> cur = new LinkedList<Integer>();
					 cur.add(this.vertexNameIdMap.get(target));
					 subGraph = new SubGraph(cur);
					 //udProcess(source,target,matrix,bonus);
					 clusterProcess(source,target,subGraph,matrix,maxScoreForReq.get(source));
				 }
			 }
			 resultLinks.add(new SingleLink(source, target, score+i));
			 originLinks = matrix.allLinks();
			 Collections.sort(originLinks, Collections.reverseOrder());
			 i--;
		 }///////////////////外层while loop
		 
		SimilarityMatrix res = new SimilarityMatrix();
		for(SingleLink link:resultLinks){
			res.addLink(link.getSourceArtifactId(), link.getTargetArtifactId(),link.getScore());
		}
		return res;
	}
	
	private void clusterProcess(String source,String target,SubGraph subGraph,SimilarityMatrix originMatrix,
			double maxScore) {
		int representId = vertexNameIdMap.get(target);
		List<Integer> temp = new ArrayList<Integer>();
		temp.add(representId);
		SubGraph newSubGraph = new SubGraph(temp);
		
		Map<Integer,Double> outerBonusMap = new HashMap<Integer,Double>();
		for(int vertexId:subGraph.getVertexList()) {
			if(vertexId==representId) {
				continue;
			}
			double outerBonusWeight = getOuterBonus(newSubGraph,vertexId,source);
			outerBonusMap.put(vertexId, outerBonusWeight);
		}
		
		List<Integer> vertexList = subGraph.getVertexList();
		for(int vertexId:vertexList) {
			String vertexName = vertexIdNameMap.get(vertexId);
			if(originMatrix.getScoreForLink(source, vertexName)<0.0) {
				continue;
			}
			double curValue = originMatrix.getScoreForLink(source, vertexName);
			int graphSize = subGraph.getVertexList().size();
			double innerBonus = maxScore/(graphSize);
			double newValue = (curValue + innerBonus)*(1+outerBonusMap.get(vertexId));
			curValue = Math.min(maxScore*0.9999, newValue);
			subGraph.setMaxBonus(Math.max(subGraph.getMaxBonus(), newValue));
			originMatrix.setScoreForLink(source, vertexName, curValue);
		}
		Set<Integer> curLoneVertexList = generateLoneVertexList(subGraph,originMatrix,source);
		giveBonusForLoneNotInThisRegion(originMatrix, newSubGraph,curLoneVertexList,source,maxScore);
	}

	private Set<Integer> generateLoneVertexList(SubGraph subGraph,SimilarityMatrix originMatrix,String req) {
		Set<Integer> set = new HashSet<Integer>();
		for(int id:vertexIdNameMap.keySet()) {
			String className = vertexIdNameMap.get(id);
			if(!subGraph.getVertexList().contains(id) &&
					originMatrix.getScoreForLink(req, className)!=null) {
				set.add(id);
			}
		}
		return set;
	}

	/**
	 * @date 2018.5.17
	 * @author zzf
	 * @description whether this element(target) is a represent class of region? If yes, return this region; else return null.
	 */
	private SubGraph getRegionRepresent(String source, String target, List<SubGraph> regionList) {
		int id = this.vertexNameIdMap.get(target);
		for(SubGraph region:regionList) {
			if(region.getVertexList().contains(id) && !region.isVisited(source)) {
				region.setVisited(source);
				return region;
			}
		}
		return null;
	}


	private void udProcess(String source,String target, SimilarityMatrix matrix,double bonus) {
		 List<CodeVertex> neighbours = ((CallDataRelationGraph) relationGraph).getNeighboursByCall(target);
         for (CodeVertex nb : neighbours) {
        	 double originScore = matrix.getScoreForLink(source, nb.getName());
             if (originScore != -1) {
            	 matrix.setScoreForLink(source, target, originScore * (1 + bonus));
             }
         }
	}

	private double getOuterBonus(SubGraph subGraph, int vertexId, String req) {
		double callBonus = giveBonusForLonePointBasedCallGraph(callGraphs, subGraph, vertexId, 1);
		double dataBonus = giveBonusForLonePointBasedDataGraph(dataGraphs, subGraph, vertexId, 1);
		return callBonus + dataBonus;
	}


	public double showClosenessInGraph(SubGraph subGraph) {
		List<Double> list = new LinkedList<Double>();
		int[] nums = new int[subGraph.getVertexList().size()];
		for(int i = 0; i < nums.length;i++) {
			nums[i] = subGraph.getVertexList().get(i);
		}
		for(int i = 0; i < nums.length;i++) {
			for(int j = 0; j < nums.length;j++) {
//				System.out.println(nums[i]+"-----"+nums[j]);
//				System.out.println("call:"+callGraphs[nums[i]][nums[j]]+
//						"data:"+dataGraphs[nums[i]][nums[j]]);
				list.add(callGraphs[nums[i]][nums[j]]);
				list.add(dataGraphs[nums[i]][nums[j]]);
			}
		}//outer for
		Collections.sort(list, Collections.reverseOrder());
		double sum = 0;
		for(int i = 0; i < nums.length-1;i++) {
			sum += list.get(i);
		}
		return sum / (nums.length-1);
	}


	private void giveBonusForLoneNotInThisRegion(SimilarityMatrix matrix, SubGraph subGraph,
			Set<Integer> curLoneVertexList,String req,double maxScore) {
		giveBonusBasedCallGraph(matrix, subGraph, curLoneVertexList,req,maxScore);
		giveBonusBasedDataGraph(matrix, subGraph, curLoneVertexList,req,maxScore);
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
			Set<Integer> curLoneVertexList, String req,double maxScore) {
		List<Integer> loneVertexList = fillWithLoneSet(curLoneVertexList);
		for (int loneVertex : loneVertexList) {
			String loneVertexName = vertexIdNameMap.get(loneVertex);
			double bonus = giveBonusForLonePointBasedCallGraph(callGraphs, subGraph, loneVertex, 1);
			double validValueSum = maxScore * bonus;
			double originValue = matrix.getScoreForLink(req, loneVertexName);
			double nowValue = originValue + validValueSum;
			//2018.1.13
			nowValue = Math.min(nowValue, maxScore);
			if(matrix.getScoreForLink(req, loneVertexName)<0.0) {
				return;
			}
			matrix.setScoreForLink(req, loneVertexName, nowValue);
		}
	}
	
	public int getRegionCountWhichHasOnlyOneVertex(List<SubGraph> regionList) {
		int bachelorCount = 0;
		for(SubGraph region:regionList) {
			if(region.getVertexList().size()==1) {
				bachelorCount++;
			}
		}
		return bachelorCount;
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
			Set<Integer> curLoneVertexList, String req,double maxScore) {
		List<Integer> loneVertexList = fillWithLoneSet(curLoneVertexList);
		for (int loneVertex : loneVertexList) {
			String loneVertexName = vertexIdNameMap.get(loneVertex);
			double bonus = giveBonusForLonePointBasedDataGraph(dataGraphs, subGraph, loneVertex, 1);
			double validValueSum = maxScore * bonus;
			double originValue = matrix.getScoreForLink(req, loneVertexName);
			double nowValue = originValue + validValueSum;
			//2018.1.13
			nowValue = Math.min(nowValue, maxScore);
			if(matrix.getScoreForLink(req, loneVertexName)<0.0) {
				return;
			}
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
		return "UD_CodeTextAsWholeInRegion"+callThreshold+"_"+dataThreshold;
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
	
	 private double getAdaptiveBonus(SimilarityMatrix matrix) {
	        List<Double> arrayScore = new ArrayList<>();
	        for (String source : matrix.sourceArtifactsIds()) {
	            Map<String, Double> links = matrix.getLinksForSourceId(source);

	            List<Double> valuesList = new ArrayList<>();

	            for (double value : links.values()) {
	                valuesList.add(value);
	            }

	            Collections.sort(valuesList, Collections.reverseOrder());
	            arrayScore.add((valuesList.get(0) - valuesList.get(valuesList.size() - 1)) / 2.0);
	        }

	        Collections.sort(arrayScore);
	        double median;
	        if (arrayScore.size() % 2 == 0)
	            median = ((double) arrayScore.get(arrayScore.size() / 2) + (double) arrayScore.get(arrayScore.size() / 2 - 1)) / 2;
	        else
	            median = (double) arrayScore.get(arrayScore.size() / 2);

	        return median;
	    }

}
