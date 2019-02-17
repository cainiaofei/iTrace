package cn.edu.nju.cs.itrace4.core.algo.legacy;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.*;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by niejia on 15/4/21.
 */
public class PruningData_PruningO implements CSTI {

    private RelationInfo infoForRest;
    private RelationInfo brokenInfo;

    public PruningData_PruningO(RelationInfo infoForRest,RelationInfo brokenInfo) {
        this.infoForRest = infoForRest;
        this.brokenInfo = brokenInfo;
    }

    @Override
    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset, SimilarityMatrix similarityMatrix) {
        SimilarityMatrix sm = (new PruningData_CSTI(brokenInfo, UseEdge.Data)).improve(matrix, textDataset, similarityMatrix);
        return (new O_Data_CSTI(infoForRest)).improve(sm, textDataset, similarityMatrix);
    }

    @Override
    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset) {
        return null;
    }

    @Override
    public String getAlgorithmName() {
        return "PruningData_PruningO";
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
        return "";
    }

    @Override
    public List<String> getCorrectImprovedTargetsList() {
        return null;
    }


}
