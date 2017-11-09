package cn.edu.nju.cs.itrace4.demo.algo.innerVertex;

import java.util.Map;

import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;

public class InnerVertexProcess {
	private Map<Integer,String> vertexIdNameMap;
	public InnerVertexProcess(Map<Integer,String> vertexIdNameMap){
		this.vertexIdNameMap = vertexIdNameMap;
	}
	
	public boolean hasContainedThisLink(SimilarityMatrix matrix, String req, int id) {
		String codeTarget = vertexIdNameMap.get(id);
		if(matrix.sourceArtifactsIds().contains(req)&&
				matrix.getLinksForSourceId(req).containsKey(codeTarget)){
			return true;
		}
		return false;
	}
	
	public double allWeight(Map<String, Double> vertexMapWeight) {
		double result = 0;
		for(String vertex:vertexMapWeight.keySet()){
			result += vertexMapWeight.get(vertex);
		}
		return result;
	}
}
