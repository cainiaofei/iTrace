package cn.edu.nju.cs.itrace4.core.algo.region.callthendata;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.CSTI;
import cn.edu.nju.cs.itrace4.core.algo.region.relation.StoreSubGraphInfoByThreshold;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import javafx.util.Pair;

public class UD_Call_Data_SubGraph implements CSTI{

	double callThreshold, dataThreshold;
	private RelationInfo ri;
	private StoreSubGraphInfoByThreshold storeSubGraphInfoByThreshold;
	
	public UD_Call_Data_SubGraph(double callThreshold, double dataThreshold,
			String relationPath) throws IOException, ClassNotFoundException{
		this.callThreshold = callThreshold;
		this.dataThreshold = dataThreshold;
	    FileInputStream fis = new FileInputStream(relationPath);
	    ObjectInputStream ois = new ObjectInputStream(fis);
	    ri = (RelationInfo) ois.readObject();
	    ois.close();
	    storeSubGraphInfoByThreshold = new StoreSubGraphInfoByThreshold();
	}
	
	@Override
	public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset,
			SimilarityMatrix similarityMatrix) {
		ri.setPruning(callThreshold, 2);
		UD_SubGraph_Closeness call_subGraph = new UD_SubGraph_Closeness(callThreshold,dataThreshold,
				storeSubGraphInfoByThreshold.getSubGraphs(ri),ri.getVertexIdNameMap());
		SimilarityMatrix callSimilirity = call_subGraph.improve(matrix, textDataset,matrix);
		
		ri.setPruning(2, dataThreshold);
		UD_SubGraph_Closeness data_subGraph = new UD_SubGraph_Closeness(callThreshold,dataThreshold,
				storeSubGraphInfoByThreshold.getSubGraphs(ri),ri.getVertexIdNameMap());
		return data_subGraph.improve(callSimilirity, textDataset,callSimilirity);
	}

	@Override
	public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAlgorithmName() {
		// TODO Auto-generated method stub
		return "Call_Data_SubGraph"+callThreshold+"_"+dataThreshold;
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
