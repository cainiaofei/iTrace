package cn.edu.nju.cs.itrace4.demo.algo;

import java.util.Collections;
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
import cn.edu.nju.cs.itrace4.demo.relation.StoreCallSubGraph;
import cn.edu.nju.cs.itrace4.demo.relation.SubGraph;
import cn.edu.nju.cs.itrace4.relation.CallDataRelationGraph;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.relation.graph.CodeEdge;
import javafx.util.Pair;

public class UD_Percent_CallSubGraphWithBonusForLone implements CSTI{

	private double[][] graphs;
	private List<SubGraph> callSubGraphList;
	protected Map<Integer, String> vertexIdNameMap;
	private Set<Integer> loneVertexSet = new HashSet<Integer>();
	double percent;
	
	public UD_Percent_CallSubGraphWithBonusForLone(RelationInfo ri,double percent){
		callSubGraphList = new StoreCallSubGraph().getSubGraphs(ri);
		graphs = describeGraphWithMatrix(new CallDataRelationGraph(ri).callEdgeScoreMap,ri.getVertexes().size());
		vertexIdNameMap = ri.getVertexIdNameMap();
		this.percent = percent;
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


	@Override
	public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset,
			SimilarityMatrix similarityMatrix) {
		 SimilarityMatrix oracle = textDataset.getRtm();
		 SimilarityMatrix matrix_ud = new SimilarityMatrix();
		 //姣忎釜闇�姹傞兘浼氬拰鍓╀笅鐨勬墍浠ョ被杩涜鐩镐技搴﹁绠�  鎵�浠ヨ繖閲岄殢渚块�夋嫨涓�涓被灏卞彲浠�
		 String firstKey = getFirstKey(matrix.sourceArtifactsIds());
		 //鍥犱负鏄�夊彇鐨勪竴閮ㄥ垎绫� 杩欓噷鏄槻姝㈡湁浜涚被 涓嶆槸琚�変腑鐨�
		 filterSubGraphsList(matrix.getLinksForSourceId(firstKey).keySet());
		 fillLoneVertex(loneVertexSet,callSubGraphList);
		 // 瀵逛簬姣忎釜闇�姹備細鏈変竴涓搴旂殑鎺掑簭
		 for(String req:matrix.sourceArtifactsIds()){
			//鎺掑簭 杩欎竴姝ュ叾瀹炴湁娌℃湁閮借锛堜笉绠℃�庝箞鏍烽兘鏄瘡涓�缁勯�変竴涓級
			Collections.sort(callSubGraphList,new SortBySubGraph(vertexIdNameMap,matrix,req));
			int maxId = callSubGraphList.get(0).getMaxId();
			double maxScore = matrix.getScoreForLink(req, vertexIdNameMap.get(maxId));
			
			int subGraphAmount = callSubGraphList.size();
			int index = 1;
			for(SubGraph subGraph:callSubGraphList){//////
				List<Integer> vertexList = subGraph.getVertexList();
				
				if(vertexList.size()==1&&!hasContainedThisLink(matrix_ud, req, vertexList.get(0))){
					matrix_ud.addLink(req, vertexIdNameMap.get(vertexList.get(0)),
							matrix.getScoreForLink(req, vertexIdNameMap.get(vertexList.get(0))));
					continue;
				}
				if(vertexList.size()==1){///////琚潙姝讳簡
					continue;
				}
				//鎵惧埌鏈�澶х殑閭ｄ釜
				String represent = vertexIdNameMap.get(subGraph.getMaxId());
				double representValue = matrix.getScoreForLink(req, represent);
				if(index<(subGraphAmount*percent)||oracle.isLinkAboveThreshold(req,represent)){
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
								curValue = Math.min(maxScore, curValue+(maxScore)/vertexList.size()*2);
							}
							matrix_ud.addLink(req, vertexIdNameMap.get(id),curValue);//
						}
					}
					
					/**
					 * 缁欏崟涓殑鐐筨onus  鏈夊彲鑳借繖涓摼鎺ヨ繕娌℃湁
					 **/
					//int index = 1;
					for(int loneVertex:loneVertexSet){
						double curValue = matrix.getScoreForLink(req, vertexIdNameMap.get(loneVertex));
						double bonus = giveBonusForLonePoint(graphs,subGraph,loneVertex,representValue-curValue); 
						if(hasContainedThisLink(matrix_ud, req, loneVertex)){
					       double preValue = matrix_ud.getScoreForLink(req, vertexIdNameMap.get(loneVertex));
					       curValue = Math.max(preValue,curValue+bonus) - bonus;
						   matrix_ud.setScoreForLink(req, vertexIdNameMap.get(loneVertex),curValue+bonus);
						}
						else{
							matrix_ud.addLink(req, vertexIdNameMap.get(loneVertex),bonus+curValue);
						}
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
								curValue = Math.max(0, curValue-(maxScore/vertexList.size()));
							}
							matrix_ud.addLink(req, vertexIdNameMap.get(id),curValue);//
						}
					}
				}
				index++;
			}///
		}//for
		LinksList allLinks = matrix_ud.allLinks();
		Collections.sort(allLinks, Collections.reverseOrder());
		SimilarityMatrix res = new SimilarityMatrix();
		for(SingleLink link:allLinks){
			res.addLink(link.getSourceArtifactId(), link.getTargetArtifactId(),link.getScore());
			//System.out.println(link.getSourceArtifactId()+" "+link.getTargetArtifactId()+" "+link.getScore());
		}
		//print(res);
		return res;
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
	     else if(vertexInGraph.contains(curVertex)||curRoute.size()==1){
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
