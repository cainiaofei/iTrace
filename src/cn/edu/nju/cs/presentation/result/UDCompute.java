package cn.edu.nju.cs.presentation.result;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.cs.itrace4.core.algo.None_CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.demo.cdgraph.inneroutter.UD_InnerAndOuterSeq;
import cn.edu.nju.cs.itrace4.demo.exp.project.Itrust;
import cn.edu.nju.cs.itrace4.demo.exp.project.Project;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;

/**
 * @author zzf
 * @date 2018.4.14
 * @description  return the link which has been verified and result.
 */
public class UDCompute {
	private Project project = new Itrust();
	private double callThreshold = 0.4;
	private double dataThreshold = 0.8;
	private int userVerifyCount;
	private double percent = 0.035;
	
	
	public Result udExecute(String model) throws ClassNotFoundException, IOException {
		TextDataset textDataset = getTextDataset(project);
		RelationInfo ri = getRelationInfo(project);
		userVerifyCount = (int)(ri.getVertexIdNameMap().size() * percent);
		
		 Map<String,Set<String>> valid = new HashMap<String,Set<String>>();
	        ri.setPruning(callThreshold, dataThreshold);
	    valid = new HashMap<String,Set<String>>();
		
		Result result_UD_CallDataTreatEqual = IR.compute(textDataset,model,
        		new UD_InnerAndOuterSeq(ri,callThreshold,
        			dataThreshold,userVerifyCount,valid));//0.7
		return result_UD_CallDataTreatEqual;
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
