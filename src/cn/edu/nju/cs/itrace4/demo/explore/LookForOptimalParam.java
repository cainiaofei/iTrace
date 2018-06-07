package cn.edu.nju.cs.itrace4.demo.explore;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.core.algo.region.callthendata.UD_CallThenDataWithBonusForLone;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.ir.IRModelConst;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;

class Node{
	double callThreshold,dataThreshold;
	double averagePrecision;
	String modelName,projectName;
	public Node(double callThreshold,double dataThreshold,String projectName,
			String modelName,double averagePrecision){
		this.callThreshold = callThreshold;
		this.dataThreshold = dataThreshold;
		this.projectName = projectName;
		this.modelName = modelName;
		this.averagePrecision = averagePrecision;
	}
}

public class LookForOptimalParam {
	
	private Map<String,String> map = new HashMap<String,String>();
	private List<Node> res = new LinkedList<Node>();
	
	
	public LookForOptimalParam(){
		map.put("gannt", "Gantt");
		map.put("itrust", "iTrust");
		map.put("jhotdraw", "jHotDraw");
	}
	
	public void exhaustAllParams(){
		ExpirementSystem[] expirementSystems = {new ITrust(),new Gantt(),new JHotDraw()};
		for(double i = 0.7; i <= 0.9;i+=0.05){
			for(double j = 0.7; j <= 0.9; j+=0.05){
				double callThreshold = i;
				double dataThreshold = j;
				try {
					processAllSystem(expirementSystems,callThreshold,dataThreshold);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void processAllSystem(ExpirementSystem[] expirementSystems, double callThreshold, double dataThreshold) 
			throws IOException, ClassNotFoundException {
		for(ExpirementSystem expirement:expirementSystems){
			TextDataset textDataset = new TextDataset(expirement.getUcPath(),expirement.getClassDirPath(),
					expirement.getRtmClassPath()); 
			
			FileInputStream fis = new FileInputStream(expirement.getClass_relationInfoPath());
		    ObjectInputStream ois = new ObjectInputStream(fis);
		    RelationInfo ri = (RelationInfo) ois.readObject();
		    ois.close();
		    ri.setPruning(callThreshold, dataThreshold);
			processExpirement(ri,textDataset,expirement);
		}
	}

	private void processExpirement(RelationInfo ri, TextDataset textDataset, ExpirementSystem expirement) {
		/*
		 * there are three algorithm models. 
		 */
		String[] models = {IRModelConst.VSM,IRModelConst.LSI,IRModelConst.JSD};
		
		for(String model:models){
			Result result = IR.compute(textDataset,model,
					new UD_CallThenDataWithBonusForLone(ri,ri.getCallEdgeScoreThreshold(),
	        				ri.getDataEdgeScoreThreshold()));
			
			try {
				res.add(new Node(ri.getCallEdgeScoreThreshold(),ri.getDataEdgeScoreThreshold(),
						expirement.getProjectName(),model,result.getAveragePrecisionByRanklist()));
				writeTXT(result,"/out/"+map.get(expirement.getProjectName().toLowerCase())+"/"
						+model+ri.getCallEdgeScoreThreshold()+"_"+ri.getDataEdgeScoreThreshold()
						+".txt");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void writeTXT(Result result, String path) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(path));
		bw.write("averagePrecision"+":");
		bw.write(result.getAveragePrecisionByRanklist()+"");
		bw.newLine();
		Set<String> reqSet = result.getAveragePrecisionByQuery().keySet();
		for(String req:reqSet){
			bw.write(req+":");
			bw.write(result.getAveragePrecisionByQuery().get(req)+"");
			bw.newLine();
		}
		bw.close();
	}

	/**
	 * sort all combination
	 * */
	public void sort(){
		Collections.sort(res,new Comparator<Node>(){

			@Override
			public int compare(Node o1, Node o2) {
				double diff = o2.averagePrecision - o1.averagePrecision;
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
			
		});
		
		//print res
		try {
			print(res);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void print(List<Node> res) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter("/out/result.txt"));
		
		for(Node node:res){
			bw.write(node.callThreshold+"_"+node.dataThreshold+"_"+node.projectName+"_"+
					node.modelName+node.averagePrecision);
			bw.newLine();
		}
		bw.close();
	}

	public static void main(String[] args){
		LookForOptimalParam lookForOptimalParam = new LookForOptimalParam();
		lookForOptimalParam.exhaustAllParams();
		lookForOptimalParam.sort();
	}
}
