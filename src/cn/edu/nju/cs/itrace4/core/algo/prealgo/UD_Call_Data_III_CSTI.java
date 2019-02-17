package cn.edu.nju.cs.itrace4.core.algo.prealgo;

import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.relation.CallDataRelationGraph;
import cn.edu.nju.cs.itrace4.relation.RelationGraph;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.relation.SubGraphInfo;
import cn.edu.nju.cs.itrace4.relation.graph.CodeVertex;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by niejia on 15/4/15.
 */
public class UD_Call_Data_III_CSTI implements CSTI {

    public double bonus;
    private RelationGraph relationGraph;
    private RelationInfo relationInfo;
    private final UseEdge useEdge;

    private StringBuilder index;

    private SubGraphInfo subGraphInfo;

    private StringBuilder log;
    private List<String> correctImprovedTargetsList;

    public UD_Call_Data_III_CSTI(RelationInfo relationInfo, UseEdge useEdge) {
        this.relationInfo = relationInfo;
        this.relationGraph = new CallDataRelationGraph(relationInfo);
        this.useEdge = useEdge;
        log = new StringBuilder();
        correctImprovedTargetsList = new ArrayList<>();
    }

    @Override
    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset, SimilarityMatrix similarityMatrix) {

        int correctNum = 0;
        int wrongNum = 0;
        SimilarityMatrix rtm = textDataset.getRtm();

        SimilarityMatrix matrix_ud = new SimilarityMatrix();
        SimilarityMatrix oracle = textDataset.getRtm();

        this.subGraphInfo = new SubGraphInfo(textDataset, relationGraph);

        LinksList originLinks = new LinksList();
        LinksList allLinks = matrix.allLinks();
        Collections.sort(allLinks, Collections.reverseOrder());
        for (SingleLink link : allLinks) {
            originLinks.add(link);
        }

        LinksList resultLinks = new LinksList();

        int i = originLinks.size();

        List<String> improvedLinks = new ArrayList<>();

        while (originLinks.size() != 0) {
            SingleLink link = originLinks.get(0);

            originLinks.remove(0);
            String source = link.getSourceArtifactId();
            String target = link.getTargetArtifactId();
            double score = link.getScore();

            if (oracle.isLinkAboveThreshold(source, target) && !improvedLinks.contains(source + "#" + target)) {
                List<CodeVertex> connectedVertexByCall = new ArrayList<>();
                List<CodeVertex> connectedVertexByData = new ArrayList<>();


                ((CallDataRelationGraph) relationGraph).searhNeighbourConnectedGraphByCall(relationGraph.getCodeVertexByName(target), connectedVertexByCall);

                for (CodeVertex cv : connectedVertexByCall) {
                    List<CodeVertex> dataNeighbours = new ArrayList<>();
                    ((CallDataRelationGraph) relationGraph).searhNeighbourConnectedGraphByData(relationGraph.getCodeVertexByName(cv.getName()), dataNeighbours);
                    for (CodeVertex v : dataNeighbours) {
                        if (!connectedVertexByData.contains(v)) {
                            connectedVertexByData.add(v);
                        }
                    }
                }

                System.out.println(connectedVertexByData.size());
                List<CodeVertex> connectedVertex = new ArrayList<>();

                for (CodeVertex cv : connectedVertexByCall) {
                    if (!connectedVertex.contains(cv)) {
                        connectedVertex.add(cv);
                    }
                }

                for (CodeVertex cv : connectedVertexByData) {
                    if (!connectedVertex.contains(cv)) {
                        connectedVertex.add(cv);
                    }
                }
//

                double improvedScore = link.getScore();

                for (CodeVertex nb : connectedVertex) {
//                    System.out.println(" improvedLink = " + source + " " + nb.getName() + " " + improvedScore);
                    originLinks.updateLink(source, nb.getName(), improvedScore);
                    improvedLinks.add(source + "#" + nb.getName());

                    if (rtm.isLinkAboveThreshold(source, nb.getName())) {
                        correctNum++;
                        correctImprovedTargetsList.add(target + "@" + source);
                        log.append("Correct\n");
                    } else {
                        wrongNum++;
                        log.append("Wrong\n");
                    }
                }
            }

            if (score != 0.0) {
                resultLinks.add(new SingleLink(source, target, score + i));
            } else {
                resultLinks.add(new SingleLink(source, target, 0.0));
            }

            Collections.sort(originLinks, Collections.reverseOrder());
            i--;
        }

//        System.out.println(" resultLinks = " + resultLinks );

        for (SingleLink link : resultLinks) {
            matrix_ud.addLink(link.getSourceArtifactId(), link.getTargetArtifactId(), link.getScore());
        }

        index = new StringBuilder();
        double precision = correctNum * 1.0 / (correctNum + wrongNum);
        String indexPrecision = "Improved Number " + (correctNum + wrongNum) + "\nCorrect: " + correctNum + " Wrong: " + wrongNum + "\nImproved Precision = " + precision + "\n";
        index.append(indexPrecision);
        double recall = correctNum * 1.0 / rtm.getLinksAboveThreshold().size();
        index.append("Improved Recall = " + recall + "\n");
        double fMeasure = 2.0 * precision * recall / (precision + recall);
        index.append("Improved F Measure = " + fMeasure + "\n");

        System.out.println(index.toString());
//        System.out.println(" matrix_ud = " + matrix_ud );
        return matrix_ud;
    }

    @Override
    public SimilarityMatrix improve(SimilarityMatrix matrix, TextDataset textDataset) {
        return null;
    }

    @Override
    public String getAlgorithmName() {
        return "UD_Call_Data_III";
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
        return index.toString();
    }

    @Override
    public List<String> getCorrectImprovedTargetsList() {
        return null;
    }
}
