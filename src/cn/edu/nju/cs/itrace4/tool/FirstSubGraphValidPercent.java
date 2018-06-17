package cn.edu.nju.cs.tool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.None_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.region.util.sort.SortBySubGraph;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.StringHashSet;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.demo.FileParse.XmlParse;
import cn.edu.nju.cs.itrace4.demo.relation.StoreCallSubGraph;
import cn.edu.nju.cs.itrace4.demo.relation.StoreDataSubGraph;
import cn.edu.nju.cs.itrace4.demo.relation.SubGraph;
import cn.edu.nju.cs.itrace4.demo.tool.AnalyzeResult;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;

public class FirstSubGraphValidPercent {
	
	private List<SubGraph> callSubGraphList;
	private List<SubGraph> dataSubGraphList;
	private List<SubGraph> callDataSubGraphList;
	
	protected Map<Integer, String> vertexIdNameMap;
	private Set<Integer> allVertexIdList = new HashSet<Integer>();
	private Set<Integer> absoluteLoneVertexSet = new HashSet<Integer>();
	private Map<String,Boolean> reqMapFirstValid = new HashMap<String,Boolean>();
	
	private int countThreshold = 2;
	
	public FirstSubGraphValidPercent(RelationInfo ri,Map<String,Boolean> reqMapFirstValid){
		callSubGraphList = new StoreCallSubGraph().getSubGraphs(ri);
		dataSubGraphList = new StoreDataSubGraph().getSubGraphs(ri);
		callDataSubGraphList = mergeSubGraphList(callSubGraphList,dataSubGraphList);
		vertexIdNameMap = ri.getVertexIdNameMap();
		this.reqMapFirstValid = reqMapFirstValid;
	}
	
	private List<SubGraph> mergeSubGraphList(List<SubGraph> callSubGraphList,
			List<SubGraph> dataSubGraphList) {
		List<SubGraph> callDataSubGraphList = new LinkedList<SubGraph>();
		callDataSubGraphList.addAll(callSubGraphList);
		callDataSubGraphList.addAll(dataSubGraphList);
		return callDataSubGraphList;
	}

	/**
	 * @author zzf
	 * @date 2017.10.26
	 * @description use map to arrange req and relevant lone vertex, Because it has different vertext list
	 * 	about different req. 
	 */
	public void traveSubGraph(SimilarityMatrix matrix, TextDataset textDataset){
		 SimilarityMatrix oracle = textDataset.getRtm();
		 //get all target artifacts
		 Set<String> targetArtifacts = matrix.targetArtifactsIds();
		 //remove target artifacts which not corresponding with any source artifacts.
		 filterSubGraphsList(targetArtifacts,callDataSubGraphList);
		 buildAllVertexIdList(allVertexIdList,callDataSubGraphList);
		 fillAbsoluteLoneVertexSet(absoluteLoneVertexSet,callDataSubGraphList);
		 removeLoneVertexList(callDataSubGraphList);
		 
		 for(String req:matrix.sourceArtifactsIds()){
			Collections.sort(callDataSubGraphList,new SortBySubGraph(vertexIdNameMap,matrix,req));
			int maxId = callDataSubGraphList.get(0).getMaxId();
			String first = vertexIdNameMap.get(maxId);
			if(oracle.isLinkAboveThreshold(req,first)){
				reqMapFirstValid.put(req, true);
			}
			else {
				reqMapFirstValid.put(req, false);
			}
		}//req
	}
	
	public void printInfo() throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("./备份/10-06/firstValid.txt")));
		for(String key:reqMapFirstValid.keySet()) {
			bw.write(key+":"+reqMapFirstValid.get(key));
			bw.newLine();
		}
		bw.close();
	}
	
	private void removeLoneVertexList(List<SubGraph> callDataSubGraphList) {
		Iterator<SubGraph> ite = callDataSubGraphList.iterator();
		while(ite.hasNext()) {
			SubGraph subGraph = ite.next();
			if(subGraph.getVertexList().size()<countThreshold) {
				ite.remove();
			}
		}
	}

	private void buildAllVertexIdList(Set<Integer> allVertexIdList, List<SubGraph> callDataSubGraphList2) {
		for(SubGraph subGraph:callDataSubGraphList){
			for(int id:subGraph.getVertexList()) {
				allVertexIdList.add(id);
			}
		}
		
	}


	private void filterSubGraphsList(Set<String> set, List<SubGraph> graphList) {
		for(SubGraph subGraph:graphList){
			List<Integer> vertexList = subGraph.getVertexList();
			Iterator<Integer> ite = vertexList.iterator();
			while(ite.hasNext()){
				if(!set.contains(vertexIdNameMap.get(ite.next()))){
					ite.remove();
				}
			}
		}
		
		Iterator<SubGraph> graphIte = graphList.iterator();
		while(graphIte.hasNext()){
			if(graphIte.next().getVertexList().size()==0){
				graphIte.remove();
			}
		}
		
	}

	private void fillAbsoluteLoneVertexSet(Set<Integer> absoluteLoneVertexSet, List<SubGraph> callDataSubGraphList) {
		for(SubGraph subGraph:callDataSubGraphList) {
			for(int ele:subGraph.getVertexList()) {
				absoluteLoneVertexSet.add(ele);
			}
		}
		
		for(SubGraph subGraph:callDataSubGraphList) {
			List<Integer> curList = subGraph.getVertexList();
			if(curList.size()>=countThreshold) {
				for(int ele:subGraph.getVertexList()) {
					if(absoluteLoneVertexSet.contains(ele)) {
						absoluteLoneVertexSet.remove(ele);
					}
				}
			}
		}
	}

	
	public void filterSubGraphsList(Set<String> set){
		for(SubGraph subGraph:callDataSubGraphList){
			List<Integer> vertexList = subGraph.getVertexList();
			Iterator<Integer> ite = vertexList.iterator();
			while(ite.hasNext()){
				if(!set.contains(vertexIdNameMap.get(ite.next()))){
					ite.remove();
				}
			}
		}
		
		Iterator<SubGraph> subGraphIte = callDataSubGraphList.iterator();
		while(subGraphIte.hasNext()){
			if(subGraphIte.next().getVertexList().size()==0){
				subGraphIte.remove();
			}
		}
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		//RelationInfo ri,Map<String,Boolean> reqMapFirstValid
		String ucPath = "data/exp/iTrust/uc";
		String classDirPath = "data/exp/iTrust/class/code";
		String rtmClassPath = "data/exp/iTrust/rtm/RTM_CLASS.txt";
		String class_relationInfoPath = "data/exp/iTrust/relation/CLASS_relationInfo_whole.ser";
		TextDataset textDataset = new TextDataset(ucPath, classDirPath,rtmClassPath);
        FileInputStream fis = new FileInputStream(class_relationInfoPath);
        ObjectInputStream ois = new ObjectInputStream(fis);
        RelationInfo ri = (RelationInfo) ois.readObject();
        ois.close();
        Result result_ir = IR.compute(textDataset, "cn.edu.nju.cs.itrace4.core.ir.VSM", 
        		new None_CSTI());
        Map<String,Boolean> map = new HashMap<String,Boolean>();
		FirstSubGraphValidPercent tool = new FirstSubGraphValidPercent(ri,map);
		tool.traveSubGraph(result_ir.matrix, textDataset);
		tool.printInfo();
	}
	
}
