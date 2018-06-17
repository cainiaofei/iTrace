package cn.edu.nju.cs.itrace4.demo.boot;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.inference.MannWhitneyUTest;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.None_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.UD_CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.ir.IRModelConst;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.demo.algo.UD_CallSubGraph_Then_DataSubGraph_Closeness;
import cn.edu.nju.cs.itrace4.demo.relation.StoreCallAndDataSubGraphByThreshold;
import cn.edu.nju.cs.itrace4.demo.relation.StoreSubGraphInfoByThreshold;
import cn.edu.nju.cs.itrace4.demo.relation.SubGraph;
import cn.edu.nju.cs.itrace4.exp.itrust.ITRUST_CONSTANTS;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.visual.VisualCurve;
 
public class UD_CallSubGraph_Then_DataSubGraph_Closeness_Boot {
	
	private StoreSubGraphInfoByThreshold storeSubGraphInfoByThreshold;
	
	public UD_CallSubGraph_Then_DataSubGraph_Closeness_Boot(){
		storeSubGraphInfoByThreshold = new StoreCallAndDataSubGraphByThreshold();
	}
	
	public void run() throws IOException, ClassNotFoundException {
        TextDataset textDataset = new TextDataset(ITRUST_CONSTANTS.ucPath, ITRUST_CONSTANTS.classDirPath, 
        		ITRUST_CONSTANTS.rtmClassPath);

        FileInputStream fis = new FileInputStream(ITRUST_CONSTANTS.class_relationInfoPath);
        ObjectInputStream ois = new ObjectInputStream(fis);
        RelationInfo ri = (RelationInfo) ois.readObject();
        ois.close();

        FileInputStream fisForO = new FileInputStream(ITRUST_CONSTANTS.class_relationInfoPath);
        ObjectInputStream oisForO = new ObjectInputStream(fisForO);
        RelationInfo class_relationForO = (RelationInfo) oisForO.readObject();
        oisForO.close();

        FileInputStream fisForAllDependencies = new FileInputStream(ITRUST_CONSTANTS.class_relationInfoPathWhole);
        ObjectInputStream oisForAllDependencies = new ObjectInputStream(fisForAllDependencies);
        RelationInfo class_relationForAllDependencies = (RelationInfo) oisForAllDependencies.readObject();
        oisForAllDependencies.close();
        
        Result result_ir = IR.compute(textDataset, IRModelConst.VSM, new None_CSTI());
        Result result_UD_CSTI = IR.compute(textDataset, IRModelConst.VSM, new UD_CSTI(ri));
        
        double callEdgeScoreThreshold = 0.8;
        double dataEdgeScoreThreshold = 2;
        Map<Integer, String> vertexIdNameMap = ri.getVertexIdNameMap();
        
        ri.setPruning(callEdgeScoreThreshold, dataEdgeScoreThreshold);
        Result result_UD_SubGraph_Closeness1 = IR.compute(textDataset, IRModelConst.VSM,
        		new UD_CallSubGraph_Then_DataSubGraph_Closeness(callEdgeScoreThreshold,dataEdgeScoreThreshold,
        				vertexIdNameMap,ri));
        
        callEdgeScoreThreshold = 0.8;
        dataEdgeScoreThreshold = 0.7;
        ri.setPruning(callEdgeScoreThreshold, dataEdgeScoreThreshold);
        Result result_UD_SubGraph_Closeness2 = IR.compute(textDataset, IRModelConst.VSM,
        		new UD_CallSubGraph_Then_DataSubGraph_Closeness(callEdgeScoreThreshold,dataEdgeScoreThreshold,
        				vertexIdNameMap,ri));
        
        callEdgeScoreThreshold = 0.8;
        dataEdgeScoreThreshold = 0.5;
        ri.setPruning(callEdgeScoreThreshold, dataEdgeScoreThreshold);
        Result result_UD_SubGraph_Closeness3 = IR.compute(textDataset, IRModelConst.VSM,
        		new UD_CallSubGraph_Then_DataSubGraph_Closeness(callEdgeScoreThreshold,dataEdgeScoreThreshold,
        				vertexIdNameMap,ri));
        callEdgeScoreThreshold = 0.8;
        dataEdgeScoreThreshold = 0.6;
        ri.setPruning(callEdgeScoreThreshold, dataEdgeScoreThreshold);
        Result result_UD_SubGraph_Closeness4 = IR.compute(textDataset, IRModelConst.VSM,
        		new UD_CallSubGraph_Then_DataSubGraph_Closeness(callEdgeScoreThreshold,dataEdgeScoreThreshold,
        				vertexIdNameMap,ri));
        callEdgeScoreThreshold = 0.7;
        dataEdgeScoreThreshold = 0.7;
        ri.setPruning(callEdgeScoreThreshold, dataEdgeScoreThreshold);
        Result result_UD_SubGraph_Closeness5 = IR.compute(textDataset, IRModelConst.VSM,
        		new UD_CallSubGraph_Then_DataSubGraph_Closeness(callEdgeScoreThreshold,dataEdgeScoreThreshold,
        				vertexIdNameMap,ri));
        
        VisualCurve curve = new VisualCurve();
        curve.addLine(result_ir);
        curve.addLine(result_UD_CSTI);
        curve.addLine(result_UD_SubGraph_Closeness1);
        curve.addLine(result_UD_SubGraph_Closeness2);
        curve.addLine(result_UD_SubGraph_Closeness3);
        curve.addLine(result_UD_SubGraph_Closeness4);
        curve.addLine(result_UD_SubGraph_Closeness5);
        curve.showChart();
        int allSize = storeSubGraphInfoByThreshold.getSubGraphs(ri).size();
        System.out.println("----size-----"+allSize);
//        curve.resultStore(ITRUST_CONSTANTS.vsmExpExportPath_ICSME, "call_0.7_data_0.9_idtf_1.4");
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
    	UD_CallSubGraph_Then_DataSubGraph_Closeness_Boot ud_SubGraph_Closeness_Boot = new UD_CallSubGraph_Then_DataSubGraph_Closeness_Boot();
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
