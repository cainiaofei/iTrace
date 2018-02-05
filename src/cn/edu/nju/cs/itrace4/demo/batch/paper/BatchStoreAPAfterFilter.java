package cn.edu.nju.cs.itrace4.demo.batch.paper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.inference.MannWhitneyUTest;

import cn.edu.nju.cs.itrace4.core.algo.None_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.UD_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.UseEdge;
import cn.edu.nju.cs.itrace4.core.algo.icse.PruningCall_Data_Connection_Closenss;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.core.metrics.cut.ConstantThresholdResult;
import cn.edu.nju.cs.itrace4.demo.cdgraph.inneroutter.UD_InnerAndOuterSeq;
import cn.edu.nju.cs.itrace4.demo.exp.project.Gantt;
import cn.edu.nju.cs.itrace4.demo.exp.project.Infinispan;
import cn.edu.nju.cs.itrace4.demo.exp.project.Itrust;
import cn.edu.nju.cs.itrace4.demo.exp.project.JhotDraw;
import cn.edu.nju.cs.itrace4.demo.exp.project.Maven;
import cn.edu.nju.cs.itrace4.demo.exp.project.Maven_Cluster;
import cn.edu.nju.cs.itrace4.demo.exp.project.Maven_Cluster_40;
import cn.edu.nju.cs.itrace4.demo.exp.project.Maven_TestCase;
import cn.edu.nju.cs.itrace4.demo.exp.project.Pig;
import cn.edu.nju.cs.itrace4.demo.exp.project.Pig_Cluster;
import cn.edu.nju.cs.itrace4.demo.exp.project.Project;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.util.Setting;
import cn.edu.nju.cs.refactor.exception.FileException;
import cn.edu.nju.cs.refactor.plug.FindBadReq;
import cn.edu.nju.cs.refactor.plug.Plug;
import cn.edu.nju.cs.refactor.util.FileProcess;
import cn.edu.nju.cs.refactor.util.FileProcessTool;

/**
 * @author zzf
 * @date 2018.1.21
 * @description try to use some parameter and find the best one.
 */
public class BatchStoreAPAfterFilter {
	private String projectPath;
	private String modelPath;
	private FileProcess fileProcess;
	private Map<String, Project> projectMap;
	private Map<String, String> modelMap;
	private Map<Integer, String> idMapProject;
	private Map<Integer, String> idMapModel;
	private int userVerifyNumber;
	private double percent = 0.035;
	private String targetPath = "paper1/OuterInnerSeq/"+percent;
	
	private int countThreshold = 2;
	private String rtmPath;
	
	private Plug findBadReq;

	public BatchStoreAPAfterFilter(String projectPath, String modelPath) {
		this.projectPath = projectPath;
		this.modelPath = modelPath;
		this.fileProcess = new FileProcessTool();
		this.projectMap = new HashMap<String, Project>();
		this.modelMap = new HashMap<String, String>();
		this.idMapProject = new HashMap<Integer, String>();
		this.idMapModel = new HashMap<Integer, String>();
		
		this.findBadReq = new FindBadReq();
		
		init();
	}

	public void batch() throws ClassNotFoundException, IOException, FileException {
		String[] projects = getArrFromFile(projectPath);
		String[] models = getArrFromFile(modelPath);
		// the first two is Itrust and gannt
		for (double callThreshold = 0.4; callThreshold < 0.41; callThreshold += 0.1) {
			for (double dataThreshold = 0.8; dataThreshold < 0.81; dataThreshold += 0.1) {
				//2018.1.25
				// project:4 model:3 method:4 StringBuilder:ap,map,p-value,rate
				String[][][] result = new String[4][3][4];
				// the first two is iTrust and Gantt
				for (int projectIndex = 0; projectIndex < projects.length; projectIndex++) {
					TextDataset textDataset = getTextDataset(projects[projectIndex].toLowerCase());
					RelationInfo ri = getRelationInfo(projects[projectIndex].toLowerCase());
					
					RelationInfo class_relation = getRelationInfo(projects[projectIndex].toLowerCase());
					RelationInfo class_relationForO = getRelationInfo(projects[projectIndex].toLowerCase());
					RelationInfo class_relationForAllDependencies = getRelationInfo(projects[projectIndex].toLowerCase());
					
					userVerifyNumber = (int)(ri.getVertexIdNameMap().size()*percent);
					for (int modelIndex = 0; modelIndex < models.length; modelIndex++) {
						rtmPath = projectMap.get(projects[projectIndex].toLowerCase()).getRtmClassPath();
						calculateResult(result, projects,
								projectIndex, models, modelIndex, textDataset, ri,
								callThreshold, dataThreshold,class_relation,
								class_relationForO,class_relationForAllDependencies);
					}
				}
				// write file
				writeFile(callThreshold, dataThreshold, result);
			}
		}
	}

