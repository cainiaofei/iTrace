package cn.edu.nju.cs.itrace4.core.algo;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.demo.relation.SubGraph;

public class SortByMergedClass implements Comparator<SubGraph>{
	private SimilarityMatrix mergedMatrix;
	private String requirement;//requirement
	
	public SortByMergedClass(SimilarityMatrix mergedMatrix, String requirement) {
		this.mergedMatrix = mergedMatrix;
		this.requirement = requirement;
	}
	
	@Override
	public int compare(SubGraph sub1, SubGraph sub2) {
		double sim2 = mergedMatrix.getScoreForLink(requirement, sub2.getRegionName());
		double sim1 = mergedMatrix.getScoreForLink(requirement, sub1.getRegionName());
		double diff = sim2 - sim1;
		//System.out.println("campare:"+sub1Value+":"+sub2Value);
		if(diff>0){
			return 1;
		}
		else if(diff<0){
			return -1;
		}
		else{
			return 0;
		}
	}

}
