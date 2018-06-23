package cn.edu.nju.cs.itrace4.boot;


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

import cn.edu.nju.cs.itrace4.core.algo.prealgo.None_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.prealgo.UD_CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.demo.FileParse.XmlParse;
import cn.edu.nju.cs.itrace4.core.algo.region.calldata.UD_CallDataDynamic;
import cn.edu.nju.cs.itrace4.core.algo.region.calldata.UD_CallDataTreatEqual;
import cn.edu.nju.cs.itrace4.demo.exp.project.Itrust;
import cn.edu.nju.cs.itrace4.demo.exp.project.JhotDraw;
import cn.edu.nju.cs.itrace4.demo.exp.project.Maven;
import cn.edu.nju.cs.itrace4.demo.exp.project.Gantt;
import cn.edu.nju.cs.itrace4.demo.exp.project.Infinispan;
import cn.edu.nju.cs.itrace4.demo.exp.project.Project;
import cn.edu.nju.cs.itrace4.demo.relation.SubGraph;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.visual.MyVisualCurve;
 
public class CallDataTreatEqualTest{
	
	//private StoreSubGraphInfoByThreshold storeSubGraphInfoByThreshold;
	private Project project;
	private String model;
	private Map<String,Project> projectMap = new HashMap<String,Project>();
	private Map<String,String> modelMap = new HashMap<String,String>();
	private XmlParse xmlParse; 
	private double callEdgeScoreThreshold;
    private double dataEdgeScoreThreshold;
	private double percent;
    
	public CallDataTreatEqualTest() throws ParserConfigurationException, SAXException, IOException{
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
		modelMap.put("jsd", "cn.edu.nju.cs.itrace4.core.ir.JSD");
		modelMap.put("lsi", "cn.edu.nju.cs.itrace4.core.ir.LSI");
	} 
	
	private void initProjectMap() {
		projectMap.put("itrust", new Itrust());
		projectMap.put("gantt", new Gantt());
		projectMap.put("jhotdraw", new JhotDraw());
		projectMap.put("maven", new Maven());
		projectMap.put("infinispan", new Infinispan());
	}


	public void run() throws IOException, ClassNotFoundException {
        TextDataset textDataset = new TextDataset(project.getUcPath(), project.getClassDirPath(), 
        		project.getRtmClassPath());

        FileInputStream fis = new FileInputStream(project.getClass_RelationInfoPathWhole());
        ObjectInputStream ois = new ObjectInputStream(fis);
        RelationInfo ri = (RelationInfo) ois.readObject();
        ois.close();
        
        Result result_ir = IR.compute(textDataset, model, new None_CSTI());
        Result result_UD_CSTI = IR.compute(textDataset, model, new UD_CSTI(ri));
        
        Map<String,Set<String>> valid = new HashMap<String,Set<String>>();
        
        callEdgeScoreThreshold = 0.6;
        dataEdgeScoreThreshold = 0.7;
        ri.setPruning(callEdgeScoreThreshold, dataEdgeScoreThreshold);
        Result result_UD_CallDataDynamic = IR.compute(textDataset,model,
        		new UD_CallDataDynamic(ri,callEdgeScoreThreshold,
        				dataEdgeScoreThreshold,percent,valid));//0.7
       
        callEdgeScoreThreshold = 0.8;
        dataEdgeScoreThreshold = 0.9;

        ri.setPruning(callEdgeScoreThreshold, dataEdgeScoreThreshold);
        valid = new HashMap<String,Set<String>>();
        Result result_UD_CallDataTreatEqual = IR.compute(textDataset,model,
        		new UD_CallDataTreatEqual(ri,callEdgeScoreThreshold,
        			dataEdgeScoreThreshold,percent,valid));//0.7
        
        
        MyVisualCurve curve = new MyVisualCurve();
        curve.addLine(result_ir);
        curve.addLine(result_UD_CSTI);
        curve.addLine(result_UD_CallDataTreatEqual);
        double rate = Double.valueOf(System.getProperty("rate"));
        String rateStr = (rate+"").substring(0, 5);
        
        double ud_pValue = printPValue(result_UD_CallDataDynamic,result_UD_CSTI);
        //double ud_pValue = printPValue(result_UD_CallDataTreatEqual,result_UD_CSTI);
        curve.showChart(project.getProjectName()+"rate:"+rateStr+"_ud_pV:"+ud_pValue);
        
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
    	CallDataTreatEqualTest bonusForLoneBoot = new CallDataTreatEqualTest();
    	bonusForLoneBoot.run();
    	long endTime = System.currentTimeMillis();
    	System.out.println("time cost:"+(endTime-startTime)*1.0/1000/60);
    }
}
