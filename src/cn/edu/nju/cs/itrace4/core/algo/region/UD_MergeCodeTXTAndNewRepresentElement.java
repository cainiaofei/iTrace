package cn.edu.nju.cs.itrace4.core.algo.region;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.core.algo.region.relation.StoreDataSubGraphRemoveEdge;
import cn.edu.nju.cs.itrace4.core.algo.region.relation.SubGraph;
import cn.edu.nju.cs.itrace4.core.algo.region.util.graph.GraphNode;
import cn.edu.nju.cs.itrace4.core.algo.region.util.sort.SortByMergedClass;
import cn.edu.nju.cs.itrace4.core.algo.region.util.sort.SortBySubGraph;
import cn.edu.nju.cs.itrace4.core.algo.region.util.sort.SortVertexByScore;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.core.ir.IRModel;
import cn.edu.nju.cs.itrace4.relation.CallDataRelationGraph;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.relation.graph.CodeEdge;
import cn.edu.nju.cs.itrace4.util.FileProcess;
import cn.edu.nju.cs.itrace4.util.FileProcessTool;
import cn.edu.nju.cs.itrace4.util.FileParse.project.Project;
import cn.edu.nju.cs.itrace4.util.exception.FileException;
import javafx.util.Pair;


public class UD_MergeCodeTXTAndNewRepresentElement extends AlgoBaseOnCodeRegion{
	private int callRouterLen = 4;
	private int dataRouterLen = 2;
	private double[][] callGraphs;
	private double[][] dataGraphs;
	
	
	private List<SubGraph> regionList;
	
	protected Map<Integer, String> vertexIdNameMap;
	private Map<String,Set<String>> valid;
	private int verifyCount;
	private Set<Integer> allVertexIdList = new HashSet<Integer>();
	private boolean hidden = false;//
	
	private double callThreshold;
	private double dataThreshold;
	
	private Map<Integer,Map<Integer,Double>> callRouterCache = new HashMap<Integer,Map<Integer,Double>>();
	private int countThreshold = 2;
	
