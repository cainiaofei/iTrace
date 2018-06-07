package cn.edu.nju.cs.itrace4.core.algo.region.callthendata;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import cn.edu.nju.cs.itrace4.core.algo.region.util.sort.SortBySubGraph;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.demo.relation.SubGraph;

public class UD_SubGraph_ClosenessWithPenalty extends UD_SubGraph_Closeness{

	public UD_SubGraph_ClosenessWithPenalty(double callThreshold, double dataThreshold, List<SubGraph> subGraphList,
			Map<Integer, String> vertexIdNameMap) {
		super(callThreshold, dataThreshold, subGraphList, vertexIdNameMap);
	}
	

	@Override
	public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset,
			SimilarityMatrix similarityMatrix) {
		 SimilarityMatrix oracle = textDataset.getRtm();
		 SimilarityMatrix matrix_ud = new SimilarityMatrix();
		 //每个需求都会和剩下的所以类进行相似度计算  所以这里随便选择一个类就可以
		 String firstKey = getFirstKey(matrix.sourceArtifactsIds());
		 //因为是选取的一部分类 这里是防止有些类 不是被选中的
		 filterSubGraphsList(matrix.getLinksForSourceId(firstKey).keySet());
		 // 对于每个需求会有一个对应的排序
		 for(String req:matrix.sourceArtifactsIds()){
			//排序 这一步其实有没有都行（不管怎么样都是每一组选一个）
			Collections.sort(subGraphList,new SortBySubGraph(vertexIdNameMap,matrix,req));
			int maxId = subGraphList.get(0).getMaxId();
			double maxScore = matrix.getScoreForLink(req, vertexIdNameMap.get(maxId));
			for(SubGraph subGraph:subGraphList){
				List<Integer> vertexList = subGraph.getVertexList();
				
				if(vertexList.size()==1){
					matrix_ud.addLink(req, vertexIdNameMap.get(vertexList.get(0)),
							matrix.getScoreForLink(req, vertexIdNameMap.get(vertexList.get(0))));
					continue;
				}
				
				//找到最大的那个
				String represent = vertexIdNameMap.get(subGraph.getMaxId());
				double representValue = matrix.getScoreForLink(req, represent);
				if(oracle.isLinkAboveThreshold(req,represent)){
					for(int id:vertexList){
						if(hasContainedThisLink(matrix_ud,req,id)){
							matrix_ud.setScoreForLink(req, vertexIdNameMap.get(id), 
									Math.max(representValue, matrix_ud.getScoreForLink(req, vertexIdNameMap.get(id))));
						}
						else{
							double curValue = matrix.getScoreForLink(req, vertexIdNameMap.get(id));
							if(curValue==representValue){
								curValue = Math.min(representValue, curValue+(representValue)/vertexList.size());
							}
							else{
								curValue = Math.min(maxScore, curValue+(maxScore)/vertexList.size());
							}
							matrix_ud.addLink(req, vertexIdNameMap.get(id),curValue);//
						}
					}
				}
				else{
					for(int id:vertexList){
						double curValue = matrix.getScoreForLink(req, vertexIdNameMap.get(id));
						if(hasContainedThisLink(matrix_ud,req,id)){
							matrix_ud.setScoreForLink(req, vertexIdNameMap.get(id), 
									Math.max(curValue, matrix_ud.getScoreForLink(req, vertexIdNameMap.get(id))));
						}
						else{
							if(curValue==representValue){
								;
							}
							else{
								curValue = Math.max(0, curValue-(maxScore/vertexList.size()));
							}
							matrix_ud.addLink(req, vertexIdNameMap.get(id),curValue);//
						}
					}
				}
			}///
		}//for
		LinksList allLinks = matrix_ud.allLinks();
		Collections.sort(allLinks, Collections.reverseOrder());
		SimilarityMatrix res = new SimilarityMatrix();
		for(SingleLink link:allLinks){
			res.addLink(link.getSourceArtifactId(), link.getTargetArtifactId(),link.getScore());
			System.out.println(link.getSourceArtifactId()+" "+link.getTargetArtifactId()+" "+link.getScore());
		}
		//print(res);
		return res;
	}
	
	@Override
	public String getAlgorithmName() {
		// TODO Auto-generated method stub
		return "UD_SubGraph_ClosenessWithPenalty"+callThreshold+"_"+dataThreshold;
	}

}
