package cn.edu.nju.cs.itrace4.demo.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.commons.math3.stat.inference.MannWhitneyUTest;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.None_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.prealgo.UD_CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.ir.IRModelConst;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.demo.specifyByGroup.UD_CSTI_First_Count_EveryGroup;
import cn.edu.nju.cs.itrace4.demo.specifyByGroup.UD_CSTI_First_Count_EveryGroup_Format;
import cn.edu.nju.cs.itrace4.demo.specifyMixture.UD_CSTI_First_Count_Percent;
import cn.edu.nju.cs.itrace4.exp.itrust.ITRUST_CONSTANTS;
import cn.edu.nju.cs.itrace4.explore.ShowWhichFP;
import cn.edu.nju.cs.itrace4.explore.UD_CSTI_MyChange;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.visual.VisualCurve;

public class Gantt_VSM_ICSME_Percent {
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
        Result result_ud_csti = IR.compute(textDataset, IRModelConst.VSM, new UD_CSTI(class_relation));
        Result result_ud_csti_first_ten_percent = IR.compute(textDataset, IRModelConst.VSM, 
        		new UD_CSTI_First_Count_Percent(class_relation,0.01));
        Result result_ud_csti_first_twenty_percent = IR.compute(textDataset, IRModelConst.VSM, 
        		new UD_CSTI_First_Count_Percent(class_relation,0.015));
        Result result_ud_csti_first_fifty_percent = IR.compute(textDataset, IRModelConst.VSM, 
        		new UD_CSTI_First_Count_Percent(class_relation,0.4));
        
        VisualCurve curve = new VisualCurve();
        curve.addLine(result_ir);
        curve.addLine(result_ud_csti);
        curve.addLine(result_ud_csti_first_ten_percent);
        curve.addLine(result_ud_csti_first_twenty_percent);
        curve.addLine(result_ud_csti_first_fifty_percent);
        //curve.addLine(result_ud_csti_first_fifty_percent);
        //curve.addLine(result_UD_CSTI_First_Five_EveryGroup);
        //curve.addLine(result_UD_CSTI_First_Five_EveryGroup_Format);
        curve.showChart();
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        run();
    }
}
