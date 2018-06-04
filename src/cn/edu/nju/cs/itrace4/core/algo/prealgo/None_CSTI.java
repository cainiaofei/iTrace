package cn.edu.nju.cs.itrace4.core.algo.prealgo;

import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by niejia on 15/3/3.
 */
public class None_CSTI implements CSTI {
    @Override
    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset, SimilarityMatrix similarityMatrix) {
    	/*
    	 * debug
    	 */
    	LinksList allLinks = matrix.allLinks();
    	return matrix;
    }

    @Override
    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset) {
        return null;
    }

    @Override
    public String getAlgorithmName() {
        return "IR_Only";
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
