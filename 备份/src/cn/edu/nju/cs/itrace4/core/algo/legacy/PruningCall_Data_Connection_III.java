package cn.edu.nju.cs.itrace4.core.algo.legacy;

import cn.edu.nju.cs.itrace4.core.algo.CSTI;
import cn.edu.nju.cs.itrace4.core.algo.UseEdge;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.relation.*;
import cn.edu.nju.cs.itrace4.relation.graph.CodeVertex;
import javafx.util.Pair;

import java.util.*;

/**
 * Created by niejia on 15/5/12.
 */
public class PruningCall_Data_Connection_III implements CSTI {

    private RelationGraph prunedRelationGraph;
    private RelationInfo prunedRelationInfo;

    private RelationGraph fullRelationGraph;
    private RelationInfo fullRelationInfo;
    private final UseEdge useEdge;

    //    private PruningInfo pruningInfo;
    private SubGraphInfo subGraphInfo;

    private StringBuilder log;
    private StringBuilder index;
    private List<String> correctImprovedTargetsList;

    private Map<String, Integer> vertexDistByCall;
    private Map<String, Double> vertexBonusByCall;
    private Map<String, Integer> vertexDistByData;
    private Map<String, Double> vertexBonusByData;

    private int currentComputedVertexNumByCall = 0;
    private int preComputedVertexNumByCall = 0;
    private int currentComputedVertexNumByData = 0;
    private int preComputedVertexNumByData = 0;


    private double bonusForCall;
    private Double bonus_thresholdForCall;
    private double bonusForData;
    private Double bonus_thresholdForData;

    public PruningCall_Data_Connection_III(RelationInfo prunedRelationInfo, RelationInfo fullRelationInfo, UseEdge useEdge, Double bonus_thresholdForCall, Double bonus_thresholdForData) {
        this.prunedRelationInfo = prunedRelationInfo;
        this.prunedRelationGraph = new CallDataRelationGraph(prunedRelationInfo);
        this.fullRelationInfo = fullRelationInfo;
        this.fullRelationGraph = new CallDataRelationGraphUnormalized(fullRelationInfo);
        this.useEdge = useEdge;
        log = new StringBuilder();
        correctImprovedTargetsList = new ArrayList<>();
        this.bonus_thresholdForCall = bonus_thresholdForCall;
        this.bonus_thresholdForData = bonus_thresholdForData;
    }

