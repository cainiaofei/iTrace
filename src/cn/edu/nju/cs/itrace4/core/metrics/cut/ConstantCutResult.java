package cn.edu.nju.cs.itrace4.core.metrics.cut;

import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.core.document.StringHashSet;
import cn.edu.nju.cs.itrace4.core.metrics.Result;

/**
 * Created by niejia on 14-8-29.
 */
public class ConstantCutResult extends Result {


    public ConstantCutResult(SimilarityMatrix matrix, SimilarityMatrix oracle) {
        super(matrix, oracle);
        resultName = "constant cut";
        cutParameter = originMatrix.getCutN();
    }

    @Override
    public void showMetrics() {
        metricsAtCut(matrix.getCutN());
    }

    public void setCutN(double val) {
        setCutParameter(val);
    }

    @Override
    public void setCutParameter(double val) {

        SimilarityMatrix sims = new SimilarityMatrix();
        originMatrix.setCutN((int) val);
        cutParameter = (int) val;
        for (SingleLink link : originMatrix.allLinks()) {
            if (originMatrix.isLinkInCutN(link.getSourceArtifactId(), link.getTargetArtifactId())) {
                sims.addLink(link.getSourceArtifactId(), link.getTargetArtifactId(), link.getScore());
            } else {
                sims.addLink(link.getSourceArtifactId(), link.getTargetArtifactId(), 0.0);
            }
        }
        matrix = sims;
    }

    public void metricsAtCut(int n) {

        LinksList actualLinks = oracle.allLinks();

        StringHashSet sourceIDs = matrix.sourceArtifactsIds();
        int actualNum = oracle.count();
        for (int i = 1; i <= n; i++) {

            int retNum = 0;
            int relNum = 0;

            for (String id : sourceIDs) {
                LinksList links = matrix.getLinksAboveThresholdForSourceArtifact(id);
                if (links.size() == 0) {
                    System.out.println(id + " no match");
                }

                if (links.size() != 0) {
                    String ret_rel = cut(i, links, actualLinks);
                    retNum += Integer.parseInt(ret_rel.split(",")[0]);
                    relNum += Integer.parseInt(ret_rel.split(",")[1]);
                }
            }

            System.out.println("cut " + i);
            System.out.println("Retrieved " + retNum);
            System.out.println("Relevant " + relNum);
            System.out.println("Precision " + 1.0 * relNum / retNum);
            System.out.println("Recall " + 1.0 * relNum / actualNum);
        }
    }

    private String cut(int firstN, LinksList links, LinksList actualLinks) {
        int rel = 0;
        int ret = 0;
        for (int i = 0; i < firstN; i++) {
            if (links.size() > i) {
                ret++;
                links.get(i).setScore(1.0);

                if (actualLinks.contains(links.get(i))) {
                    rel++;
                }
            }

        }
        return String.valueOf(ret) + "," + String.valueOf(rel);
    }
}
