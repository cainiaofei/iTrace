package cn.edu.nju.cs.itrace4.core.metrics;

import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by niejia on 15/4/30.
 */
public class PrecisionCompare {

    private PrecisionPosition pp1;
    private PrecisionPosition pp2;

    private List<Pair<Double, Double>> precisionPair;
    public PrecisionCompare(PrecisionPosition pp1, PrecisionPosition pp2) {
        this.pp1 = pp1;
        this.pp2 = pp2;



        precisionPair = new ArrayList<>();
        computePosition(pp1.getPrecisionPosition(), pp2.getPrecisionPosition());
    }

    private void computePosition(SimilarityMatrix sm1, SimilarityMatrix sm2) {

        for (SingleLink link : sm1.allLinks()) {

            Double score1 = link.getScore();
            Double score2 = sm2.getScoreForLink(link.getSourceArtifactId(), link.getTargetArtifactId());

            if (score1 > score2) {
                System.out.println(link.getSourceArtifactId() + " " + link.getTargetArtifactId()+" "+score1+ " "+score2);
            }

            Pair<Double, Double> pair = new Pair<>(score1, score2);
            System.out.println(score1 + " " + score2);
                    precisionPair.add(pair);
        }
    }

    public List<Pair<Double, Double>> getPrecisionPair() {
        return precisionPair;
    }
}