    @Override
    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset, SimilarityMatrix similarityMatrix) {
        SimilarityMatrix rtm = textDataset.getRtm();
        this.subGraphInfo = new SubGraphInfo(textDataset, prunedRelationGraph);
        SimilarityMatrix matrix_neighbour = new SimilarityMatrix();

        // warning
        this.bonusForCall = bonus_thresholdForCall * getAdaptiveBonus(matrix);
        this.bonusForData = bonus_thresholdForData * getAdaptiveBonus(matrix);

        int correctNum = 0;
        int wrongNum = 0;

        int firstPieceSize = 0;

        for (String source : matrix.sourceArtifactsIds()) {

            LinksList linksList = new LinksList();

            Map<String, Double> links = matrix.getLinksForSourceId(source);
            List<String> firstValidPiecesCode = new ArrayList<>();
            String firstValidTarget = subGraphInfo.getFirstValidPieceCodeForSource(source, firstValidPiecesCode, useEdge);

            List<String> extendedByDataRelation = extendFirstPieceByDataRelation(firstValidPiecesCode, firstValidTarget);


            firstPieceSize += firstValidPiecesCode.size();

            List<String> allPiece = new ArrayList<>();
            for (String s : firstValidPiecesCode) {
                if (!allPiece.contains(s)) {
                    allPiece.add(s);
                }
            }

            for (String s : extendedByDataRelation) {
                if (!allPiece.contains(s)) {
                    allPiece.add(s);
                }
            }

//            double maxBonusedValue = getMaxValueAfterBonus(matrix, source, allPiece);
            double maxBonusedValue = matrix.getScoreForLink(source, firstValidTarget);

            for (String target : links.keySet()) {
                double score = matrix.getScoreForLink(source, target);
                SingleLink originLink = new SingleLink(source, target, score);

                if (firstValidPiecesCode.contains(target)) {
                    double improvedScore = matrix.getScoreForLink(source, firstValidTarget);

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
                } else if (extendedByDataRelation.contains(target)) {
                    double improvedScore = matrix.getScoreForLink(source, firstValidTarget);

                    log.append("Improve link = " + source + " " + target + " " + score + "\n");

                    linksList.add(new SingleLink(source, target, improvedScore));

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


            for (String target : links.keySet()) {
                if (!allPiece.contains(target)) {


                    double sumBonus = 0.0;
//                    for (String vertexInfirstCut : allPiece) {
                    double bonusValue = ((CallDataRelationGraphUnormalized) fullRelationGraph).computeCallBonus(target, firstValidTarget);
//                        System.out.println(" bonusValue = " + bonusValue);
//                        sumBonus += bonusValue;
                    double score = linksList.getScore(source, target);
                    double afterBonus = score * (1 + bonusValue);
//                        if (afterBonus < maxBonusedValue) {
                    linksList.updateLink(source, target, afterBonus);
//                        }
//                    }
                }
            }

            for (String target : links.keySet()) {
                if (!allPiece.contains(target)) {

//                    for (String vertexInfirstCut : allPiece) {
                    double bonusValue = computeDataBonus(target, firstValidTarget);
                    if (bonusValue > 0) {
                        System.out.println(" bonusValue = " + bonusValue);
                    }

                    double score = linksList.getScore(source, target);
                    double afterBonus = score * (1 + bonusValue);
//                        if (afterBonus < maxBonusedValue) {
                    linksList.updateLink(source, target, afterBonus);
//                        }
//                    }
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

    private boolean notAllNeighbourDistComputedByCall() {
        return currentComputedVertexNumByCall != preComputedVertexNumByCall;
    }

    private boolean notAllNeighbourDistComputedByData() {
        return currentComputedVertexNumByData != preComputedVertexNumByData;
    }

    private void searchNeighbourDistByCall() {
        List<Integer> values = new ArrayList<Integer>(vertexDistByCall.values());
        Collections.sort(values, Collections.reverseOrder());
        int cur = values.get(0);
        if (cur <= -1) throw new NoSuchElementException("My fault");

        for (String t : vertexDistByCall.keySet()) {
            if (vertexDistByCall.get(t) == cur) {
                List<CodeVertex> neighboursByCall = ((CallDataRelationGraphUnormalized) fullRelationGraph).getNeighboursByCall(t);

                if (neighboursByCall != null) {
                    for (CodeVertex cv : neighboursByCall) {
                        if (vertexDistByCall.get(cv.getName()) < 0) {
                            vertexDistByCall.put(cv.getName(), cur + 1);
                            double extraBonus = ((CallDataRelationGraphUnormalized) fullRelationGraph).getCallEdgeBonus(t, cv.getName());
//                            double afterBonusValue = bonusForCall * (1.0 / (cur + 1) + extraBonus);
//                            double afterBonusValue = 1.0 / (cur + 1) * extraBonus;

                            double afterBonusValue = 1.0 / Math.pow(2, cur) * extraBonus;

//                                                       double afterBonusValue = bonus * (1.0 / (cur + 1) + extraBonus);
//                            afterBonusValue = afterBonusValue < maxValueAfterBonus ? afterBonusValue : maxValueAfterBonus;
                            vertexBonusByCall.put(cv.getName(), afterBonusValue);
//                            vertexBonusByCall.put(cv.getName(), bonusForCall * (1.0 / (cur + 1) + extraBonus));
                        }
                    }
                }
            }
        }

        preComputedVertexNumByCall = currentComputedVertexNumByCall;
        currentComputedVertexNumByCall = countVaildTarget(vertexDistByCall);
    }

    private void searchNeighbourDistByData() {
        List<Integer> values = new ArrayList<Integer>(vertexDistByData.values());
        Collections.sort(values, Collections.reverseOrder());
        int cur = values.get(0);
        if (cur <= -1) throw new NoSuchElementException("My fault");

        for (String t : vertexDistByData.keySet()) {
            if (vertexDistByData.get(t) == cur) {
                List<CodeVertex> neighboursByData = ((CallDataRelationGraphUnormalized) fullRelationGraph).getNeighboursByData(t);

                if (neighboursByData != null) {
                    for (CodeVertex cv : neighboursByData) {
                        if (vertexDistByData.get(cv.getName()) < 0) {
                            vertexDistByData.put(cv.getName(), cur + 1);
                            double extraBonus = ((CallDataRelationGraphUnormalized) fullRelationGraph).getDataEdgeBonusII(t, cv.getName());
//                            double afterBonusValue = bonusForData * (1.0 / (cur + 1)+extraBonus);
//                            double afterBonusValue = 1.0 / (cur + 1) * extraBonus;
                            double afterBonusValue = 1.0 / Math.pow(2, cur) * extraBonus;
//                            double afterBonusValue = bonus * (1.0 / (cur + 1) + extraBonus);
                            vertexBonusByData.put(cv.getName(), afterBonusValue);
                        }
                    }
                }
            }
        }

        preComputedVertexNumByData = currentComputedVertexNumByData;
        currentComputedVertexNumByData = countVaildTarget(vertexDistByData);
    }


    private int countVaildTarget(Map<String, Integer> vetexDist) {
        int i = 0;
        for (String s : vetexDist.keySet()) {
            if (vetexDist.get(s) != -1) {
                i++;
            }
        }

        return i;
    }

    @Override
    public String getAlgorithmName() {
        return "PruningCall_Data_Connection_III";
    }

    @Override
    public List<Pair<String, String>> getAlgorithmParameters() {
        List parameters = new ArrayList();
        Pair<String, String> p = new Pair<>("CallEdgeScoreThreshold", String.valueOf(prunedRelationInfo.getCallEdgeScoreThreshold()));
        Pair<String, String> bonusCall = new Pair<>("BonusThresholdCall", String.valueOf(bonus_thresholdForCall));
        Pair<String, String> bonusData = new Pair<>("BonusThresholdData", String.valueOf(bonus_thresholdForData));
        parameters.add(p);
        parameters.add(bonusCall);
        parameters.add(bonusData);
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

    private double getAdaptiveBonus(SimilarityMatrix matrix) {
        List<Double> arrayScore = new ArrayList<>();
        for (String source : matrix.sourceArtifactsIds()) {
            Map<String, Double> links = matrix.getLinksForSourceId(source);

            List<Double> valuesList = new ArrayList<>();

            for (double value : links.values()) {
                valuesList.add(value);
            }

            Collections.sort(valuesList, Collections.reverseOrder());
            arrayScore.add((valuesList.get(0) - valuesList.get(valuesList.size() - 1)) / 2.0);
        }

        Collections.sort(arrayScore);
        double median;
        if (arrayScore.size() % 2 == 0)
            median = ((double) arrayScore.get(arrayScore.size() / 2) + (double) arrayScore.get(arrayScore.size() / 2 - 1)) / 2;
        else
            median = (double) arrayScore.get(arrayScore.size() / 2);
        return median;
    }


    private List<String> extendFirstPieceByDataRelation(List<String> firstPiecesCode, String highestTarget) {
        CallDataRelationGraph cdGraph = (CallDataRelationGraph) prunedRelationGraph;
        Map<CodeVertex, Integer> neighbourVertexNum = new HashMap<>();

        firstPiecesCode.add(highestTarget);
        for (String vertexName : firstPiecesCode) {
            List<CodeVertex> codeVertexList = cdGraph.getNeighboursByData(vertexName);
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
        return extendVertexByDataRelation;
    }

    private double getMaxValueAfterBonus(SimilarityMatrix matrix, String source, List<String> piect) {
        List<Double> values = new ArrayList<>();
        for (String target : piect) {
            values.add(matrix.getScoreForLink(source, target));
        }

        Collections.sort(values);
        return values.get(0);
    }

    private double computeDataBonus(String target, String vertexInfirstCut) {

        List<CodeVertex> neighboursByData = ((CallDataRelationGraphUnormalized) fullRelationGraph).getNeighboursByData(target);

        for (CodeVertex nb : neighboursByData) {
            if (nb.getName().equals(vertexInfirstCut)) {
                double extraBonus = ((CallDataRelationGraphUnormalized) fullRelationGraph).getDataEdgeBonusII(vertexInfirstCut, target);
                return extraBonus;
            }
        }
        return 0.0;
    }
}
