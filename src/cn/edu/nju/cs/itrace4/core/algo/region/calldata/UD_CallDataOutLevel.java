package cn.edu.nju.cs.itrace4.core.algo.region.calldata;

import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.demo.algo.outerVertex.process.MethodTypeProcessLone;
import cn.edu.nju.cs.itrace4.demo.relation.StoreSubGraphInfoByThreshold;
import cn.edu.nju.cs.itrace4.exp.tool.GetLinkCount;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import javafx.util.Pair;

public class UD_CallDataOutLevel implements CSTI{
	
	protected RelationInfo ri;
	double callThreshold, dataThreshold;
	MethodTypeProcessLone methodType;
	double percent;
	Map<String,Set<String>> valid;
	StoreSubGraphInfoByThreshold cdg;
	
	public UD_CallDataOutLevel(RelationInfo ri,double callThreshold,
			double dataThreshold,MethodTypeProcessLone methodType,double percent,
			Map<String,Set<String>> valid){
		this.ri = ri;
		this.callThreshold = callThreshold;
		this.dataThreshold = dataThreshold;
		this.methodType = methodType;
		this.percent = percent;
		this.valid = valid;
		this.cdg = new StoreSubGraphInfoByThreshold(callThreshold,dataThreshold);
	}
	
	@Override
	public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset,
			SimilarityMatrix similarityMatrix) {
		//Map<String,Set<String>> valid = new HashMap<String,Set<String>>();
		UD_CallDataWithBonusForLone improveBasedCallData = new UD_CallDataWithBonusForLone(
				ri,valid,percent,cdg);
		return improveBasedCallData.improve(matrix, textDataset,methodType);
//		UD_CallSubGraphWithBonusForLone improveBasedCall = new UD_CallSubGraphWithBonusForLone(ri,valid,percent);
//		SimilarityMatrix afterImproveBasedCall = improveBasedCall.improve(matrix, textDataset,methodType);
//		UD_DataSubGraphWithBonusForLoneWithTrans improveBasedData = new
//				UD_DataSubGraphWithBonusForLoneWithTrans(ri,valid,
//				matrix,percent);
//		return improveBasedData.improve(afterImproveBasedCall, textDataset, methodType);
	}

	@Override
	public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAlgorithmName() {
		// TODO Auto-generated method stub
		return "UD_CallDataWithBonusForLone"+callThreshold+"_"+dataThreshold+"_"+methodType+"_"+percent;
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