	//2018.4.19 regard file text in same region as a whole.
	private FileProcess fileProcess = new FileProcessTool();
	private Project project;
	private String codeMergePath;
	private String model;
	//used to merge text which in a same region.
	List<SubGraph> regions;
	
	
	public UD_MergeCodeTXTAndNewRepresentElement(Project project,RelationInfo ri,double callThreshold,
			double dataThreshold, int verifyCount,Map<String,Set<String>> valid,
			String model){
		allVertexIdList = ri.getVertexIdNameMap().keySet();
		this.callThreshold = callThreshold;
		this.dataThreshold = dataThreshold;
		this.project = project;
		this.valid = valid;
		this.verifyCount = verifyCount;
		this.model = model;
		
		vertexIdNameMap = ri.getVertexIdNameMap();
		
		regionList = new StoreDataSubGraphRemoveEdge().getSubGraphs(ri);
		
		//2018.4.19 the region list
		try {
			mergeTXTInSameRegion(regionList);
		} catch (FileException | IOException e) {
			e.printStackTrace();
		}
		
		callGraphs = describeCallGraphWithMatrix(new CallDataRelationGraph(ri,false).callEdgeScoreMap,
				ri.getVertexes().size());
		dataGraphs = describeDataGraphWithMatrix(new CallDataRelationGraph(ri,false).dataEdgeScoreMap,
				ri.getVertexes().size());
		
	}
	
	
	private String mergeTXTInSameRegion(List<SubGraph> subGraphList) throws FileException, IOException {
		regions = new ArrayList<SubGraph>(); 
		for(SubGraph subGraph:subGraphList) {
			List<Integer> classIdList = subGraph.getVertexList();
			if(classIdList.size()>1) {
				regions.add(subGraph);
			}
		}
		String ucPath = project.getUcPath();
		codeMergePath = (new File(ucPath)).getParent()+File.separator+"mergeClass";
		
		File dir = new File(codeMergePath);
		if(!dir.exists()) {
			dir.mkdirs();
		}
		
		String classBase = project.getClassDirPath();
		for(SubGraph region:regions) {
			StringBuilder content = new StringBuilder();
			String className = null;
			for(int id:region.getVertexList()) {
				className = vertexIdNameMap.get(id);
				content.append(" "+fileProcess.getFileConent(classBase+File.separator+className+".txt"));
			}
			String mergeFileName = className+"_"+region.getVertexList().size();
			region.setRegionName(mergeFileName);
			fileProcess.writeFile(codeMergePath+File.separator+mergeFileName+".txt", content.toString());
//			File f = new File(classBase+File.separator+mergeFileName+".txt");
//			f.delete();
		}
		return codeMergePath;
	}
	
	
	/**
	 * @author zzf
	 * @date 2018.4.19
	 * @description compute the similarity between merged code text and requirements. 
	 */
	private SimilarityMatrix compute(TextDataset textDataset, String modelType) {

		Class modelTypeClass = null;
		IRModel irModel = null;
		try {
			modelTypeClass = Class.forName(modelType);
			irModel = (IRModel) modelTypeClass.newInstance();
		} catch (ClassNotFoundException |InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}

		SimilarityMatrix similarityMatrix = irModel.Compute(textDataset.getSourceCollection(),
				textDataset.getTargetCollection());
		return similarityMatrix;
	}
	
	
	public SimilarityMatrix optimizeIRMatrix(SimilarityMatrix matrix, TextDataset textDataset) {
		 
		 SimilarityMatrix oracle = textDataset.getRtm();
		 TextDataset dataset = new TextDataset(project.getUcPath(), codeMergePath, 
	        		project.getRtmClassPath());
		 SimilarityMatrix mergedCodeAndReq = compute(dataset, model);
		 
		 for(String req:matrix.sourceArtifactsIds()){
			List<SubGraph> regionsList = new ArrayList<SubGraph>(regions); 
			Collections.sort(regionsList,new SortByMergedClass(mergedCodeAndReq,req));
			double maxScore = findBiggestValue(matrix,req);
			int index = 1;
			//the count of region which has more than one vertex.
			Set<Integer> hasVisitedRegion = new HashSet<Integer>();
			while(regionsList.size() != 0) {
				//update the local class, the similarity of which and requirement is max in region.
				findBiggestValue(matrix,req);
				SubGraph subGraph = regionsList.get(0);
				List<Integer> vertexList = subGraph.getVertexList();
				GraphNode representNode = getRepresentativeElement(subGraph);
				String representName = representNode.getClassName();
				int representId = representNode.getId();
				
				if(index<=verifyCount){
					if(valid.containsKey(req)){
						valid.get(req).add(representName);
					}
					else{
						valid.put(req, new HashSet<String>());
						valid.get(req).add(representName);
					}
					subGraph.setVisited(req);
				}
				
				if(oracle.isLinkAboveThreshold(req,representName) && index<=verifyCount){//if start
					subGraph.addReq(req);
					List<Integer> temp = new ArrayList<Integer>();
					//temp.add(localMaxId);
					temp.add(representId);
					SubGraph newSubGraph =  new SubGraph(temp);
					
					Map<Integer,Double> outerBonusMap = new HashMap<Integer,Double>();
					for(int vertexId:vertexList) {
						if(vertexId==representId) {
							continue;
						}
						else {
							double outerBonusWeight = getOuterBonus(newSubGraph,vertexId,req);
							outerBonusMap.put(vertexId, outerBonusWeight);
						}
					}
					
					for(int vertexId:vertexList) {
						String vertexName = vertexIdNameMap.get(vertexId);
						double curValue = matrix.getScoreForLink(req, vertexName);
						if(!vertexName.equals(representName)){
							int graphSize = subGraph.getVertexList().size();
							double innerBonus = maxScore/(graphSize);
							double newValue = (curValue + innerBonus)*(1+outerBonusMap.get(vertexId));
							curValue = Math.min(maxScore*0.9999, newValue);
							subGraph.setMaxBonus(Math.max(subGraph.getMaxBonus(), newValue));
						}
						matrix.setScoreForLink(req, vertexName, curValue);
						
					}
					Set<Integer> curLoneVertexList = fillLoneVertex(subGraph,
							hasVisitedRegion,hidden);
					
					giveBonusForLoneNotInThisRegion(matrix, newSubGraph,curLoneVertexList,req);
					hasVisitedRegion.addAll(subGraph.getVertexList());
				}//if end
				index++;
				regionsList.remove(0);
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
	
	/**
	 * @date 2018.4.22
	 * @author zzf
	 * @description choose the code element which has not been called anyone in region
	 * as the representative element. 
	 */
	private GraphNode getRepresentativeElement(SubGraph subGraph) {
		int localMaxId = subGraph.getMaxId();
		String representClassName = vertexIdNameMap.get(localMaxId);
		GraphNode representNode = new GraphNode(localMaxId,representClassName);
		
		List<Integer> vertexList = subGraph.getVertexList();
		List<GraphNode> entranceNodeList = new ArrayList<GraphNode>();
		for(int id:vertexList) {
			GraphNode graphNode = new GraphNode(id,vertexIdNameMap.get(id));
			neighborWithGraphNode(graphNode,vertexList);
			if(graphNode.getCallerList().size()==0) {
				entranceNodeList.add(graphNode);
			}
		}
		
		System.out.println("---------------------------------------");
		int maxLen = 1;
		for(GraphNode curNode:entranceNodeList) {
			int maxLenStartFromCurNode = getMaxCallRouterLength(subGraph,curNode.getId(),
					new HashSet<Integer>());
			if(maxLenStartFromCurNode>maxLen) {
				representNode = curNode;
				maxLen = Math.max(maxLen, maxLenStartFromCurNode);
			}
		}
		return representNode;
	}

	/**
	 * @author zzf
	 * @date 2018.4.23 14:21
	 * @description get the max length of call router which start from curId in region 
	 */
	private int getMaxCallRouterLength(SubGraph region,int curId,Set<Integer> visited) {
		List<Integer> neighborList = region.getVertexList();
		int maxLen = 1;
		for(int neighbor:neighborList) {
			if(callGraphs[curId][neighbor]>0 && !visited.contains(neighbor)) {
				visited.add(neighbor);
				int curLen = 1 + getMaxCallRouterLength(region,neighbor,visited);
				visited.remove(neighbor);
				maxLen = Math.max(maxLen, curLen);
			}
		}
		return maxLen;
	}
	
	/**
	 * @author zzf
	 * @date 2018.4.23
	 * @description calculate the caller class and callee class which are in same region with graphNode
	 */
	private void neighborWithGraphNode(GraphNode graphNode, List<Integer> vertexList) {
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


	private double findBiggestValue(SimilarityMatrix matrix,String requirement) {
		double maxScore = 0;
		for(SubGraph subGraph:regions) {
			int maxId = subGraph.getVertexList().get(0);
			List<Integer> vertexList = subGraph.getVertexList();
			for(int i = 0; i<vertexList.size(); i++){
//				System.out.println(vertexList.get(i));
//				System.out.println(vertexIdNameMap.get(vertexList.get(i)));
				double curScore = matrix.getScoreForLink(requirement,vertexIdNameMap.get(vertexList.get(i)));
				maxScore = Math.max(maxScore, curScore);
				if(Math.abs(curScore-maxScore)<=0.000000000001){
					maxId = vertexList.get(i);
				}
			}
			subGraph.setMaxId(maxId);
		}
		return maxScore;
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

		//	double localMaxScore = matrix.getScoreForLink(req, vertexIdNameMap.get(subGraph.getMaxId()));

			//System.out.println("outBonus:"+bonus);
			
			double validValueSum = maxScore * bonus;
			//double validValueSum = (localMaxScore) * bonus;
			double originValue = matrix.getScoreForLink(req, loneVertexName);
			double nowValue = originValue + validValueSum;
			//2018.1.13
			nowValue = Math.min(nowValue, maxScore);
			//nowValue = Math.min(nowValue, originValue+subGraph.getMaxBonus());
			
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
	
	public int getRegionCountWhichHasOnlyOneVertex(List<SubGraph> regionList) {
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
			//double localMaxScore = matrix.getScoreForLink(req, vertexIdNameMap.get(subGraph.getMaxId()));
			//double validValueSum = (localMaxScore) * bonus;
			//System.out.println("outer bonus:"+bonus);
			
			double validValueSum = maxScore * bonus;
			
			double originValue = matrix.getScoreForLink(req, loneVertexName);
			double nowValue = originValue + validValueSum;
			
			//2018.1.13
			nowValue = Math.min(nowValue, maxScore);
			//nowValue = Math.min(nowValue, originValue+subGraph.getMaxBonus());
			
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
		return "UD_MergeCodeTXTAndNewRepresentElement"+callThreshold+"_"+dataThreshold;
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
