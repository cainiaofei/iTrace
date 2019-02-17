package cn.edu.nju.cs.itrace4.core.algo.region.relation;

import cn.edu.nju.cs.itrace4.relation.CallDataRelationGraph;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.relation.graph.CodeEdge;
import cn.edu.nju.cs.itrace4.relation.graph.CodeVertex;
import edu.uci.ics.jung.graph.Graph;

public class MyCallDataRelationGraph extends CallDataRelationGraph{

	public MyCallDataRelationGraph(RelationInfo relationInfo) {
		super(relationInfo);
	}
	
	public Graph<CodeVertex, CodeEdge> getDirGraph(){
		return this.dirGraph;
	}
	
	public Graph<CodeVertex, CodeEdge> getUnDirGraph(){
		return this.unDirGraph;
	}

}
