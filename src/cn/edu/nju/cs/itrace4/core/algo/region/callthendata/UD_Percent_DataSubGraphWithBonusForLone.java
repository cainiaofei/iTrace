package cn.edu.nju.cs.itrace4.core.algo.region.callthendata;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.CSTI;
import cn.edu.nju.cs.itrace4.core.algo.region.util.sort.SortBySubGraph;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.core.document.StringHashSet;
import cn.edu.nju.cs.itrace4.demo.relation.StoreDataSubGraph;
import cn.edu.nju.cs.itrace4.demo.relation.SubGraph;
import cn.edu.nju.cs.itrace4.relation.CallDataRelationGraph;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.relation.graph.CodeEdge;
import javafx.util.Pair;

public class UD_Percent_DataSubGraphWithBonusForLone implements CSTI{

	private double[][] graphs;
	private List<SubGraph> dataSubGraphList;
	protected Map<Integer, String> vertexIdNameMap;
	private Set<Integer> loneVertexSet = new HashSet<Integer>();
	private double percent;
	
	public UD_Percent_DataSubGraphWithBonusForLone(RelationInfo ri,double percent){
		dataSubGraphList = new StoreDataSubGraph().getSubGraphs(ri);
		graphs = describeGraphWithMatrix(new CallDataRelationGraph(ri).dataEdgeScoreMap,ri.getVertexes().size());
		vertexIdNameMap = ri.getVertexIdNameMap();
		this.percent = percent;
	}
	
	private double[][] describeGraphWithMatrix(Map<CodeEdge, Double> dataEdgeScoreMap, int size) {
		double[][] matrix = new double[size+1][size+1];
		for(CodeEdge edge:dataEdgeScoreMap.keySet()){
			int callerId = edge.getSource().getId();
			int calleeId = edge.getTarget().getId();
			double score = dataEdgeScoreMap.get(edge);
			matrix[callerId][calleeId] = score;
			matrix[calleeId][callerId] = score;
		}
		return matrix;
	}

	private void fillLoneVertex(Set<Integer> loneVertexSet, List<SubGraph> callSubGraphList) {
		for(SubGraph subGraph:dataSubGraphList){
			if(subGraph.getVertexList().size()==1){
				loneVertexSet.add(subGraph.getVertexList().get(0));
			}
		}
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
		 fillLoneVertex(loneVertexSet,dataSubGraphList);
		 // 对于每个需求会有一个对应的排序
		 for(String req:matrix.sourceArtifactsIds()){
			//排序 这一步其实有没有都行（不管怎么样都是每一组选一个）
			Collections.sort(dataSubGraphList,new SortBySubGraph(vertexIdNameMap,matrix,req));
			int maxId = dataSubGraphList.get(0).getMaxId();
			double maxScore = matrix.getScoreForLink(req, vertexIdNameMap.get(maxId));
			
			int subGraphAmount = dataSubGraphList.size();
			int index = 1;
			for(SubGraph subGraph:dataSubGraphList){
				List<Integer> vertexList = subGraph.getVertexList();
				
				if(vertexList.size()==1&&!hasContainedThisLink(matrix_ud, req, vertexList.get(0))){
					matrix_ud.addLink(req, vertexIdNameMap.get(vertexList.get(0)),
							matrix.getScoreForLink(req, vertexIdNameMap.get(vertexList.get(0))));
					continue;
				}
				if(vertexList.size()==1){///////被坑死了
					continue;
				}
				//找到最大的那个
				String represent = vertexIdNameMap.get(subGraph.getMaxId());
				double representValue = matrix.getScoreForLink(req, represent);
				if(oracle.isLinkAboveThreshold(req,represent)&&index<(subGraphAmount*percent)){
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
					
					/**
					 * 给单个的点bonus  有可能这个链接还没有
					 **/
					for(int loneVertex:loneVertexSet){
						double curValue =  matrix.getScoreForLink(req, vertexIdNameMap.get(loneVertex));
						double bonus = giveBonusForLonePoint(graphs,subGraph,loneVertex,representValue-curValue); 
						if(hasContainedThisLink(matrix_ud, req, loneVertex)){
							double preValue = matrix_ud.getScoreForLink(req, vertexIdNameMap.get(loneVertex));
							matrix_ud.setScoreForLink(req, vertexIdNameMap.get(loneVertex),bonus+preValue);
						}
						else{
							matrix_ud.addLink(req, vertexIdNameMap.get(loneVertex),bonus+curValue);
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
			//System.out.println(link.getSourceArtifactId()+" "+link.getTargetArtifactId()+" "+link.getScore());
		}
		//print(res);
		return res;
	}

	private double giveBonusForLonePoint(double[][] graphs, SubGraph subGraph, 
			int loneVertex, double diffBetweenTopAndCur) {
		double max = 0;
		for(int innerPoint:subGraph.getVertexList()){
			if(graphs[innerPoint][loneVertex]!=0){
				max = Math.max(max, graphs[loneVertex][innerPoint]);
			}
		}
		return max*diffBetweenTopAndCur;
	}

	public void filterSubGraphsList(Set<String> set){
		for(SubGraph subGraph:dataSubGraphList){
			List<Integer> vertexList = subGraph.getVertexList();
			Iterator<Integer> ite = vertexList.iterator();
			while(ite.hasNext()){
				if(!set.contains(vertexIdNameMap.get(ite.next()))){
					ite.remove();
				}
			}
		}
		
		Iterator<SubGraph> subGraphIte = dataSubGraphList.iterator();
		while(subGraphIte.hasNext()){
			if(subGraphIte.next().getVertexList().size()==0){
				subGraphIte.remove();
			}
		}
	}
	
	protected boolean hasContainedThisLink(SimilarityMatrix matrix, String req, int id) {
		String codeTarget = vertexIdNameMap.get(id);
		if(matrix.sourceArtifactsIds().contains(req)&&
				matrix.getLinksForSourceId(req).containsKey(codeTarget)){
			return true;
		}
		return false;
	}
	
	protected String getFirstKey(StringHashSet sourceArtifactsIds) {
		Iterator<String> ite = sourceArtifactsIds.iterator();
		return ite.next();
	}

	
	@Override
	public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAlgorithmName() {
		// TODO Auto-generated method stub
		return "UD_DataSubGraphWithBonusForLone";
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
