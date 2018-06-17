package cn.edu.nju.cs.itrace4.tool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.None_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.prealgo.UD_CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.demo.FileParse.XmlParse;
import cn.edu.nju.cs.itrace4.demo.algo.outerVertex.process.MethodTypeProcessLone;
import cn.edu.nju.cs.itrace4.demo.algo.outerVertex.process.UD_CallThenDataWithBonusForLone;
import cn.edu.nju.cs.itrace4.demo.exp.project.Gantt;
import cn.edu.nju.cs.itrace4.demo.exp.project.Itrust;
import cn.edu.nju.cs.itrace4.demo.exp.project.JhotDraw;
import cn.edu.nju.cs.itrace4.demo.exp.project.Project;
import cn.edu.nju.cs.itrace4.demo.visual.MyVisualCurve;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;

public class CliffAnalyze {
	//private StoreSubGraphInfoByThreshold storeSubGraphInfoByThreshold;
	private Project project;
	private String model;
	private Map<String,Project> projectMap = new HashMap<String,Project>();
	private Map<String,String> modelMap = new HashMap<String,String>();
	private XmlParse xmlParse;
	private double callEdgeScoreThreshold;
	private double dataEdgeScoreThreshold;
		
	public CliffAnalyze() throws ParserConfigurationException, SAXException, IOException{
		initProjectMap();
		initModelMap();
		xmlParse = new XmlParse();
		//read xml
		String[] res = xmlParse.process();
		project = projectMap.get(res[0]);
		model = modelMap.get(res[1]);
		callEdgeScoreThreshold = Double.valueOf(res[2]);
		dataEdgeScoreThreshold = Double.valueOf(res[3]);
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
	}

	public void process() throws IOException, ClassNotFoundException {
        TextDataset textDataset = new TextDataset(project.getUcPath(), project.getClassDirPath(), 
        		project.getRtmClassPath());
        FileInputStream fis = new FileInputStream(project.getClass_RelationInfoPath());
        ObjectInputStream ois = new ObjectInputStream(fis);
        RelationInfo ri = (RelationInfo) ois.readObject();
        ois.close();

        Result result_ir = IR.compute(textDataset, model, new None_CSTI());
        Result result_UD_CSTI = IR.compute(textDataset,model, new UD_CSTI(ri));
        ri.setPruning(callEdgeScoreThreshold, dataEdgeScoreThreshold);
        
        Map<String,Set<String>> map = new HashMap<String,Set<String>>();
        Result result_UD_CallThenDataProcessLoneInnerMean07 = IR.compute(textDataset,model,
        		new UD_CallThenDataWithBonusForLone(ri,callEdgeScoreThreshold,
        				dataEdgeScoreThreshold,MethodTypeProcessLone.InnerMean,1,map));
        MyVisualCurve curve = new MyVisualCurve();
        curve.addLine(result_ir);
        curve.addLine(result_UD_CSTI);
        curve.addLine(result_UD_CallThenDataProcessLoneInnerMean07);//
        curve.showChart(project.getProjectName());
        
        doCliff(result_UD_CallThenDataProcessLoneInnerMean07,result_UD_CSTI,textDataset.getRtm());
        
//        verifyResult(result_UD_CallThenDataProcessLoneInnerMean07.getMatrix(),
//        		result_UD_CSTI.getMatrix(), textDataset.getRtm());
//        
        String ud = result_UD_CSTI.getWilcoxonDataCol_fmeasure("UD");
        String innerMean7 = result_UD_CallThenDataProcessLoneInnerMean07.getWilcoxonDataCol_fmeasure("innerMean07");
        List<String> list = new LinkedList<String>();
        list.add(ud);
        list.add(innerMean7);
        storeRFile(list);
    }
	
