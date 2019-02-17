package cn.edu.nju.cs.itrace4.core.algo.legacy;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.CSTI;
import cn.edu.nju.cs.itrace4.core.algo.prealgo.UseEdge;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.relation.*;
import javafx.util.Pair;

import java.util.*;

/**
 * Created by niejia on 15/5/12.
 */
public class PruningCall_Connection_III implements CSTI {

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

    private Map<String, Integer> vertexDist;
    private Map<String, Double> vertexBonus;
    private int currentComputedVertexNum = 0;
    private int preComputedVertexNum = 0;

    private double bonus;
    private Double percent = 0.0;
    private Double bonus_threshold;

    public PruningCall_Connection_III(RelationInfo prunedRelationInfo, RelationInfo fullRelationInfo, UseEdge useEdge, Double bonus_threshold) {
        this.prunedRelationInfo = prunedRelationInfo;
        this.prunedRelationGraph = new CallDataRelationGraph(prunedRelationInfo);
        this.fullRelationInfo = fullRelationInfo;
        this.fullRelationGraph = new CallDataRelationGraphUnormalized(fullRelationInfo);
        this.useEdge = useEdge;
        log = new StringBuilder();
        correctImprovedTargetsList = new ArrayList<>();
        this.bonus_threshold = bonus_threshold;
    }

    @Override
    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset, SimilarityMatrix similarityMatrix) {
        SimilarityMatrix rtm = textDataset.getRtm();
        this.subGraphInfo = new SubGraphInfo(textDataset, prunedRelationGraph);
        SimilarityMatrix matrix_neighbour = new SimilarityMatrix();

        // warning
        this.bonus = bonus_threshold * getAdaptiveBonus(matrix);

        int correctNum = 0;
        int wrongNum = 0;

        int firstPieceSize = 0;



//        System.out.println("-----------------");
//        ((CallDataRelationGraphUnormalized) fullRelationGraph).computeCallBonus("DrugInteractionAction", "MedicationBeanValidator");
//        System.out.println("-----------------");

        for (String source : matrix.sourceArtifactsIds()) {

            LinksList linksList = new LinksList();


            Map<String, Double> links = matrix.getLinksForSourceId(source);
            List<String> firstValidPiecesCode = new ArrayList<>();
            String firstValidTarget = subGraphInfo.getFirstValidPieceCodeForSource(source, firstValidPiecesCode, useEdge);

//            currentComputedVertexNum = firstValidPiecesCode.size() + 1;

            firstPieceSize += firstValidPiecesCode.size();

//            vertexDist = new HashMap<>();
//            vertexBonus = new HashMap<>();
//            for (String t : matrix.targetArtifactsIds()) {
//                if (!vertexDist.containsKey(t)) {
//                    vertexDist.put(t, -1);
//                    vertexBonus.put(t, 0.0);
//                }
//            }
//
//            for (String s : firstValidPiecesCode) {
//                vertexDist.put(s, 0);
//            }
//
//            vertexDist.put(firstValidTarget, 0);
//
//            while (notAllNeighbourDistComputed()) {
//                searchNeighbourDist();
//            }
//
//            if (source.equals("UC1")) {
//                System.out.println(vertexDist);
//                System.out.println(vertexBonus);
//            }

            double maxBonusedValue = getMaxValueAfterBonus(matrix, source, firstValidPiecesCode);

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
                } else {
                    linksList.add(originLink);
                }
            }

            for (String target : links.keySet()) {
                if (!firstValidPiecesCode.contains(target)) {

                        double bonusValue = ((CallDataRelationGraphUnormalized) fullRelationGraph).computeCallBonus(target, firstValidTarget);
//                        System.out.println(" bonusValue = " + bonusValue);
//                        sumBonus += bonusValue;
                        double score = linksList.getScore(source, target);
                        double afterBonus = score * (1 + bonusValue);
//                        if (afterBonus < maxBonusedValue) {
                            linksList.updateLink(source, target, afterBonus);
//                        }
                }
            }

//            vertexDist.clear();
//            vertexBonus.clear();

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
        System.out.println(" matrix_neighbour = " + matrix_neighbour);
        return matrix_neighbour;
    }

    @Override
    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset) {
        return null;
    }

    private double getMaxValueAfterBonus(SimilarityMatrix matrix, String source, List<String> firstValidPiecesCode) {
        List<Double> values = new ArrayList<>();
        for (String target : firstValidPiecesCode) {
            values.add(matrix.getScoreForLink(source, target));
        }

        Collections.sort(values);
        return values.get(0);
    }



    @Override
    public String getAlgorithmName() {
        return "PruningCall_Connection_III";
    }

    @Override
    public List<Pair<String, String>> getAlgorithmParameters() {
        List parameters = new ArrayList();
        Pair<String, String> p = new Pair<>("CallEdgeScoreThreshold", String.valueOf(prunedRelationInfo.getCallEdgeScoreThreshold()));
        Pair<String, String> bonus = new Pair<>("BonusThreshold", String.valueOf(bonus_threshold));
        parameters.add(p);
        parameters.add(bonus);
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
}
