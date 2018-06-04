package cn.edu.nju.cs.itrace4.relation;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.UseEdge;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.relation.graph.CodeVertex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by niejia on 15/3/16.
 */
public class SubGraphInfo {
    private TextDataset textDataset;
    private RelationGraph relationGraph;
    //    private Result result;
    private SimilarityMatrix matrix;
    public SubGraphInfo(TextDataset textDataset, RelationGraph relationGraph) {
//        this.relationGraph = new CallDataRelationGraph(relationInfo);
        this.relationGraph = relationGraph;


//        this.result = IR.compute(textDataset, IRModelConst.VSM, new None_CSTI());


        this.textDataset = textDataset;
    }

    public SubGraphInfo(TextDataset textDataset, RelationGraph relationGraph, SimilarityMatrix sm) {
        this.relationGraph = relationGraph;
        this.matrix = sm;
        this.textDataset = textDataset;
    }

    // original
//    public String getFirstValidPieceCodeForSource(String source, List<String> firstValidPieceCode, UseEdge useEdge) {
////        SimilarityMatrix matrix = result.getMatrix();
//        Map<String, Double> links = matrix.getLinksForSourceId(source);
//
//        String firstValidTarget = null;
//
//        for (String target : links.keySet()) {
//            List<CodeVertex> connectedVertex = new ArrayList<>();
//            if (useEdge.equals(UseEdge.Call)) {
//                ((CallDataRelationGraph) relationGraph).searhNeighbourConnectedGraphByCall(relationGraph.getCodeVertexByName(target), connectedVertex);
//            } else if (useEdge.equals(UseEdge.Data)){
//                ((CallDataRelationGraph) relationGraph).searhNeighbourConnectedGraphByData(relationGraph.getCodeVertexByName(target), connectedVertex);
////                ((CallDataRelationGraph) relationGraph).searhNeighbourVertexByData(relationGraph.getCodeVertexByName(target), connectedVertex);
//            }
//
//            if (connectedVertex.size() > 0) {
//                firstValidTarget = target;
//                break;
//            }
//        }
//
//        if (firstValidTarget != null) {
//            List<CodeVertex> connectedVertex = new ArrayList<>();
//
//            if (useEdge.equals(UseEdge.Call)) {
//                ((CallDataRelationGraph) relationGraph).searhNeighbourConnectedGraphByCall(relationGraph.getCodeVertexByName(firstValidTarget), connectedVertex);
//            } else if (useEdge.equals(UseEdge.Data)){
//                ((CallDataRelationGraph) relationGraph).searhNeighbourConnectedGraphByData(relationGraph.getCodeVertexByName(firstValidTarget), connectedVertex);
////                ((CallDataRelationGraph) relationGraph).searhNeighbourVertexByData(relationGraph.getCodeVertexByName(firstValidTarget), connectedVertex);
//            }
//
//            for (CodeVertex codeVertex : connectedVertex) {
//                firstValidPieceCode.add(codeVertex.getName());
//            }
//        } else {
//            firstValidTarget = matrix.getFirstMaxValueTarget(source);
//        }
//        return firstValidTarget;
//    }




    public String getFirstPieceCodeForSource(String source, List<String> firstPieceCode, UseEdge useEdge) {
//        SimilarityMatrix matrix = result.getMatrix();
        String maxValueTarget = matrix.getFirstMaxValueTarget(source);
        List<CodeVertex> connectedVertex = new ArrayList<>();

        if (useEdge.equals(UseEdge.Call)) {
            ((CallDataRelationGraph) relationGraph).searhNeighbourConnectedGraphByCall(relationGraph.getCodeVertexByName(maxValueTarget), connectedVertex);
        } else if (useEdge.equals(UseEdge.Data)){
            ((CallDataRelationGraph) relationGraph).searhNeighbourConnectedGraphByData(relationGraph.getCodeVertexByName(maxValueTarget), connectedVertex);
//                ((CallDataRelationGraph) relationGraph).searhNeighbourVert exByData(relationGraph.getCodeVertexByName(firstValidTarget), connectedVertex);
        }

        for (CodeVertex codeVertex : connectedVertex) {
            firstPieceCode.add(codeVertex.getName());
        }

        return maxValueTarget;
    }

    // 3.29
    public String getFirstValidPieceCodeForSource(String source, 
    		List<String> firstValidPieceCode, UseEdge useEdge) {
        Map<String, Double> links = matrix.getLinksForSourceId(source);
        String firstValidTarget = null;
        double maxIRValue = 0.0;//这个值没用到呀
        
        //好奇怪的for循环 只执行一次    最大值就是第一个 （往里放的时候好像已经保证有序了）
        for (String target : links.keySet()) {
            maxIRValue = matrix.getScoreForLink(source, target);
            break;
        }

        //从IR值最大的一个开始遍历
        for (String target : links.keySet()) {///for
            //double currentIRValue = matrix.getScoreForLink(source, target);

            List<CodeVertex> connectedVertex = new ArrayList<>();
            if(relationGraph.getCodeVertexByName(target)==null){
            	continue;
            }
            ((CallDataRelationGraph) relationGraph).searhNeighbourConnectedGraphByCall(
            		relationGraph.getCodeVertexByName(target), 
            		connectedVertex);
            ((CallDataRelationGraph) relationGraph).searhNeighbourConnectedGraphByData(
            		relationGraph.getCodeVertexByName(target),
            		connectedVertex);
            if (connectedVertex.size() > 0) {
                firstValidTarget = target;
                break;
            }
        }///for

        if (firstValidTarget != null) {
            List<CodeVertex> connectedVertex = new ArrayList<>();
            ((CallDataRelationGraph) relationGraph).searhFathersByCall(relationGraph.getCodeVertexByName(firstValidTarget),
            		connectedVertex);
            ((CallDataRelationGraph) relationGraph).searhChildrenByCall(relationGraph.getCodeVertexByName(firstValidTarget),
            		connectedVertex);
            ((CallDataRelationGraph) relationGraph).searhNeighbourConnectedGraphByData(relationGraph.getCodeVertexByName(firstValidTarget),
            		connectedVertex);
            for(CodeVertex codeVertex : connectedVertex) {
                firstValidPieceCode.add(codeVertex.getName());
            }
        } else {
            firstValidTarget = matrix.getFirstMaxValueTarget(source);
        }
        return firstValidTarget;
    }
}