	private void writeFile(double callThreshold, double dataThreshold, String[][][] result) throws IOException {
		File file = new File(targetPath + File.separator + callThreshold + "-" + dataThreshold+
				File.separator + "ap_map_after_filter_"+countThreshold);
		if (!file.exists()) {
			file.mkdirs();
		}
		for (int projectIndex = 0; projectIndex < result.length; projectIndex++) {
			String projectName = idMapProject.get(projectIndex);
			BufferedWriter bw = new BufferedWriter(
					new FileWriter(new File(file.getAbsolutePath() + File.separator + projectName + ".csv")));
			bw.write(callThreshold+";"+dataThreshold);
			bw.newLine();
			for (int modelIndex = 0; modelIndex < result[projectIndex].length; modelIndex++) {
				StringBuilder sb = new StringBuilder();
				String modelName = idMapModel.get(modelIndex);
				String header = getHeader(modelName);
				String[] methods = { "ir", "ud","closeness", "cluster" };
				sb.append(header+"\n");
				for (int methodIndex = 0; methodIndex < result[projectIndex][modelIndex].length; methodIndex++) {
					String methodName = methods[methodIndex];
					sb.append(methodName + ";" + result[projectIndex][modelIndex][methodIndex]+"\n");
				}
				sb.append("\n\n");
				bw.write(sb.toString());
			}
			bw.close();
		}
	}

	private String getHeader(String modelName) {
		StringBuilder sb = new StringBuilder();
		sb.append(modelName + ";ap;map;p-value;rate");
		return sb.toString();
	}

	
	private void calculateResult(String[][][] result, String[] projects, int projectIndex, String[] models,
			int modelIndex, TextDataset textDataset, RelationInfo ri,
			double callThreshold, double dataThreshold,
			RelationInfo class_relation,RelationInfo class_relationForO,
			RelationInfo class_relationForAllDependencies) throws FileException, IOException {
		System.setProperty("projectName", projects[projectIndex]);
		
		String fullModelName = modelMap.get(models[modelIndex].trim().toLowerCase());
		Result result_ir = IR.compute(textDataset, fullModelName, new None_CSTI());
		ri.setPruning(0, 0);
		Result result_UD_CSTI = IR.compute(textDataset, fullModelName, new UD_CSTI(ri));
		Map<String, Set<String>> valid = new HashMap<String, Set<String>>();
		ri.setPruning(callThreshold, dataThreshold);
		valid = new HashMap<String, Set<String>>();
		Result result_UD_CallDataTreatEqual = IR.compute(textDataset, fullModelName,
				new UD_InnerAndOuterSeq(ri, callThreshold, dataThreshold, 
						userVerifyNumber,valid));// 
		
		
		class_relation.setPruning(Setting.callThreshold, Setting.dataThreshold);
        class_relationForO.setPruning(-1, -1);
        class_relationForAllDependencies.setPruning(-1, -1);

        Result result_pruningeCall_Data_Dir = IR.compute(textDataset, fullModelName, 
        		new PruningCall_Data_Connection_Closenss(class_relation, class_relationForO, 
        				class_relationForAllDependencies,
        				UseEdge.Call, 1.0, 1.0));
        /**
         * @date 2018.1.27
         * @author zzf
         * @description remove requirements which achieved by specified classes. 
         */
        findBadReq.detect(result_UD_CallDataTreatEqual, result_UD_CSTI);
        
        
        Map<String,Map<String,Double>> rtmMap = read(rtmPath);
        result_ir = filter(result_ir,rtmMap);
        result_UD_CSTI = filter(result_UD_CSTI,rtmMap);
        result_pruningeCall_Data_Dir = filter(result_pruningeCall_Data_Dir,rtmMap);
        result_UD_CallDataTreatEqual = filter(result_UD_CallDataTreatEqual,rtmMap);
        
        
        
		String irRecord = getRecord(result_ir, result_UD_CallDataTreatEqual);
		result[projectIndex][modelIndex][0] = irRecord;
		String udRecord = getRecord(result_UD_CSTI, result_UD_CallDataTreatEqual);
		result[projectIndex][modelIndex][1] = udRecord;
		
		String closenessRecord = getRecord(result_pruningeCall_Data_Dir,result_UD_CallDataTreatEqual);
		result[projectIndex][modelIndex][2] = closenessRecord;
		
		String clusterRecord = getRecord(result_UD_CallDataTreatEqual);
		result[projectIndex][modelIndex][3] = clusterRecord;
	}

	private Map<String, Map<String, Double>> read(String rtmPath) throws FileException, IOException {
		Map<String,Map<String,Double>> rtmMap = new HashMap<String,Map<String,Double>>();
		String content = fileProcess.getFileConent(rtmPath);
	    String[] lines = content.split("\n");
	    for(int i = 0; i < lines.length;i++) {
	    	String[] strs = lines[i].split("\\s+");
	    	String req = strs[0];
	    	String source = strs[1];
	    	Double score = Double.valueOf(strs[2]);
	    	if(!rtmMap.containsKey(req)) {
	    		rtmMap.put(req, new HashMap<String,Double>());
	    	}
	    	rtmMap.get(req).put(source, score);
	    }
	    
	    Iterator<String> ite = rtmMap.keySet().iterator();
	    while(ite.hasNext()) {
	    	String req = ite.next();
	    	if(rtmMap.get(req).size()<=countThreshold) {
	    		ite.remove();
	    	}
	    }
		return rtmMap;
	}

