package cn.edu.nju.cs.itrace4.core.algo.legacy;

import cn.edu.nju.cs.itrace4.core.algo.CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.ir.DocVectorLookUp;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by niejia on 15/5/20.
 */
public class Rocchio_CSTI implements CSTI {

    private DocVectorLookUp docVectorLookUp;

    private List<double[]> relevantDocs;
    private List<double[]> irrelevantDocs;

    private static final double alpa = 1.0;
    private static final double beta = 0.75;
    private static final double gama = 0.25;


    /**
     * Rocchio's Algorithm
     */

    public Rocchio_CSTI() {
        relevantDocs = new ArrayList<>();
        irrelevantDocs = new ArrayList<>();
    }

    @Override
    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset, SimilarityMatrix similarityMatrix) {


        return null;
    }

    @Override
    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset) {
        return null;
    }

    @Override
    public String getAlgorithmName() {
        return "Rocchio";
    }

    @Override
    public List<Pair<String, String>> getAlgorithmParameters() {
        return null;
    }

    @Override
    public String getDetails() {
        return null;
    }

    @Override
    public List<String> getCorrectImprovedTargetsList() {
        return null;
    }

    public double[] rocchio(double[] query) {
        double[] p1 = new double[query.length];
        for (int i = 0; i < query.length; i++) {
            p1[i] = alpa * query[i];
        }

        double[] meanRelevantDocs = mean(relevantDocs, query.length);

        double[] p2 = new double[query.length];

        for (int i = 0; i < meanRelevantDocs.length; i++) {
            p2[i] = beta * meanRelevantDocs[i];
        }


        double[] p3 = new double[query.length];
        double[] meanIrRelevantDocs = mean(irrelevantDocs, query.length);
        for (int i = 0; i < meanIrRelevantDocs.length; i++) {
            p3[i] = gama * meanIrRelevantDocs[i];
        }

        double[] result = new double[query.length];
        for (int i = 0; i < result.length; i++) {
            double tmp = p1[i] + p2[i] - p3[i];
            if (tmp > 0) {
                result[i] = tmp;
            } else {
                result[i] = 0.0;
            }
        }

        return result;
    }

    public double[] mean(List<double[]> documentSet, int len) {
        if (documentSet == null || documentSet.size() == 0) return new double[len];
        double[] m = new double[len];

        for (double[] doc : documentSet) {
            for (int i = 0; i < doc.length; i++) {
                m[i] += doc[i];
            }
        }

        for (int i = 0; i < m.length; i++) {
            m[i] = m[i] / documentSet.size();
        }

        return m;
    }

    private double ComputeSimilarities(double[] query, double[] documents) {
        double product = 0.0;
        double asquared = 0.0;
        double bsquared = 0.0;
        for (int k = 0; k < query.length; k++) {
            double a = query[k];
            double b = documents[k];
            product += (a * b);
            asquared += Math.pow(a, 2);
            bsquared += Math.pow(b, 2);
        }
        double cross = Math.sqrt(asquared) * Math.sqrt(bsquared);
        if (cross == 0.0) {
            return 0.0;
        } else {
            return product / cross;
        }
    }
}
