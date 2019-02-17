package cn.edu.nju.cs.itrace4.util;

/**
 * Created by niejia on 15/11/5.
 */

import java.util.Arrays;
import java.util.List;

public class NDCG {

    public static void main(String[] args) {
        List<Double> urls = Arrays.asList(new Double[]{3.0, 1.0, 0.0});
        List<Double> oracleUrls = Arrays.asList(new Double[]{3.0, 1.0, 0.0});

        System.out.println(NDCG.getNDCG(urls, oracleUrls, 3));
    }

    public static double getNDCG(List<Double> urls, List<Double> oracleUrls, int r) {
        // get DCG of urls
        double urlDCG = getDCG(urls, r);
        // get DCG of perfect ranking
        double perfectDCG = getDCG(oracleUrls, r);
        // normalize by dividing
        double normalized = urlDCG / perfectDCG;
        return normalized;
    }

    public static double getDCG(List<Double> urls, int p) {
        double score = 0;

        for (int i = 0; i < p; i++) {
            double relevance = urls.get(i);
            int ranking = i + 1;

            double numerator = 1.0 * (Math.pow(2, relevance) - 1);
            double denominator = logBase2(ranking + 1);
            score += (numerator / denominator);
        }

        return score;
    }

    public static double logBase2(double value) {
        return Math.log(value) / Math.log(2);
    }
}
