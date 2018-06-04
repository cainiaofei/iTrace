package cn.edu.nju.cs.itrace4.core.algo.prealgo;

import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.tool.PrintPageRankResult;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by niejia on 15/3/17.
 */
public class PageRank_CSTI implements CSTI{

    private RelationInfo relationInfo;

    public PageRank_CSTI(RelationInfo relationInfo) {
        this.relationInfo = relationInfo;
    }

    @Override
    public SimilarityMatrix improve(SimilarityMatrix matrix, 
    		TextDataset textDataset, SimilarityMatrix similarityMatrix) {
//        SimilarityMatrix oracle = textDataset.getRtm();
//
        SimilarityMatrix matrix_pageRank = new SimilarityMatrix();
//
        Map<String, Number> pageRank = relationInfo.getPageRank();
        
        PrintPageRankResult printResult = new PrintPageRankResult();
        //printResult.doTask(pageRank);
        
        for (String source : matrix.sourceArtifactsIds()) {
            LinksList linksList = new LinksList();
            Map<String, Double> links = matrix.getLinksForSourceId(source);

            for (String target : links.keySet()) {
                double score = matrix.getScoreForLink(source, target);
                if (pageRank.get(target) == null) {////////这种情况从来不会出现     不然的话 代码会出问题
                    System.out.println(source + " " + target + " " + score);
                }
                double weight = (double) pageRank.get(target);
                linksList.add(new SingleLink(source, target,weight * score));
            }
            Collections.sort(linksList, Collections.reverseOrder());

            for (SingleLink link : linksList) {
                matrix_pageRank.addLink(link.getSourceArtifactId(), link.getTargetArtifactId(), link.getScore());
            }
        }
        return matrix_pageRank;
    }

    @Override
    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset) {
        return null;
    }

    @Override
    public String getAlgorithmName() {
        return "PageRank";
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