	private Result filter(Result origin, Map<String,Map<String,Double>> rtmMap) {
		SimilarityMatrix matrixAfterFilter = new SimilarityMatrix();
		LinksList allLinks = origin.matrix.allLinks();
		for(SingleLink link:allLinks) {
			String req = link.getSourceArtifactId();
			if(rtmMap.containsKey(req)) {
				matrixAfterFilter.addLink(link.getSourceArtifactId(), link.getTargetArtifactId(),
						link.getScore());
			}
		}
		
		SimilarityMatrix rtmMatrix = new SimilarityMatrix();
		for(String req:rtmMap.keySet()) {
			Map<String,Double> sourceScoreMap = rtmMap.get(req);
			for(String source:sourceScoreMap.keySet()) {
				rtmMatrix.addLink(req,source,sourceScoreMap.get(source));
			}
		}
		
		return new ConstantThresholdResult(matrixAfterFilter,rtmMatrix);
	}

	private String getRecord(Result result) {
		StringBuilder record = new StringBuilder();
		double irAp = result.getAveragePrecisionByRanklist();
		double irMap = result.getMeanAveragePrecisionByQuery();

		double rate = Double.valueOf(System.getProperty("rate"));
		String rateStr = (rate + "").substring(0, Math.min(5, (rate + "").length()));
		record.append(irAp + ";" + irMap + ";" + "_" + ";" + rateStr);
		return record.toString();
	}

	private String getRecord(Result to, Result ours) {
		StringBuilder record = new StringBuilder();
		double irAp = to.getAveragePrecisionByRanklist();
		double irMap = to.getMeanAveragePrecisionByQuery();
		record.append(irAp + ";" + irMap + ";" + printPValue(ours, to) + ";" + "_");
		return record.toString();
	}

	private RelationInfo getRelationInfo(String projectName) throws IOException, ClassNotFoundException {
		Project project = projectMap.get(projectName);
		FileInputStream fis = new FileInputStream(project.getClass_RelationInfoPathWhole());
		ObjectInputStream ois = new ObjectInputStream(fis);
		RelationInfo ri = (RelationInfo) ois.readObject();
		ois.close();
		fis.close();
		return ri;
	}

	private TextDataset getTextDataset(String projectName) {
		Project project = projectMap.get(projectName.trim());
		TextDataset textDataset = new TextDataset(project.getUcPath(), project.getClassDirPath(),
				project.getRtmClassPath());
		return textDataset;
	}

	public String[] getArrFromFile(String path) {
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

	public void init() {
		initModelMap();
		initProjectMap();
		initIdMapProject();
		initIdMapModel();
	}

	private void initModelMap() {
		modelMap.put("vsm", "cn.edu.nju.cs.itrace4.core.ir.VSM");
		modelMap.put("js", "cn.edu.nju.cs.itrace4.core.ir.JSD");
		modelMap.put("lsi", "cn.edu.nju.cs.itrace4.core.ir.LSI");
	}

	private void initProjectMap() {
		projectMap.put("itrust", new Itrust());
		projectMap.put("gantt", new Gantt());
		projectMap.put("jhotdraw", new JhotDraw());
		projectMap.put("maven", new Maven());
		projectMap.put("infinispan", new Infinispan());
		projectMap.put("pig", new Pig());
		projectMap.put("maven_cluster", new Maven_Cluster());
		projectMap.put("maven_cluster_40", new Maven_Cluster_40());
		projectMap.put("pig_cluster", new Pig_Cluster());
		projectMap.put("maven_testcase", new Maven_TestCase());
	}

	private void initIdMapProject() {
		idMapProject.put(0, "iTrust");
		idMapProject.put(1, "maven_testcase");
		idMapProject.put(2, "pig");
		idMapProject.put(3, "infinispan");
	}

	private void initIdMapModel() {
		idMapModel.put(0, "vsm");
		idMapModel.put(1, "lsi");
		idMapModel.put(2, "js");
	}

	private double printPValue(Result ours, Result compareTo) {
		MannWhitneyUTest mannWhitneyUTest = new MannWhitneyUTest();
		double pValue_fmeasure = mannWhitneyUTest.mannWhitneyUTest(ours.getWilcoxonDataArray_fmeasure(),
				compareTo.getWilcoxonDataArray_fmeasure());
		return pValue_fmeasure;
	}

	public static void main(String[] args) throws FileException {
		//String projectPath = "resource/config/project.txt";
		String projectPath = "resource/config/test.txt";
		
		String modelPath = "resource/config/model.txt";
		BatchStoreAPAfterFilter bp = new BatchStoreAPAfterFilter(projectPath, modelPath);
		try {
			bp.batch();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}