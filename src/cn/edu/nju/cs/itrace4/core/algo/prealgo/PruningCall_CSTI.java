package cn.edu.nju.cs.itrace4.core.algo;

import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.relation.*;
import javafx.util.Pair;

import java.util.*;

/**
 * Created by niejia on 15/3/15.
 */
public class PruningCall_CSTI implements CSTI {

    private RelationGraph relationGraph;
    private RelationInfo relationInfo;
    private final UseEdge useEdge;

//    private PruningInfo pruningInfo;
    private SubGraphInfo subGraphInfo;

    private StringBuilder log;
    private StringBuilder index;
    private List<String> correctImprovedTargetsList;

    public PruningCall_CSTI(RelationInfo relationInfo, UseEdge useEdge) {
        this.relationInfo = relationInfo;
        this.relationGraph = new CallDataRelationGraph(relationInfo);
        this.useEdge = useEdge;
        log = new StringBuilder();
        correctImprovedTargetsList = new ArrayList<>();
    }

    @Override
    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset, SimilarityMatrix similarityMatrix) {
        SimilarityMatrix rtm = textDataset.getRtm();
//        this.pruningInfo = new PruningInfo(textDataset, relationInfo);
        this.subGraphInfo = new SubGraphInfo(textDataset, relationGraph);
        SimilarityMatrix matrix_neighbour = new SimilarityMatrix();

        LinksList qualityLinks = matrix.getQualityLinks();

        int correctNum = 0;
        int wrongNum = 0;

        int firstPieceSize = 0;


        for (String source : matrix.sourceArtifactsIds()) {

            LinksList linksList = new LinksList();

            Map<String, Double> links = matrix.getLinksForSourceId(source);
            List<String> firstValidPiecesCode = new ArrayList<>();
            String firstValidTarget = subGraphInfo.getFirstValidPieceCodeForSource(source, firstValidPiecesCode, useEdge);

//            System.out.printf("%s %s %d.\n", source, firstValidTarget, firstValidPiecesCode.size());

//            System.out.println(" firstValidPiecesCode = " + firstValidPiecesCode );
//            System.out.println(" firstValidPiecesCode Size = " + firstValidPiecesCode.size() );
            firstPieceSize += firstValidPiecesCode.size();

            for (String target : links.keySet()) {
                double score = matrix.getScoreForLink(source, target);
                SingleLink originLink = new SingleLink(source, target, score);

                if (firstValidPiecesCode.contains(target)) {
                    double improvedScore = matrix.getScoreForLink(source, firstValidTarget) ;
//                    double improvedScore = minQualityScore+score;
//                    double improvedScore = minQualityScore;
//                    double improvedScore = score * 1.5;

                    log.append("Improve link = " + source + " " + target + " " + score + "\n");
                    linksList.add(new SingleLink(source, target, improvedScore));
                    log.append("After Improved = " + source + " " + target + " " + improvedScore + "\n");

                    if (rtm.isLinkAboveThreshold(source, target)) {
                        correctNum++;
                        correctImprovedTargetsList.add(target + "@" + source);
                        log.append("Correct\n");
                    } else {
                        wrongNum++;
                        log.append("Wrong\n");
                    }
                } else {
                    linksList.add(originLink);
                }
            }
            Collections.sort(linksList, Collections.reverseOrder());
            for (SingleLink link : linksList) {
                matrix_neighbour.addLink(link.getSourceArtifactId(), link.getTargetArtifactId(), link.getScore());
            }
        }

        index = new StringBuilder();
        double precision = correctNum * 1.0 / (correctNum + wrongNum);
        String indexPrecision = "Improved Number " + (correctNum + wrongNum) + "\nCorrect: " + correctNum + " Wrong: " + wrongNum + "\nImproved Precision = " + precision + "\n";
        index.append(indexPrecision);
        double recall = correctNum * 1.0 / rtm.getLinksAboveThreshold().size();
        index.append("Improved Recall = " + recall + "\n");
        double fMeasure = 2.0 * precision * recall / (precision + recall);
        index.append("Improved F Measure = " + fMeasure + "\n");

        log.append(index.toString());
        System.out.println(index.toString());
//        System.out.println(log.toString());
//        System.out.println(" firstPieceSize = " + firstPieceSize );

//        System.out.println(matrix_neighbour);
        return matrix_neighbour;
    }

    @Override
    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset) {
        return null;
    }

    @Override
    public String getAlgorithmName() {
        return "PruningCall";
    }

    @Override
    public List<Pair<String, String>> getAlgorithmParameters() {
        List parameters = new ArrayList();
        Pair<String, String> p = new Pair<>("CallEdgeScoreThreshold", String.valueOf(relationInfo.getCallEdgeScoreThreshold()));
        parameters.add(p);
        return parameters;
    }

    @Override
    public String getDetails() {
        return index.toString();
    }

    @Override
    public List<String> getCorrectImprovedTargetsList() {
        return correctImprovedTargetsList;
    }
}
