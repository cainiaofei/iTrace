package cn.edu.nju.cs.itrace4.boot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.None_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.prealgo.UD_CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.ir.IRModelConst;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.core.algo.region.callthendata.UD_CallSubGraph_Then_DataSubGraph_Closeness;
import cn.edu.nju.cs.itrace4.core.algo.region.callthendata.UD_CallThenDataWithBonusForLone;
import cn.edu.nju.cs.itrace4.core.algo.region.relation.StoreCallAndDataSubGraphByThreshold;
import cn.edu.nju.cs.itrace4.core.algo.region.relation.StoreCallSubGraph;
import cn.edu.nju.cs.itrace4.core.algo.region.relation.StoreDataSubGraph;
import cn.edu.nju.cs.itrace4.core.algo.region.relation.StoreSubGraphInfoByThreshold;
import cn.edu.nju.cs.itrace4.core.algo.region.relation.SubGraph;
import cn.edu.nju.cs.itrace4.exp.itrust.ITRUST_CONSTANTS;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.visual.MyVisualCurve;
 
public class BonusForLoneWithPenalty {
	
	private StoreSubGraphInfoByThreshold storeSubGraphInfoByThreshold;
	
	public BonusForLoneWithPenalty(){
		storeSubGraphInfoByThreshold = new StoreCallAndDataSubGraphByThreshold();
	}
	
	public void run() throws IOException, ClassNotFoundException {
        TextDataset textDataset = new TextDataset(ITRUST_CONSTANTS.ucPath, ITRUST_CONSTANTS.classDirPath, 
        		ITRUST_CONSTANTS.rtmClassPath);

        FileInputStream fis = new FileInputStream(ITRUST_CONSTANTS.class_relationInfoPath);
        ObjectInputStream ois = new ObjectInputStream(fis);
        RelationInfo ri = (RelationInfo) ois.readObject();
        ois.close();

       /* FileInputStream fisForO = new FileInputStream(ITRUST_CONSTANTS.class_relationInfoPath);
        ObjectInputStream oisForO = new ObjectInputStream(fisForO);
        RelationInfo class_relationForO = (RelationInfo) oisForO.readObject();
        oisForO.close();

        FileInputStream fisForAllDependencies = new FileInputStream(ITRUST_CONSTANTS.class_relationInfoPathWhole);
        ObjectInputStream oisForAllDependencies = new ObjectInputStream(fisForAllDependencies);
        RelationInfo class_relationForAllDependencies = (RelationInfo) oisForAllDependencies.readObject();
        oisForAllDependencies.close();*/
        
        
        Result result_ir = IR.compute(textDataset, IRModelConst.LSI, new None_CSTI());
        Result result_UD_CSTI = IR.compute(textDataset, IRModelConst.LSI, new UD_CSTI(ri));
        
        double callEdgeScoreThreshold = 0.7;
        double dataEdgeScoreThreshold = 0.7;
        Map<Integer, String> vertexIdNameMap = ri.getVertexIdNameMap();
        
        ri.setPruning(callEdgeScoreThreshold, dataEdgeScoreThreshold);
        
        Result result_UD_CallThenDataWithBonusForLone = IR.compute(textDataset, IRModelConst.LSI,
        		new UD_CallThenDataWithBonusForLone(ri,callEdgeScoreThreshold,dataEdgeScoreThreshold));
        
        Result result_UD_CallThenDataIgnoreLone = IR.compute(textDataset, IRModelConst.LSI,
        		new UD_CallSubGraph_Then_DataSubGraph_Closeness(callEdgeScoreThreshold,dataEdgeScoreThreshold,
        				vertexIdNameMap,ri));
        
        MyVisualCurve curve = new MyVisualCurve();
        curve.addLine(result_ir);
        curve.addLine(result_UD_CSTI);
        curve.addLine(result_UD_CallThenDataIgnoreLone);
        curve.addLine(result_UD_CallThenDataWithBonusForLone);
        curve.showChart(ITRUST_CONSTANTS.projectName);
        
        //--------------------------------
        System.out.println("----------------------IR-----------------");
        System.out.println("--------------averagePrecisionByRankList---------");
        result_ir.showAveragePrecisionByRanklist();
        System.out.println("--------------averagePrecisionByQuery----------");
        result_ir.showAveragePrecisionByQuery();
        
        expressWithExcel(result_ir,result_UD_CSTI,result_UD_CallThenDataIgnoreLone,result_UD_CallThenDataWithBonusForLone);
       
        showRate(ri,textDataset);
    }

