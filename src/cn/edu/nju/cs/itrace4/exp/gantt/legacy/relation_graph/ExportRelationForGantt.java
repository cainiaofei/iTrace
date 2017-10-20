package cn.edu.nju.cs.itrace4.exp.gantt.legacy.relation_graph;

import cn.edu.nju.cs.itrace4.exp.gantt.Gantt_CONSTANTS;
import cn.edu.nju.cs.itrace4.relation.CallDataRelationGraph;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.relation.info.RelationPair;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

/**
 * Created by niejia on 16/3/10.
 */
public class ExportRelationForGantt {

    public static void main(String[] args) {

        try {
            FileInputStream fis = new FileInputStream(Gantt_CONSTANTS.class_relationInfoPath);
            ObjectInputStream ois = new ObjectInputStream(fis);
            RelationInfo ri = (RelationInfo) ois.readObject();
//
            StringBuffer result = new StringBuffer();


            ri.setPruning(-1, -1);
            System.out.println(ri.getRelationGraphFile());


            CallDataRelationGraph cdGraph = new CallDataRelationGraph(ri);


            List<RelationPair> callList = ri.getCallRelationPairList();
//            System.out.println(callList.size());

            result.append("Call \n");
            for (RelationPair pair : callList) {
                int callerID = pair.getKey();
                int calleeID = pair.getValue();

                String caller = ri.getVertexNameById(callerID);
                String callee = ri.getVertexNameById(calleeID);

//                System.out.println(pair.getKey() + " " + pair.getValue() + " " + cdGraph.getCallFrequency(caller, callee));
                result.append(pair.getKey() + " " + pair.getValue() + " " + cdGraph.getCallFrequency(caller, callee));
                result.append("\n");
            }


            result.append("Data \n");

            for (RelationPair pair : ri.getDataRelationPairList()) {
                int callerID = pair.getKey();
                int calleeID = pair.getValue();

                String caller = ri.getVertexNameById(callerID);
                String callee = ri.getVertexNameById(calleeID);

//                System.out.println(pair.getKey() + " " + pair.getValue() + " " + cdGraph.getDataTypeInEdge(caller, callee));
                String dataType = cdGraph.getDataTypeInEdge(caller, callee);
                if (dataType != null) {
                    result.append(pair.getKey() + " " + pair.getValue() + " " + dataType);
                    result.append("\n");
                }

            }
            System.out.println("HaHa");

            System.out.println(result.toString());

//            _.writeFile("");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
