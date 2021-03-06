package cn.edu.nju.cs.itrace4.boot.batch.count;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.None_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.prealgo.UD_CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.core.algo.region.calldata.UD_CallDataTreatEqualCount;
import cn.edu.nju.cs.itrace4.core.algo.region.calldata.UD_CallDataTreatEqualOuterLessThanInner;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.util.FileProcess;
import cn.edu.nju.cs.itrace4.util.FileProcessTool;
import cn.edu.nju.cs.itrace4.util.FileParse.project.Infinispan;
import cn.edu.nju.cs.itrace4.util.FileParse.project.Itrust;
import cn.edu.nju.cs.itrace4.util.FileParse.project.Maven_TestCase;
import cn.edu.nju.cs.itrace4.util.FileParse.project.Pig;
import cn.edu.nju.cs.itrace4.util.FileParse.project.Project;
import cn.edu.nju.cs.itrace4.util.exception.FileException;
import cn.edu.nju.cs.itrace4.visual.MyVisualCurve;
import cn.edu.nju.cs.itrace4.visual.VisualCurve;

public class BatchStorePng {
	private double callThreshold;
	private double dataThreshold;
	private String projectPath;
	private String modelPath;
	private String pngPath;
	
	private FileProcess fileProcess;
	
	private Map<String,Project> projectMap; 
	private Map<String,String> modelMap;
	
	public BatchStorePng(double callThreshold,double dataThreshold,String projectPath,
			String modelPath,String pngPath) {
		this.callThreshold = callThreshold;
		this.dataThreshold = dataThreshold;
		this.projectPath = projectPath;
		this.modelPath = modelPath;
		this.pngPath = pngPath;
		this.fileProcess = new FileProcessTool();
		init();
	}
	
	private void initProjectMap() {
		this.projectMap = new HashMap<String,Project>();
		projectMap.put("itrust", new Itrust());
		projectMap.put("infinispan", new Infinispan());
		projectMap.put("pig", new Pig());
		projectMap.put("maven_testcase", new Maven_TestCase());
	}
	
	private void init() {
		initProjectMap();
		initModelMap();
	}
	
	private void initModelMap() {
		this.modelMap = new HashMap<String,String>();
		modelMap.put("vsm", "cn.edu.nju.cs.itrace4.core.ir.VSM");
		modelMap.put("js", "cn.edu.nju.cs.itrace4.core.ir.JSD");
		modelMap.put("lsi", "cn.edu.nju.cs.itrace4.core.ir.LSI");
	}

	private String[] getArrFromFile(String path) {
		String content = null;
		try {
			content = fileProcess.getFileConent(path);
		} catch (FileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String[] res = content.split("\n");
		return res;
	}
	
	public void batchStorePng() throws Exception {
		String[] projects = getArrFromFile(projectPath);
		String[] models = getArrFromFile(modelPath);
		for(String projectName:projects) {
			Project project = projectMap.get(projectName.trim().toLowerCase());
			for(String modelName:models) {
				String modelFullName = modelMap.get(modelName);
				execute(project,modelFullName);
			}
		}
	}
	
	private void execute(Project project, String model) 
			throws Exception {
		TextDataset textDataset = new TextDataset(project.getUcPath(), project.getClassDirPath(), 
	        		project.getRtmClassPath());
		 
		FileInputStream fis = new FileInputStream(project.getClass_RelationInfoPathWhole());
        ObjectInputStream ois = new ObjectInputStream(fis);
        RelationInfo ri = (RelationInfo) ois.readObject();
        ois.close();
        
        Result result_ir = IR.compute(textDataset, model, new None_CSTI());
        Result result_UD_CSTI = IR.compute(textDataset, model, new UD_CSTI(ri));
        
        Map<String,Set<String>> valid = new HashMap<String,Set<String>>();
        ri.setPruning(callThreshold, dataThreshold);
        valid = new HashMap<String,Set<String>>();
        Result result_UD_CallDataTreatEqual = IR.compute(textDataset,model,
        		new UD_CallDataTreatEqualOuterLessThanInner(ri,callThreshold,
        			dataThreshold,6,valid));//0.7
        VisualCurve curve = new MyVisualCurve();
        curve.addLine(result_ir);
        curve.addLine(result_UD_CSTI);
        curve.addLine(result_UD_CallDataTreatEqual);
        
        //curve.addLine(result_UD_CallDataTreatEqualTemp);
        File baseFile = new File(pngPath+File.separator+project.getProjectName());
        if(!baseFile.exists()) {
        	baseFile.mkdir();
        }
        
        curve.showChart(project.getProjectName());
        curve.curveStore(baseFile.getAbsolutePath(),model);
	}

	public static void main(String[] args) throws Exception {
		double callThreshold = 0.4;
		double dataThreshold = 0.8;
		String projectPath = "resource/config/project.txt";
		String modelPath = "resource/config/model.txt";
		String pngPath = "newData/batch-3-all/0.4-0.8";
		BatchStorePng bsp = new BatchStorePng(callThreshold,dataThreshold,projectPath,modelPath,pngPath);
		bsp.batchStorePng();
	}
	
}
