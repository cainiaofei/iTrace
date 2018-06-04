package cn.edu.nju.cs.itrace4.core.algo;

import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.relation.*;
import cn.edu.nju.cs.itrace4.relation.graph.CodeVertex;
import javafx.util.Pair;

import java.util.*;

/**
 * Created by niejia on 15/3/16.
 */
public class PruningCall_Data_CSTI implements CSTI {

    private RelationGraph relationGraph;
    private RelationInfo relationInfo;
    private final UseEdge useEdge;

//    private PruningInfo pruningInfo;
    private SubGraphInfo subGraphInfo;

    private StringBuilder log;
    private List<String> correctImprovedTargetsList;

    public PruningCall_Data_CSTI(RelationInfo relationInfo, UseEdge useEdge) {
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
//        this.subGraphInfo = new SubGraphInfo(textDataset, relationInfo);
        this.subGraphInfo = new SubGraphInfo(textDataset, relationGraph,matrix);
        SimilarityMatrix matrix_neighbour = new SimilarityMatrix();

        LinksList qualityLinks = matrix.getQualityLinks();

        int correctNum = 0;
        int wrongNum = 0;

        int extendSumSize = 0;
        int firstPieceSize = 0;

        for (String source : matrix.sourceArtifactsIds()) {

            LinksList linksList = new LinksList();

            Map<String, Double> links = matrix.getLinksForSourceId(source);
            List<String> firstValidPiecesCode = new ArrayList<>();
            String firstValidTarget = subGraphInfo.getFirstValidPieceCodeForSource(source, firstValidPiecesCode, useEdge);

//            System.out.printf("%s %s %d.\n", source, firstValidTarget, firstValidPiecesCode.size());

//            System.out.println(source);

            List<String> extendedByDataRelation = extendFirstPieceByDataRelation(firstValidPiecesCode, firstValidTarget);

//            System.out.println(" firstValidPiecesCode = " + firstValidPiecesCode );
//            System.out.println(" firstValidPiecesCode Size = " + firstValidPiecesCode.size() );
//            System.out.println(" extendedByDataRelation = " + extendedByDataRelation );
//            System.out.println(" extendedByDataRelation Size = " + extendedByDataRelation.size() );

            extendSumSize += extendedByDataRelation.size();
            firstPieceSize += firstValidPiecesCode.size();
            for (String target : links.keySet()) {
                double score = matrix.getScoreForLink(source, target);
                SingleLink originLink = new SingleLink(source, target, score);

                if (firstValidPiecesCode.contains(target)) {
                    double improvedScore = matrix.getScoreForLink(source, firstValidTarget);
//                    double improvedScore = minQualityScore+score;
//                    double improvedScore = minQualityScore;
//                    double improvedScore = score * 1.5;

                    log.append("Improve link = " + source + " " + target + " " + score + "\n");
                    linksList.add(new SingleLink(source, target, improvedScore));
                    log.append("After Improved = " + source + " " + target + " " + improvedScore + "\n");

                    if (rtm.isLinkAboveThreshold(source, target)) {
                        correctNum++;
                        correctImprovedTargetsList.add(target + "@" + source);
//                        System.out.println(("Improve link = " + source + " " + target + " " + score + "\n"));
                        log.append("Correct\n");
                    } else {
                        wrongNum++;
                        log.append("Wrong\n");
                    }
                } else if (extendedByDataRelation.contains(target)) {
                    double improvedScore = matrix.getScoreForLink(source, firstValidTarget);

                    log.append("Improve link = " + source + " " + target + " " + score + "\n");
//                    System.out.println(("Improve link = " + source + " " + target + " " + score + "\n"));

                    linksList.add(new SingleLink(source, target, improvedScore));
//                    log.append("After Improved = " + source + " " + target + " " + improvedScore + "\n");
//                    System.out.println(("After Improved = " + source + " " + target + " " + improvedScore + "\n"));
//                    System.out.println(source + " " + target);
                    if (rtm.isLinkAboveThreshold(source, target)) {
                        correctNum++;
                        correctImprovedTargetsList.add(target + "@" + source);
//                        System.out.println(("Improve link = " + source + " " + target + " " + score + "\n"));
                        log.append("Correct\n");
//                        System.out.println("Correct\n");
                    } else {
                        wrongNum++;
                        log.append("Wrong\n");
//                        System.out.println("Wrong\n");
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


        StringBuilder index = new StringBuilder();
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

//        System.out.println(" extendSumSize = " + extendSumSize );
//        System.out.println(" firstPieceSize = " + firstPieceSize );
        return matrix_neighbour;
    }

    @Override
    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset) {
        return null;
    }

    private List<String> extendFirstPieceByDataRelation(List<String> firstPiecesCode, String highestTarget) {
        CallDataRelationGraph cdGraph = (CallDataRelationGraph) relationGraph;
        Map<CodeVertex, Integer> neighbourVertexNum = new HashMap<>();

        firstPiecesCode.add(highestTarget);
        for (String vertexName : firstPiecesCode) {
            List<CodeVertex> codeVertexList = cdGraph.getNeighboursByData(vertexName);
//            System.out.println(" vertexName = " + vertexName + " " + codeVertexList.size());
            for (CodeVertex vertex : codeVertexList) {
                if (!firstPiecesCode.contains(vertex.getName())) {
                    if (!neighbourVertexNum.containsKey(vertex)) {
                        neighbourVertexNum.put(vertex, 1);
                    } else {
                        neighbourVertexNum.put(vertex, neighbourVertexNum.get(vertex) + 1);
                    }
                }
            }
        }

        List<String> extendVertexByDataRelation = new ArrayList<>();
        for (CodeVertex vertex : neighbourVertexNum.keySet()) {
            if (neighbourVertexNum.get(vertex) >= 1) {
                extendVertexByDataRelation.add(vertex.getName());
            }
        }

        firstPiecesCode.remove(highestTarget);
//        System.out.println(extendVertexByDataRelation.size());
        return extendVertexByDataRelation;
    }

    @Override
    public String getAlgorithmName() {
        return "Call_Data";
    }

    @Override
    public List<Pair<String, String>> getAlgorithmParameters() {
        List parameters = new ArrayList();
        Pair<String, String> p1 = new Pair<>("CallEdgeScoreThreshold", String.valueOf(relationInfo.getCallEdgeScoreThreshold()));
        Pair<String, String> p2 = new Pair<>("DataEdgeScoreThreshold", String.valueOf(relationInfo.getDataEdgeScoreThreshold()));
        parameters.add(p1);
        parameters.add(p2);
        return parameters;
    }

    @Override
    public String getDetails() {
        return "";
    }

    @Override
    public List<String> getCorrectImprovedTargetsList() {
        return correctImprovedTargetsList;
    }
}
