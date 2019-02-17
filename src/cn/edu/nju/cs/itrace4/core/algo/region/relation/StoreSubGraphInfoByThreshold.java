package cn.edu.nju.cs.itrace4.core.algo.region.relation;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import cn.edu.nju.cs.itrace4.relation.CallDataRelationGraph;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.relation.graph.CodeEdge;
import cn.edu.nju.cs.itrace4.relation.graph.CodeVertex;
import edu.uci.ics.jung.graph.Graph;

public class StoreSubGraphInfoByThreshold {
	
	private CallDataRelationGraph cdGraph;
	private double callThreshold ;
	private double dataThreshold ;
	
	public StoreSubGraphInfoByThreshold() {}
	
	public StoreSubGraphInfoByThreshold(double callThreshold,double dataThreshold) {
		this.callThreshold = callThreshold;
		this.dataThreshold = dataThreshold;
	}
	
	public List<SubGraph> getSubGraphs(RelationInfo ri){
        //会根据阈值进行pruning
        cdGraph = new CallDataRelationGraph(ri);
        
        Graph<CodeVertex, CodeEdge> prunedGraph = cdGraph.getPrunedGraph();
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
        return subGraphList;
	}
	

	public void getSubGrapByBfs(int[][] graphs, int curVertex, List<SubGraph> subGraphList,char[] flag) {
		Queue<Integer> queue = new LinkedList<Integer>();
		List<Integer> subGraph = new LinkedList<Integer>();
		queue.add(curVertex);
		flag[curVertex] = 'X';
		while(!queue.isEmpty()){////////////while
			int cur = queue.poll();
			subGraph.add(cur);
			for(int i = 0; i < graphs.length;i++){
				if(flag[i]!='X'&&graphs[i][cur]==1){
					queue.add(i);
					flag[i] = 'X';
				}
			}
			for(int i = 0; i < graphs[0].length;i++){
				if(flag[i]!='X'&&graphs[cur][i]==1){
					queue.add(i);
					flag[i] = 'X';
				}
			}
		}////////////while
		subGraphList.add(new SubGraph(subGraph));
	}


	public void buildGraph(int[][] graphs, Graph<CodeVertex, CodeEdge> prunedGraph) {
		//这里当成无向图来处理了  这里是域内不考虑方向的
		for(CodeEdge edge:prunedGraph.getEdges()){
			int formerId = edge.getSource().getId();
			int latterId = edge.getTarget().getId();
			graphs[formerId][latterId] = 1;
			graphs[latterId][formerId] = 1;
		}
	}
	
	public static void main(String[] args) throws ClassNotFoundException, IOException{
//		StoreSubGraphInfoByThreshold storeSubGraphInfo = new StoreSubGraphInfoByThreshold();
//		List<SubGraph> subGraphList = storeSubGraphInfo.getSubGraphs();
//		System.out.println(subGraphList.size());
//		for(SubGraph subGraph:subGraphList){
//			for(int ele:subGraph.getVertexList()){
//				System.out.print(ele + " ");
//			}
//			System.out.println();
//		}
	}
}
