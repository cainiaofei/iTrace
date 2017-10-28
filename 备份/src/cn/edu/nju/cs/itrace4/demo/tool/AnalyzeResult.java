package cn.edu.nju.cs.itrace4.demo.tool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.core.metrics.Result;

public class AnalyzeResult {
	
	/*
	 * write the link positive but lie end
	 * */
	public void analyzeTrueNegative(Result result,String name,
			Map<Integer,String> vertexIdNameMap) throws IOException{
		Map<String,Integer> vertexNameIdMap = reverse(vertexIdNameMap);
		BufferedWriter bw = new BufferedWriter(new FileWriter(
				//new File("output/"+name,"trueNegative"+System.currentTimeMillis()+".txt")));
				new File("output/"+name+"/trueNegative"+System.currentTimeMillis()+".txt")));
		SimilarityMatrix oracle = result.getOracle();
		SimilarityMatrix matrix = result.getMatrix();
		LinksList allLinks = matrix.allLinks();
	    Collections.sort(allLinks);
	    
	    int index = allLinks.size();
	    for (SingleLink link : allLinks) {
	        String source = link.getSourceArtifactId();
	        String target = link.getTargetArtifactId();
	        if (oracle.isLinkAboveThreshold(source, target)) {
	        	bw.write(index+":   "+vertexNameIdMap.get(target)+"--->"+source);
	        	bw.newLine();
	        } else {
            	;
            }
	        index--;
	    }
		bw.close();
	}
	
	/*
	 * write the link false 
	 */
	public void analyzeFalsePositive(Result result,String name,
			Map<Integer,String> vertexIdNameMap) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(
				new File("output/"+name+"/falsePositive"+System.currentTimeMillis()+".txt")));
		
		Map<String,Integer> vertexNameIdMap = reverse(vertexIdNameMap);
		SimilarityMatrix oracle = result.getOracle();
		SimilarityMatrix matrix = result.getMatrix();
		LinksList allLinks = matrix.allLinks();
	    Collections.sort(allLinks, Collections.reverseOrder());

	    int index = 1;
	    for (SingleLink link : allLinks) {
	        String source = link.getSourceArtifactId();
	        String target = link.getTargetArtifactId();
	        if (!oracle.isLinkAboveThreshold(source, target)) {
	        	bw.write(index+":   "+vertexNameIdMap.get(target)+"--->"+source);
	        	bw.newLine();
	        } else {
            	;
            }
	        index++;
	    }
		bw.close();
	}

	private Map<String, Integer> reverse(Map<Integer, String> vertexIdNameMap) {
		Map<String,Integer> map = new HashMap<String,Integer>();
		for(int id:vertexIdNameMap.keySet()){
			map.put(vertexIdNameMap.get(id), id);
		}
		return map;
	}
}
