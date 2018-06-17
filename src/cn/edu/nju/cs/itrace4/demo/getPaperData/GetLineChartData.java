package cn.edu.nju.cs.itrace4.demo.getPaperData;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.None_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.prealgo.UD_CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.core.algo.region.calldata.UD_CallDataDynamic;
import cn.edu.nju.cs.itrace4.core.algo.region.calldata.UD_CallDataTreatEqualCount;
import cn.edu.nju.cs.itrace4.demo.exp.project.Gantt;
import cn.edu.nju.cs.itrace4.demo.exp.project.Infinispan;
import cn.edu.nju.cs.itrace4.demo.exp.project.Itrust;
import cn.edu.nju.cs.itrace4.demo.exp.project.Maven;
import cn.edu.nju.cs.itrace4.demo.exp.project.Project;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;

public class GetLineChartData {
	private String basePath;
	private Project[] projects = new Project[3];
	private String[] models = new String[3];
	private double callThreshold = 0.8, dataThreshold = 0.8;
	
	public GetLineChartData(String basePath) {
		this.basePath = basePath;
		initProjects(projects);
		initModels(models);
	}
	
	public void initProjects(Project[] projects){
		projects[0] = new Itrust();
		projects[1] = new Maven();
		projects[2] = new Gantt();
	}
	
	public void initModels(String[] models){
		models[0] = "cn.edu.nju.cs.itrace4.core.ir.VSM";
		models[1] = "cn.edu.nju.cs.itrace4.core.ir.LSI";
		models[2] = "cn.edu.nju.cs.itrace4.core.ir.JSD";
	}
	
	public void getLineData() throws IOException, ClassNotFoundException {
		//for (Project project : projects) {
		for(int i = 2; i <=2;i++) {
			Project project = projects[i];
			TextDataset textDataset = new TextDataset(project.getUcPath(), project.getClassDirPath(),
					project.getRtmClassPath());
			FileInputStream fis = new FileInputStream(project.getClass_RelationInfoPath());
			ObjectInputStream ois = new ObjectInputStream(fis);
			RelationInfo ri = (RelationInfo) ois.readObject();
			ois.close();
			for(String model:models) {
				ri.setPruning(0, 0);
				Result result_ir = IR.compute(textDataset, model, new None_CSTI());
				Result result_ud = IR.compute(textDataset,model, new UD_CSTI(ri));
				ri.setPruning(callThreshold, dataThreshold);
				
				Map<String,Set<String>> valid = new HashMap<String,Set<String>>();
				Result result_UD_CallDataTreatEqual = IR.compute(textDataset,model,
		        		new UD_CallDataTreatEqualCount(ri,callThreshold,
		        				callThreshold,6,valid));//0.7
				
//				Map<String,Set<String>> valid = new HashMap<String,Set<String>>();
//				Result result_cluster_two = IR.compute(textDataset,model,
//						new UD_CallDataDynamic(ri,callThreshold,
//								dataThreshold,0.2,valid));//0.7
//				valid = new HashMap<String,Set<String>>();
//				Result result_cluster_four = IR.compute(textDataset,model,
//						new UD_CallDataDynamic(ri,callThreshold,
//								dataThreshold,0.4,valid));//0.7
				String fileName = project.getProjectName()+"_"+
								model.substring(model.lastIndexOf(".")+1)+".csv";
				storeLineGraphData(result_ir,result_ud,result_UD_CallDataTreatEqual,fileName);
			}
		}

	}
	
	private void storeLineGraphData(Result result_ir, Result result_ud,
			Result result_UD_CallDataTreatEqual,
			String fileName) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(basePath+File.separator+fileName));
		String header = "percent;ir;ud;cluster;";
		List<Double> irList = result_ir.getPrecisionAtRecallByTen();
		List<Double> udList = result_ud.getPrecisionAtRecallByTen();
		List<Double> clusterList = result_UD_CallDataTreatEqual.getPrecisionAtRecallByTen();
		bw.write(header);
		bw.newLine();
		for(int recall = 1,index=0; recall <=10;recall++,index++) {
			StringBuilder sb = new StringBuilder();
			sb.append(0.1*recall+";");
			sb.append(irList.get(index)+";");
			sb.append(udList.get(index)+";");
			sb.append(clusterList.get(index)+";");
			bw.write(sb.toString());
			bw.newLine();
		}
		bw.close();
	}

	public static void main(String[] args) throws ClassNotFoundException, IOException {
		System.setProperty("routerLen", "6");
		String basePath = "./lineGraph";
		GetLineChartData getLineChartData = new GetLineChartData(basePath);
		getLineChartData.getLineData();
		
	}
}
