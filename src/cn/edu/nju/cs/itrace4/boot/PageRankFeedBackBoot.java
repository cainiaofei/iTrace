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
import cn.edu.nju.cs.itrace4.demo.explore.PageRankFeedBack;
import cn.edu.nju.cs.itrace4.exp.gantt.Gantt_CONSTANTS;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.visual.VisualCurve;

public class PageRankFeedBackBoot {
	public static void run() throws IOException, ClassNotFoundException {
        TextDataset textDataset = new TextDataset(Gantt_CONSTANTS.ucPath, Gantt_CONSTANTS.classDirPath, Gantt_CONSTANTS.rtmClassPath);

        FileInputStream fis = new FileInputStream(Gantt_CONSTANTS.class_relationInfoPath);
        ObjectInputStream ois = new ObjectInputStream(fis);
        RelationInfo class_relation = (RelationInfo) ois.readObject();

        FileInputStream fisForO = new FileInputStream(Gantt_CONSTANTS.class_relationInfoPath);
        ObjectInputStream oisForO = new ObjectInputStream(fisForO);
        RelationInfo class_relationForO = (RelationInfo) oisForO.readObject();

        FileInputStream fisForAllDependencies = new FileInputStream(Gantt_CONSTANTS.class_relationInfoPathWhole);
        ObjectInputStream oisForAllDependencies = new ObjectInputStream(fisForAllDependencies);
        RelationInfo class_relationForAllDependencies = (RelationInfo) oisForAllDependencies.readObject();

        Result result_ir = IR.compute(textDataset, IRModelConst.VSM, new None_CSTI());
        Result result_pagerank = IR.compute(textDataset, IRModelConst.VSM, new PageRank_CSTI(class_relation));
        Result result_PageRankFeedBack_First_Ten_Percent = IR.compute(textDataset, IRModelConst.VSM, 
        		new PageRankFeedBack(class_relation,0));
        Result result_PageRankFeedBack_First_Twenty_Percent = IR.compute(textDataset, IRModelConst.VSM, 
        		new PageRankFeedBack(class_relation,1));
        Result result_PageRankFeedBack_First_Fifty_Percent = IR.compute(textDataset, IRModelConst.VSM, 
        		new PageRankFeedBack(class_relation,0.2));
        Result result_UD_CSTI = IR.compute(textDataset, IRModelConst.VSM, new UD_CSTI(class_relation));
        
        VisualCurve curve = new VisualCurve();
        curve.addLine(result_ir);
        curve.addLine(result_UD_CSTI);
      //  curve.addLine(result_pagerank);
        curve.addLine(result_PageRankFeedBack_First_Ten_Percent);
        curve.addLine(result_PageRankFeedBack_First_Twenty_Percent);
        curve.addLine(result_PageRankFeedBack_First_Fifty_Percent);
        curve.showChart();
//        curve.resultStore(Gantt_CONSTANTS.vsmExpExportPath_ICSME, "call_0.7_data_0.9_idtf_1.4");
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
