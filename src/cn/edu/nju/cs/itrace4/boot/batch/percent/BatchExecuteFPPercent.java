package cn.edu.nju.cs.itrace4.boot.batch.percent;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.None_CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.core.algo.region.calldata.UD_CallDataTreatEqualOuterLessThanInner;
import cn.edu.nju.cs.itrace4.core.algo.region.calldata.innerBonus.UD_InnerAndOuterSeq;
import cn.edu.nju.cs.itrace4.exp.input.ModelFactory;
import cn.edu.nju.cs.itrace4.exp.input.ModelFactoryImp;
import cn.edu.nju.cs.itrace4.exp.input.ProjectFactory;
import cn.edu.nju.cs.itrace4.exp.input.ProjectFactoryImp;
import cn.edu.nju.cs.itrace4.exp.out.FPReduce;
import cn.edu.nju.cs.itrace4.exp.out.FPReduceThinkVisitBasedOnCount;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.util.FileProcess;
import cn.edu.nju.cs.itrace4.util.FileProcessTool;
import cn.edu.nju.cs.itrace4.util.FileWrite;
import cn.edu.nju.cs.itrace4.util.FileWriterImp;
import cn.edu.nju.cs.itrace4.util.FileParse.project.Project;
import cn.edu.nju.cs.itrace4.util.exception.FileException;

/**
 * @author zzf
 * @date 2018.1.23
 * @description  
 */
public class BatchExecuteFPPercent {
	private String projectPath = "resource/config/project.txt";
	private String modelPath = "resource/config/model.txt";
	private String template = "resource/template/fpData.format";
	
	private String targetPath;
	
	private double callThreshold;
	private double dataThreshold;
	
	private FPReduce fpReduce = new FPReduceThinkVisitBasedOnCount();
	private ProjectFactory projectFactory = new ProjectFactoryImp();
	private ModelFactory modelFactory = new ModelFactoryImp();
	private FileProcess fileProcess = new FileProcessTool();
	private FileWrite fileWrite = new FileWriterImp();
	
	private double percent;
	private int userVerifyCount;
	
	
	public BatchExecuteFPPercent(String targetPath,double callThreshold,
			double dataThreshold,double percent) {
		this.targetPath = targetPath;
		this.callThreshold = callThreshold;
		this.dataThreshold = dataThreshold;
		this.percent = percent;
	}
	
	public void getFPData() throws ClassNotFoundException, IOException, FileException {
		String[] projects = getArrFromFile(projectPath);
		String[] models = getArrFromFile(modelPath);
		for(int projectIndex = 0; projectIndex<projects.length;projectIndex++) {
			createFPFile(targetPath,projects[projectIndex]);
			StringBuilder sb = new StringBuilder();
			sb.append(fileProcess.getFileConent(template)+"\n");
			Project project = projectFactory.generate(projects[projectIndex].trim());
			TextDataset textDataset = getTextDataset(project);
			RelationInfo ri = getRelationInfo(project);
			userVerifyCount = (int)(ri.getVertexIdNameMap().size()*percent);
			for(int modelIndex = 0; modelIndex<models.length;modelIndex++) {
				String model = modelFactory.generate(models[modelIndex]);
				ri.setPruning(0, 0);
				Result result_ir = IR.compute(textDataset, model, new None_CSTI());
				Map<String, Set<String>> valid = new HashMap<String, Set<String>>();
				ri.setPruning(callThreshold, dataThreshold);
				valid = new HashMap<String, Set<String>>();
				Result result_UD_CallDataTreatEqual = IR.compute(textDataset, model,
						new UD_InnerAndOuterSeq(ri, callThreshold, dataThreshold, 
								userVerifyCount,valid));// 0.7
				String[] fpDataList = fpReduce.getFPReduceData(result_UD_CallDataTreatEqual, result_ir, valid);
				sb.append(models[modelIndex]+";");
				for(String fpData:fpDataList) {
					sb.append(fpData+";");
				}
				sb.append("\n");
			}
			fileWrite.writeContent(sb.toString());
			fileWrite.close();
		}//project
	}
	
	private void createFPFile(String targetPath, String projectName) {
		File dir = new File(targetPath+File.separator+"fp");
		if(!dir.exists()) {
			dir.mkdir();
		}
		fileWrite.createFile(dir.getAbsolutePath() + File.separator + projectName + ".csv");
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
	
	private RelationInfo getRelationInfo(Project project) throws IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream(project.getClass_RelationInfoPathWhole());
		ObjectInputStream ois = new ObjectInputStream(fis);
		RelationInfo ri = (RelationInfo) ois.readObject();
		ois.close();
		return ri;
	}

	private TextDataset getTextDataset(Project project) {
		TextDataset textDataset = new TextDataset(project.getUcPath(), project.getClassDirPath(),
				project.getRtmClassPath());
		return textDataset;
	}
	
	public static void main(String[] args) throws FileException {
		double callThreshold = 0.4;
		double dataThreshold = 0.8;
		double percent = 0.035;
		String targetPath = "percent/OuterInnerSeq/"+percent + File.separator + 
				callThreshold + "-" + dataThreshold;
		BatchExecuteFPPercent batchExecuteFPPercent = new BatchExecuteFPPercent(targetPath,callThreshold,
				dataThreshold,percent);
		try {
			batchExecuteFPPercent.getFPData();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
}
