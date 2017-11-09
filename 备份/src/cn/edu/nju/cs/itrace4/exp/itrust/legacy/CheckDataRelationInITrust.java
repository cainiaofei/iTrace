package cn.edu.nju.cs.itrace4.exp.itrust.legacy;

import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.relation.info.DataRelationList;
import cn.edu.nju.cs.itrace4.relation.info.RelationPair;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Created by niejia on 15/3/9.
 */
public class CheckDataRelationInITrust {

    public static void main(String[] args) {
        String class_relationInfo = "data/exp/iTrust/relation/Class_relationInfo.ser";

        try {
            FileInputStream fis = new FileInputStream(class_relationInfo);
            ObjectInputStream ois = new ObjectInputStream(fis);
            RelationInfo ri = (RelationInfo) ois.readObject();
//            System.out.println(ri.getRelationGraphFile());

            for (RelationPair r : ri.getDataRelationPairList()) {
                if (r.getKey() == 42 && r.getValue() == 131) {
                    DataRelationList dataRelations = ri.getDataRelationListForRelationPair(r);
                    System.out.println(ri.getDataRelationListForRelationPair(r));
                }

//                if (r.getKey() == 130 || r.getValue() == 130) {
//                    DataRelationList dataRelations = ri.getDataRelationListForRelationPair(r);
//                    for (DataRelation dr : dataRelations) {
////                        if (dr.type.equals("String")) {
//                            System.out.println(r.getKey() + " " + r.getValue());
////                        }
//                    }
//                }
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
