package cn.edu.nju.cs.itrace4.demo.algo.outerVertex;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.demo.relation.SubGraph;

public class OuterVertexProcessWithCallSubGraph extends
	OuterVertexProcess{
	
	private Map<Integer,String> vertexIdNameMap; 
	private double[][] graphs;
	
	public OuterVertexProcessWithCallSubGraph(Map<Integer, String> vertexIdNameMap,double[][] graphs) {
		super(vertexIdNameMap);
		this.vertexIdNameMap = vertexIdNameMap;
		this.graphs = graphs;
	}
	
	public int getOuterSizeConnectWithInner(Set<Integer> loneVertexSet, SubGraph subGraph) {
		int outerSize = 0;
		for(int curVertex:loneVertexSet){
			double bonus = giveBonusForLonePoint(graphs,subGraph,curVertex,1);
			if(bonus!=0){
				outerSize++;
			}
		}
		return outerSize;
	}
	
	public double giveBonusForLonePoint(double[][] graphs, SubGraph subGraph, int loneVertex,double diffBetweenTopAndCur) {
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
	
	
	public double geometricMean(double[][] graphs, List<Integer> route) {
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
		//double geometryMean = Math.pow(res, 1.0/(routes.length-1));
		double geometryMean = Math.pow(res, 1.0/(1));
		return geometryMean;
	}

	
	public void getAllRoutesFromOuterToInnerByDfs(double[][] graphs, int curVertex, List<Integer> curRoute,List<List<Integer>> allRoutes,
			Set<Integer> vertexInGraph, Set<Integer> visited, int target) {
		 if(curVertex==target){
	            allRoutes.add(new LinkedList<Integer>(curRoute));
	     }
	     else if(vertexInGraph.contains(curVertex)||curRoute.size()==6){
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
	
	
	public void getAllRoutesFromInnerToOuterByDfs(double[][] graphs, int curVertex, List<Integer> curRoute,List<List<Integer>> allRoutes,
			Set<Integer> vertexInGraph, Set<Integer> visited, int target) {
		 if(curVertex==target){
	            allRoutes.add(new LinkedList<Integer>(curRoute));
	     }
	     else if(vertexInGraph.contains(curVertex)||curRoute.size()==6){
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

	/*
	 *  
	 */
	public double getBonusForLonePoint(double[][] graphs, SubGraph subGraph, int loneVertex,double diffBetweenTopAndCur) {
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
	
	public void processOuterVertexWithDataSubGraph(SimilarityMatrix matrix,SimilarityMatrix matrix_ud){
	}
}
