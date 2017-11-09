package cn.edu.nju.cs.itrace4.demo.algo;

import java.util.List;
import java.util.Map;

import cn.edu.nju.cs.itrace4.core.algo.CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.demo.relation.StoreCallSubGraph;
import cn.edu.nju.cs.itrace4.demo.relation.StoreDataSubGraph;
import cn.edu.nju.cs.itrace4.demo.relation.SubGraph;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import javafx.util.Pair;

public class UD_CallSubGraph_Then_DataSubGraph_Closeness implements CSTI{

	protected StoreCallSubGraph storeCallSubGraph;
	protected StoreDataSubGraph storeDataSubGraph;
	protected RelationInfo ri;
	
	
	double callThreshold, dataThreshold;
	protected Map<Integer, String> vertexIdNameMap;
	
	public UD_CallSubGraph_Then_DataSubGraph_Closeness(double callThreshold, double dataThreshold,  
			Map<Integer, String> vertexIdNameMap,RelationInfo ri){
		this.callThreshold = callThreshold;
		this.dataThreshold = dataThreshold;
		this.ri = ri;
		this.vertexIdNameMap = vertexIdNameMap;
		this.storeCallSubGraph = new StoreCallSubGraph();
		this.storeDataSubGraph = new StoreDataSubGraph();
	}
	
	
	@Override
	public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset,
			SimilarityMatrix similarityMatrix) {
		List<SubGraph> callSubGraph = storeCallSubGraph.getSubGraphs(ri);
		UD_SubGraph_Closeness improveBasedCall = new UD_SubGraph_Closeness(callThreshold,dataThreshold,
				callSubGraph,vertexIdNameMap);
		SimilarityMatrix afterImproveBasedCall = improveBasedCall.improve(matrix, textDataset,matrix);
		
		List<SubGraph> dataSubGraph = storeDataSubGraph.getSubGraphs(ri);
		UD_SubGraph_Closeness improveBasedData = new UD_SubGraph_Closeness(callThreshold,dataThreshold,
				dataSubGraph,vertexIdNameMap);
		
		return improveBasedData.improve(afterImproveBasedCall, textDataset, afterImproveBasedCall);
	}

	@Override
	public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAlgorithmName() {
		// TODO Auto-generated method stub
		return "Call_Then_Data";
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
