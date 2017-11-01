package cn.edu.nju.cs.itrace4.myExplore;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;

/**
 * zhangsan 17/3/26
 * used to show which FP
 * */
public class ShowWhichFP {
	private Result result;
	private RelationInfo class_relation;
	public ShowWhichFP(Result result,RelationInfo class_relation){
		this.result = result;
		this.class_relation = class_relation;
	}
	
	/**
	 * print FP information
	 * */
	public void doTask(){
 		String basePath = System.getProperty("user.dir");
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(basePath+
					"/src/cn/edu/nju/cs/itrace4/myExplore/output"+result.getAlgorithmName()+".txt"));
			SimilarityMatrix matrix = result.getMatrix();
			SimilarityMatrix rtmMatrix = result.getOracle();
			
			int rank = 1;//
			LinksList lists = matrix.allLinks();
			Collections.sort(lists,Collections.reverseOrder());
			for(SingleLink link:lists){
				String source = link.getSourceArtifactId();
				String target = link.getTargetArtifactId();
				Map<String,Double> map = rtmMatrix.getLinksForSourceId(source);
				if(!map.containsKey(target)){
					bw.write(rank+":  this link is FP:"+source+"---->"+target+link.getScore());
					bw.newLine();
				}
				rank++;
			}
		bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		ShowWhichFP test = new ShowWhichFP(null,null);
		test.doTask();
		
	}
}
