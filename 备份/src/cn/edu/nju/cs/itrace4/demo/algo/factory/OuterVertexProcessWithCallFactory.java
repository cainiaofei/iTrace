package cn.edu.nju.cs.itrace4.demo.algo.factory;

import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.demo.algo.outerVertex.MeanAllocateWithCallSubGraph;
import cn.edu.nju.cs.itrace4.demo.algo.outerVertex.OuterVertexProcessWithCallSubGraph;
import cn.edu.nju.cs.itrace4.demo.algo.outerVertex.WeightAverageWithCallSubGraph;
import cn.edu.nju.cs.itrace4.demo.relation.SubGraph;

public class OuterVertexProcessWithCallFactory {
	private Map<Integer,String> vertexIdNameMap;
	private List<SubGraph> callSubGraphList;
	//store have judge in order to calculate rate which need to judge by user
	private double[][] graphs;
	private Set<Integer> loneVertexSet;
	private Map<String,Set<String>> judged;
	
	public OuterVertexProcessWithCallFactory(Map<Integer,String> vertexIdNameMap,List<SubGraph> callSubGraphList,
			double[][] graphs,Set<Integer> loneVertexSet){
		this.vertexIdNameMap = vertexIdNameMap;
		this.callSubGraphList = callSubGraphList;
		this.graphs = graphs;
		this.loneVertexSet = loneVertexSet;
	}
	
	public OuterVertexProcessWithCallSubGraph getOuterVertexProcessWithCallSubGraph(Class<?> method){
		if(method.equals(MeanAllocateWithCallSubGraph.class)){
			return new MeanAllocateWithCallSubGraph(vertexIdNameMap,callSubGraphList,judged,graphs);
		}
		else if(method.equals(WeightAverageWithCallSubGraph.class)){
			return new WeightAverageWithCallSubGraph(vertexIdNameMap,callSubGraphList,
					judged,graphs);
		}
		else{
			return null;
		}
	}
}
