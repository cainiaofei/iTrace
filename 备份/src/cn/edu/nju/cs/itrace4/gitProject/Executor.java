package cn.edu.nju.cs.itrace4.gitProject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.math3.stat.inference.MannWhitneyUTest;

import cn.edu.nju.cs.itrace4.core.algo.None_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.UD_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.UseEdge;
import cn.edu.nju.cs.itrace4.core.algo.icse.PruningCall_Data_Connection_Closenss;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.ir.IRModelConst;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.demo.algo.outerVertex.process.MethodTypeProcessLone;
import cn.edu.nju.cs.itrace4.demo.algo.outerVertex.process.UD_CallThenDataWithBonusForLone;
import cn.edu.nju.cs.itrace4.demo.exp.project.Itrust;
import cn.edu.nju.cs.itrace4.demo.exp.project.Project;
import cn.edu.nju.cs.itrace4.demo.visual.MyVisualCurve;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.util.Setting;
import cn.edu.nju.cs.itrace4.visual.VisualCurve;

public class Executor implements Runnable{
	private double callThreshold;
	private double dataThreshold;
	private Project project;
	private Map<String,Double> irPvalueMap;
	private Map<String,Double> udPvalueMap;
	private String basePath = "parameterMapData";
	private String model;
	
	private TextDataset textDataset;
	private RelationInfo ri;
//	private RelationInfo class_relation;
//    private RelationInfo class_relationForO;
//    private RelationInfo class_relationForAllDependencies;
    
	public void init(TextDataset textDataset,RelationInfo ri/*, RelationInfo class_relation,
			RelationInfo class_relationForO,RelationInfo class_relationForAllDependencies*/) {
		this.textDataset = textDataset;
		this.ri = ri;
//		this.class_relation = class_relation;
//		this.class_relationForO = class_relationForO;
//		this.class_relationForAllDependencies = class_relationForAllDependencies;
	}
	
	public Executor(double callThreshold,double dataThreshold,Project project,
			String model,Map<String,Double> irPvalueMap,
			Map<String,Double> udPvalueMap) {
		this.callThreshold = callThreshold;
		this.dataThreshold = dataThreshold;
		this.project = project;
		this.model = model;
		this.irPvalueMap = irPvalueMap;
		this.udPvalueMap = udPvalueMap;
	}
	
	@Override
	public void run() {
		try {
			getDataAndGraph();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void getDataAndGraph() throws IOException, ClassNotFoundException {
        Result result_ir = IR.compute(textDataset, model, new None_CSTI());
        Result result_UD_CSTI = IR.compute(textDataset,model, new UD_CSTI(ri));
        ri.setPruning(callThreshold, dataThreshold);
        
        Map<String,Set<String>> valid = new HashMap<String,Set<String>>();
        Result result_UD_CallThenDataProcessLoneInnerMean07 = IR.compute(textDataset,model,
        		new UD_CallThenDataWithBonusForLone(ri,callThreshold,
        				dataThreshold,MethodTypeProcessLone.InnerMean,0.7,valid));//0.7
        
        //below closeness method
//        class_relation.setPruning(Setting.callThreshold, Setting.dataThreshold);
//        class_relationForO.setPruning(-1, -1);
//        class_relationForAllDependencies.setPruning(-1, -1);

//        Result result_pruningeCall_Data_Dir = IR.compute(textDataset, model, 
//        		new PruningCall_Data_Connection_Closenss(class_relation, class_relationForO, 
//        				class_relationForAllDependencies,
//        				UseEdge.Call, 1.0, 1.0));
        
//        MyVisualCurve curve = new MyVisualCurve();
//        curve.addLine(result_ir);
//        curve.addLine(result_UD_CSTI);
//        curve.addLine(result_pruningeCall_Data_Dir);
//        curve.addLine(result_UD_CallThenDataProcessLoneInnerMean07);//累加 内部 直接平均
        double irPvalue = printPValue(result_ir, result_UD_CallThenDataProcessLoneInnerMean07);
        double udPvalue = printPValue(result_UD_CSTI, result_UD_CallThenDataProcessLoneInnerMean07);
        System.out.println(project.getProjectName()+"-"+callThreshold+"-"+dataThreshold+"-"+model+":"+irPvalue);
        System.out.println(project.getProjectName()+"-"+callThreshold+"-"+dataThreshold+"-"+model+":"+udPvalue);
        irPvalueMap.put(project.getProjectName()+"-"+callThreshold+"-"+dataThreshold+"-"+model, irPvalue);
        udPvalueMap.put(project.getProjectName()+"-"+callThreshold+"-"+dataThreshold+"-"+model, udPvalue);
//        String irPvalueStr = (irPvalue+"").substring(0, 5);
//        String udPvalueStr = (udPvalue+"").substring(0, 5);
//        curve.showChart(project.getProjectName()+"-"+irPvalueStr+"-"+udPvalueStr);
//        curve.showChart(project.getProjectName());
//        store(curve);
    }
	
	/**
	 * The structure of project is projetName+callThreshold+dataThreshold; 
	 */
	private void store(VisualCurve curve) {
		File dir = new File(basePath+File.separator+project.getProjectName());
		if(!dir.exists()) {
			dir.mkdirs();
		}
		dir = new File(dir.getPath()+File.separator+callThreshold);
		if(!dir.exists()) {
			dir.mkdirs();
		}
		dir = new File(dir.getPath()+File.separator+dataThreshold);
		if(!dir.exists()) {
			dir.mkdirs();
		}
		try {
			System.out.println(dir.getAbsolutePath());
			curve.curveStore(dir.getPath(),model);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private double printPValue(Result ours, Result compareTo) {
	      MannWhitneyUTest mannWhitneyUTest = new MannWhitneyUTest();
	      double pValue_fmeasure = mannWhitneyUTest.mannWhitneyUTest(ours.getWilcoxonDataArray_fmeasure(), compareTo.getWilcoxonDataArray_fmeasure());
	      return pValue_fmeasure;
	}
	
	
	public static void main(String[] args) {
		System.setProperty("routerLen", 6+"");
		Executor executor = new Executor(0.4,0.5,new Itrust(),
				"cn.edu.nju.cs.itrace4.core.ir.VSM",
				new ConcurrentHashMap<String,Double>(),new ConcurrentHashMap<String,Double>());
		executor.run();
	}
}
