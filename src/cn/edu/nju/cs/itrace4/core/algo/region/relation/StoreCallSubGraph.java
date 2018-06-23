package cn.edu.nju.cs.itrace4.core.algo.region.relation;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.relation.graph.CodeEdge;
import cn.edu.nju.cs.itrace4.relation.graph.CodeVertex;
import edu.uci.ics.jung.graph.Graph;

public class StoreCallSubGraph extends StoreSubGraphInfoByThreshold{
	private MyCallDataRelationGraph myCdGraph;
	
	@Override
	public List<SubGraph> getSubGraphs(RelationInfo ri){
		myCdGraph = new MyCallDataRelationGraph(ri);
        //会根据阈值进行pruning
        Graph<CodeVertex, CodeEdge> prunedGraph = myCdGraph.getDirGraph();
        //序号和类名对应关系
        Map<Integer, String> vertexIdNameMap = ri.getVertexIdNameMap();
        //还是不直接在原来的代码上改了  因为涉及到反序列化  如果该的话会导致需要重新序列化
        //用矩阵来存储这张图    map序号和类名的对应关系是从1开始的
        int[][] graphs = new int[vertexIdNameMap.size()+1][vertexIdNameMap.size()+1];
        
        buildGraph(graphs,prunedGraph);
        //遍历出所有子图
        List<SubGraph> subGraphList = new LinkedList<SubGraph>();
        //防止重复
        char[] flag = new char[vertexIdNameMap.size()+1];
        for(int i = 1; i < graphs.length;i++){
       	  	if(flag[i]!='X'){
       	  		getSubGrapByBfs(graphs,i,subGraphList,flag);
       	  	}
        }
        //要对这个子图进行拓展
        return subGraphList;
	}
}
