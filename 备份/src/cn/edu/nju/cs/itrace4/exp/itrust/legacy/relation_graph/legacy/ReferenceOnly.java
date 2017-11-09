package cn.edu.nju.cs.itrace4.exp.itrust.legacy.relation_graph.legacy;

import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.relation.CallDataRelationGraph;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.visual.VisualRelationGraph;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Created by niejia on 15/3/9.
 */
public class ReferenceOnly {
    public static void main(String[] args) {
        String class_relationInfo = "data/exp/iTrust/relation/Class_relationInfo.ser";

        try {
            FileInputStream fis = new FileInputStream(class_relationInfo);
            ObjectInputStream ois = new ObjectInputStream(fis);
            RelationInfo ri = (RelationInfo) ois.readObject();

            ri.setPruning(2, 0.4);
            System.out.println(ri.getRelationGraphFile());

            String rtmClassPath = "data/exp/iTrust/rtm/RTM_CLASS.txt";
            String ucPath = "data/exp/iTrust/uc";
            String classDirPath = "data/exp/iTrust/class/code";
            TextDataset textDataset = new TextDataset(ucPath, classDirPath, rtmClassPath);

            CallDataRelationGraph cdGraph = new CallDataRelationGraph(ri);

            String layoutPath = "data/exp/iTrust/class/relationGraph/ReferencePure.out";
            VisualRelationGraph visualRelationGraph = new VisualRelationGraph(textDataset, cdGraph, layoutPath);
            visualRelationGraph.show();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
