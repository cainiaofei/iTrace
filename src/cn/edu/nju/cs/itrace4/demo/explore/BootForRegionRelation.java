package cn.edu.nju.cs.itrace4.demo.explore;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.None_CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.demo.FileParse.XmlParse;
import cn.edu.nju.cs.itrace4.demo.exp.project.Gantt;
import cn.edu.nju.cs.itrace4.demo.exp.project.Infinispan;
import cn.edu.nju.cs.itrace4.demo.exp.project.Itrust;
import cn.edu.nju.cs.itrace4.demo.exp.project.JhotDraw;
import cn.edu.nju.cs.itrace4.demo.exp.project.Maven;
import cn.edu.nju.cs.itrace4.demo.exp.project.Project;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;

public class BootForRegionRelation {
	//private StoreSubGraphInfoByThreshold storeSubGraphInfoByThreshold;
		private Project project;
		private String model;
		private Map<String,Project> projectMap = new HashMap<String,Project>();
		private Map<String,String> modelMap = new HashMap<String,String>();
		private XmlParse xmlParse; 
		private double callEdgeScoreThreshold;
	    private double dataEdgeScoreThreshold;
	    
		public BootForRegionRelation() throws ParserConfigurationException, SAXException, IOException{
			initProjectMap();
			initModelMap();
			xmlParse = new XmlParse();
			//read xml
			String[] res = xmlParse.process();
			project = projectMap.get(res[0]);
			model = modelMap.get(res[1]);
			callEdgeScoreThreshold = Double.valueOf(res[2]);
			dataEdgeScoreThreshold = Double.valueOf(res[3]);
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

	        FileInputStream fis = new FileInputStream(project.getClass_RelationInfoPath());
	        ObjectInputStream ois = new ObjectInputStream(fis);
	        RelationInfo ri = (RelationInfo) ois.readObject();
	        ois.close();
	        Result result_ir = IR.compute(textDataset, model, new None_CSTI());
	        ri.setPruning(callEdgeScoreThreshold, dataEdgeScoreThreshold);
	        RelationBetweenRegion relationBetweenRegion = new RelationBetweenRegion(ri);
	        relationBetweenRegion.getRelationBetweenRegion(result_ir.getMatrix(), textDataset);
	    }

		public static void main(String[] args) throws IOException, ClassNotFoundException,
				ParserConfigurationException, SAXException {
			long startTime = System.currentTimeMillis();
	    	BootForRegionRelation bonusForLoneBoot = new BootForRegionRelation();
	    	bonusForLoneBoot.run();
	    	long endTime = System.currentTimeMillis();
	    	System.out.println("time cost:"+(endTime-startTime)*1.0/1000/60);
	    }
}
