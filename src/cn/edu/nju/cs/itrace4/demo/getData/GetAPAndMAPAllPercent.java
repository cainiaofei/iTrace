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

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.None_CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.core.algo.region.calldata.UD_CallDataTreatEqualCount;
import cn.edu.nju.cs.itrace4.demo.exp.project.Itrust;
import cn.edu.nju.cs.itrace4.demo.exp.project.Maven;
import cn.edu.nju.cs.itrace4.demo.exp.project.Gantt;
import cn.edu.nju.cs.itrace4.demo.exp.project.Project;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.tool.CliffAnalyze;
 
public class GetAPAndMAPAllPercent{
	
	private Map<Integer,String> projectMap = new HashMap<Integer,String>();
	private Map<Integer,String> modelMap = new HashMap<Integer,String>();
	private CliffAnalyze cliffAnalyze;
	
	
	public GetAPAndMAPAllPercent() throws ParserConfigurationException, SAXException, IOException{
		initProjectMap();
		initModelMap();
		cliffAnalyze = new CliffAnalyze();
	}
	
	public void initProjectMap(){
		projectMap.put(0, "iTrust");
		projectMap.put(1, "Maven");
		projectMap.put(2, "Gantt");
	}
	
	public void initModelMap(){
		modelMap.put(0, "vsm");
		modelMap.put(1, "js");
		modelMap.put(2, "lsi");
	}
	
	public void initModels(String[] models){
		models[0] = "cn.edu.nju.cs.itrace4.core.ir.VSM";
		models[1] = "cn.edu.nju.cs.itrace4.core.ir.JSD";
		models[2] = "cn.edu.nju.cs.itrace4.core.ir.LSI";
	}
	
	public void initProjects(Project[] project){
		project[0] = new Itrust();
		project[1] = new Maven();
		project[2] = new Gantt();
	}
	
	public void start() throws Exception{
		Project[] projects = new Project[3];
		initProjects(projects);
		String[] models = new String[3];
		initModels(models);
		doTask(projects,models);
	}

	public void doTask(Project[] projects,String[] models) throws Exception{
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("out"+File.separator+
				"AP_MAP_AllPercent.csv")));
		BufferedWriter cpBw = new BufferedWriter(new FileWriter(new File("out"+File.separator+"compare_AllPercent.R"))); 
		for(int projectIndex = 0; projectIndex < projects.length;projectIndex++){
			  Project project = projects[projectIndex];
			  TextDataset textDataset = new TextDataset(project.getUcPath(), project.getClassDirPath(), 
		        		project.getRtmClassPath());
		      FileInputStream fis = new FileInputStream(project.getClass_RelationInfoPath());
		      ObjectInputStream ois = new ObjectInputStream(fis);
		      RelationInfo ri = (RelationInfo) ois.readObject();
		      ois.close();
		      
		      for(int modelIndex = 0; modelIndex<models.length;modelIndex++){
		    	  String model = models[modelIndex];
		          Result result_ir = IR.compute(textDataset, model, new None_CSTI());
		          ri.setPruning(0.8, 0.8);
		          for(int percent = 1; percent <= 10;percent++){
		        	   Map<String,Set<String>> valid = new HashMap<String,Set<String>>();
		        	   Result result_UD_CallDataTreatEqual = IR.compute(textDataset,model,
		   	        		new UD_CallDataTreatEqualCount(ri,0.8,
		   	        			0.8,6,valid));//0.7
				       bw.write(projectMap.get(projectIndex)+";"+"CLUSTER;"+modelMap.get(modelIndex)+";");
				       double apValue = result_UD_CallDataTreatEqual.getAveragePrecisionByRanklist();
				       String ap = String.format("%.2f", apValue);
				       double mapValue = result_UD_CallDataTreatEqual.getMeanAveragePrecisionByQuery();
				       String map = String.format("%.2f", mapValue);
				       bw.write(ap+";"+map+";"+"_"+";");
				       double cliffValue = cliffAnalyze.doCliff(result_UD_CallDataTreatEqual, result_ir, textDataset.getRtm());
				       String cliff = String.format("%.2f", cliffValue);
				       bw.write(cliff+";");
				       compare(result_UD_CallDataTreatEqual,result_ir,cpBw,project,model,percent);
		          }
		          bw.newLine();
		      }
		}///outer for loop
		bw.close();
	    cpBw.close();
	}
	
	
	
	
	private void compare(Result ours, Result compareTo,BufferedWriter cpBw,Project project,
			String model,double percent) throws IOException {
        String irName = project.getProjectName()+"_"+model;
        String ir = compareTo.getWilcoxonDataCol_fmeasure(irName);
        cpBw.write(ir);
        cpBw.newLine();
        
        String name = project.getProjectName()+"_"+model+percent;
        String our = ours.getWilcoxonDataCol_fmeasure(name);
        String notice = "print("+name+")";
        String command = "wilcox.test("+name+","+irName+")";
        cpBw.newLine();
        cpBw.write(our);
        cpBw.newLine();
        cpBw.write(notice);
        cpBw.newLine();
        cpBw.write(command);
        cpBw.newLine();
    }
	
	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();
		GetAPAndMAPAllPercent bonusForLoneBoot = new GetAPAndMAPAllPercent();
    	bonusForLoneBoot.start();
    	long endTime = System.currentTimeMillis();
    	System.out.println("time cost:"+(endTime-startTime)*1.0/1000/60);
    }

}
