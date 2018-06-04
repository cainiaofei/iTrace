package cn.edu.nju.cs.itrace4.core.algo;

import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.relation.CallDataRelationGraph;
import cn.edu.nju.cs.itrace4.relation.RelationGraph;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.relation.graph.CodeVertex;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by niejia on 15/4/21.
 */
public class O_Call_Data_CSTI implements CSTI {

    private double bonus;
    private RelationGraph relationGraph;

    public O_Call_Data_CSTI(RelationInfo relationInfo) {
        this.relationGraph = new CallDataRelationGraph(relationInfo);
    }

    public void setBonus(double v) {
        bonus = v;
    }

    public double getBonus() {
        return bonus;
    }

    @Override
    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset, SimilarityMatrix similarityMatrix) {

        SimilarityMatrix matrix_o = new SimilarityMatrix();

        bonus = getAdaptiveBonus(matrix);

        for (String source : matrix.sourceArtifactsIds()) {
            LinksList linksList = new LinksList();
            Map<String, Double> links = matrix.getLinksForSourceId(source);
            for (String target : links.keySet()) {
                List<CodeVertex> neighboursByCall = ((CallDataRelationGraph) relationGraph).getNeighboursByCall(target);
//                List<CodeVertex> neighboursByCall = ((CallDataRelationGraph) relationGraph).getChildrenByCall(target);
                List<CodeVertex> neighboursByData = ((CallDataRelationGraph) relationGraph).getNeighboursByData(target);

                List<CodeVertex> neighbours = new ArrayList<>();
                for (CodeVertex cv : neighboursByCall) {
                    if (!neighbours.contains(cv)) {
                        neighbours.add(cv);
                    }
                }

                for (CodeVertex cv : neighboursByData) {
                    if (!neighbours.contains(cv)) {
                        neighbours.add(cv);
                    }
                }

                for (CodeVertex nb : neighbours) {
                    if (matrix.getScoreForLink(source, nb.getName()) == null) {
                        System.out.println(target);
                    }
                    double originScore = matrix.getScoreForLink(source, nb.getName());
//                        System.out.println("Original Links: " + source + " " + child + " " + originScore);
                    matrix.setScoreForLink(source, nb.getName(), originScore * (1 + bonus));
//                        System.out.println("Improved Links: " + source + " " + child + " " + originScore * (1 + IR.CONSTANT_BONUS));
                }
            }

            for (String target : links.keySet()) {
                linksList.add(new SingleLink(source, target, matrix.getScoreForLink(source, target)));
            }

            Collections.sort(linksList, Collections.reverseOrder());

            for (SingleLink link : linksList) {
                matrix_o.addLink(link.getSourceArtifactId(), link.getTargetArtifactId(), link.getScore());
            }
        }
        return matrix_o;
    }

    @Override
    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset) {
        return null;
    }

    @Override
    public String getAlgorithmName() {
        return "O_Call_Data_CSTI";
    }

    @Override
    public List<Pair<String, String>> getAlgorithmParameters() {
        List parameters = new ArrayList();
        Pair<String, String> p = new Pair<>("Bonus", String.valueOf(bonus));
        parameters.add(p);
        return parameters;
    }

    @Override
    public String getDetails() {
        return null;
    }

    @Override
    public List<String> getCorrectImprovedTargetsList() {
        return null;
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
