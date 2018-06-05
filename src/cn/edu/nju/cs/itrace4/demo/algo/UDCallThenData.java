package cn.edu.nju.cs.itrace4.demo.algo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.core.algo.CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.demo.algo.factory.InnerVertexProcessWithCallFactory;
import cn.edu.nju.cs.itrace4.demo.algo.factory.InnerVertexProcessWithDataFactory;
import cn.edu.nju.cs.itrace4.demo.algo.factory.OuterVertexProcessWithCallFactory;
import cn.edu.nju.cs.itrace4.demo.algo.factory.OuterVertexProcessWithDataFactory;
import cn.edu.nju.cs.itrace4.demo.algo.innerVertex.InnerVertexProcessWithCallSubGraph;
import cn.edu.nju.cs.itrace4.demo.algo.innerVertex.InnerVertexProcessWithDataSubGraph;
import cn.edu.nju.cs.itrace4.demo.algo.outerVertex.OuterVertexProcessWithCallSubGraph;
import cn.edu.nju.cs.itrace4.demo.algo.outerVertex.OuterVertexProcessWithDataSubGraph;
import cn.edu.nju.cs.itrace4.demo.relation.StoreCallSubGraph;
import cn.edu.nju.cs.itrace4.demo.relation.StoreDataSubGraph;
import cn.edu.nju.cs.itrace4.demo.relation.SubGraph;
import cn.edu.nju.cs.itrace4.relation.CallDataRelationGraph;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.relation.graph.CodeEdge;
import javafx.util.Pair;

public class UDCallThenData implements CSTI{
	protected RelationInfo ri;
	double callThreshold, dataThreshold;
	private InnerVertexProcessWithCallFactory innerVertexProcessWithCallFactory;
	private InnerVertexProcessWithDataFactory innerVertexProcessWithDataFactory;
	
	private OuterVertexProcessWithCallFactory outerVertexProcessWithCallFactory;
	private OuterVertexProcessWithDataFactory outerVertexProcessWithDataFactory;
	
	private InnerVertexProcessWithCallSubGraph innerMethodWithCall;
	private InnerVertexProcessWithDataSubGraph innerMethodWithData;
	
	private OuterVertexProcessWithCallSubGraph outerMethodWithCall;
	private OuterVertexProcessWithDataSubGraph outerMethodWithData;
	
	private double[][] callGraphs;
	private double[][] dataGraphs;
	
	private List<SubGraph> callSubGraphList;
	private List<SubGraph> dataSubGraphList;
	
	private Map<Integer,String> vertexIdNameMap;
	
	private Set<Integer> callLoneVertexSet;
	private Set<Integer> dataLoneVertexSet;
	
	private Class<?> innerMethodWithCallClass;
	private Class<?> innerMethodWithDataClass;
	private Class<?> outerMethodWithCallClass;
	private Class<?> outerMethodWithDataClass;
	
