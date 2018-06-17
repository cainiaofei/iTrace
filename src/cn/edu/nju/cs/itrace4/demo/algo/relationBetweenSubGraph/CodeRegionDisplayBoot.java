package cn.edu.nju.cs.itrace4.demo.algo.relationBetweenSubGraph;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.demo.FileParse.XmlParse;
import cn.edu.nju.cs.itrace4.demo.algo.outerVertex.process.CodeRegionClosenessType;
import cn.edu.nju.cs.itrace4.demo.exp.project.Gantt;
import cn.edu.nju.cs.itrace4.demo.exp.project.Infinispan;
import cn.edu.nju.cs.itrace4.demo.exp.project.Itrust;
import cn.edu.nju.cs.itrace4.demo.exp.project.JhotDraw;
import cn.edu.nju.cs.itrace4.demo.exp.project.Maven;
import cn.edu.nju.cs.itrace4.demo.exp.project.Maven_Cluster_40;
import cn.edu.nju.cs.itrace4.demo.exp.project.Project;
import cn.edu.nju.cs.itrace4.tool.AnalyzeResult;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;

/**
 * @date 2018.5.29
 * @author zzf
 * @description the main class used to display the information code region.
 */
public class CodeRegionDisplayBoot {
	
	private Project project;
	private AnalyzeResult analyzeResult;
	private Map<String,Project> projectMap = new HashMap<String,Project>();
	private XmlParse xmlParse; 
	private double callEdgeScoreThreshold;
    private double dataEdgeScoreThreshold;
    private CodeRegionInfo codeRegionInfo;
    
    
	public CodeRegionDisplayBoot() throws ParserConfigurationException, SAXException, IOException {
		initProjectMap();
		analyzeResult = new AnalyzeResult();
		xmlParse = new XmlParse();
		//read xml
		String[] res = xmlParse.process();
		project = projectMap.get(res[0]);
		callEdgeScoreThreshold = Double.valueOf(res[2]);
		dataEdgeScoreThreshold = Double.valueOf(res[3]);
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
        ri.setPruning(callEdgeScoreThreshold, dataEdgeScoreThreshold);
        codeRegionInfo = new CodeRegionInfo(textDataset,ri,
        		CodeRegionClosenessType.closenessBetweenRepresent);
        codeRegionInfo.showClosenessBetweenRegion();
    }
	
	public static void main(String[] args) throws IOException, ClassNotFoundException,
			ParserConfigurationException, SAXException {
		long startTime = System.currentTimeMillis();
		CodeRegionDisplayBoot codeRegionDisplayBoot = new CodeRegionDisplayBoot();
		codeRegionDisplayBoot.run();
		long endTime = System.currentTimeMillis();
		System.out.println("time cost:"+(endTime-startTime)*1.0/1000/60);
	}
}
