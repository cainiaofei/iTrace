package cn.edu.nju.cs.itrace4.relation.graph;

import cn.edu.nju.cs.itrace4.relation.info.CallRelationList;

/**
 * Created by niejia on 15/3/2.
 */
public class CallEdge extends CodeEdge {

    private CallRelationList callRelationList;

    public CallEdge(Integer id, EdgeType type) {
        super(id, type);
    }

    public CallEdge(Integer id, EdgeType type, CodeVertex source, CodeVertex target) {
        super(id, type, source, target);
    }

    public int getCallRelationSize() {
        return getCallRelationList().size();
    }


    public CallRelationList getCallRelationList() {
        return callRelationList;
    }

    public void setCallRelationList(CallRelationList callRelationList) {
        this.callRelationList = callRelationList;
    }
}
