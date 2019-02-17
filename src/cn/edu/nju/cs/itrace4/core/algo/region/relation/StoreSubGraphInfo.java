package cn.edu.nju.cs.itrace4.core.algo.region.relation;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.relation.info.RelationPair;

public class StoreSubGraphInfo {
	
	public List<SubGraph> getSubGraphs() throws IOException, ClassNotFoundException{
		 String class_relationInfo = "data/exp/iTrust/relation/Class_relationInfo.ser";
		 FileInputStream fis = new FileInputStream(class_relationInfo);
         ObjectInputStream ois = new ObjectInputStream(fis);
         //反序列化
         RelationInfo ri = (RelationInfo) ois.readObject();
         ois.close();
         //序号和类名对应关系
         Map<Integer, String> vertexIdNameMap = ri.getVertexIdNameMap(); 
         //用矩阵来存储这张图    map序号和类名的对应关系是从1开始的
         int[][] graphs = new int[vertexIdNameMap.size()+1][vertexIdNameMap.size()+1];
         List<RelationPair> callRelationPairList = ri.getCallRelationPairList();
         buildGraph(graphs,callRelationPairList);
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
	
	
	private void getSubGrapByBfs(int[][] graphs, int curVertex, List<SubGraph> subGraphList,char[] flag) {
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


	private void buildGraph(int[][] graphs, List<RelationPair> callRelationPairList) {
		//这里当成无向图来处理了
		for(RelationPair pair:callRelationPairList){
			graphs[pair.getKey()][pair.getValue()] = 1;
			graphs[pair.getValue()][pair.getKey()] = 1;
		}
	}


	public static void main(String[] args) throws ClassNotFoundException, IOException{
		StoreSubGraphInfo storeSubGraphInfo = new StoreSubGraphInfo();
		List<SubGraph> subGraphList = storeSubGraphInfo.getSubGraphs();
		System.out.println(subGraphList.size());
		for(SubGraph subGraph:subGraphList){
			for(int ele:subGraph.getVertexList()){
				System.out.print(ele + " ");
			}
			System.out.println();
		}
		
	}
}
