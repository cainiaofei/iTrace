package cn.edu.nju.cs.itrace4.demo.specifyMixture;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.CSTI;
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
import java.util.List;
import java.util.Map;

/**
 * Created by niejia on 15/3/3.
 */
public class UD_CSTI_First_Count implements CSTI {

    public double bonus;
    private RelationGraph relationGraph;
    //现在是只需要用户指定前count个即可   现在这个还是全体排序进行的
    private int count;
    public UD_CSTI_First_Count(RelationInfo relationInfo,int count) {
        this.relationGraph = new CallDataRelationGraph(relationInfo);
        this.count = count;
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
        int size = originLinks.size();
        while (count>0) {////////////////////////
        	count--;
            SingleLink link = originLinks.remove(0);
            String source = link.getSourceArtifactId();
            String target = link.getTargetArtifactId();
            double score = link.getScore();
            if (oracle.isLinkAboveThreshold(source, target)) {
                List<CodeVertex> neighbours = ((CallDataRelationGraph) relationGraph).getNeighboursByCall(target);
                for (CodeVertex nb : neighbours) {
                    double originScore = originLinks.getScore(source, nb.getName());
                    if (originScore != -1) {
                        originLinks.updateLink(source, nb.getName(), originScore * (1 + bonus));
                    }
                }
            }

            if (score != 0.0) {
                resultLinks.add(new SingleLink(source, target, score+size));
            } else {
                resultLinks.add(new SingleLink(source, target, 0.0+size));
            }

            Collections.sort(originLinks, Collections.reverseOrder());
            size--;
        }///////////////////外层while loop

        //origin里面只拿出来了count个  存在代码依赖的值可能更新了 全放到 resultLinks中
        while(!originLinks.isEmpty()){
        	resultLinks.add(originLinks.remove(0));
        }
        
        
        for (SingleLink link : resultLinks) {
            matrix_ud.addLink(link.getSourceArtifactId(), link.getTargetArtifactId(), link.getScore());
        }

//        System.out.println(" matrix_ud = " + matrix_ud );
        //这个语句应该没太大意义吧
        matrix_ud.allLinks();
        //分类存好了  底层是个map
        return matrix_ud;
    }

    @Override
    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset) {
        return null;
    }

    @Override
    public String getAlgorithmName() {
    	//返回这个方法名字
        return "UD_CSTI_First_Five";
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
