package cn.edu.nju.cs.itrace4.demo.explore;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.demo.specifyMixture.UD_CSTI_First_Count_Percent;
import cn.edu.nju.cs.itrace4.demo.specifyMixture.UD_CSTI_First_Five;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.tool.PrintPageRankResult;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by niejia on 15/3/17.
 */
public class PageRankFeedBack implements CSTI{

    private RelationInfo relationInfo;
    private double count;
    
    public PageRankFeedBack(RelationInfo relationInfo,double count) {
        this.relationInfo = relationInfo;
        this.count = count;
    }

    @Override
    public SimilarityMatrix improve(SimilarityMatrix matrix, 
    		TextDataset textDataset, SimilarityMatrix similarityMatrix) {
        SimilarityMatrix matrix_pageRank = new SimilarityMatrix();
        Map<String, Number> pageRank = relationInfo.getPageRank();
        
        PrintPageRankResult printResult = new PrintPageRankResult();
        //printResult.doTask(pageRank);
        
        process(pageRank);
        //printResult.doTask(pageRank);
        for (String source : matrix.sourceArtifactsIds()) {///////////////////
            LinksList linksList = new LinksList();
            Map<String, Double> links = matrix.getLinksForSourceId(source);
            for (String target : links.keySet()) {
                double score = matrix.getScoreForLink(source, target);
                if (pageRank.get(target) == null) {////////这种情况从来不会出现     不然的话 代码会出问题
                    System.out.println(source + " " + target + " " + score);
                }
                double weight = pageRank.get(target).doubleValue();
                linksList.add(new SingleLink(source, target,weight * score));
            }
            Collections.sort(linksList, Collections.reverseOrder());

            for (SingleLink link : linksList) {
                matrix_pageRank.addLink(link.getSourceArtifactId(), link.getTargetArtifactId(), link.getScore());
            }
        }
        //invoke feedback
        UD_CSTI_First_Count_Percent feedback = new UD_CSTI_First_Count_Percent(relationInfo,count);
       // return matrix_pageRank;
        return feedback.improve(matrix_pageRank, textDataset,matrix_pageRank);
    }

    private void process(Map<String, Number> pageRank) {
    	double mean = mean(pageRank);
        //3aomige
        double var = var(pageRank,mean);
        Set<String> temporalMaxDeleted = new HashSet<String>(); 
        Set<String> temporalMinDeleted = new HashSet<String>(); 
        Iterator<String> ite = pageRank.keySet().iterator();
        System.out.println("刚开始的大小："+pageRank.size());
        while(ite.hasNext()){
        	String codeName = ite.next();
        	double curWeight = (double)pageRank.get(codeName);
        	double diff = Math.abs(curWeight-mean);
        	if(diff>(3*var)&&curWeight>mean){
        		temporalMaxDeleted.add(codeName);
        		ite.remove();
        	}
        	if(diff>(3*var)&&curWeight<mean){
        		temporalMinDeleted.add(codeName);
        		ite.remove();
        	}
        }
        System.out.println("过滤之后的大小："+pageRank.size());
        //min_max_normalize(pageRank);
        // add the deleted and assign value 1
        ite = temporalMaxDeleted.iterator();
        while(ite.hasNext()){
        	pageRank.put(ite.next(),  mean+3*var);
        }
        ite = temporalMinDeleted.iterator();
        while(ite.hasNext()){
        	pageRank.put(ite.next(),  mean-3*var);
        }
	}

	private void min_max_normalize(Map<String, Number> pageRank) {
		double max = max(pageRank);
		double min = min(pageRank);
		Iterator<String> ite = pageRank.keySet().iterator();
		while(ite.hasNext()){
			String codeName = ite.next();
			double curValue = (double)pageRank.get(codeName);
			pageRank.put(codeName, (curValue-min)/(max-min));
		}
	}

	private double min(Map<String, Number> pageRank) {
		double min = 1;
		Iterator<String> ite = pageRank.keySet().iterator();
		while(ite.hasNext()){
			min = Math.min(min, pageRank.get(ite.next()).doubleValue());
		}
		return min;
	}

	private double max(Map<String, Number> pageRank) {
		double max = 0;
		Iterator<String> ite = pageRank.keySet().iterator();
		while(ite.hasNext()){
			max = Math.max(max, pageRank.get(ite.next()).doubleValue());
		}
		return max;
	}

	private double var(Map<String, Number> pageRank, double meanWeight) {
		Iterator<String> ite = pageRank.keySet().iterator();
		double sum = 0;
		while(ite.hasNext()){
			String codeName = ite.next();
			double weight = pageRank.get(codeName).doubleValue();
			sum += Math.pow(weight-meanWeight, 2);
		}
		return Math.sqrt(sum/pageRank.size());
	}

	private double mean(Map<String, Number> pageRank) {
		double sum = 0;
		Iterator<String> ite = pageRank.keySet().iterator();
		while(ite.hasNext()){
			String codeName = ite.next();
			sum += (double)pageRank.get(codeName);
		}
		return sum/pageRank.size();//
	}

	@Override
    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset) {
        return null;
    }

    @Override
    public String getAlgorithmName() {
        return "PageRankFeedBack"+count;
    }

    @Override
    public List<Pair<String, String>> getAlgorithmParameters() {
        List parameters = new ArrayList();
        Pair<String, String> p = new Pair<>("None", "");
        parameters.add(p);
        return parameters;
    }

    @Override
    public String getDetails() {
        return null;
    }

    @Override
    public List<String> getCorrectImprovedTargetsList() {
        return null;
    }
}
