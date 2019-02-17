package cn.edu.nju.cs.itrace4.core.algo.region.closeness;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.tool.AnalyzeResult;
import cn.edu.nju.cs.itrace4.util.FileParse.XmlParse;
import cn.edu.nju.cs.itrace4.util.FileParse.project.Gantt;
import cn.edu.nju.cs.itrace4.util.FileParse.project.Infinispan;
import cn.edu.nju.cs.itrace4.util.FileParse.project.Itrust;
import cn.edu.nju.cs.itrace4.util.FileParse.project.JhotDraw;
import cn.edu.nju.cs.itrace4.util.FileParse.project.Maven;
import cn.edu.nju.cs.itrace4.util.FileParse.project.Maven_Cluster_40;
import cn.edu.nju.cs.itrace4.util.FileParse.project.Project;

public class MinClosenessInConnectGraphBoot {
	private Project project;
	private AnalyzeResult analyzeResult;
	private Map<String,Project> projectMap = new HashMap<String,Project>();
	private XmlParse xmlParse; 
	
	public MinClosenessInConnectGraphBoot() throws ParserConfigurationException, SAXException, IOException {
		initProjectMap();
		analyzeResult = new AnalyzeResult();
		xmlParse = new XmlParse();
		//read xml
		String[] res = xmlParse.process();
		project = projectMap.get(res[0]);
	}
	
	private void initProjectMap() {
		projectMap.put("itrust", new Itrust());
		projectMap.put("gantt", new Gantt());
		projectMap.put("jhotdraw", new JhotDraw());
		projectMap.put("maven", new Maven());
		projectMap.put("infinispan", new Infinispan());
		projectMap.put("maven_cluster_40", new Maven_Cluster_40());
	}
	
	public void run() throws IOException, ClassNotFoundException {
		TextDataset textDataset = new TextDataset(project.getUcPath(), project.getClassDirPath(), 
        		project.getRtmClassPath());
        FileInputStream fis = new FileInputStream(project.getClass_RelationInfoPath());
        ObjectInputStream ois = new ObjectInputStream(fis);
        RelationInfo ri = (RelationInfo) ois.readObject();
        ois.close();
        ri.setPruning(0, 0);//has to be added, or rather cant get call/data edge through ri.
        
        MinClosenessInConnectGraph minClosenessInConnectGraph = new MinClosenessInConnectGraph(ri,
        		textDataset);
        minClosenessInConnectGraph.showMinClosenessInConnectedArea();
	}
	
	public static void main(String[] args) throws ParserConfigurationException, 
		SAXException, IOException, ClassNotFoundException {
		MinClosenessInConnectGraphBoot boot = new MinClosenessInConnectGraphBoot();
		boot.run();
	}
}
