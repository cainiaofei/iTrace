package cn.edu.nju.cs.itrace4.tool;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.edu.nju.cs.itrace4.demo.relation.StoreCallAndDataSubGraphByThreshold;
import cn.edu.nju.cs.itrace4.demo.relation.SubGraph;
import cn.edu.nju.cs.itrace4.exp.itrust.ITRUST_CONSTANTS;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;

public class GetOverlapSubGraph {
	
	private StoreCallAndDataSubGraphByThreshold getSubGraphList;
	private List<SubGraph> subGraphList;
	
	public GetOverlapSubGraph() throws IOException, ClassNotFoundException{
		FileInputStream fis = new FileInputStream(ITRUST_CONSTANTS.class_relationInfoPath);
	    ObjectInputStream ois = new ObjectInputStream(fis);
	    RelationInfo ri = (RelationInfo) ois.readObject();
		getSubGraphList = 
				new StoreCallAndDataSubGraphByThreshold();
		ois.close();
		double callEdgeScoreThreshold = 0.7;
		double dataEdgeScoreThreshold = 0.7;
		ri.setPruning(callEdgeScoreThreshold, dataEdgeScoreThreshold);
		subGraphList = getSubGraphList.getSubGraphs(ri);
	}
	
	public void showOverlap(){
		int sizeMoreThanOneVertex = getSizeMoreThanOneVertex(subGraphList);
		SubGraph[] graphs = new SubGraph[sizeMoreThanOneVertex];
		int index = 0;
		for(SubGraph subGraph:subGraphList){
			if(subGraph.getVertexList().size()>1){
				graphs[index++] = subGraph;
			}
		}
		int[][] matrix = new int[graphs.length][graphs.length];
		int overlapCount = 0;
		for(int i = 0; i < matrix.length;i++){
			for(int j = i+1; j < matrix[0].length;j++){
				if(hasOverlap(graphs[i],graphs[j])){
					matrix[i][j] = 1;
					overlapCount++;
				}
			}
		}
		System.out.println(overlapCount);
		print(matrix);
	}
	
	private void print(int[][] graphs) {
		for(int i = 0; i < graphs.length;i++){
			for(int j = 0; j < graphs.length;j++){
				System.out.print(graphs[i][j]+" ");
			}
			System.out.println();
		}
	}

	private boolean hasOverlap(SubGraph formerSubGraph, SubGraph latterSubGraph) {
		Set<Integer> set = new HashSet<Integer>();
		for(int ele:formerSubGraph.getVertexList()){
			set.add(ele);
		}
		for(int ele:latterSubGraph.getVertexList()){
			if(set.contains(ele)){
				return true;
			}
		}
		return false;
	}

	private int getSizeMoreThanOneVertex(List<SubGraph> subGraphList) {
		int count = 0;
		for(SubGraph subGraph:subGraphList){
			if(subGraph.getVertexList().size()>1){
				count++;
			}
		}
		return count;
	}

	public static void main(String[] args) throws ClassNotFoundException, IOException{
		GetOverlapSubGraph getOverSubGraph = new GetOverlapSubGraph();
		getOverSubGraph.showOverlap();
	}
}
