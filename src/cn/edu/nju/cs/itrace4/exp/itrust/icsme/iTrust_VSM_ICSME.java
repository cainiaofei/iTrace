package cn.edu.nju.cs.itrace4.exp.itrust.icsme;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.None_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.prealgo.O_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.prealgo.PageRank_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.prealgo.UseEdge;
import cn.edu.nju.cs.itrace4.core.algo.icse.PruningCall_Data_Connection_Closenss;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.ir.IRModelConst;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.exp.itrust.ITRUST_CONSTANTS;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.util.Setting;
import cn.edu.nju.cs.itrace4.visual.VisualCurve;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

/**
 * Created by niejia on 16/3/22.
 */
public class iTrust_VSM_ICSME {

    public static void run() throws IOException, ClassNotFoundException {
        TextDataset textDataset = new TextDataset(ITRUST_CONSTANTS.ucPath, ITRUST_CONSTANTS.classDirPath, ITRUST_CONSTANTS.rtmClassPath);

        FileInputStream fis = new FileInputStream(ITRUST_CONSTANTS.class_relationInfoPathWhole);
        ObjectInputStream ois = new ObjectInputStream(fis);
        RelationInfo class_relation = (RelationInfo) ois.readObject();

        FileInputStream fisForO = new FileInputStream(ITRUST_CONSTANTS.class_relationInfoPathWhole);
        ObjectInputStream oisForO = new ObjectInputStream(fisForO);
        RelationInfo class_relationForO = (RelationInfo) oisForO.readObject();

        FileInputStream fisForAllDependencies = new FileInputStream(ITRUST_CONSTANTS.class_relationInfoPathWhole);
        ObjectInputStream oisForAllDependencies = new ObjectInputStream(fisForAllDependencies);
        RelationInfo class_relationForAllDependencies = (RelationInfo) oisForAllDependencies.readObject();



        Result result_ir = IR.compute(textDataset, IRModelConst.VSM, new None_CSTI());
        result_ir.showMatrix();
        Result result_o = IR.compute(textDataset, IRModelConst.VSM, new O_CSTI(class_relation));
        Result result_pagerank = IR.compute(textDataset, IRModelConst.VSM, new PageRank_CSTI(class_relation));

        class_relation.setPruning(Setting.callThreshold, Setting.dataThreshold);
        class_relationForO.setPruning(-1, -1);
        class_relationForAllDependencies.setPruning(-1, -1);

        Result result_pruningeCall_Data_Dir = IR.compute(textDataset, IRModelConst.VSM, 
        		new PruningCall_Data_Connection_Closenss(class_relation, class_relationForO, 
        				class_relationForAllDependencies,
        				UseEdge.Call, 1.0, 1.0));

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
        System.out.println("result_pruningeCall_Data_Dir iTrust_VSM");
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
        compare(result_pruningeCall_Data_Dir, result_ir);
        System.out.println("CompareTo PageRank");
        compare(result_pruningeCall_Data_Dir, result_pagerank);
        System.out.println("CompareTo O_CSTI");
        compare(result_pruningeCall_Data_Dir, result_o);


        VisualCurve curve = new VisualCurve();
        curve.addLine(result_ir);
        curve.addLine(result_pagerank);
        curve.addLine(result_o);
        curve.addLine(result_pruningeCall_Data_Dir);
        curve.showChart();
//        curve.resultStore(ITRUST_CONSTANTS.vsmExpExportPath_ICSME, "call_0.7_data_0.9_idtf_1.4");

//
//        System.out.println("CompareTo IR");
//        compare(result_pruningeCall_Data_Dir, result_ir);
//
//        System.out.println("CompareTo PageRank");
//
//        System.out.println("CompareTo O_CSTI");
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

    private static void compare(Result ours, Result compareTo) {
        List<Double> oursPrecisionList = ours.getPrecisionAtRecallByTen();
        List<Double> compareToPrecisionList = compareTo.getPrecisionAtRecallByTen();
        List<Integer> oursFP = ours.getFalsePositiveAtRecallByTen();
        List<Integer> compareFP = compareTo.getFalsePositiveAtRecallByTen();

        int recall = 10;

        for (int i = 0; i < oursPrecisionList.size(); i++) {
            double ourPrecision = oursPrecisionList.get(i);
            double theirPrecision = compareToPrecisionList.get(i);
            System.out.println("Recall " + recall);
            System.out.println(ourPrecision - theirPrecision);
            int ourFP = oursFP.get(i);
            int theirFP = compareFP.get(i);
            System.out.println(ourFP - theirFP);
            recall += 10;
        }
    }

//    private static void comparePrecision(Result ours, Result compareTo) {
//        List<Double> oursPrecisionList = ours.getPrecisionAtRecallByTen();
//        List<Double> compareToPrecisionList = compareTo.getPrecisionAtRecallByTen();
//
//        int recall = 10;
//
//        for (int i = 0; i < oursPrecisionList.size(); i++) {
//            double ourPrecision = oursPrecisionList.get(i);
//            double theirPrecision = compareToPrecisionList.get(i);
//            System.out.println("Recall " + recall);
//            System.out.println(ourPrecision - theirPrecision);
//            recall += 10;
//        }
//    }
//
//    private static void compareFalsePositive(Result ours, Result compareTo) {
//        List<Integer> oursFP = ours.getFalsePositiveAtRecallByTen();
//        List<Integer> compareFP = compareTo.getFalsePositiveAtRecallByTen();
//
//        int recall = 10;
//
//        for (int i = 0; i < oursFP.size(); i++) {
//            int ourFP = oursFP.get(i);
//            int theirFP = compareFP.get(i);
//            System.out.println("Recall " + recall);
//            System.out.println(ourFP - theirFP);
//            recall += 10;
//        }
//    }
}
