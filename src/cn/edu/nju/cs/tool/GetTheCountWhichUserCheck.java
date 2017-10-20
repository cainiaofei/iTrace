package cn.edu.nju.cs.tool;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import cn.edu.nju.cs.itrace4.demo.relation.StoreCallSubGraph;
import cn.edu.nju.cs.itrace4.demo.relation.StoreDataSubGraph;
import cn.edu.nju.cs.itrace4.demo.relation.SubGraph;
import cn.edu.nju.cs.itrace4.exp.jhotdraw.JHotDraw_CONSTANTS;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;

public class GetTheCountWhichUserCheck {
	
	private StoreCallSubGraph getCallSubGraphList;
	private StoreDataSubGraph getDataSubGraphList;
	List<SubGraph> callSubGraphList;
	List<SubGraph> dataSubGraphList;
	
	public GetTheCountWhichUserCheck() throws IOException, ClassNotFoundException{
		FileInputStream fis = new FileInputStream(JHotDraw_CONSTANTS.class_relationInfoPath);
	    ObjectInputStream ois = new ObjectInputStream(fis);
	    RelationInfo ri = (RelationInfo) ois.readObject();
		getCallSubGraphList = new StoreCallSubGraph();
		getDataSubGraphList = new StoreDataSubGraph();
		ois.close();
		double callEdgeScoreThreshold = 0.7;
		double dataEdgeScoreThreshold = 0.7;
		ri.setPruning(callEdgeScoreThreshold, dataEdgeScoreThreshold);
		callSubGraphList = getCallSubGraphList.getSubGraphs(ri);
		dataSubGraphList = getDataSubGraphList.getSubGraphs(ri);
	}
	
	public void showOverlap(){
		int sizeMoreThanOneVertex = getSizeMoreThanOneVertex(callSubGraphList)+
				getSizeMoreThanOneVertex(dataSubGraphList);
		int allAmount = getAllAmount(callSubGraphList);
		System.out.println(sizeMoreThanOneVertex*1.0/allAmount);
	}
	
	

	private int getAllAmount(List<SubGraph> callSubGraphList) {
		int sum = 0;
		for(SubGraph subGraph:callSubGraphList){
			sum += subGraph.getVertexList().size();
		}
		return sum;
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
		GetTheCountWhichUserCheck tool = new GetTheCountWhichUserCheck();
		tool.showOverlap();
	}
}
