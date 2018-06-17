package cn.edu.nju.cs.itrace4.visual.presentation;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.None_CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.demo.exp.project.Itrust;
import cn.edu.nju.cs.itrace4.demo.exp.project.Project;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;

/**
 * @author zzf
 * @date 2018.3.15
 * @description  
 */
public class IRCompute {
	Project project = new Itrust();
	public Result irExecute(String model) throws ClassNotFoundException, IOException {
		TextDataset textDataset = getTextDataset(project);
		RelationInfo ri = getRelationInfo(project);
		Result result_ir = IR.compute(textDataset, model, new None_CSTI());
		return result_ir;
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
