package cn.edu.nju.cs.itrace4.exp.gantt.icsme;

import cn.edu.nju.cs.itrace4.core.algo.None_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.O_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.PageRank_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.UseEdge;
import cn.edu.nju.cs.itrace4.core.algo.icse.PruningCall_Data_Connection_Closenss;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.ir.IRModelConst;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.exp.gantt.Gantt_CONSTANTS;
import cn.edu.nju.cs.itrace4.io._;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.util.Setting;
import cn.edu.nju.cs.itrace4.visual.VisualCurve;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Created by niejia on 16/3/22.
 */
public class Gantt_LSI_ICSME {


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


        Result result_ir = IR.compute(textDataset, IRModelConst.LSI, new None_CSTI());
        Result result_o = IR.compute(textDataset, IRModelConst.LSI, new O_CSTI(class_relation));
        Result result_pagerank = IR.compute(textDataset, IRModelConst.LSI, new PageRank_CSTI(class_relation));

        class_relation.setPruning(Setting.callThreshold, Setting.dataThreshold);
        class_relationForO.setPruning(-1, -1);
        class_relationForAllDependencies.setPruning(-1, -1);



        Result result_pruningeCall_Data_Dir = IR.compute(textDataset, IRModelConst.LSI, new PruningCall_Data_Connection_Closenss(class_relation, class_relationForO, class_relationForAllDependencies,UseEdge.Call, 1.0, 1.0));

        System.out.println("------------------------------");
        System.out.println("result_ir");
        result_ir.showAveragePrecisionByRanklist();
        result_ir.showMeanAveragePrecisionByQuery();
        result_ir.getFalsePositiveAtRecallByTen();

        System.out.println("------------------------------");
        System.out.println("result_pagerank");
        result_pagerank.showAveragePrecisionByRanklist();
        result_pagerank.showMeanAveragePrecisionByQuery();
        result_pagerank.getFalsePositiveAtRecallByTen();

        System.out.println("------------------------------");
        System.out.println("result_o");
        result_o.showAveragePrecisionByRanklist();
        result_o.showMeanAveragePrecisionByQuery();
        result_o.getFalsePositiveAtRecallByTen();

        System.out.println("------------------------------");
        System.out.println("result_pruningeCall_Data_Dir Gantt_LSI");
        result_pruningeCall_Data_Dir.showAveragePrecisionByRanklist();
        result_pruningeCall_Data_Dir.showMeanAveragePrecisionByQuery();
        result_pruningeCall_Data_Dir.getFalsePositiveAtRecallByTen();

        System.out.println("CompareTo IR");
        printPValue(result_pruningeCall_Data_Dir, result_ir);
        System.out.println("CompareTo PageRank");
        printPValue(result_pruningeCall_Data_Dir, result_pagerank);
        System.out.println("CompareTo O_CSTI");
        printPValue(result_pruningeCall_Data_Dir, result_o);

        System.out.println("CompareTo IR");
        _.compare(result_pruningeCall_Data_Dir, result_ir);
        System.out.println("CompareTo PageRank");
        _.compare(result_pruningeCall_Data_Dir, result_pagerank);
        System.out.println("CompareTo O_CSTI");
        _.compare(result_pruningeCall_Data_Dir, result_o);


        VisualCurve curve = new VisualCurve();
        curve.addLine(result_ir);
        curve.addLine(result_pagerank);
        curve.addLine(result_o);
        curve.addLine(result_pruningeCall_Data_Dir);
        curve.showChart();
//        curve.resultStore(Gantt_CONSTANTS.lsiExpExportPath_ICSME, "call_0.7_data_0.9_idtf_1.4");
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
