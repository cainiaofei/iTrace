package cn.edu.nju.cs.itrace4.demo.getData;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


import cn.edu.nju.cs.itrace4.core.algo.prealgo.None_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.UD_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.prealgo.UseEdge;
import cn.edu.nju.cs.itrace4.core.algo.icse.PruningCall_Data_Connection_Closenss;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.ir.IRModelConst;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.demo.algo.outerVertex.process.MethodTypeProcessLone;
import cn.edu.nju.cs.itrace4.demo.algo.outerVertex.process.UD_CallThenDataWithBonusForLone;
import cn.edu.nju.cs.itrace4.demo.exp.project.Itrust;
import cn.edu.nju.cs.itrace4.demo.exp.project.JhotDraw;
import cn.edu.nju.cs.itrace4.demo.exp.project.Gantt;
import cn.edu.nju.cs.itrace4.demo.exp.project.Project;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.util.Setting;
 
public class GetAPAndMAPData{
	
	private Map<Integer,String> projectMap = new HashMap<Integer,String>();
	private Map<Integer,String> methodMap = new HashMap<Integer,String>();
	
	public GetAPAndMAPData(){
		initProjectMap();
		initModelMap();
	}
	
	public void initProjectMap(){
		projectMap.put(0, "iTrust");
		projectMap.put(1, "Maven_TestCase");
		projectMap.put(2, "jHotDraw");
	}
	
	public void initModelMap(){
		methodMap.put(0, "ir");
		methodMap.put(1, "ud");
		methodMap.put(2, "closeness");
		methodMap.put(3, "ud_closeness");
	}
	
	public void initModels(String[] models){
		models[0] = "cn.edu.nju.cs.itrace4.core.ir.VSM";
		models[1] = "cn.edu.nju.cs.itrace4.core.ir.JSD";
		models[2] = "cn.edu.nju.cs.itrace4.core.ir.LSI";
	}
	
	public void initProjects(Project[] project){
		project[0] = new Itrust();
		project[1] = new Gantt();
		project[2] = new JhotDraw();
	}
	
	public void start() throws Exception{
		Project[] projects = new Project[3];
		initProjects(projects);
		String[] models = new String[3];
		initModels(models);
		doTask(projects,models);
	}

	public void doTask(Project[] projects,String[] models) throws Exception{
		/**
		 * project 0-iTrust 1-Gantt 2-jHotDraw
		 * method 0-ir 1-ud 2-closeness 3-ud_closeness
		 * model 0-vsm 1-jsd 2-lsi
		 * value 0-ap 1-map
		 * **/
		double[][][][] data = new double[3][4][3][2];// 
		for(int projectIndex = 0; projectIndex < projects.length;projectIndex++){
			  Project project = projects[projectIndex];
			  TextDataset textDataset = new TextDataset(project.getUcPath(), project.getClassDirPath(), 
		        		project.getRtmClassPath());
		      FileInputStream fis = new FileInputStream(project.getClass_RelationInfoPath());
		      ObjectInputStream ois = new ObjectInputStream(fis);
		      RelationInfo ri = (RelationInfo) ois.readObject();
		      ois.close();
		      
		      //below closeness method
		      FileInputStream fis1 = new FileInputStream(project.getClass_RelationInfoPath());
		      ObjectInputStream ois1 = new ObjectInputStream(fis1);
		      RelationInfo class_relation = (RelationInfo) ois1.readObject();ois1.close();
		      FileInputStream fisForO = new FileInputStream(project.getClass_RelationInfoPath());
		      ObjectInputStream oisForO = new ObjectInputStream(fisForO);
		      RelationInfo class_relationForO = (RelationInfo) oisForO.readObject();oisForO.close();
		      FileInputStream fisForAllDependencies = new FileInputStream(project.getClass_RelationInfoPathWhole());
		      ObjectInputStream oisForAllDependencies = new ObjectInputStream(fisForAllDependencies);
		      RelationInfo class_relationForAllDependencies = (RelationInfo) oisForAllDependencies.readObject();oisForAllDependencies.close();
		      class_relation.setPruning(Setting.callThreshold, Setting.dataThreshold);
		      class_relationForO.setPruning(-1, -1);
		      class_relationForAllDependencies.setPruning(-1, -1);
		      for(int modelIndex = 0; modelIndex<models.length;modelIndex++){
		    	  String model = models[modelIndex];
		    	  Result result_ir = IR.compute(textDataset, model, new None_CSTI());
		          Result result_UD_CSTI = IR.compute(textDataset,model, new UD_CSTI(ri));
		          
		          Result result_pruningeCall_Data_Dir = IR.compute(textDataset, IRModelConst.VSM, 
		          		new PruningCall_Data_Connection_Closenss(class_relation, class_relationForO, 
		          				class_relationForAllDependencies,
		          				UseEdge.Call, 1.0, 1.0));
		          
		          ri.setPruning(0.6, 0.6);
		          Map<String,Set<String>> valid = new HashMap<String,Set<String>>();
		          Result result_UD_CallThenDataProcessLoneInnerMean = IR.compute(textDataset,model,
		          		new UD_CallThenDataWithBonusForLone(ri,0.6,
		          				0.6,MethodTypeProcessLone.InnerMean,1,valid));
		          ri.setPruning(0,0);
		          
		          //store result
		          //ir
		          data[projectIndex][0][modelIndex][0] = result_ir.getAveragePrecisionByRanklist();
		          data[projectIndex][0][modelIndex][1] = result_ir.getMeanAveragePrecisionByQuery();
		          //ud
		          data[projectIndex][1][modelIndex][0] = result_UD_CSTI.getAveragePrecisionByRanklist();
		          data[projectIndex][1][modelIndex][1] = result_UD_CSTI.getMeanAveragePrecisionByQuery();
		          //closeness
		          data[projectIndex][2][modelIndex][0] = result_pruningeCall_Data_Dir.getAveragePrecisionByRanklist();
		          data[projectIndex][2][modelIndex][1] = result_pruningeCall_Data_Dir.getMeanAveragePrecisionByQuery();
		          //ud_closeness
		          data[projectIndex][3][modelIndex][0] = result_UD_CallThenDataProcessLoneInnerMean.getAveragePrecisionByRanklist();
		          data[projectIndex][3][modelIndex][1] = result_UD_CallThenDataProcessLoneInnerMean.getMeanAveragePrecisionByQuery();
		          store(data);
		      }
		}
	}
	
	
	
	private void store(double[][][][] data) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("out"+File.separator+"AP_MAP.csv")));
		for(int projectIndex = 0; projectIndex < data.length;projectIndex++){
			for(int methodIndex = 0; methodIndex < data[projectIndex].length;methodIndex++){
				StringBuilder sb = new StringBuilder();
				sb.append(projectMap.get(projectIndex)+";");
				sb.append(methodMap.get(methodIndex)+";");
				for(int modelIndex = 0; modelIndex < data[projectIndex][methodIndex].length;modelIndex++){
					String ap = String.format("%.2f", data[projectIndex][methodIndex][modelIndex][0]);
					sb.append(ap+";");
					String map = String.format("%.2f", data[projectIndex][methodIndex][modelIndex][1]);
					sb.append(map+";");
				}
				bw.write(sb.toString());
				bw.newLine();
			}
		}
		bw.close();
	}
	
	
	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();
		GetAPAndMAPData bonusForLoneBoot = new GetAPAndMAPData();
    	bonusForLoneBoot.start();
    	long endTime = System.currentTimeMillis();
    	System.out.println("time cost:"+(endTime-startTime)*1.0/1000/60);
    }

}
