package cn.edu.nju.cs.itrace4.exp.gantt.call_graph;

import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.ir.IRModelConst;
import cn.edu.nju.cs.itrace4.relation.CallDataRelationGraph;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.visual.VisualRelationGraph_RE;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Created by niejia on 15/3/18.
 */
public class GanttCallRelation_InitialRegion {

    public static void main(String[] args) {
        String class_relationInfo = "data/exp/Gantt/relation/CLASS_relationInfo_new.ser";

        try {
            FileInputStream fis = new FileInputStream(class_relationInfo);
            ObjectInputStream ois = new ObjectInputStream(fis);
            RelationInfo ri = (RelationInfo) ois.readObject();

            ri.setPruning(0.7, 2);

            String rtmClassPath = "data/exp/Gantt/rtm/RTM_CLASS.txt";
            String ucPath = "data/exp/Gantt/uc";
            String classDirPath = "data/exp/Gantt/class/code";
            TextDataset textDataset = new TextDataset(ucPath, classDirPath, rtmClassPath);


            System.out.println(textDataset.getRtm());
            CallDataRelationGraph cdGraph = new CallDataRelationGraph(ri);

            String layoutPath = "data/exp/Gantt/relationGraph/PureCall_VSM_intersection_InitialRegion.out";
            VisualRelationGraph_RE visualRelationGraph = new VisualRelationGraph_RE(textDataset, cdGraph, layoutPath, IRModelConst.JSD);
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
