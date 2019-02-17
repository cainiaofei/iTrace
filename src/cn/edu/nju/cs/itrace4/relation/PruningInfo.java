package cn.edu.nju.cs.itrace4.relation;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.None_CSTI;
import cn.edu.nju.cs.itrace4.core.algo.prealgo.UseEdge;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.core.ir.IR;
import cn.edu.nju.cs.itrace4.core.ir.IRModelConst;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.relation.graph.CodeVertex;

import java.util.*;

/**
 * Created by niejia on 15/3/4.
 */
public class PruningInfo {

    private TextDataset textDataset;
    private RelationGraph relationGraph;
    private Result result;

    private List<String> sourcesList;
//    private List<String> candidateSeeds;
    private Map<String, List<String>> firstPieceForSourceMap;
    private Map<String, List<String>> secondPieceForSourceMap;

    private Map<String, Integer> retrievedNumForSourceMap;
    private Map<String, Integer> correctedNumForSourceMap;

    private StringBuilder improvedCorrectedTarget;
    private StringBuilder improvedWrongTarget;

    public PruningInfo(TextDataset textDataset, RelationInfo relationInfo) {
        this.relationGraph = new CallDataRelationGraph(relationInfo);
        this.result = IR.compute(textDataset, IRModelConst.VSM, new None_CSTI());
        this.textDataset = textDataset;
        this.firstPieceForSourceMap = new LinkedHashMap<>();
        this.secondPieceForSourceMap = new LinkedHashMap<>();
        this.retrievedNumForSourceMap = new HashMap<>();
        this.correctedNumForSourceMap = new HashMap<>();
        this.improvedCorrectedTarget = new StringBuilder();
        improvedCorrectedTarget.append("improvedCorrectedTarget:\n");
        this.improvedWrongTarget = new StringBuilder();
        improvedWrongTarget.append("improvedWrongTarget:\n");

        sourcesList = new ArrayList<>();
        for (String source : textDataset.getSourceCollection().keySet()) {
        	//新增 只考虑RTM中的需求
        	if(!textDataset.getSourceCollection().containsKey(source)) {
        		continue;
        	}
        	if(!result.matrix.sourceArtifactsIds().contains(source)) {
        		continue;
        	}
            sourcesList.add(source);
        }

        computePruningInfo();
    }

    private void computePruningInfo() {

        double averagePrecision = 0.0;
        double averageRecall = 0.0;

        for (String source : sourcesList) {
            List<String> candidateSeeds = findCandidateSeeds(source);

            List<String> retrievedFirstPiece = new ArrayList<>();
            List<String> retrievedSecondPiece = new ArrayList<>();
            List<String> retrievedCodes = new ArrayList<>();

            for (int i = 0; i < candidateSeeds.size(); i++) {
                if (i == 0) {
                    List<CodeVertex> connectedVertex = new ArrayList<>();
                    ((CallDataRelationGraph) relationGraph).searhNeighbourConnectedGraphByPruning(relationGraph.
                    		getCodeVertexByName(candidateSeeds.get(i)), connectedVertex);
//                    ((CallDataRelationGraph) relationGraph).searhNeighbourVertexByPruning(relationGraph.getCodeVertexByName(candidateSeeds.get(i)), connectedVertex);
                    for (CodeVertex codeVertex : connectedVertex) {
                        retrievedFirstPiece.add(codeVertex.getName());
                        if (!retrievedCodes.contains(codeVertex.getName())) {
                            retrievedCodes.add(codeVertex.getName());
                        }
                    }
                } else if (i == 1) {
                }
            }

            List<String> codesInRTMForSource = new ArrayList<>();
            for (SingleLink link : textDataset.getRtm().getLinksAboveThresholdForSourceArtifact(source)) {
                codesInRTMForSource.add(link.getTargetArtifactId());
            }

            int findN = 0;
            for (String rc : retrievedCodes) {
                if (codesInRTMForSource.contains(rc)) {
                    improvedCorrectedTarget.append(rc);
                    improvedCorrectedTarget.append("(" + source + ")");
                    improvedCorrectedTarget.append(" ");
                    findN++;
                } else {
                    improvedWrongTarget.append(rc);
                    improvedWrongTarget.append("(" + source + ")");
                    improvedWrongTarget.append(" ");
                }
            }

            retrievedNumForSourceMap.put(source, retrievedCodes.size());
            correctedNumForSourceMap.put(source, findN);
            firstPieceForSourceMap.put(source, retrievedFirstPiece);
//            secondPieceForSourceMap.put(source, remainSecond);

            averagePrecision += (findN / (1.0 * retrievedCodes.size()));
            averageRecall += (findN / (1.0 * codesInRTMForSource.size()));
        }


    }