	private void storeRFile(List<String> list) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("out"+File.separator+
				project.getProjectName()+"_"+model+"2"+".R")));
		for(String str:list){
			bw.write(str);
			bw.newLine();
		}
		bw.close();
	}

	private void verifyResult(SimilarityMatrix ours, SimilarityMatrix opp,SimilarityMatrix rtm) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("out/verifyResult.txt")));
		
		int correctSoFarOurs = 0;
		int correctSoFarOpp = 0;
		LinksList oursLists = ours.allLinks();
		LinksList oppLists = opp.allLinks();
		
		
		Collections.sort(oursLists,Collections.reverseOrder());
		Collections.sort(oppLists,Collections.reverseOrder());
		for(int i = 0; i < oursLists.size();i++){
			SingleLink ourLink = oursLists.get(i);
			SingleLink oppLink = oppLists.get(i);
			if(rtm.isLinkAboveThreshold(ourLink.getSourceArtifactId(),ourLink.getTargetArtifactId())){
				correctSoFarOurs++;
			}
			
			double precisionOurs = 1.0*correctSoFarOurs / (i+1);
			double recallOurs = 1.0*correctSoFarOurs / rtm.count();
			if(rtm.isLinkAboveThreshold(oppLink.getSourceArtifactId(),oppLink.getTargetArtifactId())){
				correctSoFarOpp++;
			}
			double precisionOpp = 1.0 * correctSoFarOpp / (i+1);
			double recallOpp = 1.0 * correctSoFarOurs / rtm.count();
			bw.write("ours:"+"precison@"+precisionOurs+":"+"recall@"+recallOurs);
			bw.write("cliff:"+computeF1Measure(precisionOurs, recallOurs));
			
			bw.write("opp:"+"precision@"+precisionOpp+":"+"recall@"+recallOpp);
			bw.write("cliff:"+computeF1Measure(precisionOpp, recallOpp));
			bw.newLine();
		}
		bw.close();
	}
	
	public double doCliff(Result result_UD_CallThenDataProcessLoneInnerMean07,Result result_UD_CSTI,
			SimilarityMatrix rtm) {

		List<Double> ourMethodFmeasureList = new ArrayList<Double>();
		List<Double> udFmeasureList = new ArrayList<Double>();
		
		getFmeasureList(result_UD_CallThenDataProcessLoneInnerMean07.getMatrix(), rtm, 
				ourMethodFmeasureList);
		getFmeasureList(result_UD_CSTI.getMatrix(), rtm, 
				udFmeasureList);
		
		double cliff = delta(ourMethodFmeasureList,udFmeasureList);
		
		try {
//			//compareWithTXT(ourMethodFmeasureList,udFmeasureList);
			analyzeFmeasure(ourMethodFmeasureList, udFmeasureList);
////			storeR(ourMethodFmeasureList,udFmeasureList);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return cliff;
		//System.out.println("the cliff is:"+cliff);
	}

	private void storeR(List<Double> ourMethodFmeasureList, List<Double> udFmeasureList) throws IOException {
		StringBuilder ours = new StringBuilder();
		ours.append("ours<-c(");
		for(double fmeasure:ourMethodFmeasureList){
			ours.append(fmeasure);
			ours.append(",");
		}
		ours.deleteCharAt(ours.length()-1);
		ours.append(")");
		
		StringBuilder ud = new StringBuilder();
		ud.append("ud<-c(");
		for(double fmeasure:udFmeasureList){
			ud.append(fmeasure+",");
		}
		ud.deleteCharAt(ud.length()-1);
		ud.append(")");
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("out/cliff.R")));
		bw.write(ours.toString());
		bw.newLine();
		bw.write(ud.toString());
		bw.newLine();
		bw.write("res = cliff.delta(ours,ud,return.dm=TRUE)");
		bw.newLine();
		bw.write("print(res)");
		bw.newLine();
		bw.write("print(res$dm)");
		bw.newLine();
		bw.close();
	}

	private void getFmeasureList(SimilarityMatrix matrix, SimilarityMatrix oracle,
			List<Double> f1List) {
		int currentLink = 0;
        int correctSoFar = 0;

        LinksList allLinks = matrix.allLinks();
        Collections.sort(allLinks, Collections.reverseOrder());
        System.out.println("allLinks:"+allLinks.size());
        for (SingleLink singleLink : allLinks) {
            currentLink++;
            if (oracle.isLinkAboveThreshold(singleLink.getSourceArtifactId(), singleLink.getTargetArtifactId())) {
                correctSoFar++;
                double precision = 1.0 * correctSoFar / currentLink;
                double recall = 1.0 * correctSoFar / oracle.count();
                double f1Measure = computeF1Measure(precision, recall);
                f1List.add(f1Measure);
            }
        }
	}

	 private double computeF1Measure(double precision, double recall) {
		 if(precision==0&&recall==0){
			 System.out.println("��ĸΪ0");
			 return 0;
		 }
		 else{
			 return 2.0 * precision * recall / (precision + recall);
		 }
	 }
	
	
	private double delta(List<Double> ourMethodPrecisionList, List<Double> udPrecisionList) {
        System.out.println("ourMethodPrecisionList:"+ourMethodPrecisionList.size());
        System.out.println("udPrecisionList:"+udPrecisionList.size());
        
		int tGEc = 0;
        int cGEt = 0;

        for (int i = 0; i < ourMethodPrecisionList.size(); i++) {
        	double t = ourMethodPrecisionList.get(i);
            for (int j = 0; j < udPrecisionList.size(); j++) {
            	double c = udPrecisionList.get(j);
                if (t > c) {
                    tGEc++;
                }

                if (c > t) {
                    cGEt++;
                }
            }
        }

        double result = 1.0 * (tGEc - cGEt) / (1.0 * (ourMethodPrecisionList.size() * udPrecisionList.size()));
        return result;
    }

	private void compareWithTXT(List<Double> ourMethodFmeasureList, List<Double> udFmeasureList) 
			throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("out"+File.separator+"compare.txt")));
		for(int i = 0; i < ourMethodFmeasureList.size();i++){
			for(int j = 0; j < udFmeasureList.size();j++){
				double our = ourMethodFmeasureList.get(i);
				double opp = udFmeasureList.get(j);
				bw.write(our+"");
				bw.write("------");
				bw.write(opp+"");
				bw.newLine();
			}
		}
		bw.close();
		//analyzeFmeasure(ourMethodFmeasureList,udFmeasureList);
	}
	
	private void analyzeFmeasure(List<Double> ourMethodPrecisionList, List<Double> udPrecisionList) 
			throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter("out"+File.separator+"analyzeResult.txt"));
		for(int i = 0; i < ourMethodPrecisionList.size();i++){
			double ourFmeasure = ourMethodPrecisionList.get(i);
			double oppFmeasure = udPrecisionList.get(i);
			bw.write("ourMethod:"+ourFmeasure+"@"+"oppMethod:"+oppFmeasure);
			bw.newLine();
		}
		bw.close();
	}
	
	public static void main(String[] args) throws ParserConfigurationException,
		SAXException, IOException, ClassNotFoundException{
		
		CliffAnalyze cliffAnalyze = new CliffAnalyze();
		cliffAnalyze.process();
	}
}
