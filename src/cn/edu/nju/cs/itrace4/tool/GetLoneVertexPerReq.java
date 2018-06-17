package cn.edu.nju.cs.itrace4.tool;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;

import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.demo.relation.StoreCallSubGraph;
import cn.edu.nju.cs.itrace4.demo.relation.SubGraph;
import cn.edu.nju.cs.itrace4.exp.jhotdraw.JHotDraw_CONSTANTS;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;

public class GetLoneVertexPerReq {
	TextDataset textDataSet;
	RelationInfo ri;
	Map<Integer,String> idMapName;
	List<SubGraph> subGraphList;
	public GetLoneVertexPerReq() throws IOException, ClassNotFoundException{
		textDataSet = new TextDataset(JHotDraw_CONSTANTS.ucPath, JHotDraw_CONSTANTS.classDirPath, 
				JHotDraw_CONSTANTS.rtmClassPath);
		 FileInputStream fis = new FileInputStream(JHotDraw_CONSTANTS.class_relationInfoPath);
	     ObjectInputStream ois = new ObjectInputStream(fis);
	     ri = (RelationInfo) ois.readObject();
	     ois.close();
	     idMapName = ri.getVertexIdNameMap();
	     ri.setPruning(0.7,0.7);
	     subGraphList = new StoreCallSubGraph().getSubGraphs(ri);
	}
	
	public void doTask(){
		SimilarityMatrix oracle = textDataSet.getRtm();
		for(String req:oracle.sourceArtifactsIds()){
			int relevantLoneVertexCount = 0;
			for(SubGraph subGraph:subGraphList){
				List<Integer> curList = subGraph.getVertexList();
				String target = idMapName.get(curList.get(0));
				if(curList.size()==1&&oracle.isLinkAboveThreshold(req,target)){
					relevantLoneVertexCount++;
					System.out.print(curList.get(0)+" ");
				}
			}
			System.out.println();
			System.out.println(req+":"+relevantLoneVertexCount);
		}
	}
	
	public static void main(String[] args) throws ClassNotFoundException, IOException{
		GetLoneVertexPerReq getLoneVertexPerReq = new GetLoneVertexPerReq();
		getLoneVertexPerReq.doTask();
	}
}
