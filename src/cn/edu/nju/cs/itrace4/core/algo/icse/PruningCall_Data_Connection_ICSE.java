package cn.edu.nju.cs.itrace4.core.algo.icse;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.CSTI;
import cn.edu.nju.cs.itrace4.core.algo.prealgo.UseEdge;
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
public class PruningCall_Data_Connection_ICSE implements CSTI {

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

    public PruningCall_Data_Connection_ICSE(RelationInfo prunedRelationInfo, RelationInfo fullRelationInfo, UseEdge useEdge, Double bonus_thresholdForCall, Double bonus_thresholdForData) {
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

        int correctNumForInitialRegion = 0;
        int wrongNumForInitialRegion = 0;

        int correctNumForRelink = 0;
        int wrongNumForRelink = 0;

        int correctNumForSeed = 0;
        int wrongNumForSeed = 0;

        int firstPieceSize = 0;
        for (String source : matrix.sourceArtifactsIds()) {

            LinksList linksList = new LinksList();

            Map<String, Double> links = matrix.getLinksForSourceId(source);
            List<String> firstValidPiecesCode = new ArrayList<>();
            String firstValidTarget = subGraphInfo.getFirstValidPieceCodeForSource(source, firstValidPiecesCode, useEdge);

            if (rtm.isLinkAboveThreshold(source, firstValidTarget)) {
                correctNumForSeed++;
            } else {
                wrongNumForSeed++;
            }

            // 12.22
//            System.out.println(" source = " + source );
//            System.out.println(" firstValidTarget = " + firstValidTarget );

//            List<String> extendedByDataRelation = extendFirstPieceByDataRelation(firstValidPiecesCode, firstValidTarget);
            List<String> extendedByDataRelation = extendFirstValidTargetByDataRelation(firstValidPiecesCode, firstValidTarget);
//            System.out.println(" extendedByDataRelation = " + extendedByDataRelation.size());

//            System.out.println(" extendedByDataRelation = " + extendedByDataRelation.size() );

            firstPieceSize += firstValidPiecesCode.size();

            List<String> firstCut = new ArrayList<>();
            firstCut.add(firstValidTarget);
            for (String s : firstValidPiecesCode) {
                if (!firstCut.contains(s)) {
                    firstCut.add(s);
                }
            }

            for (String s : extendedByDataRelation) {
                if (!firstCut.contains(s)) {
                    firstCut.add(s);
                }
            }

//            System.out.println(" firstCut = " + firstCut.size() );

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
                        correctNumForInitialRegion++;
                        correctImprovedTargetsList.add(target + "@" + source);
                        log.append("Correct\n");
                    } else {
                        wrongNumForInitialRegion++;
                        log.append("Wrong\n");
                    }
                } else if (extendedByDataRelation.contains(target)) {
                    double improvedScore = matrix.getScoreForLink(source, firstValidTarget);

                    log.append("Improve link = " + source + " " + target + " " + score + "\n");

                    linksList.add(new SingleLink(source, target, improvedScore));

                    if (rtm.isLinkAboveThreshold(source, target)) {
                        correctNumForInitialRegion++;
                        correctImprovedTargetsList.add(target + "@" + source);
                        log.append("Correct\n");
                    } else {
                        wrongNumForInitialRegion++;
                        log.append("Wrong\n");
                    }
                } else {
                    linksList.add(originLink);
                }
            }

            //
            for (String target : links.keySet()) {
                if (linksList.getScore(source, target) > maxBonusedValue) {
                    if (!firstCut.contains(target)) {
                        firstCut.add(target);
                    }
                }
            }


            for (String target : links.keySet()) {
                if (!firstCut.contains(target)) {

                    for (String vertexInfirstCut : firstCut) {
                        double bonusValue = ((CallDataRelationGraphUnormalized) fullRelationGraph).computeCallBonus(target, vertexInfirstCut, firstCut);
                        if (bonusValue != 0.0) {
                            System.out.println("Call bonusValue = " + bonusValue);
                        }
                        if (bonusValue > 1.0) {
                            System.out.println("zzz " + bonusValue);
                        }

                        double score = linksList.getScore(source, target);

                        if (score < maxBonusedValue) {

                            double afterBonus = score * (1 + bonusValue);

                            if (afterBonus < maxBonusedValue) {
                                linksList.updateLink(source, target, afterBonus);
                            } else {
                                linksList.updateLink(source, target, maxBonusedValue * 0.999);
                            }
                        }

                        if (bonusValue <= 0.0) {
//                            System.out.println("No bunus For Relink by Call");
                        } else {
                            if (rtm.isLinkAboveThreshold(source, target)) {
                                correctNumForRelink++;
                                correctImprovedTargetsList.add(target + "@" + source);
                                log.append("Correct\n");
                            } else {
                                wrongNumForRelink++;
                                log.append("Wrong\n");
                            }
                        }
                    }
                }
            }

            for (String target : links.keySet()) {
                if (!firstCut.contains(target)) {


                    for (String vertexInfirstCut : firstCut) {
                        double bonusValue = computeDataBonus(target, vertexInfirstCut,firstCut);
                        if (bonusValue != 0.0) {
                            System.out.println("Data bonusValue = " + bonusValue);
                        }

                        double score = linksList.getScore(source, target);


                        if (score < maxBonusedValue) {

                            double afterBonus = score * (1 + bonusValue);

                            if (afterBonus < maxBonusedValue) {
                                linksList.updateLink(source, target, afterBonus);
                            } else {
                                linksList.updateLink(source, target, maxBonusedValue * 0.999);
                            }
                        }

                        if (bonusValue <= 0.0) {
//                            System.out.println("No bunus For Relink by Data");
                        } else {
                            if (rtm.isLinkAboveThreshold(source, target)) {
                                correctNumForRelink++;
                                correctImprovedTargetsList.add(target + "@" + source);
                                log.append("Correct\n");
                            } else {
                                wrongNumForRelink++;
                                log.append("Wrong\n");
                            }
                        }

                    }
                }
            }

            Collections.sort(linksList, Collections.reverseOrder());
            for (SingleLink link : linksList) {
                matrix_neighbour.addLink(link.getSourceArtifactId(), link.getTargetArtifactId(), link.getScore());
            }
        }


        index = new StringBuilder();
        double precisionForInitialRegion = correctNumForInitialRegion * 1.0 / (correctNumForInitialRegion + wrongNumForInitialRegion);
        String indexPrecisionForInitialRegion = "Improved Number For Initial Region: " + (correctNumForInitialRegion + wrongNumForInitialRegion) + "\nCorrect: " + correctNumForInitialRegion + " Wrong: " + wrongNumForInitialRegion + "\nPrecision = " + precisionForInitialRegion + "\n";
        index.append(indexPrecisionForInitialRegion);
        double recallForInitialRegion = correctNumForInitialRegion * 1.0 / rtm.getLinksAboveThreshold().size();
        index.append("Improved Recall For Initial Region = " + recallForInitialRegion + "\n");
        double fMeasureForInitialRegion = 2.0 * precisionForInitialRegion * recallForInitialRegion / (precisionForInitialRegion + recallForInitialRegion);
        index.append("Improved F Measure For Initial Region = " + fMeasureForInitialRegion + "\n");

        double precisionForRelink = correctNumForRelink * 1.0 / (correctNumForRelink + wrongNumForRelink);
        String indexPrecisionForRelink = "Improved Number For Relink: " + (correctNumForRelink + wrongNumForRelink) + "\nCorrect: " + correctNumForRelink + " Wrong: " + wrongNumForRelink + "\nPrecision = " + precisionForRelink + "\n";
        index.append(indexPrecisionForRelink);
        double recallForRelink = correctNumForRelink * 1.0 / rtm.getLinksAboveThreshold().size();
        index.append("Improved Recall For Relink = " + recallForRelink + "\n");
        double fMeasureForRelink = 2.0 * precisionForRelink * recallForRelink / (precisionForRelink + recallForRelink);
        index.append("Improved F Measure For Relink = " + fMeasureForRelink + "\n");

        double precisionForSeed = correctNumForSeed * 1.0 / (correctNumForSeed + wrongNumForSeed);
        String indexPrecisionForSeed = "Improved Number For Seed: " + (correctNumForSeed + wrongNumForSeed) + "\nCorrect: " + correctNumForSeed + " Wrong: " + wrongNumForSeed + "\nImproved Precision = " + precisionForSeed + "\n";
        index.append(indexPrecisionForSeed);
        double recallForSeed = correctNumForSeed * 1.0 / rtm.getLinksAboveThreshold().size();
        index.append("Improved Recall For Seed = " + recallForSeed + "\n");
        double fMeasureForSeed = 2.0 * precisionForSeed * recallForSeed / (precisionForSeed + recallForSeed);
        index.append("Improved F Measure For Seed = " + fMeasureForSeed + "\n");

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

    private List<String> extendFirstValidTargetByDataRelation(List<String> firstValidPiecesCode, String firstValidTarget) {
        CallDataRelationGraph cdGraph = (CallDataRelationGraph) prunedRelationGraph;

        List<String> extendVertexByDataRelation = new ArrayList<>();
        List<CodeVertex> codeVertexList = cdGraph.getNeighboursByData(firstValidTarget);
        for (CodeVertex nb : codeVertexList) {
            if (!codeVertexList.contains(nb.getName())) {
                extendVertexByDataRelation.add(nb.getName());

            }
        }

        return extendVertexByDataRelation;
    }


    @Override
    public String getAlgorithmName() {
        return "PruningCall_Data_Connection_ICSE";
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


    private double computeDataBonus(String target, String vertexInfirstCut, List<String> firstCut) {

        List<CodeVertex> neighboursByData = ((CallDataRelationGraphUnormalized) fullRelationGraph).getNeighboursByData(target);

        for (CodeVertex nb : neighboursByData) {
            if (nb.getName().equals(vertexInfirstCut)) {
//                double extraBonus = ((CallDataRelationGraphUnormalized) fullRelationGraph).getDataEdgeBonusII(target, vertexInfirstCut);
//                double extraBonus = ((CallDataRelationGraphUnormalized) fullRelationGraph).getDataEdgeBonusBothVertex(vertexInfirstCut, target);
                double extraBonus = ((CallDataRelationGraphUnormalized) fullRelationGraph).getDataEdgeBonusCloseness(vertexInfirstCut, target);
//                double extraBonus = ((CallDataRelationGraphUnormalized) fullRelationGraph).getDataEdgeBonus_involveInitialRegion(vertexInfirstCut, target, firstCut);
                return extraBonus;
            }
        }
        return 0.0;
    }
}