	public UDCallThenData(RelationInfo ri,double callThreshold,double dataThreshold,
			InnerVertexProcessWithCallSubGraph innerMethodWithCall,Class<?> innerMethodWithCallClass,
			Class<?> innerMethodWithDataClass,Class<?> outerMethodWithCallClass,Class<?> outerMethodWithDataClass){
		this.ri = ri;
		this.callThreshold = callThreshold;
		this.dataThreshold = dataThreshold;
		this.vertexIdNameMap = ri.getVertexIdNameMap();
		
		callGraphs = describeGraphWithMatrix(new CallDataRelationGraph(ri).callEdgeScoreMap,ri.getVertexes().size());
		dataGraphs = describeGraphWithMatrix(new CallDataRelationGraph(ri).dataEdgeScoreMap,ri.getVertexes().size());
		callSubGraphList = new StoreCallSubGraph().getSubGraphs(ri);
		dataSubGraphList = new StoreDataSubGraph().getSubGraphs(ri);
		
		this.innerVertexProcessWithCallFactory = new InnerVertexProcessWithCallFactory(vertexIdNameMap,callSubGraphList,
				callGraphs);
		this.innerVertexProcessWithDataFactory = new InnerVertexProcessWithDataFactory(vertexIdNameMap,dataSubGraphList,
				dataGraphs);
		this.outerVertexProcessWithCallFactory = new OuterVertexProcessWithCallFactory(vertexIdNameMap,callSubGraphList,
				callGraphs,callLoneVertexSet);
		this.outerVertexProcessWithDataFactory = new OuterVertexProcessWithDataFactory(vertexIdNameMap,dataSubGraphList,
				dataGraphs,dataLoneVertexSet);
		
		this.innerMethodWithCallClass = innerMethodWithCallClass;
		this.innerMethodWithDataClass = innerMethodWithDataClass;
		this.outerMethodWithCallClass = outerMethodWithCallClass;
		this.outerMethodWithDataClass = outerMethodWithDataClass;
	}
	
	
	@Override
	public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset,
			SimilarityMatrix similarityMatrix) {
		
		filterSubGraphsList(callSubGraphList,matrix.targetArtifactsIds());
		filterSubGraphsList(dataSubGraphList,matrix.targetArtifactsIds());
		
		//
		generateVertexProcessObject();
		
		Map<String,Set<String>> valid = new HashMap<String,Set<String>>();
		SimilarityMatrix matrix_afterCall = new SimilarityMatrix();
		innerMethodWithCall.processInnerVertexWithCallSubGraph(matrix, matrix_afterCall, textDataset);
		//valid
		outerMethodWithCall.processOuterVertexWithDataSubGraph(similarityMatrix, matrix_afterCall);
		
		SimilarityMatrix matrix_ud = new SimilarityMatrix();
		innerMethodWithData.processInnerVertex(matrix_afterCall, matrix_ud, textDataset);
		outerMethodWithData.processOuterVertexWithDataSubGraph(matrix_afterCall, matrix_ud);
		
		return matrix_ud;
	}

	
	
	private void generateVertexProcessObject() {
		innerMethodWithCall = innerVertexProcessWithCallFactory.
				getInnerVertexProcessWithCallSubGraphObj(innerMethodWithCallClass);
		innerMethodWithData = innerVertexProcessWithDataFactory.
				getInnerVertexProcessWithDataSubGraphObj(innerMethodWithDataClass);
		this.outerMethodWithCall = this.outerVertexProcessWithCallFactory.
				getOuterVertexProcessWithCallSubGraph(innerMethodWithCallClass);
		
	}


	public void filterSubGraphsList(List<SubGraph> subGraphList,Set<String> set){
		for(SubGraph subGraph:subGraphList){
			List<Integer> vertexList = subGraph.getVertexList();
			Iterator<Integer> ite = vertexList.iterator();
			while(ite.hasNext()){
				if(!set.contains(vertexIdNameMap.get(ite.next()))){
					ite.remove();
				}
			}
		}
		
		Iterator<SubGraph> subGraphIte = subGraphList.iterator();
		while(subGraphIte.hasNext()){
			if(subGraphIte.next().getVertexList().size()==0){
				subGraphIte.remove();
			}
		}
	}
	
	private double[][] describeGraphWithMatrix(Map<CodeEdge, Double> edgeScoreMap, int size) {
		double[][] matrix = new double[size+1][size+1];
		for(CodeEdge edge:edgeScoreMap.keySet()){
			int callerId = edge.getSource().getId();
			int calleeId = edge.getTarget().getId();
			double score = edgeScoreMap.get(edge);
			matrix[callerId][calleeId] = score;
		}
		return matrix;
	}
	
	
	@Override
	public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAlgorithmName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<String, String>> getAlgorithmParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDetails() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getCorrectImprovedTargetsList() {
		// TODO Auto-generated method stub
		return null;
	}

}
