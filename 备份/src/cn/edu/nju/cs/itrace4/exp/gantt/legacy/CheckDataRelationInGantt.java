package cn.edu.nju.cs.itrace4.exp.gantt.legacy;

import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.relation.info.DataRelationList;
import cn.edu.nju.cs.itrace4.relation.info.RelationPair;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Created by niejia on 15/5/8.
 */
public class CheckDataRelationInGantt {

    public static void main(String[] args) {
        String class_relationInfo = "data/exp/Gantt/relation/Class_relationInfo.ser";

        try {
            FileInputStream fis = new FileInputStream(class_relationInfo);
            ObjectInputStream ois = new ObjectInputStream(fis);
            RelationInfo ri = (RelationInfo) ois.readObject();

            for (RelationPair r : ri.getDataRelationPairList()) {
                if (r.getKey() == 63 && r.getValue() == 80) {
                    DataRelationList dataRelations = ri.getDataRelationListForRelationPair(r);
                    System.out.println(ri.getDataRelationListForRelationPair(r));
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
