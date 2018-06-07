package cn.edu.nju.cs.itrace4.demo.boot;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.commons.math3.stat.inference.MannWhitneyUTest;

import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.ir.IRModelConst;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.demo.remainPR.UD_CSTI_First_Count_Percent_Remain;
import cn.edu.nju.cs.itrace4.demo.specifyMixture.UD_CSTI_First_Count_Percent;
import cn.edu.nju.cs.itrace4.exp.gantt.Gantt_CONSTANTS;
import cn.edu.nju.cs.itrace4.myExplore.ShowWhichFP;
import cn.edu.nju.cs.itrace4.myExplore.UD_CSTI_MyChange;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.visual.VisualCurve;

public class Gantt_VSM_ICSME_Percent_Remain {
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
        
        IR.compute(textDataset, IRModelConst.VSM, 
        		new UD_CSTI_First_Count_Percent_Remain(class_relation,0.1));
        IR.compute(textDataset, IRModelConst.VSM, 
            	new UD_CSTI_First_Count_Percent_Remain(class_relation,0.2));
        IR.compute(textDataset, IRModelConst.VSM, 
        		new UD_CSTI_First_Count_Percent_Remain(class_relation,0.5));
        
       
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        run();
    }
}
