package cn.edu.nju.cs.itrace4.core.algo.region.outerVertex.process;

import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.exp.tool.GetLinkCount;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import javafx.util.Pair;

public class UD_DataThenCallWithBonusForLone implements CSTI{
	
	protected RelationInfo ri;
	double callThreshold, dataThreshold;
	MethodTypeProcessLone methodType;
	double percent;
	Map<String,Set<String>> valid;
	
	public UD_DataThenCallWithBonusForLone(RelationInfo ri,double callThreshold,
			double dataThreshold,MethodTypeProcessLone methodType,double percent,Map<String,Set<String>> valid){
		this.ri = ri;
		this.callThreshold = callThreshold;
		this.dataThreshold = dataThreshold;
		this.methodType = methodType;
		this.percent = percent;
		this.valid = valid;
	}
	
	@Override
	public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset,
			SimilarityMatrix similarityMatrix) {
		//Map<String,Set<String>> valid = new HashMap<String,Set<String>>();
		UD_DataSubGraphWithBonusForLoneWithTrans improveBasedCall = new UD_DataSubGraphWithBonusForLoneWithTrans(ri,valid,percent);
		SimilarityMatrix afterImproveBasedCall = improveBasedCall.improve(matrix, textDataset,methodType);
		UD_CallSubGraphWithBonusForLone improveBasedData = new UD_CallSubGraphWithBonusForLone(ri,valid,percent);
//		//还是改成noTrans吧
//		UD_DataSubGraphWithBonusForLoneNoTrans improveBasedData = new
//				UD_DataSubGraphWithBonusForLoneNoTrans(ri,valid,
//				matrix,percent);
		
		return improveBasedData.improve(afterImproveBasedCall, textDataset, methodType);
	}

	@Override
	public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAlgorithmName() {
		// TODO Auto-generated method stub
		return "UD_DataThenCallWithBonusForLone"+callThreshold+"_"+dataThreshold+"_"+methodType+"_"+percent;
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
