package cn.edu.nju.cs.itrace4.exp.jhotdraw.call_graph;

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
 * Created by niejia on 15/12/31.
 */
public class JHotDrawCallRelation {
    public static void main(String[] args) {
        String class_relationInfo = "data/exp/JHotDraw/relation_intersection/Class_relationInfo_144.ser";

        try {
            FileInputStream fis = new FileInputStream(class_relationInfo);
            ObjectInputStream ois = new ObjectInputStream(fis);
            RelationInfo ri = (RelationInfo) ois.readObject();

            ri.setPruning(-1, 2);
            System.out.println(ri.getRelationGraphFile());

            String rtmClassPath = "data/exp/JHotDraw/rtm/RTM_CLASS.txt";
            String ucPath = "data/exp/JHotDraw/uc";
            String classDirPath = "data/exp/JHotDraw/class/code";
            TextDataset textDataset = new TextDataset(ucPath, classDirPath, rtmClassPath);

            CallDataRelationGraph cdGraph = new CallDataRelationGraph(ri);

            String layoutPath = "data/exp/JHotDraw/relationGraph/PureCall_VSM_intersection";
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
