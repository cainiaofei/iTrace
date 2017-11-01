package cn.edu.nju.cs.itrace4.demo.boot;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.inference.MannWhitneyUTest;

import cn.edu.nju.cs.itrace4.core.algo.None_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.UD_CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.ir.IRModelConst;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.demo.algo.UD_CallSubGraph_Then_DataSubGraph_Closeness;
import cn.edu.nju.cs.itrace4.demo.algo.UD_CallSubGraph_Then_DataSubGraph_ClosenessWithPenalty;
import cn.edu.nju.cs.itrace4.demo.algo.UD_SubGraph_Closeness;
import cn.edu.nju.cs.itrace4.demo.algo.UD_SubGraph_ClosenessWithPenalty;
import cn.edu.nju.cs.itrace4.demo.relation.StoreCallAndDataSubGraphByThreshold;
import cn.edu.nju.cs.itrace4.demo.relation.StoreSubGraphInfoByThreshold;
import cn.edu.nju.cs.itrace4.demo.relation.SubGraph;
import cn.edu.nju.cs.itrace4.demo.visual.MyVisualCurve;
import cn.edu.nju.cs.itrace4.exp.jhotdraw.JHotDraw_CONSTANTS;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
 
public class WhetherWithPenaltyBoot {
	
	private StoreSubGraphInfoByThreshold storeSubGraphInfoByThreshold;
	
	public WhetherWithPenaltyBoot(){
		storeSubGraphInfoByThreshold = new StoreCallAndDataSubGraphByThreshold();
	}
	
	public void run() throws IOException, ClassNotFoundException {
        TextDataset textDataset = new TextDataset(JHotDraw_CONSTANTS.ucPath, JHotDraw_CONSTANTS.classDirPath, 
        		JHotDraw_CONSTANTS.rtmClassPath);

        FileInputStream fis = new FileInputStream(JHotDraw_CONSTANTS.class_relationInfoPath);
        ObjectInputStream ois = new ObjectInputStream(fis);
        RelationInfo ri = (RelationInfo) ois.readObject();
        ois.close();

        Result result_ir = IR.compute(textDataset, IRModelConst.VSM, new None_CSTI());
        Result result_UD_CSTI = IR.compute(textDataset, IRModelConst.VSM, new UD_CSTI(ri));
        
        Map<Integer, String> vertexIdNameMap = ri.getVertexIdNameMap();
        
        double callEdgeScoreThreshold = 0.75;
        double dataEdgeScoreThreshold = 0.75;
        ri.setPruning(callEdgeScoreThreshold, dataEdgeScoreThreshold);
        ri.setPruning(callEdgeScoreThreshold, dataEdgeScoreThreshold);
        Result result_UD_SubGraph_Closeness1 = IR.compute(textDataset, IRModelConst.VSM,
        		new UD_SubGraph_Closeness(callEdgeScoreThreshold,dataEdgeScoreThreshold,
        				(new StoreSubGraphInfoByThreshold()).getSubGraphs(ri),vertexIdNameMap));
        Result result_UD_SubGraph_Closeness2 = IR.compute(textDataset, IRModelConst.VSM,
        		new UD_CallSubGraph_Then_DataSubGraph_Closeness(callEdgeScoreThreshold,dataEdgeScoreThreshold,
        				vertexIdNameMap,ri));
        
        Result result_UD_SubGraph_Closeness3 = IR.compute(textDataset, IRModelConst.VSM,
        		new UD_SubGraph_ClosenessWithPenalty(callEdgeScoreThreshold,dataEdgeScoreThreshold,
        				(new StoreSubGraphInfoByThreshold()).getSubGraphs(ri),vertexIdNameMap));
        Result result_UD_SubGraph_Closeness4 = IR.compute(textDataset, IRModelConst.VSM,
        		new UD_CallSubGraph_Then_DataSubGraph_ClosenessWithPenalty(callEdgeScoreThreshold,dataEdgeScoreThreshold,
        				vertexIdNameMap,ri));
        
        MyVisualCurve curve = new MyVisualCurve();
        curve.addLine(result_ir);
        curve.addLine(result_UD_CSTI);
       // curve.addLine(result_UD_SubGraph_Closeness1);
        curve.addLine(result_UD_SubGraph_Closeness1);
        curve.addLine(result_UD_SubGraph_Closeness2);
        curve.addLine(result_UD_SubGraph_Closeness3);
        curve.addLine(result_UD_SubGraph_Closeness4);
        curve.showChart(JHotDraw_CONSTANTS.projectName);
        int allSize = storeSubGraphInfoByThreshold.getSubGraphs(ri).size();
        System.out.println("----size-----"+allSize);
//        curve.resultStore(JHotDraw_CONSTANTS.vsmExpExportPath_ICSME, "call_0.7_data_0.9_idtf_1.4");
        System.out.println("think about the subGraph which exceed one nodes");
        int prunSize = getSubGraphsCountMoreThanOne(storeSubGraphInfoByThreshold.getSubGraphs(ri));
        System.out.println("--the subGraph which has more than one node--"+prunSize);
        System.out.println("the rate subGraph compare all nodes"+prunSize*1.0/
        		allNodes(storeSubGraphInfoByThreshold.getSubGraphs(ri)));
    }

    private double allNodes(List<SubGraph> subGraphs) {
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
    	WhetherWithPenaltyBoot ud_SubGraph_Closeness_Boot = new WhetherWithPenaltyBoot();
    	ud_SubGraph_Closeness_Boot.run();
    	long endTime = System.currentTimeMillis();
    	System.out.println("time cost:"+(endTime-startTime)*1.0/1000/60);
    }

    private static void printPValue(Result ours, Result compareTo) {
        MannWhitneyUTest mannWhitneyUTest = new MannWhitneyUTest();
        double pValue_fmeasure = mannWhitneyUTest.mannWhitneyUTest(ours.getWilcoxonDataArray_fmeasure(), compareTo.getWilcoxonDataArray_fmeasure());
        double pValue_fp = mannWhitneyUTest.mannWhitneyUTest(ours.getWilcoxonDataArray_fp(), compareTo.getWilcoxonDataArray_fp());
        System.out.println("F-measure pValue = " + pValue_fmeasure );
        System.out.println("FP pValue = " + pValue_fp );
    }
}
