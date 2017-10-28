package cn.edu.nju.cs.itrace4.core.ir;

import cn.edu.nju.cs.itrace4.core.document.*;

import java.util.Collections;
import java.util.List;

/**
 * Created by niejia on 15/2/23.
 */

public class JSD implements IRModel {
    public SimilarityMatrix Compute(ArtifactsCollection source, ArtifactsCollection target) {
        return Compute(new TermDocumentMatrix(source), new TermDocumentMatrix(target));
    }


    private SimilarityMatrix Compute(TermDocumentMatrix source, TermDocumentMatrix target) {
        List<TermDocumentMatrix> matrices = TermDocumentMatrix.Equalize(source, target);
        SimilarityMatrix sims = new SimilarityMatrix();

        for (int i = 0; i < matrices.get(0).NumDocs(); i++) {
            LinksList list = new LinksList();
            for (int j = 0; j < matrices.get(1).NumDocs(); j++) {
                list.add(new SingleLink(matrices.get(0).getDocumentName(i), matrices.get(1).getDocumentName(j),
                        documentSimilarity(matrices.get(0).getDocument(i), matrices.get(1).getDocument(j))));
            }

            Collections.sort(list, Collections.reverseOrder());

            for (SingleLink link : list) {
                sims.addLink(link.getSourceArtifactId(), link.getTargetArtifactId(), link.getScore());
            }
        }
        return sims;
    }

    private double documentSimilarity(double[] document1, double[] document2) {
        double similarity;

//        1. Transform documents in two probability distributions
        double[] distribution1 = new double[document1.length];
        double[] distribution2 = new double[document2.length];

        double sum1 = 0;
        double sum2 = 0;

        for (int i = 0; i < document1.length; i++) {
            sum1 = sum1 + document1[i];
            sum2 = sum2 + document2[i];
        }

        for (int i = 0; i < document1.length; i++) {
            distribution1[i] = document1[i] / sum1;
            distribution2[i] = document2[i] / sum2;
        }

//        2. Compute Jensen-Shannon divergence between probability distribution
        double[] temp;
        temp = sumDocument(distribution1, distribution2);
        temp = mulDocument(0.5, temp);
        similarity = entropy(temp);
        similarity = similarity - (entropy(distribution1) + entropy(distribution2)) / 2;

//        3. Compute Jensen Shannon similarity
        similarity = 1 - similarity;
        return similarity;
    }



    private double entropy(double[] docDistrib) {
        int i;
        double entropia = 0;
        for (i = 0; i < docDistrib.length; i++) {
            if (docDistrib[i] > 0) {
                entropia = entropia - docDistrib[i] * (Math.log(docDistrib[i]) / Math.log(2.0));
//                entropia = entropia - docDistrib[i] * (Math.log(docDistrib[i]));
            }
        }
        return entropia;
    }

    private double[] sumDocument(double[] document1, double[] document2) {
        double[] sum = new double[document1.length];

        for (int i = 0; i < sum.length; i++) {
            sum[i] = document1[i] + document2[i];
        }
        return sum;
    }

    private double[] mulDocument(double pScalar, double[] pVector) {
        double[] mul = new double[pVector.length];
        for (int i = 0; i < mul.length; i++) {
            mul[i] = pScalar * pVector[i];
        }
        return mul;
    }

    @Override
    public TermDocumentMatrix getTermDocumentMatrixOfQueries() {
        return null;
    }

    @Override
    public TermDocumentMatrix getTermDocumentMatrixOfDocuments() {
        return null;
    }
}
