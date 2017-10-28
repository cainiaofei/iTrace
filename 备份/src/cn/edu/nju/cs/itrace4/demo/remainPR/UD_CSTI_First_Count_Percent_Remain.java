package cn.edu.nju.cs.itrace4.demo.remainPR;

import cn.edu.nju.cs.itrace4.core.algo.CSTI;
import cn.edu.nju.cs.itrace4.core.algo.None_CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.core.metrics.MetricComputation;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.core.metrics.cut.CutStrategy;
import cn.edu.nju.cs.itrace4.relation.CallDataRelationGraph;
import cn.edu.nju.cs.itrace4.relation.RelationGraph;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.relation.graph.CodeVertex;
import cn.edu.nju.cs.itrace4.visual.VisualCurve;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by niejia on 15/3/3.
 */
public class UD_CSTI_First_Count_Percent_Remain implements CSTI {

    public double bonus;
    private RelationGraph relationGraph;
    //现在是只需要用户指定前count个即可   现在这个还是全体排序进行的
    private double percent;
    public UD_CSTI_First_Count_Percent_Remain(RelationInfo relationInfo,double percent) {
        this.relationGraph = new CallDataRelationGraph(relationInfo);
        this.percent = percent;
    }

    @Override
    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset, 
    		SimilarityMatrix similarityMatrix) {

        SimilarityMatrix matrix_ud = new SimilarityMatrix();
        SimilarityMatrix oracle = textDataset.getRtm();

        bonus = getAdaptiveBonus(matrix);

        LinksList originLinks = new LinksList() ;
        LinksList allLinks = matrix.allLinks();
        Collections.sort(allLinks, Collections.reverseOrder());
        for (SingleLink link : allLinks) {
            originLinks.add(link);
        }

       // LinksList resultLinks = new LinksList();
        LinksList hasBeenJudgedLinks = new LinksList();
        int size = originLinks.size();
        int count = (int)(size * percent);
        while (count != 0) {////////////////////////
        	count--;
            SingleLink link = originLinks.remove(0);
            String source = link.getSourceArtifactId();
            String target = link.getTargetArtifactId();
            double score = link.getScore();
            hasBeenJudgedLinks.add(new SingleLink(source, target, score));
            if (oracle.isLinkAboveThreshold(source, target)) {
                List<CodeVertex> neighbours = ((CallDataRelationGraph) relationGraph).getNeighboursByCall(target);
                for (CodeVertex nb : neighbours) {
                    double originScore = originLinks.getScore(source, nb.getName());
                    if (originScore != -1) {
                        originLinks.updateLink(source, nb.getName(), originScore * (1 + bonus));
                    }
                }//for
            }
           
            
//            if (score != 0.0) {
//                resultLinks.add(new SingleLink(source, target, score+size));
//            } else {
//                resultLinks.add(new SingleLink(source, target, 0.0+size));
//            }

            Collections.sort(originLinks, Collections.reverseOrder());
            size--;
        }///////////////////外层while loop

        
        oracle = prone(hasBeenJudgedLinks,oracle);
        matrix = prone(hasBeenJudgedLinks,matrix);
        
        MetricComputation metricComputation_ir_only = new MetricComputation(matrix, oracle);
        Result result_ir_only = metricComputation_ir_only.compute(CutStrategy.CONSTANT_THRESHOLD);
        result_ir_only.setAlgorithmName((new None_CSTI()).getAlgorithmName());
        result_ir_only.setCorrectImprovedTargetsList((new None_CSTI()).getCorrectImprovedTargetsList());
        result_ir_only.setAlgorithmParameters((new None_CSTI()).getAlgorithmParameters());
        result_ir_only.setLog((new None_CSTI()).getDetails());
        
        for (SingleLink link : originLinks) {
            matrix_ud.addLink(link.getSourceArtifactId(), link.getTargetArtifactId(), link.getScore());
        }
        MetricComputation metricComputation_first_count_percent = new MetricComputation(matrix_ud, oracle);
        Result result_first_count_percent = metricComputation_first_count_percent.compute(CutStrategy.CONSTANT_THRESHOLD);
        result_first_count_percent.setAlgorithmName(this.getAlgorithmName());
        result_first_count_percent.setCorrectImprovedTargetsList(this.getCorrectImprovedTargetsList());
        result_first_count_percent.setAlgorithmParameters(this.getAlgorithmParameters());
        result_first_count_percent.setLog(this.getDetails());
        
        VisualCurve curve = new VisualCurve();
        curve.addLine(result_ir_only);
        curve.addLine(result_first_count_percent);
        curve.showChart();
//        System.out.println(" matrix_ud = " + matrix_ud );
        matrix_ud.allLinks();
        return matrix_ud;
    }

    private SimilarityMatrix prone(LinksList hasBeenJudgedLinks, SimilarityMatrix oracle) {
    	SimilarityMatrix newOracle = new SimilarityMatrix();
    	Iterator<SingleLink> ite = oracle.allLinks().iterator();
    	while(ite.hasNext()){
    		SingleLink curLink = ite.next();
    		if(!hasBeenJudgedLinksContains(hasBeenJudgedLinks,curLink)){
    			newOracle.addLink(curLink.getSourceArtifactId(), curLink.getTargetArtifactId(), curLink.getScore());
    		}
    	}
    	System.out.println(oracle.allLinks().size()-newOracle.allLinks().size()+" be proned");
    	return newOracle;
	}

	private boolean hasBeenJudgedLinksContains(LinksList links, SingleLink targetLink) {
		Iterator<SingleLink> ite = links.iterator();
		while(ite.hasNext()){
			SingleLink curLink = ite.next();
			if(curLink.getSourceArtifactId().equals(targetLink.getSourceArtifactId())&&
					curLink.getTargetArtifactId().equals(targetLink.getTargetArtifactId())){
				return true;
			}
		}
		return false;
	}

	@Override
    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset) {
        return null;
    }

    @Override
    public String getAlgorithmName() {
    	//返回这个方法名字
        return "UD_CSTI_First_"+percent*100+"_Percent";
    }

    @Override
    public List<Pair<String, String>> getAlgorithmParameters() {
        List parameters = new ArrayList();
        Pair<String, String> p = new Pair<>("Bonus", String.valueOf(bonus));
        parameters.add(p);
        return parameters;
    }

    @Override
    public String getDetails() {
        return "";
    }

    @Override
    public List<String> getCorrectImprovedTargetsList() {
        return null;
    }

    private double getAdaptiveBonus(SimilarityMatrix matrix) {
        List<Double> arrayScore = new ArrayList<>();
        for (String source : matrix.sourceArtifactsIds()) {
            Map<String, Double> links = matrix.getLinksForSourceId(source);

            List<Double> valuesList = new ArrayList<>();

            for (double value : links.values()) {
                valuesList.add(value);
            }

            Collections.sort(valuesList, Collections.reverseOrder());
            arrayScore.add((valuesList.get(0) - valuesList.get(valuesList.size() - 1)) / 2.0);
        }

        Collections.sort(arrayScore);
        double median;
        if (arrayScore.size() % 2 == 0)
            median = ((double) arrayScore.get(arrayScore.size() / 2) + (double) arrayScore.get(arrayScore.size() / 2 - 1)) / 2;
        else
            median = (double) arrayScore.get(arrayScore.size() / 2);

        return median;
    }
}