	private void expressWithExcel(Result result_ir, Result result_UD_CSTI, Result result_UD_CallThenDataIgnoreLone,
			Result result_UD_CallThenDataWithBonusForLone) throws IOException {
		Result[] results = {result_ir,result_UD_CSTI,result_UD_CallThenDataIgnoreLone,
				result_UD_CallThenDataWithBonusForLone};
		/*
		 * write the first line: modelName, algorithmName 
		 */
		BufferedWriter bw = new BufferedWriter(new FileWriter("out"+File.separator+
				ITRUST_CONSTANTS.projectName+File.separator+
				result_ir.getModel()+".csv"));
		bw.write(result_ir.getModel()+";");
		for(Result result:results){
			bw.write(result.getAlgorithmName()+";");
		}
		bw.newLine();
		/*
		 * write the second line: averagePrecision, and the averagePrecision of algorithm 
		 */
		bw.write("averagePrecision"+";");
		for(Result result:results){
			bw.write(result.getAveragePrecisionByRanklist()+";");
		}
		bw.newLine();
		/*
		 * write follow line, the req and corresponding precision 
		 */
		Set<String> reqSet = results[0].getAveragePrecisionByQuery().keySet();
		for(String req:reqSet){
			bw.write(req+";");
			for(Result result:results){
				bw.write(result.getAveragePrecisionByQuery().get(req)+";");
			}
			bw.newLine();
		}
		bw.close();
	}

	public void showRate(RelationInfo ri,TextDataset textDataset ){
		List<SubGraph> callSubGraphList = new StoreCallSubGraph().getSubGraphs(ri);
		List<SubGraph> dataSubGraphList = new StoreDataSubGraph().getSubGraphs(ri);
		Map<Integer,String> vertexIdNameMap = ri.getVertexIdNameMap();
		Set<String> set = textDataset.getTargetCollection().keySet();
		filterSubGraphsList(set,callSubGraphList,vertexIdNameMap);
		filterSubGraphsList(set,dataSubGraphList,vertexIdNameMap);
		int nodesWhichRepresent = getSubGraphsCountMoreThanOne(callSubGraphList) + 
				getSubGraphsCountMoreThanOne(dataSubGraphList);
		int allNodesAmount = allNodes(dataSubGraphList);
		int another =  allNodes(callSubGraphList);
		
		if(another!=allNodesAmount){
			System.out.println("居然不相等 明显有问题");
		}
		
		System.out.println("------------rate---------------"+(nodesWhichRepresent*1.0)/allNodesAmount);
	}
	
	public void filterSubGraphsList(Set<String> set,List<SubGraph> subGraphList,
			Map<Integer,String> vertexIdNameMap){
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
	
	
    private int allNodes(List<SubGraph> subGraphs) {
    	int count = 0;
    	for(SubGraph subGraph:subGraphs){
    		count += subGraph.getVertexList().size();
    	}
    	return count;
	}

	private int getSubGraphsCountMoreThanOne(List<SubGraph> subGraphs) {
    	int count = 0;
    	for(SubGraph subGraph:subGraphs){
    		if(subGraph.getVertexList().size()>1){
    			count++;
    		}
    	}
    	return count;
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		long startTime = System.currentTimeMillis();
    	BonusForLoneWithPenalty ud_SubGraph_Closeness_Boot = new BonusForLoneWithPenalty();
    	ud_SubGraph_Closeness_Boot.run();
    	long endTime = System.currentTimeMillis();
    	System.out.println("time cost:"+(endTime-startTime)*1.0/1000/60);
    }

}
