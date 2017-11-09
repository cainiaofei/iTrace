package cn.edu.nju.cs.itrace4.demo.specifyByGroup;

import cn.edu.nju.cs.itrace4.core.algo.CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.relation.CallDataRelationGraph;
import cn.edu.nju.cs.itrace4.relation.RelationGraph;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.relation.graph.CodeVertex;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 尽量让工程师少参与  每个需求对应的实体 指定一个.
 */
public class UD_CSTI_Judge_Group implements CSTI {

    public double bonus;
    private RelationGraph relationGraph;

    public UD_CSTI_Judge_Group(RelationInfo relationInfo) {
        this.relationGraph = new CallDataRelationGraph(relationInfo);
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

        LinksList resultLinks = new LinksList();

        //每个需求对应的链接中指定一个
        Set<String> set = new HashSet<String>();
        int i = originLinks.size();
        while (originLinks.size() != 0) {////////////////////////
            SingleLink link = originLinks.get(0);
            originLinks.remove(0);
            String source = link.getSourceArtifactId();
            String target = link.getTargetArtifactId();
            double score = link.getScore();

            //如果这个source对应的已经判断过了  就不需要在判断了
            if(set.contains(source)){
            	 List<CodeVertex> neighbours = ((CallDataRelationGraph) relationGraph).getNeighboursByCall(target);
                 for (CodeVertex nb : neighbours) {
                     double originScore = originLinks.getScore(source, nb.getName());
                     if (originScore != -1) {
                         originLinks.updateLink(source, nb.getName(), originScore * (1 + bonus));
                     }
                 }
                 resultLinks.add(new SingleLink(source, target, score+i));
            }
            else{
            	 set.add(source);
            	 if(oracle.isLinkAboveThreshold(source, target)) {
            		 List<CodeVertex> neighbours = ((CallDataRelationGraph) relationGraph).getNeighboursByCall(target);
                     for (CodeVertex nb : neighbours) {
                         double originScore = originLinks.getScore(source, nb.getName());
                         if (originScore != -1) {
                             originLinks.updateLink(source, nb.getName(), originScore * (1 + bonus));
                         }
                     }
                     resultLinks.add(new SingleLink(source, target, score+i));
                 }
            	 else{
            		 resultLinks.add(new SingleLink(source, target, -i*1.0));
            	 }
            }
            
           /* if (score != 0.0) {
                resultLinks.add(new SingleLink(source, target, score+i));
            } else {
                resultLinks.add(new SingleLink(source, target, 0.0+i));
            }*/

            Collections.sort(originLinks, Collections.reverseOrder());
            i--;
        }///////////////////外层while loop

//        System.out.println(" resultLinks = " + resultLinks );

        for (SingleLink link : resultLinks) {
            matrix_ud.addLink(link.getSourceArtifactId(), link.getTargetArtifactId(), link.getScore());
        }

//        System.out.println(" matrix_ud = " + matrix_ud );
        matrix_ud.allLinks();
        return matrix_ud;
    }

    @Override
    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset) {
        return null;
    }

    @Override
    public String getAlgorithmName() {
        return "UD_CSTI_JUDGE_GROUP";
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
