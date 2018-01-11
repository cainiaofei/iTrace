package cn.edu.nju.cs.itrace4.demo.cdgraph.algoTest;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.xml.sax.SAXException;

import cn.edu.nju.cs.itrace4.core.algo.None_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.UD_CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.demo.FileParse.XmlParse;
import cn.edu.nju.cs.itrace4.demo.cdgraph.UD_CallDataTreatEqual;
import cn.edu.nju.cs.itrace4.demo.cdgraph.UD_CallDataTreatEqualCount;
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
import cn.edu.nju.cs.itrace4.demo.relation.SubGraph;
import cn.edu.nju.cs.itrace4.demo.visual.MyVisualCurve;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;

public class CallDataTreatEqualCountTest {
	//private StoreSubGraphInfoByThreshold storeSubGraphInfoByThreshold;
		private Project project;
		private String model;
		private Map<String,Project> projectMap = new HashMap<String,Project>();
		private Map<String,String> modelMap = new HashMap<String,String>();
		private XmlParse xmlParse; 
		private double callEdgeScoreThreshold;
	    private double dataEdgeScoreThreshold;
		private double percent;
	    
		public CallDataTreatEqualCountTest() throws ParserConfigurationException,
			SAXException, IOException{
			initProjectMap();
			initModelMap();
			xmlParse = new XmlParse();
			//read xml
			String[] res = xmlParse.process();
			project = projectMap.get(res[0]);
			model = modelMap.get(res[1]);
			callEdgeScoreThreshold = Double.valueOf(res[2]);
			dataEdgeScoreThreshold = Double.valueOf(res[3]);
			percent = Double.valueOf(res[4]);
			System.setProperty("routerLen", Integer.valueOf(res[5])+"");
		}
		
		private void initModelMap(){
			modelMap.put("vsm","cn.edu.nju.cs.itrace4.core.ir.VSM");
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


		public void run() throws IOException, ClassNotFoundException {
	        TextDataset textDataset = new TextDataset(project.getUcPath(), project.getClassDirPath(), 
	        		project.getRtmClassPath());

	        FileInputStream fis = new FileInputStream(project.getClass_RelationInfoPathWhole());
	        ObjectInputStream ois = new ObjectInputStream(fis);
	        RelationInfo ri = (RelationInfo) ois.readObject();
	        
	        ri.showMessage();
	        
	        ois.close();
	        
	        Result result_ir = IR.compute(textDataset, model, new None_CSTI());
	        Result result_UD_CSTI = IR.compute(textDataset, model, new UD_CSTI(ri));
	        
	        Map<String,Set<String>> valid = new HashMap<String,Set<String>>();
	        ri.setPruning(callEdgeScoreThreshold, dataEdgeScoreThreshold);
	        valid = new HashMap<String,Set<String>>();
	        Result result_UD_CallDataTreatEqual = IR.compute(textDataset,model,
	        		new UD_CallDataTreatEqualCount(ri,callEdgeScoreThreshold,
	        			dataEdgeScoreThreshold,600,valid));//0.7
	        
	        MyVisualCurve curve = new MyVisualCurve();
	        curve.addLine(result_ir);
	        curve.addLine(result_UD_CSTI);
	        curve.addLine(result_UD_CallDataTreatEqual);
	        double rate = Double.valueOf(System.getProperty("rate"));
	        String rateStr = (rate+"").substring(0, Math.min(5, (rate+"").length()));
	        double ud_pValue = printPValue(result_UD_CallDataTreatEqual,result_UD_CSTI);
	        double ir_pValue = printPValue(result_UD_CallDataTreatEqual,result_ir);
	        curve.showChart(project.getProjectName()+"rate:"+rateStr+"ud_pValue:"+ud_pValue);
	        System.out.println("----------------IR AP/MAP-------------");
	        System.out.println("AP:"+result_ir.getAveragePrecisionByRanklist());
	        System.out.println("MAP:"+result_ir.getMeanAveragePrecisionByQuery());
	        System.out.println("----------------UD AP/MAP-------------");
	        System.out.println("AP:"+result_UD_CSTI.getAveragePrecisionByRanklist());
	        System.out.println("MAP:"+result_UD_CSTI.getMeanAveragePrecisionByQuery());
	        System.out.println("----------------Cluster AP/MAP-------------");
	        System.out.println("AP:"+result_UD_CallDataTreatEqual.getAveragePrecisionByRanklist());
	        System.out.println("MAP:"+result_UD_CallDataTreatEqual.getMeanAveragePrecisionByQuery());
	    }

		private double printPValue(Result ours, Result compareTo) {
		      MannWhitneyUTest mannWhitneyUTest = new MannWhitneyUTest();
		      double pValue_fmeasure = mannWhitneyUTest.mannWhitneyUTest(ours.getWilcoxonDataArray_fmeasure(), compareTo.getWilcoxonDataArray_fmeasure());
		      return pValue_fmeasure;
		}
		
		private int getResultSize(Result result) {
			int count = 0;
			for(String key:result.matrix.sourceArtifactsIds()) {
				count += result.matrix.getLinksForSourceId(key).size();
			}
			return count;
		}
		
		public void filterSubGraphsList(Set<String> set,List<SubGraph> subGraphList,
				Map<Integer,String> vertexIdNameMap){
			for(SubGraph subGraph:subGraphList){
				List<Integer> vertexList = subGraph.getVertexList();
				Iterator<Integer> ite = vertexList.iterator();
				while(ite.hasNext()){
					if(!set.contains(vertexIdNameMap.get(ite.next()))){
						ite.remove();
					}
				}
			}
			
			Iterator<SubGraph> subGraphIte = subGraphList.iterator();
			while(subGraphIte.hasNext()){
				if(subGraphIte.next().getVertexList().size()==0){
					subGraphIte.remove();
				}
			}
		}
		
		public static void main(String[] args) throws IOException, ClassNotFoundException,
				ParserConfigurationException, SAXException {
			long startTime = System.currentTimeMillis();
	    	CallDataTreatEqualCountTest bonusForLoneBoot = new CallDataTreatEqualCountTest();
	    	bonusForLoneBoot.run();
	    	long endTime = System.currentTimeMillis();
	    	System.out.println("time cost:"+(endTime-startTime)*1.0/1000/60);
	    }
}
