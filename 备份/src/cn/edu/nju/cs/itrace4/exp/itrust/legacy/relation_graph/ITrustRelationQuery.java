package cn.edu.nju.cs.itrace4.exp.itrust.legacy.relation_graph;

import cn.edu.nju.cs.itrace4.relation.CallDataRelationGraph;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Created by niejia on 15/12/31.
 */
public class ITrustRelationQuery {
    public static void main(String[] args) {
        String class_relationInfo = "data/exp/iTrust/relation/Class_relationInfo.ser";

        try {
            FileInputStream fis = new FileInputStream(class_relationInfo);
            ObjectInputStream ois = new ObjectInputStream(fis);
            RelationInfo ri = (RelationInfo) ois.readObject();

            ri.setPruning(-1, -1);
            CallDataRelationGraph cdGraph = new CallDataRelationGraph(ri);

            cdGraph.printCallEdgeInfo("LOINCDAO", "UpdateLOINCListAction");
            cdGraph.printDataEdgeInfo("LOINCDAO", "UpdateLOINCListAction");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
