package cn.edu.nju.cs.itrace4.demo;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.UD_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.region.fixedcount.UD_CSTI_Judge_Group;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.ir.IRModelConst;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.demo.specifyMixture.UD_CSTI_First_Five;
import cn.edu.nju.cs.itrace4.demo.specifyMixture.UD_CSTI_First_Ten;
import cn.edu.nju.cs.itrace4.exp.gantt.Gantt_CONSTANTS;
import cn.edu.nju.cs.itrace4.explore.ShowWhichFP;
import cn.edu.nju.cs.itrace4.explore.UD_CSTI_MyChange;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.visual.VisualCurve;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Created by niejia on 16/3/22.
 */
public class Gantt_VSM_ICSME {


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
        
        Result result_UD_CSTI = IR.compute(textDataset, IRModelConst.VSM, new UD_CSTI(class_relation));
        result_UD_CSTI.showAveragePrecisionByRanklist();
        result_UD_CSTI.showMeanAveragePrecisionByQuery();
        result_UD_CSTI.getFalsePositiveAtRecallByTen();
        ShowWhichFP showFP = new ShowWhichFP(result_UD_CSTI,class_relation);
        showFP.doTask();
        
        Result result_UD_CSTI_First_Ten = IR.compute(textDataset, IRModelConst.VSM, new UD_CSTI_First_Ten(class_relation,10));
        result_UD_CSTI_First_Ten.showAveragePrecisionByRanklist();
        result_UD_CSTI_First_Ten.showMeanAveragePrecisionByQuery();
        result_UD_CSTI_First_Ten.getFalsePositiveAtRecallByTen();
        showFP = new ShowWhichFP(result_UD_CSTI_First_Ten,class_relation);
        showFP.doTask();
        
        
        Result result_UD_CSTI_First_Five = IR.compute(textDataset, IRModelConst.VSM, new UD_CSTI_First_Five(class_relation,5));
        result_UD_CSTI_First_Five.showAveragePrecisionByRanklist();
        result_UD_CSTI_First_Five.showMeanAveragePrecisionByQuery();
        result_UD_CSTI_First_Five.getFalsePositiveAtRecallByTen();
        showFP = new ShowWhichFP(result_UD_CSTI_First_Five,class_relation);
        showFP.doTask();
        
        
        Result result_UD_CSTI_Judge_Group = IR.compute(textDataset, IRModelConst.VSM, new UD_CSTI_Judge_Group(class_relation));
        result_UD_CSTI_Judge_Group.showAveragePrecisionByRanklist();
        result_UD_CSTI_Judge_Group.showMeanAveragePrecisionByQuery();
        result_UD_CSTI_Judge_Group.getFalsePositiveAtRecallByTen();
        showFP = new ShowWhichFP(result_UD_CSTI_Judge_Group,class_relation);
        showFP.doTask();
        
        Result result_UD_CSTI_MyChange = IR.compute(textDataset, IRModelConst.VSM, new UD_CSTI_MyChange(class_relation));
        result_UD_CSTI_MyChange.showAveragePrecisionByRanklist();
        result_UD_CSTI_MyChange.showMeanAveragePrecisionByQuery();
        result_UD_CSTI_MyChange.getFalsePositiveAtRecallByTen();
        showFP = new ShowWhichFP(result_UD_CSTI_MyChange,class_relation);
        showFP.doTask();
        
        VisualCurve curve = new VisualCurve();
        curve.addLine(result_UD_CSTI);
        curve.addLine(result_UD_CSTI_First_Ten);
        curve.addLine(result_UD_CSTI_First_Five);
        curve.addLine(result_UD_CSTI_Judge_Group);
        curve.addLine(result_UD_CSTI_MyChange);
        curve.showChart();
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
