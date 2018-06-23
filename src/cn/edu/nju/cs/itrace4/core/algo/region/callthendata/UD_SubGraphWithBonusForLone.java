package cn.edu.nju.cs.itrace4.core.algo.region.callthendata;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.CSTI;
import cn.edu.nju.cs.itrace4.core.algo.region.relation.StoreCallAndDataSubGraphByThreshold;
import cn.edu.nju.cs.itrace4.core.algo.region.relation.SubGraph;
import cn.edu.nju.cs.itrace4.core.algo.region.util.sort.SortBySubGraph;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.core.document.StringHashSet;
import cn.edu.nju.cs.itrace4.relation.CallDataRelationGraph;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.relation.graph.CodeEdge;
import javafx.util.Pair;

public class UD_SubGraphWithBonusForLone implements CSTI{

	double callThreshold, dataThreshold;
	private double[][] callGraphs;
	private double[][] dataGraphs;
	protected List<SubGraph> subGraphList;
	protected Map<Integer, String> vertexIdNameMap;
	private Set<Integer> loneVertexSet = new HashSet<Integer>();
	
	public UD_SubGraphWithBonusForLone(RelationInfo ri){
		this.callThreshold = ri.getCallEdgeScoreThreshold();
		this.dataThreshold = ri.getDataEdgeScoreThreshold();
		this.subGraphList = new StoreCallAndDataSubGraphByThreshold().getSubGraphs(ri);
		this.vertexIdNameMap = ri.getVertexIdNameMap();
		fillLoneVertex(loneVertexSet,subGraphList);
		callGraphs = describeGraphWithMatrix(new CallDataRelationGraph(ri).callEdgeScoreMap,ri.getVertexes().size());
		dataGraphs = describeGraphWithMatrix(new CallDataRelationGraph(ri).callEdgeScoreMap,ri.getVertexes().size());
	}
	
