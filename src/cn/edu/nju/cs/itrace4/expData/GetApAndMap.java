package cn.edu.nju.cs.itrace4.expData;

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

import cn.edu.nju.cs.itrace4.core.algo.CSTI;
import cn.edu.nju.cs.itrace4.core.algo.None_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.UD_CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.demo.cdgraph.UD_CallDataDynamic;
import cn.edu.nju.cs.itrace4.demo.exp.project.Gantt;
import cn.edu.nju.cs.itrace4.demo.exp.project.Itrust;
import cn.edu.nju.cs.itrace4.demo.exp.project.Maven;
import cn.edu.nju.cs.itrace4.demo.exp.project.Project;
import cn.edu.nju.cs.itrace4.demo.tool.CliffAnalyze;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;

public class GetApAndMap implements Runnable {
	private CliffAnalyze cliffAnalyze;
	private double percent;
	private String basePath;
	private Project[] projects = new Project[3];
	private String[] models = new String[3];
	private double callThreshold = 0.6, dataThreshold = 0.7;

	public GetApAndMap(double percent, String basePath) {
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

	public void initProjects(Project[] project) {
		project[0] = new Itrust();
		project[1] = new Maven();
		project[2] = new Gantt();
	}

	public void initModels(String[] models) {
		models[0] = "cn.edu.nju.cs.itrace4.core.ir.VSM";
		models[1] = "cn.edu.nju.cs.itrace4.core.ir.LSI";
		models[2] = "cn.edu.nju.cs.itrace4.core.ir.JSD";
	}

	public void func() throws Exception {
		System.setProperty("routerLen", "6");
		for (int projectIndex = 0; projectIndex < projects.length; projectIndex++) {///outer for loop
			BufferedWriter bw = new BufferedWriter(
					new FileWriter(new File(basePath + File.separator + "ap_map" + File.separator+
							projects[projectIndex].getProjectName() + "all_percent.csv")));
			String header = setHeader(percent);
			bw.write(header);
			bw.newLine();
			Project project = projects[projectIndex];
			TextDataset textDataset = new TextDataset(project.getUcPath(), project.getClassDirPath(),
					project.getRtmClassPath());
			FileInputStream fis = new FileInputStream(project.getClass_RelationInfoPath());
			ObjectInputStream ois = new ObjectInputStream(fis);
			RelationInfo ri = (RelationInfo) ois.readObject();
			ois.close();
			String irData = getBaseData(textDataset, new None_CSTI());
			bw.write(projects[projectIndex].getProjectName()+";ir;"+irData);
			bw.newLine();
			
			String udData = getBaseData(textDataset, new UD_CSTI(ri));
			bw.write(projects[projectIndex].getProjectName()+";ud;"+udData);
			bw.newLine();
//			for(double percent = 0.2;percent<=1.0;percent+=0.2) {
			for(double percent = 0.1;percent<=1.0;percent+=0.1) {
				StringBuilder sb = new StringBuilder();
				sb.append(projects[projectIndex].getProjectName()+";");
				String percentStr = percent+"";
				percentStr = percentStr.substring(0, Math.min(percentStr.length(), 4));
				sb.append("cluster("+percentStr+");");
				for (int modelIndex = 0; modelIndex < models.length; modelIndex++) {
					ri.setPruning(0, 0);
					String model = models[modelIndex];
					Result result_ir = IR.compute(textDataset, model, new None_CSTI());
					Result result_ud = IR.compute(textDataset, model, new UD_CSTI(ri));
					ri.setPruning(callThreshold, dataThreshold);
					Map<String, Set<String>> valid = new HashMap<String, Set<String>>();
					Result result_cluster = IR.compute(textDataset, model,
							new UD_CallDataDynamic(ri, callThreshold, dataThreshold, percent, valid));// 0.7
					
					double ap = result_cluster.getAveragePrecisionByRanklist();
					String apStr = String.format("%.2f", ap*100);
					double map = result_cluster.getMeanAveragePrecisionByQuery();
					String mapStr = String.format("%.2f",map*100);
					
					sb.append(apStr+";"+mapStr+";");
					double irPvalue = printPValue(result_cluster, result_ir);
					double udPvalue = printPValue(result_cluster, result_ud);
					double irCliffValue = cliffAnalyze.doCliff(result_cluster, result_ir, textDataset.getRtm());
					String irCliff = String.format("%.2f", irCliffValue);
					double udCliffValue = cliffAnalyze.doCliff(result_cluster, result_ud, textDataset.getRtm());
					String udCliff = String.format("%.2f", udCliffValue);
					
					String rate = System.getProperty("rate");
					rate = rate.substring(0,Math.min(6, rate.length()));
					sb.append(irPvalue+";"+udPvalue+";"+irCliff+";"+udCliff+";"+rate+";");
				}
				bw.write(sb.toString());
				bw.newLine();
			}
			bw.close();
		}
	}

	private String getBaseData(TextDataset textDataset, CSTI algo) {
		StringBuilder sb = new StringBuilder();
		for (int modelIndex = 0; modelIndex < models.length; modelIndex++) {
			String model = models[modelIndex];
			Result result = IR.compute(textDataset, model, algo);
			double ap = result.getAveragePrecisionByRanklist();
			double map = result.getMeanAveragePrecisionByQuery();
			String apStr = String.format("%.2f", ap * 100);
			String mapStr = String.format("%.2f", map * 100);
			sb.append(apStr + ";" + mapStr + ";-;-;-;-;-;");
		}
		return sb.toString();
	}

	private String setHeader(double percent) {
		StringBuilder sb = new StringBuilder();
		sb.append( "; ; ; ;VSM; ; ; ; ;LSI; ; ; ; ;JS;\n");
		String kind = ";ap;map;p-value(ir);p-value(ud);cliff(ir);cliff(pvalue);rate";
		sb.append(" ; " + kind + kind + kind);
		return sb.toString();
	}


	private int allSize(Map<String, Set<String>> valid) {
		int amount = 0;
		for (String key : valid.keySet()) {
			amount += valid.get(key).size();
		}
		return amount;
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
		double pValue_fmeasure = mannWhitneyUTest.mannWhitneyUTest(ours.getWilcoxonDataArray_fmeasure(),
				compareTo.getWilcoxonDataArray_fmeasure());
		return pValue_fmeasure;
	}

	public static void main(String[] args) throws Exception {
		GetApAndMap test = new GetApAndMap(1, "./finalPaperData");
		test.func();
	}

}
