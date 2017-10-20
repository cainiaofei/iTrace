package cn.edu.nju.cs.itrace4.exp.itrust.call_graph;

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
public class iTrustCallRelation {

    public static void main(String[] args) {
        String class_relationInfo = "data/exp/iTrust/relation/CLASS_relationInfo_131.ser";

        try {
            FileInputStream fis = new FileInputStream(class_relationInfo);
            ObjectInputStream ois = new ObjectInputStream(fis);
            RelationInfo ri = (RelationInfo) ois.readObject();

            ri.setPruning(-1, 2);

            String rtmClassPath = "data/exp/iTrust/rtm/RTM_CLASS.txt";
            String ucPath = "data/exp/iTrust/uc";
            String classDirPath = "data/exp/iTrust/class/code";
            TextDataset textDataset = new TextDataset(ucPath, classDirPath, rtmClassPath);


            System.out.println(textDataset.getRtm());
            CallDataRelationGraph cdGraph = new CallDataRelationGraph(ri);

            String layoutPath = "data/exp/iTrust/relationGraph/PureCall_intersection.out";
            VisualRelationGraph_RE visualRelationGraph = new VisualRelationGraph_RE(textDataset, cdGraph, layoutPath, IRModelConst.VSM);
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
