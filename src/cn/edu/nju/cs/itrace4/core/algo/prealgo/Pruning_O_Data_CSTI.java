package cn.edu.nju.cs.itrace4.core.algo.prealgo;

import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by niejia on 15/4/19.
 */
public class Pruning_O_Data_CSTI implements CSTI {

    private RelationInfo unbrokenInfo;
    private RelationInfo brokenInfo;

    public Pruning_O_Data_CSTI(RelationInfo unbrokenInfo,RelationInfo brokenInfo) {
        this.unbrokenInfo = unbrokenInfo;
        this.brokenInfo = brokenInfo;
    }


    @Override

    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset, SimilarityMatrix similarityMatrix) {
        SimilarityMatrix sm = (new O_CSTI(unbrokenInfo)).improve(matrix, textDataset, similarityMatrix);


        return (new PruningData_CSTI(brokenInfo, UseEdge.Data)).improve(sm, textDataset, similarityMatrix);
    }

    @Override
    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset) {
        return null;
    }

    @Override
    public String getAlgorithmName() {
        return "O_Data";
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
