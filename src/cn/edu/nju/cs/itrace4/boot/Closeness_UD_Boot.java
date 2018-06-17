package cn.edu.nju.cs.itrace4.boot;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.commons.math3.stat.inference.MannWhitneyUTest;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.None_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.prealgo.PageRank_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.prealgo.UD_CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.ir.IRModelConst;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.demo.explore.Closeness_UD;
import cn.edu.nju.cs.itrace4.exp.itrust.ITRUST_CONSTANTS;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.visual.VisualCurve;

public class Closeness_UD_Boot {
	public static void run() throws IOException, ClassNotFoundException {
        TextDataset textDataset = new TextDataset(ITRUST_CONSTANTS.ucPath, ITRUST_CONSTANTS.classDirPath, ITRUST_CONSTANTS.rtmClassPath);

        FileInputStream fis = new FileInputStream(ITRUST_CONSTANTS.class_relationInfoPath);
        ObjectInputStream ois = new ObjectInputStream(fis);
        RelationInfo class_relation = (RelationInfo) ois.readObject();

        FileInputStream fisForO = new FileInputStream(ITRUST_CONSTANTS.class_relationInfoPath);
        ObjectInputStream oisForO = new ObjectInputStream(fisForO);
        RelationInfo class_relationForO = (RelationInfo) oisForO.readObject();

        FileInputStream fisForAllDependencies = new FileInputStream(ITRUST_CONSTANTS.class_relationInfoPathWhole);
        ObjectInputStream oisForAllDependencies = new ObjectInputStream(fisForAllDependencies);
        RelationInfo class_relationForAllDependencies = (RelationInfo) oisForAllDependencies.readObject();

        Result result_ir = IR.compute(textDataset, IRModelConst.VSM, new None_CSTI());
        Result result_pagerank = IR.compute(textDataset, IRModelConst.VSM, new PageRank_CSTI(class_relation));
        Result result_UD_CSTI = IR.compute(textDataset, IRModelConst.VSM, new UD_CSTI(class_relation));
       // Result result_Closeness_UD_First_Ten_Percent = IR.compute(textDataset, IRModelConst.VSM, 
//        		new PruningCall_Data_CSTI(class_relation,UseEdge.Call_Data));
        
        Result result_Closeness_UD_First_Ten_Percent = IR.compute(textDataset, IRModelConst.VSM, 
        		new Closeness_UD(class_relation,1,class_relationForO,class_relationForAllDependencies));
        Result result_Closeness_UD_First_Twenty_Percent = IR.compute(textDataset, IRModelConst.VSM, 
        		new Closeness_UD(class_relation,0.2,class_relationForO,class_relationForAllDependencies));
        Result result_Closeness_UD_First_Fifty_Percent = IR.compute(textDataset, IRModelConst.VSM, 
        		new Closeness_UD(class_relation,0.5,class_relationForO,class_relationForAllDependencies));
        
        VisualCurve curve = new VisualCurve();
        curve.addLine(result_ir);
        curve.addLine(result_UD_CSTI);
        curve.addLine(result_Closeness_UD_First_Ten_Percent);
        curve.addLine(result_Closeness_UD_First_Twenty_Percent);
        curve.addLine(result_Closeness_UD_First_Fifty_Percent);
        curve.showChart();
//        curve.resultStore(ITRUST_CONSTANTS.vsmExpExportPath_ICSME, "call_0.7_data_0.9_idtf_1.4");
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        run();
    }

    private static void printPValue(Result ours, Result compareTo) {
        MannWhitneyUTest mannWhitneyUTest = new MannWhitneyUTest();
        double pValue_fmeasure = mannWhitneyUTest.mannWhitneyUTest(ours.getWilcoxonDataArray_fmeasure(), compareTo.getWilcoxonDataArray_fmeasure());
        double pValue_fp = mannWhitneyUTest.mannWhitneyUTest(ours.getWilcoxonDataArray_fp(), compareTo.getWilcoxonDataArray_fp());
        System.out.println("F-measure pValue = " + pValue_fmeasure );
        System.out.println("FP pValue = " + pValue_fp );
    }
}
