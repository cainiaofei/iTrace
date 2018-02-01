package cn.edu.nju.cs.itrace4.demo.batch.paper;



import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.core.algo.None_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.UD_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.UseEdge;
import cn.edu.nju.cs.itrace4.core.algo.icse.PruningCall_Data_Connection_Closenss;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.demo.cdgraph.inneroutter.UD_InnerAndOuterSeq;
import cn.edu.nju.cs.itrace4.demo.exp.project.Infinispan;
import cn.edu.nju.cs.itrace4.demo.exp.project.Itrust;
import cn.edu.nju.cs.itrace4.demo.exp.project.Maven_TestCase;
import cn.edu.nju.cs.itrace4.demo.exp.project.Pig;
import cn.edu.nju.cs.itrace4.demo.exp.project.Project;
import cn.edu.nju.cs.itrace4.demo.visual.MyVisualCurve;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.util.Setting;
import cn.edu.nju.cs.itrace4.visual.VisualCurve;
import cn.edu.nju.cs.refactor.exception.FileException;
import cn.edu.nju.cs.refactor.util.FileProcess;
import cn.edu.nju.cs.refactor.util.FileProcessTool;

public class BatchStorePngPercent {
	private double callThreshold;
	private double dataThreshold;
	private String projectPath;
	private String modelPath;
	private String pngPath;
	
	private FileProcess fileProcess;
	
	private Map<String,Project> projectMap; 
	private Map<String,String> modelMap;
	
	private int userVerifyCount;
	private double percent;
	
	public BatchStorePngPercent(double callThreshold,double dataThreshold,String projectPath,
			String modelPath,String pngPath,double percent) {
		this.callThreshold = callThreshold;
		this.dataThreshold = dataThreshold;
		this.projectPath = projectPath;
		this.modelPath = modelPath;
		this.pngPath = pngPath;
		this.percent = percent;
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
	
	public void batchStorePngPercent() throws Exception {
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
		System.setProperty("projectName", project.getProjectName());
		
		TextDataset textDataset = new TextDataset(project.getUcPath(), project.getClassDirPath(), 
	        		project.getRtmClassPath());
		 
		FileInputStream fis = new FileInputStream(project.getClass_RelationInfoPathWhole());
        ObjectInputStream ois = new ObjectInputStream(fis);
        RelationInfo ri = (RelationInfo) ois.readObject();
        ois.close();
        
    	RelationInfo class_relation = getRelationInfo(project);
		RelationInfo class_relationForO = getRelationInfo(project);
		RelationInfo class_relationForAllDependencies = getRelationInfo(project);
        
        userVerifyCount = (int)(ri.getVertexIdNameMap().size() * percent);
        
        Result result_ir = IR.compute(textDataset, model, new None_CSTI());
        Result result_UD_CSTI = IR.compute(textDataset, model, new UD_CSTI(ri));
        
        Map<String,Set<String>> valid = new HashMap<String,Set<String>>();
        ri.setPruning(callThreshold, dataThreshold);
        valid = new HashMap<String,Set<String>>();
        Result result_UD_CallDataTreatEqual = IR.compute(textDataset,model,
        		new UD_InnerAndOuterSeq(ri,callThreshold,
        			dataThreshold,userVerifyCount,valid));//0.7
        
        class_relation.setPruning(Setting.callThreshold, Setting.dataThreshold);
        class_relationForO.setPruning(-1, -1);
        class_relationForAllDependencies.setPruning(-1, -1);

        Result result_pruningeCall_Data_Dir = IR.compute(textDataset, model, 
        		new PruningCall_Data_Connection_Closenss(class_relation, class_relationForO, 
        				class_relationForAllDependencies,
        				UseEdge.Call, 1.0, 1.0));
        
        VisualCurve curve = new MyVisualCurve();
        curve.addLine(result_ir);
        curve.addLine(result_UD_CSTI);
        curve.addLine(result_UD_CallDataTreatEqual);
        curve.addLine(result_pruningeCall_Data_Dir);
        
        //curve.addLine(result_UD_CallDataTreatEqualTemp);
        File baseFile = new File(pngPath+File.separator+project.getProjectName());
        if(!baseFile.exists()) {
        	baseFile.mkdir();
        }
        
        curve.showChart(project.getProjectName());
        curve.curveStore(baseFile.getAbsolutePath(),model);
	}

	
	private RelationInfo getRelationInfo(Project project) throws IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream(project.getClass_RelationInfoPathWhole());
		ObjectInputStream ois = new ObjectInputStream(fis);
		RelationInfo ri = (RelationInfo) ois.readObject();
		ois.close();
		fis.close();
		return ri;
	}
	
	
	public static void main(String[] args) throws Exception {
		double callThreshold = 0.4;
		double dataThreshold = 0.8;
		double percent = 0.035;
		String projectPath = "resource/config/project.txt";
		String modelPath = "resource/config/model.txt";
		String pngPath = "paper/OuterInnerSeq/"+percent+File.separator+callThreshold+"-"+dataThreshold;
		BatchStorePngPercent bsp = new BatchStorePngPercent(callThreshold,dataThreshold,
				projectPath,modelPath,pngPath,percent);
		bsp.batchStorePngPercent();
	}
	
}
