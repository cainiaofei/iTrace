package cn.edu.nju.cs.itrace4.relation.graph;

import cn.edu.nju.cs.itrace4.relation.info.DataRelation;
import cn.edu.nju.cs.itrace4.relation.info.DataRelationList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by niejia on 15/3/2.
 */
public class DataEdge extends CodeEdge {

    private DataRelationList dataRelationList;

    public DataEdge(Integer id, EdgeType type) {
        super(id, type);
    }

    public DataEdge(Integer id, EdgeType type, CodeVertex source, CodeVertex target) {
        super(id, type, source, target);
    }


    public DataRelationList getDataRelationList() {
        return dataRelationList;
    }

    public void setDataRelationList(DataRelationList dataRelationList) {
        this.dataRelationList = dataRelationList;
    }

    public int getDataRelationSize() {
        return getDataRelationList().size();
    }

    // [keng]
    public int getDataRelationSizeByUniqueType() {
        Set<String> types = new HashSet<>();
        for (DataRelation dr : dataRelationList) {
                types.add(dr.getType());
        }
        return types.size();
    }

    public int getSharedDataFieldSize() {
        List<String> sharedData = new ArrayList<>();
        for (DataRelation dr : dataRelationList) {
            sharedData.add(dr.type + "#" + dr.hashcode);
        }
        return sharedData.size();
    }
}
