package cn.edu.nju.cs.itrace4.exp.gantt.preprogress;

import cn.edu.nju.cs.itrace4.exp.gantt.Gantt_CONSTANTS;
import cn.edu.nju.cs.itrace4.relation.CallDataRelationGraph;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.util.io._;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by niejia on 16/3/28.
 */
public class HandleClassIntersectionGantt {

    private static Set<String> oracleClassSet;

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        String classDirPath = "data/exp/Gantt/class/code";
        String oracle = "data/exp/Gantt/rtm/RTM_CLASS.txt";

        FileInputStream fis = new FileInputStream(Gantt_CONSTANTS.class_relationInfoPath);
        ObjectInputStream ois = new ObjectInputStream(fis);
        RelationInfo ri = (RelationInfo) ois.readObject();
        CallDataRelationGraph cdGraph = new CallDataRelationGraph(ri);

        String input = _.readFile(oracle);
        String[] tokens = input.split("\n");
        oracleClassSet = new LinkedHashSet<>();

        StringBuffer sb = new StringBuffer();
        for (String str : tokens) {
            String className = str.split(" ")[1];
            oracleClassSet.add(className);
        }

        handleCode(classDirPath, cdGraph);
        handleOracle(oracle, cdGraph);
    }

    private static void handleOracle(String oracle, CallDataRelationGraph cdGraph) {
        String input = _.readFile(oracle);
        String[] tokens = input.split("\n");

        StringBuffer sb = new StringBuffer();
        for (String str : tokens) {
            String className = str.split(" ")[1];


            if (cdGraph.getNeighboursByCall(className) != null
                    && cdGraph.getNeighboursByData(className) != null) {
                if (cdGraph.getNeighboursByCall(className).size() == 0
                        && cdGraph.getNeighboursByData(className).size() == 0) {
//                System.out.println(" className = " + className);
                } else {
                    sb.append(str);
                    sb.append("\n");
                }
            }


        }

        System.out.println(sb.toString());
    }

    private static void handleCode(String classDirPath, CallDataRelationGraph cdGraph) {
        File file = new File(classDirPath);
        Set<String> classSet = new LinkedHashSet<>();

        for (File f : file.listFiles()) {
            String fileName = f.getName();
            String className = fileName.split(".txt")[0];

            if (cdGraph.getNeighboursByCall(className).size() == 0
                    && cdGraph.getNeighboursByData(className).size() == 0) {
                classSet.add(className);

                f.delete();
                System.out.println(" className = " + className);
            }
        }
    }

}
