package cn.edu.nju.cs.itrace4.core.algo.region.util.sort;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import cn.edu.nju.cs.itrace4.core.algo.region.relation.SubGraph;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;

public class SortBySubGraph implements Comparator<SubGraph>{

	private Map<Integer, String> vertexIdNameMap;
	SimilarityMatrix matrix;
	String target;//requirement
	
	public SortBySubGraph(Map<Integer, String> vertexIdNameMap,SimilarityMatrix matrix,
			String target){
		this.vertexIdNameMap = vertexIdNameMap;
		this.matrix = matrix;
		this.target = target;
	}
	
	@Override
	public int compare(SubGraph sub1, SubGraph sub2) {
		double sub1Value = findBiggestValue(sub1);
		double sub2Value = findBiggestValue(sub2);
		double diff = sub2Value - sub1Value;
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

	/**
	 * 2017/8/12
	 * 感觉设置最大maxId时貌似是有问题呀，哦不对，虚惊一场。就是这样子的，看maxScore是不是当前值
	 */
	private double findBiggestValue(SubGraph sub1) {
		double maxScore = 0;
		//int maxId = 0;//
		int maxId = sub1.getVertexList().get(0);
		List<Integer> vertexList = sub1.getVertexList();
		for(int i = 0; i<vertexList.size(); i++){
//			System.out.println(vertexList.get(i));
//			System.out.println(vertexIdNameMap.get(vertexList.get(i)));
			double curScore = matrix.getScoreForLink(target,vertexIdNameMap.get(vertexList.get(i)));
			maxScore = Math.max(maxScore, curScore);
			if(Math.abs(curScore-maxScore)<=0.000000000001){
				maxId = vertexList.get(i);
			}
		}
		sub1.setMaxId(maxId);
		return maxScore;
	}
}
