package cn.edu.nju.cs.itrace4.core.metrics;

import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;

import java.util.Collections;

/**
 * Created by niejia on 15/4/29.
 */
public class PrecisionPosition {


    private SimilarityMatrix sm;
    private SimilarityMatrix rtm;

    private SimilarityMatrix precisionPosition;

    public PrecisionPosition(SimilarityMatrix sm, SimilarityMatrix rtm) {
        this.sm = sm;
        this.rtm = rtm;

        precisionPosition = new SimilarityMatrix();

        LinksList allLinks = sm.allLinks();
        Collections.sort(allLinks, Collections.reverseOrder());

        int num = 0;
        int correct = 0;
        for (SingleLink link : allLinks) {

            System.out.println(" link = " + link );

            num++;
            if (rtm.isLinkAboveThreshold(link.getSourceArtifactId(), link.getTargetArtifactId())) {
                correct++;
                precisionPosition.addLink(link.getSourceArtifactId(), link.getTargetArtifactId(), 1.0 * correct / num);
            }
        }

    }

    public SimilarityMatrix getPrecisionPosition() {
        return precisionPosition;
    }
}