	@Override
	public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset,
			SimilarityMatrix similarityMatrix) {
		 SimilarityMatrix oracle = textDataset.getRtm();
		 SimilarityMatrix matrix_ud = new SimilarityMatrix();
		 //每个需求都会和剩下的所以类进行相似度计算  所以这里随便选择一个类就可以
		 String firstKey = getFirstKey(matrix.sourceArtifactsIds());
		 //因为是选取的一部分类 这里是防止有些类 不是被选中的
		 filterSubGraphsList(matrix.getLinksForSourceId(firstKey).keySet());
		 // 对于每个需求会有一个对应的排序
		 for(String req:matrix.sourceArtifactsIds()){
			//排序 这一步其实有没有都行（不管怎么样都是每一组选一个）
			Collections.sort(subGraphList,new SortBySubGraph(vertexIdNameMap,matrix,req));
			int maxId = subGraphList.get(0).getMaxId();
			double maxScore = matrix.getScoreForLink(req, vertexIdNameMap.get(maxId));
			for(SubGraph subGraph:subGraphList){
				List<Integer> vertexList = subGraph.getVertexList();
				
				if(vertexList.size()==1&&!hasContainedThisLink(matrix_ud, req, vertexList.get(0))){
					matrix_ud.addLink(req, vertexIdNameMap.get(vertexList.get(0)),
							matrix.getScoreForLink(req, vertexIdNameMap.get(vertexList.get(0))));
					continue;
				}
				if(vertexList.size()==1){///////琚潙姝讳簡
					continue;
				}
				
				//找到最大的那个
				String represent = vertexIdNameMap.get(subGraph.getMaxId());
				double representValue = matrix.getScoreForLink(req, represent);
				if(oracle.isLinkAboveThreshold(req,represent)){
					for(int id:vertexList){
						if(hasContainedThisLink(matrix_ud,req,id)){
							matrix_ud.setScoreForLink(req, vertexIdNameMap.get(id), 
									Math.max(representValue, matrix_ud.getScoreForLink(req, vertexIdNameMap.get(id))));
						}
						else{
							double curValue = matrix.getScoreForLink(req, vertexIdNameMap.get(id));
							if(curValue==representValue){
								curValue = Math.min(representValue, curValue+(representValue)/vertexList.size());
							}
							else{
								curValue = Math.min(maxScore, curValue+(maxScore)/vertexList.size());
							}
							matrix_ud.addLink(req, vertexIdNameMap.get(id),curValue);//
						}
					}
					///give bonus for relevant lone vertex
					//int index = 1;
					for(int loneVertex:loneVertexSet){
						//System.out.println(req+":"+index+"  all size:"+loneVertexSet.size());
						//index++;
						if(!hasContainedThisLink(matrix,req,loneVertex)){
							continue;
						}
						double curValue = matrix.getScoreForLink(req, vertexIdNameMap.get(loneVertex));
						double bonus = giveBonusForLonePoint(callGraphs,subGraph,loneVertex,representValue-curValue); 
						if(hasContainedThisLink(matrix_ud, req, loneVertex)){
							//curValue = matrix_ud.getScoreForLink(req, vertexIdNameMap.get(loneVertex));
							matrix_ud.setScoreForLink(req, vertexIdNameMap.get(loneVertex),bonus+curValue);
						}
						else{
							matrix_ud.addLink(req, vertexIdNameMap.get(loneVertex),bonus+curValue);
						}
						
						curValue = matrix_ud.getScoreForLink(req, vertexIdNameMap.get(loneVertex));
						bonus = giveDataBonusForLonePoint(dataGraphs,subGraph,loneVertex,representValue-curValue); 
						matrix_ud.setScoreForLink(req, vertexIdNameMap.get(loneVertex),bonus+curValue);
					}
				}
				else{
					for(int id:vertexList){
						double curValue = matrix.getScoreForLink(req, vertexIdNameMap.get(id));
						if(hasContainedThisLink(matrix_ud,req,id)){
							matrix_ud.setScoreForLink(req, vertexIdNameMap.get(id), 
									Math.max(curValue, matrix_ud.getScoreForLink(req, vertexIdNameMap.get(id))));
						}
						else{
							if(curValue==representValue){
								;
							}
							else{
								//curValue = Math.max(0, curValue-(maxScore/vertexList.size()));
							}
							matrix_ud.addLink(req, vertexIdNameMap.get(id),curValue);//
						}
					}
				}
			}///
		}//for
		LinksList allLinks = matrix_ud.allLinks();
		Collections.sort(allLinks, Collections.reverseOrder());
		SimilarityMatrix res = new SimilarityMatrix();
		for(SingleLink link:allLinks){
			res.addLink(link.getSourceArtifactId(), link.getTargetArtifactId(),link.getScore());
			System.out.println(link.getSourceArtifactId()+" "+link.getTargetArtifactId()+" "+link.getScore());
		}
		//print(res);
		return res;
	}
	
	
	private double giveDataBonusForLonePoint(double[][] graphs, SubGraph subGraph, 
			int loneVertex, double diffBetweenTopAndCur) {
		double max = 0;
		for(int innerPoint:subGraph.getVertexList()){
			if(graphs[innerPoint][loneVertex]!=0){
				max = Math.max(max, graphs[loneVertex][innerPoint]);
			}
		}
		return max*diffBetweenTopAndCur;
	}
	
	protected boolean hasContainedThisLink(SimilarityMatrix matrix, String req, int id) {
		String codeTarget = vertexIdNameMap.get(id);
		if(matrix.sourceArtifactsIds().contains(req)&&
				matrix.getLinksForSourceId(req).containsKey(codeTarget)){
			return true;
		}
		return false;
	}

	private void print(SimilarityMatrix matrix) {
		for(SingleLink singleLink:matrix.allLinks()){
			String sourceId = singleLink.getSourceArtifactId();
			String targetId = singleLink.getTargetArtifactId();
			double score = singleLink.getScore();
			System.out.println(sourceId+"  "+targetId+"  "+score);
		}
	}

	protected String getFirstKey(StringHashSet sourceArtifactsIds) {
		Iterator<String> ite = sourceArtifactsIds.iterator();
		return ite.next();
	}

	public void filterSubGraphsList(Set<String> set){
		for(SubGraph subGraph:subGraphList){
			List<Integer> vertexList = subGraph.getVertexList();
			Iterator<Integer> ite = vertexList.iterator();
			while(ite.hasNext()){
				if(!set.contains(vertexIdNameMap.get(ite.next()))){
					ite.remove();
				}
			}
		}
		
		Iterator<SubGraph> subGraphIte = subGraphList.iterator();
		while(subGraphIte.hasNext()){
			if(subGraphIte.next().getVertexList().size()==0){
				subGraphIte.remove();
			}
		}
	}

	private void fillLoneVertex(Set<Integer> loneVertexSet, List<SubGraph> callSubGraphList) {
		for(SubGraph subGraph:callSubGraphList){
			if(subGraph.getVertexList().size()==1){
				loneVertexSet.add(subGraph.getVertexList().get(0));
			}
		}
	}
	
	
	private double[][] describeGraphWithMatrix(Map<CodeEdge, Double> callEdgeScoreMap, int size) {
		double[][] matrix = new double[size+1][size+1];
		for(CodeEdge edge:callEdgeScoreMap.keySet()){
			int callerId = edge.getSource().getId();
			int calleeId = edge.getTarget().getId();
			double score = callEdgeScoreMap.get(edge);
			matrix[callerId][calleeId] = score;
		}
		return matrix;
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
            //getAllRoutesFromOuterToInnerByDfs(graphs,loneVertex,curRoute,allRoutes,vertexInGraph,visited,vertex); 
            getAllRoutesFromInnerToOuterByDfs(graphs,loneVertex,curRoute,allRoutes,vertexInGraph,visited,vertex); 
            for(List<Integer> route:allRoutes){
           	 double geometryMean = geometricMean(graphs,route);//
           	 maxBonus = Math.max(maxBonus, diffBetweenTopAndCur*geometryMean);
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
				res *= graphs[one][other];
			}
			else{
				res *= graphs[other][one];
			}
		}
		double geometryMean = Math.pow(res, 1.0/(routes.length-1));
		//System.out.println("geometryMean:"+geometryMean+"--->"+(routes.length));
		return geometryMean;
	}


	private void getAllRoutesFromOuterToInnerByDfs(double[][] graphs, int curVertex, List<Integer> curRoute,List<List<Integer>> allRoutes,
			Set<Integer> vertexInGraph, Set<Integer> visited, int target) {
		 if(curVertex==target){
	            allRoutes.add(new LinkedList<Integer>(curRoute));
	     }
	     else if(vertexInGraph.contains(curVertex)||curRoute.size()==2){
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
	     else if(vertexInGraph.contains(curVertex)||curRoute.size()==2){
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


	
	@Override
	public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAlgorithmName() {
		// TODO Auto-generated method stub
		return "UD_SubGraphWithBonusForLone"+callThreshold+"_"+dataThreshold;
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
