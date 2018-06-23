package cn.edu.nju.cs.itrace4.util.multiThread;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.math3.stat.inference.MannWhitneyUTest;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.None_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.prealgo.UD_CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.ir.IRModelConst;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.core.algo.region.callthendata.UD_SubGraph_Closeness;
import cn.edu.nju.cs.itrace4.demo.relation.StoreCallAndDataSubGraphByThreshold;
import cn.edu.nju.cs.itrace4.demo.relation.StoreSubGraphInfoByThreshold;
import cn.edu.nju.cs.itrace4.demo.relation.SubGraph;
import cn.edu.nju.cs.itrace4.exp.gantt.Gantt_CONSTANTS;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.visual.VisualCurve;
 
public class UD_SubGraph_Call_Data_Boot {
	
	private StoreSubGraphInfoByThreshold storeSubGraphInfoByThreshold;
	private CountDownLatch cdl = new CountDownLatch(3);
	
	
	public UD_SubGraph_Call_Data_Boot(){
		storeSubGraphInfoByThreshold = new StoreCallAndDataSubGraphByThreshold();
	}
	
	@SuppressWarnings("unchecked")
	public void run() throws IOException, ClassNotFoundException, InterruptedException, ExecutionException {
        TextDataset textDataset = new TextDataset(Gantt_CONSTANTS.ucPath, Gantt_CONSTANTS.classDirPath, 
        		Gantt_CONSTANTS.rtmClassPath);

        long startTime = System.currentTimeMillis();
        ExecutorService pool = Executors.newFixedThreadPool(3);
        FileInputStream fis = new FileInputStream(Gantt_CONSTANTS.class_relationInfoPath);
        ObjectInputStream ois = new ObjectInputStream(fis);
        RelationInfo ri = (RelationInfo)pool.submit(new ReadDataTask(ois,cdl)).get();
        ois.close();

        FileInputStream fisForO = new FileInputStream(Gantt_CONSTANTS.class_relationInfoPath);
        ObjectInputStream oisForO = new ObjectInputStream(fisForO);
        RelationInfo class_relationForO = (RelationInfo)pool.submit(new ReadDataTask(oisForO,cdl)).get();
        oisForO.close();

        FileInputStream fisForAllDependencies = new FileInputStream(Gantt_CONSTANTS.class_relationInfoPathWhole);
        ObjectInputStream oisForAllDependencies = new ObjectInputStream(fisForAllDependencies);
        RelationInfo class_relationForAllDependencies = (RelationInfo)pool.submit(new ReadDataTask(oisForAllDependencies,
        		cdl)).get();
        oisForAllDependencies.close();
        System.out.println((System.currentTimeMillis()-startTime)/1000);
        
        cdl.await();
        Result result_ir = IR.compute(textDataset, IRModelConst.VSM, new None_CSTI());
        Result result_UD_CSTI = IR.compute(textDataset, IRModelConst.VSM, new UD_CSTI(ri));
        
        Map<Integer, String> vertexIdNameMap = ri.getVertexIdNameMap();
        
        double callEdgeScoreThreshold = 0.5;
        double dataEdgeScoreThreshold = 0.5;
        ri.setPruning(callEdgeScoreThreshold, dataEdgeScoreThreshold);
        Result result_UD_SubGraph_Closeness1 = IR.compute(textDataset, IRModelConst.VSM,
        		new UD_SubGraph_Closeness(callEdgeScoreThreshold,dataEdgeScoreThreshold,
        				storeSubGraphInfoByThreshold.getSubGraphs(ri),vertexIdNameMap));
        
        callEdgeScoreThreshold = 0.8;
        dataEdgeScoreThreshold = 2;
        ri.setPruning(callEdgeScoreThreshold, dataEdgeScoreThreshold);
        Result result_UD_SubGraph_Closeness2 = IR.compute(textDataset, IRModelConst.VSM,
        		new UD_SubGraph_Closeness(callEdgeScoreThreshold,dataEdgeScoreThreshold,
        				(new StoreSubGraphInfoByThreshold()).getSubGraphs(ri),vertexIdNameMap));
        callEdgeScoreThreshold = 0.8;
        dataEdgeScoreThreshold = 0.6;
        ri.setPruning(callEdgeScoreThreshold, dataEdgeScoreThreshold);
        Result result_UD_SubGraph_Closeness3 = IR.compute(textDataset, IRModelConst.VSM,
        		new UD_SubGraph_Closeness(callEdgeScoreThreshold,dataEdgeScoreThreshold,
        				(new StoreSubGraphInfoByThreshold()).getSubGraphs(ri),vertexIdNameMap));
        
        callEdgeScoreThreshold = 0.8;
        dataEdgeScoreThreshold = 0.8;
        ri.setPruning(callEdgeScoreThreshold, dataEdgeScoreThreshold);
        Result result_UD_SubGraph_Closeness4 = IR.compute(textDataset, IRModelConst.VSM,
        		new UD_SubGraph_Closeness(callEdgeScoreThreshold,dataEdgeScoreThreshold,
        				(new StoreSubGraphInfoByThreshold()).getSubGraphs(ri),vertexIdNameMap));
        
        callEdgeScoreThreshold = 0.7;
        dataEdgeScoreThreshold = 0.7;
        ri.setPruning(callEdgeScoreThreshold, dataEdgeScoreThreshold);
        Result result_UD_SubGraph_Closeness5 = IR.compute(textDataset, IRModelConst.VSM,
        		new UD_SubGraph_Closeness(callEdgeScoreThreshold,dataEdgeScoreThreshold,
        				(new StoreSubGraphInfoByThreshold()).getSubGraphs(ri),vertexIdNameMap));
        
        VisualCurve curve = new VisualCurve();
        curve.addLine(result_ir);
        curve.addLine(result_UD_CSTI);
       // curve.addLine(result_UD_SubGraph_Closeness1);
       // curve.addLine(result_UD_SubGraph_Closeness2);
        //curve.addLine(result_UD_SubGraph_Closeness3);
        //curve.addLine(result_UD_SubGraph_Closeness4);
        curve.addLine(result_UD_SubGraph_Closeness5);
        curve.showChart();
        int allSize = storeSubGraphInfoByThreshold.getSubGraphs(ri).size();
        System.out.println("----size-----"+allSize);
//        curve.resultStore(Gantt_CONSTANTS.vsmExpExportPath_ICSME, "call_0.7_data_0.9_idtf_1.4");
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

	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException, ExecutionException {
		long startTime = System.currentTimeMillis();
    	UD_SubGraph_Call_Data_Boot ud_SubGraph_Closeness_Boot = new UD_SubGraph_Call_Data_Boot();
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
