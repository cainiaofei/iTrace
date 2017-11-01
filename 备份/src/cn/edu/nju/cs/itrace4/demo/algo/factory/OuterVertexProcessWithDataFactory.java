package cn.edu.nju.cs.itrace4.demo.algo.factory;

import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.demo.algo.outerVertex.MeanAllocateWithDataSubGraph;
import cn.edu.nju.cs.itrace4.demo.algo.outerVertex.OuterVertexProcessWithDataSubGraph;
import cn.edu.nju.cs.itrace4.demo.algo.outerVertex.WeightAverageWithDataSubGraph;
import cn.edu.nju.cs.itrace4.demo.relation.SubGraph;

public class OuterVertexProcessWithDataFactory {
	private Map<Integer,String> vertexIdNameMap;
	private List<SubGraph> callSubGraphList;
	//store have judge in order to calculate rate which need to judge by user
	private double[][] graphs;
	private Set<Integer> loneVertexSet;
	private Map<String,Set<String>> judged;
	
	public OuterVertexProcessWithDataFactory(Map<Integer,String> vertexIdNameMap,List<SubGraph> callSubGraphList,
			double[][] graphs,Set<Integer> loneVertexSet){
		this.vertexIdNameMap = vertexIdNameMap;
		this.callSubGraphList = callSubGraphList;
		this.graphs = graphs;
		this.loneVertexSet = loneVertexSet;
	}
	
	public OuterVertexProcessWithDataSubGraph getOuterVertexProcessWithDataSubGraph(Class<?> method){
		if(method.equals(MeanAllocateWithDataSubGraph.class)){
			return new MeanAllocateWithDataSubGraph(vertexIdNameMap,callSubGraphList,judged,graphs);
		}
		else if(method.equals(WeightAverageWithDataSubGraph.class)){
			return new WeightAverageWithDataSubGraph(vertexIdNameMap,callSubGraphList,
					judged,graphs);
		}
		else{
			return null;
		}
	}
}