    private List<String> findCandidateSeeds(String source) {
        List<String> candidateSeeds = new ArrayList<>();
        candidateSeeds.add(result.getMatrix().getFirstMaxValueTarget(source));
        return candidateSeeds;
    }

    public List<String> getFirstPieceCodeForSource(String source) {
        return firstPieceForSourceMap.get(source);
    }

    public List<String> getSecondPieceCodeForSource(String source, List<String> firstPiecesCode) {

        String secondMaxValue = result.getMatrix().getSecondMaxValueTarget(source, firstPiecesCode);
        List<CodeVertex> connectedVertex = new ArrayList<>();
        ((CallDataRelationGraph) relationGraph).searhNeighbourConnectedGraphByPruning(relationGraph.getCodeVertexByName(secondMaxValue), connectedVertex);

        List<String> secondPieces = new ArrayList<>();
        for (CodeVertex codeVertex : connectedVertex) {
            secondPieces.add(codeVertex.getName());
        }

        return secondPieces;
    }

    public List<String> getNextValidPieceCodeForSource(String source, List<String> firstPiecesCode) {
        SimilarityMatrix matrix = result.getMatrix();
        Map<String, Double> links = matrix.getLinksForSourceId(source);

//        List<String> existPiecesCode = new ArrayList<>();
//        for (String s : firstPiecesCode) {
//            existPiecesCode.add(s);
//        }

        String nextValueTarget;
        for (String target : links.keySet()) {
            if (!firstPiecesCode.contains(target)) {
                nextValueTarget = target;

                List<CodeVertex> connectedVertex = new ArrayList<>();
                ((CallDataRelationGraph) relationGraph).searhNeighbourConnectedGraphByPruning(relationGraph.getCodeVertexByName(nextValueTarget), connectedVertex);
                if (connectedVertex.size() > 0) break;
            }
        }

        return null;
    }

    public String getFirstValidPieceCodeForSource(String source, List<String> firstValidPieceCode, UseEdge useEdge) {
        SimilarityMatrix matrix = result.getMatrix();
        Map<String, Double> links = matrix.getLinksForSourceId(source);

        String firstValidTarget = null;

        for (String target : links.keySet()) {
            List<CodeVertex> connectedVertex = new ArrayList<>();
            if (useEdge.equals(UseEdge.Call)) {
                ((CallDataRelationGraph) relationGraph).searhNeighbourConnectedGraphByCall(relationGraph.getCodeVertexByName(target), connectedVertex);
            } else if (useEdge.equals(UseEdge.Data)){
                ((CallDataRelationGraph) relationGraph).searhNeighbourConnectedGraphByData(relationGraph.getCodeVertexByName(target), connectedVertex);
//                ((CallDataRelationGraph) relationGraph).searhNeighbourVertexByData(relationGraph.getCodeVertexByName(target), connectedVertex);
            }

            if (connectedVertex.size() > 0) {
                firstValidTarget = target;
                break;
            }
        }

        if (firstValidTarget != null) {
            List<CodeVertex> connectedVertex = new ArrayList<>();

            if (useEdge.equals(UseEdge.Call)) {
                ((CallDataRelationGraph) relationGraph).searhNeighbourConnectedGraphByCall(relationGraph.getCodeVertexByName(firstValidTarget), connectedVertex);
            } else if (useEdge.equals(UseEdge.Data)){
                ((CallDataRelationGraph) relationGraph).searhNeighbourConnectedGraphByData(relationGraph.getCodeVertexByName(firstValidTarget), connectedVertex);
//                ((CallDataRelationGraph) relationGraph).searhNeighbourVertexByData(relationGraph.getCodeVertexByName(firstValidTarget), connectedVertex);
            }

            for (CodeVertex codeVertex : connectedVertex) {
                firstValidPieceCode.add(codeVertex.getName());
            }
        } else {
            firstValidTarget = matrix.getFirstMaxValueTarget(source);
        }
        return firstValidTarget;
    }

    public String getSecondPoint(String source, List<String> firstPiecesCode) {
        return result.getMatrix().getSecondMaxValueTarget(source, firstPiecesCode);
    }
}
