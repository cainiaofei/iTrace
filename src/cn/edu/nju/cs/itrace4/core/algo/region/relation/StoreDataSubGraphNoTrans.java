package cn.edu.nju.cs.itrace4.core.algo.region.relation;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.relation.graph.CodeEdge;
import cn.edu.nju.cs.itrace4.relation.graph.CodeVertex;
import edu.uci.ics.jung.graph.Graph;

public class StoreDataSubGraphNoTrans extends StoreSubGraphInfoByThreshold{
	private MyCallDataRelationGraph myCdGraph;

	public List<SubGraph> getSubGraphs(RelationInfo ri,Map<Integer,Set<Integer>>
		vertexMapDataRelated){
		
		myCdGraph = new MyCallDataRelationGraph(ri);
        //会根据阈值进行pruning
        Graph<CodeVertex, CodeEdge> prunedGraph = myCdGraph.getUnDirGraph();
        //序号和类名对应关系
        Map<Integer, String> vertexIdNameMap = ri.getVertexIdNameMap();
        //还是不直接在原来的代码上改了  因为涉及到反序列化  如果该的话会导致需要重新序列化
        //用矩阵来存储这张图    map序号和类名的对应关系是从1开始的
        int[][] graphs = new int[vertexIdNameMap.size()+1][vertexIdNameMap.size()+1];
        
        Set<Set<Integer>> set = new HashSet<Set<Integer>>();
        buildGraph(graphs,prunedGraph);
        //点  与之存在代码依赖的点集合
        for(int i= 1; i < graphs.length;i++){
        	Set<Integer> dataRelated = new HashSet<Integer>();
        	dataRelated.add(i);
        	for(int j = 1; j < graphs.length;j++){
        		if(graphs[i][j]==1){
        			dataRelated.add(j);
        		}
        	}
        	
        	if(dataRelated.size()==1){
        		continue;
        	}
        	else{
        		set.add(dataRelated);
        		vertexMapDataRelated.put(i, dataRelated);
        	}
        }
        
        List<SubGraph> subGraphList = new LinkedList<SubGraph>();
        for(Set<Integer> innerSet:set){
        	List<Integer> list = new LinkedList<Integer>();
        	for(int ele:innerSet){
        		list.add(ele);
        	}
        	subGraphList.add(new SubGraph(list));
        }
        
        return subGraphList;
	}

	private void getSubGrap(int[][] graphs, int curVertex, List<SubGraph> subGraphList,char[] flag) {
		List<Integer> vertexList = new LinkedList<Integer>();
		vertexList.add(curVertex);
		for(int i = 1; i < graphs.length;i++){
			if(graphs[curVertex][i]==1&&flag[i]!='X'){
				flag[i] = 'X';
				vertexList.add(i);
			}
		}
		subGraphList.add(new SubGraph(vertexList));
	}
}
