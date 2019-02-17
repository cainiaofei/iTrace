package cn.edu.nju.cs.itrace4.core.algo.prealgo;

import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.*;
import cn.edu.nju.cs.itrace4.relation.*;
import cn.edu.nju.cs.itrace4.relation.graph.CodeVertex;
import javafx.util.Pair;

import java.util.*;

/**
 * Created by niejia on 15/3/3.
 */


/*
最高值点无邻居时，算法即停止
 */
public class PruningData_CSTI implements CSTI {

    private RelationGraph relationGraph;
    private RelationInfo relationInfo;
    private final UseEdge useEdge;

//    private PruningInfo pruningInfo;
    private SubGraphInfo subGraphInfo;

    private StringBuilder log;
    private StringBuilder index;

    private List<String> correctImprovedTargetsList;


    public PruningData_CSTI(RelationInfo relationInfo, UseEdge useEdge) {
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
        this.subGraphInfo = new SubGraphInfo(textDataset, relationGraph);
        // see IDF details
//        Map<String, Double> dataTypeIDFMap = relationInfo.computeDataTypeIDF();
//        for (String source : textDataset.getSourceCollection().keySet()) {
//            StringBuilder sb = new StringBuilder();
//            sb.append(source + "\n");
//            LinksList rtmlinks = rtm.getLinksAboveThresholdForSourceArtifact(source);
//            List<String> targetsList = new ArrayList<>();
//            for (SingleLink link : rtmlinks) {
//                String targetId = link.getTargetArtifactId();
//                if (!targetsList.contains(targetId)) {
//                    targetsList.add(targetId);
//                }
//            }
//
//            List<CodeEdge> targetEdges = new ArrayList<>();
//            for (String target : targetsList) {
//                if (relationGraph.getEdges(target) != null) {
//                    Collection<CodeEdge> codeEdges = relationGraph.getEdges(target);
//                    for (CodeEdge edge : codeEdges) {
//                        String v1 = edge.getSource().getName();
//                        String v2 = edge.getTarget().getName();
//
//                        if (targetsList.contains(v1) && targetsList.contains(v2)) {
//                            sb.append(v1 + " " + v2);
//                            sb.append("\n");
//                            RelationPair rp = new RelationPair(relationInfo.getVertexIdByName(v1), relationInfo.getVertexIdByName(v2));
//                            int i = 1;
//                            for (DataRelation dataRelation : relationInfo.getDataRelationListForRelationPair(rp)) {
//                                if (!dataRelation.getType().equals("DAOFactory")) {
//                                    sb.append("type" + i + " " + dataRelation.getType()+"_"+dataRelation.getHashcode() + "(" + dataTypeIDFMap.get(dataRelation.getType()) + ") " + " "+dataRelation.callerMethod+" "+dataRelation.calleeMethod+"\n");
//                                    i++;
//                                }
//                            }
//                            sb.append("\n");
//                        }
//
//                    }
//                }
//            }
//            String outputPath = "data/exp/iTrust/dataRelation_detail/" + source + ".txt";
//            _.writeFile(sb.toString(), outputPath);
//        }


        SimilarityMatrix matrix_neighbour = new SimilarityMatrix();

        LinksList qualityLinks = matrix.getQualityLinks();

        int correctNum = 0;
        int wrongNum = 0;

        for (String source : matrix.sourceArtifactsIds()) {

            // set quality point
//            double minQualityScore = 1.0;
//            for (SingleLink ql : qualityLinks) {
//                if (ql.getSourceArtifactId().equals(source)) {
//                    if (ql.getScore() < minQualityScore) {
//                        minQualityScore = ql.getScore();
//                    }
//                }
//            }
//
//            double maxQualityScore = 0.0;
//            for (SingleLink ql : qualityLinks) {
//                if (ql.getSourceArtifactId().equals(source)) {
//                    if (ql.getScore() > maxQualityScore) {
//                        maxQualityScore = ql.getScore();
//                    }
//                }
//            }

            LinksList linksList = new LinksList();

            Map<String, Double> links = matrix.getLinksForSourceId(source);

            List<String> firstPiecesCode = new ArrayList<>();
            String maxValueTarget = subGraphInfo.getFirstPieceCodeForSource(source,firstPiecesCode, useEdge);
            double maxQualityScore = matrix.getScoreForLink(source, maxValueTarget);

            for (String target : links.keySet()) {
                double score = matrix.getScoreForLink(source, target);
                SingleLink originLink = new SingleLink(source, target, score);

                if (firstPiecesCode.contains(target)) {
                    double improvedScore = maxQualityScore;
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
                }
//                else if (extendedByDataRelation.contains(target)) {
//                    double improvedScore = maxQualityScore*0.9;
//
//                    log.append("Improve link = " + source + " " + target + " " + score + "\n");
//                    System.out.println(("Improve link = " + source + " " + target + " " + score + "\n"));
//
//                    linksList.add(new SingleLink(source, target, improvedScore));
//                    log.append("After Improved = " + source + " " + target + " " + improvedScore + "\n");
//                    System.out.println(("After Improved = " + source + " " + target + " " + improvedScore + "\n"));
//                    System.out.println(source + " " + target);
//                    if (rtm.isLinkAboveThreshold(source, target)) {
//                        correctNum++;
//                        log.append("Correct\n");
//                        System.out.println("Correct\n");
//                    } else {
//                        wrongNum++;
//                        log.append("Wrong\n");
//                        System.out.println("Wrong\n");
//                    }
//                }
                else {

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
//        System.out.println(extendVertexByDataRelation.size());
        return extendVertexByDataRelation;
    }

    @Override
    public String getAlgorithmName() {
        return "PruningData";
    }

    @Override
    public List<Pair<String, String>> getAlgorithmParameters() {
        List parameters = new ArrayList();
        Pair<String, String> p = new Pair<>("DataEdgeScoreThreshold", String.valueOf(relationInfo.getDataEdgeScoreThreshold()));
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
