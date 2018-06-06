package cn.edu.nju.cs.itrace4.core.algo.legacy;

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
public class PruningData_Connection_III implements CSTI {

    private RelationGraph prunedRelationGraph;
    private RelationInfo prunedRelationInfo;

    private RelationGraph fullRelationGraph;
    private RelationInfo fullRelationInfo;

    private final UseEdge useEdge;

    private SubGraphInfo subGraphInfo;

    private StringBuilder log;
    private StringBuilder index;

    private List<String> correctImprovedTargetsList;

    private Map<String, Integer> vertexDist;
    private Map<String, Double> vertexBonus;
    private int currentComputedVertexNum = 0;
    private int preComputedVertexNum = 0;
    private Double bonus_threshold;
    private double bonus;


    public PruningData_Connection_III(RelationInfo prunedRelationInfo, RelationInfo fullRelationInfo, UseEdge useEdge, Double bonus_threshold) {
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

        this.bonus = bonus_threshold * getAdaptiveBonus(matrix);


        LinksList qualityLinks = matrix.getQualityLinks();

        int correctNum = 0;
        int wrongNum = 0;

        for (String source : matrix.sourceArtifactsIds()) {

            LinksList linksList = new LinksList();

            Map<String, Double> links = matrix.getLinksForSourceId(source);

            List<String> firstPiecesCode = new ArrayList<>();
            String maxValueTarget = subGraphInfo.getFirstPieceCodeForSource(source, firstPiecesCode, useEdge);
            double maxQualityScore = matrix.getScoreForLink(source, maxValueTarget);

//            currentComputedVertexNum = firstPiecesCode.size() + 1;
//            vertexDist = new HashMap<>();
//            vertexBonus = new HashMap<>();
//
//            for (String t : matrix.targetArtifactsIds()) {
//                if (!vertexDist.containsKey(t)) {
//                    vertexDist.put(t, -1);
//                    vertexBonus.put(t, 0.0);
//                }
//            }
//
//            for (String s : firstPiecesCode) {
//                vertexDist.put(s, 0);
//            }
//
//            vertexDist.put(maxValueTarget, 0);
//
////            while (notAllNeighbourDistComputed()) {
//            searchNeighbourDist();
////            }
//
//            if (source.equals("UC1")) {
//                System.out.println(vertexDist);
//                System.out.println(vertexBonus);
//            }

            double maxBonusedValue = getMaxValueAfterBonus(matrix, source, firstPiecesCode);
            if (maxBonusedValue == -1) {
                maxBonusedValue = matrix.getScoreForLink(source, maxValueTarget);
            }

            for (String target : links.keySet()) {
                double score = matrix.getScoreForLink(source, target);
                SingleLink originLink = new SingleLink(source, target, score);

                if (firstPiecesCode.contains(target)) {
                    double improvedScore = maxQualityScore;

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
                if (!firstPiecesCode.contains(target)) {


//                    for (String vertexInfirstCut : firstPiecesCode) {
                    double bonusValue = computeDataBonus(target, maxValueTarget);

                    double score = linksList.getScore(source, target);
//                        System.out.println(" bonusValue = " + bonusValue);
                    double afterBonus = score * (1 + bonusValue);
//                        if (afterBonus < maxBonusedValue) {
                    linksList.updateLink(source, target, afterBonus);
//                        }
//                    }
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
        return matrix_neighbour;
    }

    @Override
    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset) {
        return null;
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

    @Override
    public String getAlgorithmName() {
        return "PruningData_Connection_III";
    }

    @Override
    public List<Pair<String, String>> getAlgorithmParameters() {
        List parameters = new ArrayList();
        Pair<String, String> p = new Pair<>("DataEdgeScoreThreshold", String.valueOf(prunedRelationInfo.getDataEdgeScoreThreshold()));
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

    private void searchNeighbourDist() {
        List<Integer> values = new ArrayList<Integer>(vertexDist.values());
        Collections.sort(values, Collections.reverseOrder());
        int cur = values.get(0);
        if (cur <= -1) throw new NoSuchElementException("My fault");

        for (String t : vertexDist.keySet()) {
            if (vertexDist.get(t) == cur) {
                List<CodeVertex> neighboursByCall = ((CallDataRelationGraphUnormalized) fullRelationGraph).getNeighboursByData(t);

                if (neighboursByCall != null) {
                    for (CodeVertex cv : neighboursByCall) {
                        if (vertexDist.get(cv.getName()) < 0) {
                            vertexDist.put(cv.getName(), cur + 1);
                            double extraBonus = ((CallDataRelationGraphUnormalized) fullRelationGraph).getDataEdgeBonusII(t, cv.getName());


//                            double afterBonusValue = bonus * (1.0 / (cur + 1));
//                            double afterBonusValue = bonus * (1.0 / (cur + 1) + extraBonus);
//                            double afterBonusValue = 1.0 / (cur + 1) * extraBonus;
                            double afterBonusValue = 1.0 / Math.pow(2, cur) * extraBonus;
                            vertexBonus.put(cv.getName(), afterBonusValue);
                        }
                    }
                }
            }
        }

        preComputedVertexNum = currentComputedVertexNum;
        currentComputedVertexNum = countVaildTarget();
    }

    private int countVaildTarget() {
        int i = 0;
        for (String s : vertexDist.keySet()) {
            if (vertexDist.get(s) != -1) {
                i++;
            }
        }

        return i;
    }

    private boolean notAllNeighbourDistComputed() {
        return currentComputedVertexNum != preComputedVertexNum;
    }

    private double getMaxValueAfterBonus(SimilarityMatrix matrix, String source, List<String> firstValidPiecesCode) {
        List<Double> values = new ArrayList<>();
        for (String target : firstValidPiecesCode) {
            values.add(matrix.getScoreForLink(source, target));
        }

        if (values.size() == 0) return -1;
        Collections.sort(values);
        return values.get(0);
    }
}
