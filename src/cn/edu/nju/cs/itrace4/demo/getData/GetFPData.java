package cn.edu.nju.cs.itrace4.demo.getData;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.None_CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.demo.algo.outerVertex.process.MethodTypeProcessLone;
import cn.edu.nju.cs.itrace4.demo.algo.outerVertex.process.UD_CallThenDataWithBonusForLone;
import cn.edu.nju.cs.itrace4.demo.cdgraph.UD_CallDataDynamic;
import cn.edu.nju.cs.itrace4.demo.exp.project.Itrust;
import cn.edu.nju.cs.itrace4.demo.exp.project.JhotDraw;
import cn.edu.nju.cs.itrace4.demo.exp.project.Maven;
import cn.edu.nju.cs.itrace4.demo.exp.project.Gantt;
import cn.edu.nju.cs.itrace4.demo.exp.project.Project;
import cn.edu.nju.cs.itrace4.demo.tool.CliffAnalyze;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
 
public class GetFPData{
	
	private Map<Integer,String> projectMap = new HashMap<Integer,String>();
	private Map<Integer,String> modelMap = new HashMap<Integer,String>();
	private CliffAnalyze cliffAnalyze;
	private double callThreshold = 0.6, dataThreshold = 0.7;
	
	public GetFPData() throws ParserConfigurationException, SAXException, IOException{
		initProjectMap();
		initModelMap();
		cliffAnalyze = new CliffAnalyze();
	}
	
	public void initProjectMap(){
		projectMap.put(0, "iTrust");
		projectMap.put(1, "Gantt");
		projectMap.put(2, "jHotDraw");
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
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("fp"+File.separator+
				"fp_compare.csv")));
		String firstLine = getFirstLine();
		String secondLine = getSecondLine();
		bw.write(firstLine);
		bw.write(secondLine);
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
		          ri.setPruning(callThreshold, dataThreshold);
		          Map<String,Set<String>> valid = new HashMap<String,Set<String>>();
		          Result result_UD_CallDataDynamic = IR.compute(textDataset,model,
		          		new UD_CallDataDynamic(ri,callThreshold,
		          				dataThreshold,1,valid));//0.7
		          ri.setPruning(0,0);
		          bw.write(projectMap.get(projectIndex)+";"+modelMap.get(modelIndex)+";");
		          compare(result_UD_CallDataDynamic,result_ir,bw,project,model,textDataset);
		      }
		}///outer for loop
		bw.close();
	}
	
	private String getSecondLine() {
		StringBuilder sb = new StringBuilder();
		sb.append(";;");
		for(int i = 1; i <= 10;i++) {
			sb.append("Precision;FP;");
		}
		sb.append("\n");
		return sb.toString();
	}

	private String getFirstLine() {
		StringBuilder sb = new StringBuilder();
		sb.append(";;");
		for(int i = 1; i <= 10;i++) {
			int percent = 10 * i;
			sb.append("Recall("+percent+"%);;");
		}
		sb.append("\n");
		return sb.toString();
	}

	private void compare(Result ours, Result compareTo,BufferedWriter bw,
			Project project,String model,TextDataset textDataset) throws IOException {
        List<Double> oursPrecisionList = ours.getPrecisionAtRecallByTen();
        List<Double> compareToPrecisionList = compareTo.getPrecisionAtRecallByTen();
        List<Integer> oursFP = ours.getFalsePositiveAtRecallByTen();
        List<Integer> compareFP = compareTo.getFalsePositiveAtRecallByTen();
        String irName = project.getProjectName()+"_"+model;
        String ir = compareTo.getWilcoxonDataCol_fmeasure(irName);
        for (int i = 0; i < oursPrecisionList.size(); i++) {
            double ourPrecision = oursPrecisionList.get(i);
            double theirPrecision = compareToPrecisionList.get(i);
            String precisionDiff = String.format("%.2f",(ourPrecision-theirPrecision)*100);
            bw.write(precisionDiff+"%;");
            int ourFP = oursFP.get(i);
            int theirFP = compareFP.get(i); 
            String fpDiff = String.format("%.2f",(ourFP-theirFP)*1.0/ourFP*100);
            bw.write(fpDiff+"%;");
            
            //p-value
//            String name = project.getProjectName()+"_"+(i+1)*10+model;
//            String our = ours.getWilcoxonDataCol_fmeasure(name);
//            String notice = "print("+project.getProjectName()+"_"+(i+1)*10+model+")";
//            String command = "wilcox.test("+name+","+irName+")";
//            //cliff
//            bw.write("_;");
//            double cliff = cliffAnalyze.doCliff(ours, compareTo,textDataset.getRtm());
//            String cliffValue = String.format("%.2f", cliff*100)+"%";
//            bw.write(cliffValue+";");
        }
        bw.newLine();
    }
	
	public static void main(String[] args) throws Exception {
		System.setProperty("routerLen", "6");
		long startTime = System.currentTimeMillis();
		GetFPData bonusForLoneBoot = new GetFPData();
    	bonusForLoneBoot.start();
    	long endTime = System.currentTimeMillis();
    	System.out.println("time cost:"+(endTime-startTime)*1.0/1000/60);
    }

}
