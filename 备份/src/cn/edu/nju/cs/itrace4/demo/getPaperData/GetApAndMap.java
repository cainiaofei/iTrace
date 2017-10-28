package cn.edu.nju.cs.itrace4.demo.getPaperData;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.apache.commons.math3.stat.inference.MannWhitneyUTest;

import cn.edu.nju.cs.itrace4.core.algo.None_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.UD_CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.demo.algo.outerVertex.process.MethodTypeProcessLone;
import cn.edu.nju.cs.itrace4.demo.algo.outerVertex.process.UD_CallThenDataWithBonusForLone;
import cn.edu.nju.cs.itrace4.demo.exp.project.Infinispan;
import cn.edu.nju.cs.itrace4.demo.exp.project.Itrust;
import cn.edu.nju.cs.itrace4.demo.exp.project.Maven;
import cn.edu.nju.cs.itrace4.demo.exp.project.Project;
import cn.edu.nju.cs.itrace4.demo.tool.CliffAnalyze;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;

public class GetApAndMap implements Runnable{
	private CliffAnalyze cliffAnalyze;
	private double percent;
	private String basePath;
	private Project[] projects = new Project[3];
	private String[] models = new String[3];
	private double callThreshold = 0.4, dataThreshold = 0.7;
	
	public GetApAndMap(double percent,String basePath) {
		this.percent = percent;
		this.basePath = basePath;
		initProjects(projects);
		initModels(models);
		try {
			cliffAnalyze = new CliffAnalyze();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void initProjects(Project[] project){
		project[0] = new Itrust();
		project[1] = new Maven();
		project[2] = new Infinispan();
	}
	
	public void initModels(String[] models){
		models[0] = "cn.edu.nju.cs.itrace4.core.ir.VSM";
		models[1] = "cn.edu.nju.cs.itrace4.core.ir.LSI";
		models[2] = "cn.edu.nju.cs.itrace4.core.ir.JSD";
	}
	
	public void func() throws Exception {
		BufferedWriter bw = new BufferedWriter(new FileWriter(
				new File(basePath+File.separator+"ap_map_pvalue_rate_"+
				percent+".csv")));
		String header = setHeader(percent);
		bw.write(header);
		bw.newLine();
		
		System.setProperty("routerLen", "6");
		for(int projectIndex = 0; projectIndex < projects.length;projectIndex++){
			  Project project = projects[projectIndex];
			  TextDataset textDataset = new TextDataset(project.getUcPath(), project.getClassDirPath(), 
		        		project.getRtmClassPath());
		      FileInputStream fis = new FileInputStream(project.getClass_RelationInfoPath());
		      ObjectInputStream ois = new ObjectInputStream(fis);
		      RelationInfo ri = (RelationInfo) ois.readObject();
		      ois.close();
		      
		      Map<String,List<String>> modelMapData = new HashMap<String,List<String>>();
		      
		      for(int modelIndex = 0; modelIndex<models.length;modelIndex++){
		    	  ri.setPruning(0, 0);
		    	  String model = models[modelIndex];
		          Result result_ir = IR.compute(textDataset, model, new None_CSTI());
		          Result result_ud = IR.compute(textDataset,model, new UD_CSTI(ri));
		          ri.setPruning(callThreshold, dataThreshold);
		          Map<String,Set<String>> valid = new HashMap<String,Set<String>>();
		          Result result_cluster = IR.compute(textDataset,model,
		          		new UD_CallThenDataWithBonusForLone(ri,callThreshold,
		          				dataThreshold,MethodTypeProcessLone.InnerMean,percent,valid));
		          retrieveData(model,result_ir,result_ud,result_cluster,modelMapData,textDataset);
		      }
		      //IR
		      List<String> irList = modelMapData.get("IR");
		      StringBuilder irSb = new StringBuilder();
		      irSb.append(project.getProjectName()+";"+"IR");
		      for(String str:irList) {
		    	  irSb.append(";"+str);
		      }
		      bw.write(irSb.toString());
		      bw.newLine();
		      //UD
		      List<String> udList = modelMapData.get("UD");
		      StringBuilder udSb = new StringBuilder();
		      udSb.append(project.getProjectName()+";"+"UD");
		      for(String str:udList) {
		    	  udSb.append(";"+str);
		      }
		      bw.write(udSb.toString());
		      bw.newLine();
		      //CLUSTER
		      List<String> clusterList = modelMapData.get("CLUSTER");
		      StringBuilder clusterSb = new StringBuilder();
		      clusterSb.append(project.getProjectName()+";"+"CLUSTER");
		      for(String str:clusterList) {
		    	  clusterSb.append(";"+str);
		      }
		      bw.write(clusterSb.toString());
		      bw.newLine();
		}
		bw.close();
	}
	
	
	private String setHeader(double percent) {
		StringBuilder sb = new StringBuilder();
		sb.append(percent*100+"%; ; ; ;VSM; ; ; ; ;LSI; ; ; ; ;JS;\n");
		String kind = ";ap;map;p-value;cliff;rate";
		sb.append(" ; "+kind+kind+kind);
		return sb.toString();
	}

	private void retrieveData(String model, Result result_ir, Result result_ud, Result result_cluster,
			Map<String, List<String>> modelMapData,TextDataset textDataset) {
		//IR
		double irApValue = result_ir.getAveragePrecisionByRanklist();
		String irAp = String.format("%.2f", irApValue*100);
		double irMapValue = result_ir.getMeanAveragePrecisionByQuery();
		String irMap = String.format("%.2f", irMapValue*100);
		double irPvalue = printPValue(result_cluster,result_ir);
		System.out.println(irPvalue);
		String irPvalueStr = irPvalue+"";
		double irCliffValue = cliffAnalyze.doCliff(result_cluster, result_ir, textDataset.getRtm());
		String irCliff = String.format("%.2f", irCliffValue);
		String irRate = "_";
		if(!modelMapData.containsKey("IR")) {
			modelMapData.put("IR",new ArrayList<String>());
		}
		List<String> irList = modelMapData.get("IR");
		irList.add(irAp);irList.add(irMap);irList.add(irPvalueStr);irList.add(irCliff);irList.add(irRate);
		//UD
		double udApValue = result_ud.getAveragePrecisionByRanklist();
		String udAp = String.format("%.2f", udApValue*100);
		double udMapValue = result_ud.getMeanAveragePrecisionByQuery();
		String udMap = String.format("%.2f", udMapValue*100);
		double udPvalue = printPValue(result_cluster,result_ud);
		String udPvalueStr = udPvalue+"";
		//String udPvalueStr = String.format("%.2f", udPvalue);
		double udCliffValue = cliffAnalyze.doCliff(result_cluster, result_ud, textDataset.getRtm());
		String udCliff = String.format("%.2f", udCliffValue);
		String udRate = "_";
		if(!modelMapData.containsKey("UD")) {
			modelMapData.put("UD",new ArrayList<String>());
		}
		List<String> udList = modelMapData.get("UD");
		udList.add(udAp);udList.add(udMap);udList.add(udPvalueStr);udList.add(udCliff);udList.add(udRate);
		//UD_CallData
		double clusterApValue = result_cluster.getAveragePrecisionByRanklist();
		String clusterAp = String.format("%.2f", clusterApValue*100);
		double clusterMapValue = result_cluster.getMeanAveragePrecisionByQuery();
		String clusterMap = String.format("%.2f", clusterMapValue*100);
		String clusterPvalueStr = "_";
		String clusterCliff = "_";
		String rate = System.getProperty("rate");
		String clusterRate = rate.substring(0,Math.min(6,rate.length()));
		if(!modelMapData.containsKey("CLUSTER")) {
			modelMapData.put("CLUSTER",new ArrayList<String>());
		}
		List<String> clusterList = modelMapData.get("CLUSTER");
		clusterList.add(clusterAp);clusterList.add(clusterMap);clusterList.add(clusterPvalueStr);
		clusterList.add(clusterCliff);clusterList.add(clusterRate);
	}

	@Override
	public void run() {
		try {
			func();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static double printPValue(Result ours, Result compareTo) {
	      MannWhitneyUTest mannWhitneyUTest = new MannWhitneyUTest();
	      double pValue_fmeasure = mannWhitneyUTest.mannWhitneyUTest(ours.getWilcoxonDataArray_fmeasure(), compareTo.getWilcoxonDataArray_fmeasure());
	      return pValue_fmeasure;
	}
	
	public static void main(String[] args) throws Exception {
		GetApAndMap test = new GetApAndMap(1,".");
		test.func();
	}

}
