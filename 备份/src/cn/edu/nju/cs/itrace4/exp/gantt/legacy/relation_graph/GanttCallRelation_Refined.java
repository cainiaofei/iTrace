package cn.edu.nju.cs.itrace4.exp.gantt.legacy.relation_graph;

import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.ir.IRModelConst;
import cn.edu.nju.cs.itrace4.relation.CallDataRelationGraph;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.relation.SubGraphInfo;
import cn.edu.nju.cs.itrace4.visual.VisualRelationGraph_RE;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.Set;

/**
 * Created by niejia on 16/1/6.
 */
public class GanttCallRelation_Refined {
    public static void main(String[] args) {
        String class_relationInfo = "data/exp/Gantt/relation/CLASS_relationInfo.ser";

        try {
            FileInputStream fis = new FileInputStream(class_relationInfo);
            ObjectInputStream ois = new ObjectInputStream(fis);
            RelationInfo ri = (RelationInfo) ois.readObject();

            RelationInfo ri_for_initial_region = (RelationInfo) ois.readObject();
            ri_for_initial_region.setPruning(0.2, 0.25);

            ri.setPruning(-1, 2);

            String rtmClassPath = "data/exp/Gantt/rtm/RTM_CLASS.txt";
            String ucPath = "data/exp/Gantt/uc";
            String classDirPath = "data/exp/Gantt/class/code";
            TextDataset textDataset = new TextDataset(ucPath, classDirPath, rtmClassPath);


            System.out.println(textDataset.getRtm());
            CallDataRelationGraph cdGraph = new CallDataRelationGraph(ri);
            CallDataRelationGraph cdGraph_pruned = new CallDataRelationGraph(ri);
            SubGraphInfo subGraphInfo = new SubGraphInfo(textDataset, cdGraph_pruned);
            for (String source : textDataset.getSourceCollection().keySet()) {
//                subGraphInfo.getFirstValidPieceCodeForSource(source,)
            }

            String layoutPath = "data/exp/Gantt/class/relationGraph/PureCall.out";
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


    private static Map<String, Set<String>> getInitialRegion() {
        return null;
    }
}
