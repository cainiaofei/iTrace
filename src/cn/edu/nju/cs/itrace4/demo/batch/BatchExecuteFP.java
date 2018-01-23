package cn.edu.nju.cs.itrace4.demo.batch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.core.algo.None_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.UD_CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.demo.cdgraph.UD_CallDataTreatEqualOuterLessThanInner;
import cn.edu.nju.cs.itrace4.demo.exp.project.Project;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.refactor.exception.FileException;
import cn.edu.nju.cs.refactor.exp.input.ModelFactory;
import cn.edu.nju.cs.refactor.exp.input.ModelFactoryImp;
import cn.edu.nju.cs.refactor.exp.input.ProjectFactory;
import cn.edu.nju.cs.refactor.exp.input.ProjectFactoryImp;
import cn.edu.nju.cs.refactor.exp.out.FPReduce;
import cn.edu.nju.cs.refactor.exp.out.FPReduceThinkVisit;
import cn.edu.nju.cs.refactor.util.FileProcess;
import cn.edu.nju.cs.refactor.util.FileProcessTool;
import cn.edu.nju.cs.refactor.util.FileWrite;
import cn.edu.nju.cs.refactor.util.FileWriterImp;

public class BatchExecuteFP {
	private String projectPath = "resource/config/project.txt";
	private String modelPath = "resource/config/model.txt";
	private String template = "resource/template/fpData.format";;
	
	private String targetPath = "batch";
	
	private double callThreshold = 0.6;
	private double dataThreshold = 0.8;
	
	private FPReduce fpReduce = new FPReduceThinkVisit();
	private ProjectFactory projectFactory = new ProjectFactoryImp();
	private ModelFactory modelFactory = new ModelFactoryImp();
	private FileProcess fileProcess = new FileProcessTool();
	private FileWrite fileWrite = new FileWriterImp();
	
	public BatchExecuteFP() {}
	
	public void getFPData() throws ClassNotFoundException, IOException {
		String[] projects = getArrFromFile(projectPath);
		String[] models = getArrFromFile(modelPath);
		for(int projectIndex = 0; projectIndex<projects.length;projectIndex++) {
			fileWrite.createFile(targetPath+File.separator+projects[projectIndex]+".csv");
			StringBuilder sb = new StringBuilder();
			Project project = projectFactory.generate(projects[projectIndex].trim());
			TextDataset textDataset = getTextDataset(project);
			RelationInfo ri = getRelationInfo(project);
			for(int modelIndex = 0; modelIndex<models.length;modelIndex++) {
				String model = modelFactory.generate(models[modelIndex]);
				ri.setPruning(0, 0);
				Result result_ir = IR.compute(textDataset, model, new None_CSTI());
				Map<String, Set<String>> valid = new HashMap<String, Set<String>>();
				ri.setPruning(callThreshold, dataThreshold);
				valid = new HashMap<String, Set<String>>();
				Result result_UD_CallDataTreatEqual = IR.compute(textDataset, model,
						new UD_CallDataTreatEqualOuterLessThanInner(ri, dataThreshold, dataThreshold, 
								3,valid));// 0.7
				String[] fpDataList = fpReduce.getFPReduceData(result_UD_CallDataTreatEqual, result_ir, valid);
				sb.append(models[modelIndex]+";");
				for(String fpData:fpDataList) {
					sb.append(fpData+";");
				}
				sb.append("\n");
				fileWrite.writeContent(sb.toString());
				fileWrite.close();
			}
			
		}
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
}
