package cn.edu.nju.cs.itrace4.relation;

/**
 * Created by niejia on 15/4/24.
 */

import cn.edu.nju.cs.itrace4.core.algo.legacy.PathSearch;
import cn.edu.nju.cs.itrace4.io._;
import cn.edu.nju.cs.itrace4.relation.graph.*;
import cn.edu.nju.cs.itrace4.relation.info.DataRelation;
import cn.edu.nju.cs.itrace4.relation.info.RelationPair;
import cn.edu.nju.cs.itrace4.util.Setting;
import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import org.apache.commons.collections15.functors.MapTransformer;

import java.util.*;

/**
 * Created by niejia on 15/3/2.
 */
public class CallDataRelationGraphUnormalized extends RelationGraph {

    public Graph<CodeVertex, CodeEdge> dirGraph;
    protected Graph<CodeVertex, CodeEdge> unDirGraph;

    private Graph<CodeVertex, CodeEdge> prunedGraph;
    private Map<String, Double> dataTypeIDFMap;

    private Map<CodeEdge, Double> callEdgeScoreMap;
    private Map<CodeEdge, Double> dataEdgeScoreMap;

    private List<Double> callEdgeScoreValues;
    private List<Double> dataEdgeScoreValues;
    private List<RelationPair> callRelationPairList;
    private Map<Integer, String> vertexIdNameMap;



    public CallDataRelationGraphUnormalized(RelationInfo relationInfo) {
        super(relationInfo);

        dirGraph = new DirectedSparseGraph<>();
        idCodeVertexMap = new LinkedHashMap<>();
        nameCodeVertexMap = new LinkedHashMap<>();
        callEdgeScoreMap = new HashMap<>();
        dataEdgeScoreMap = new HashMap<>();
        codeVertexIDMap = new HashMap<>();
        this.callRelationPairList = relationInfo.getCallRelationPairList();
        this.vertexIdNameMap = relationInfo.getVertexIdNameMap();
//        this.vertexIdNameMap = relationInfo.v

        for (Integer i : relationInfo.getVertexes().keySet()) {
            CodeVertex cv = new CodeVertex(i, relationInfo.getVertexNameById(i));
            idCodeVertexMap.put(i, cv);
            codeVertexIDMap.put(cv, i);
            nameCodeVertexMap.put(cv.getName(), cv);
            dirGraph.addVertex(cv);
        }

        for (RelationPair callPair : relationInfo.getCallRelationPairList()) {
            Integer id = edgeFactory.create();

            CodeVertex source = idCodeVertexMap.get(callPair.getKey());
            CodeVertex target = idCodeVertexMap.get(callPair.getValue());

            CallEdge callEdge = new CallEdge(id, EdgeType.Call, source, target);
            callEdge.setCallRelationList(relationInfo.getCallRelationListForRelationPair(callPair));

            dirGraph.addEdge(callEdge, source, target);
        }

        unDirGraph = new UndirectedSparseGraph<>();

        for (CodeVertex cv : dirGraph.getVertices()) {
            unDirGraph.addVertex(cv);
        }

        for (RelationPair dataPair : relationInfo.getDataRelationPairList()) {
            Integer id = edgeFactory.create();

            CodeVertex source = idCodeVertexMap.get(dataPair.getKey());
            CodeVertex target = idCodeVertexMap.get(dataPair.getValue());

            DataEdge dataEdge = new DataEdge(id, EdgeType.Data, source, target);
            dataEdge.setDataRelationList(relationInfo.getDataRelationListForRelationPair(dataPair));

            unDirGraph.addEdge(dataEdge, idCodeVertexMap.get(dataPair.getKey()), idCodeVertexMap.get(dataPair.getValue()));
        }

        if (relationInfo.isPruning()) {
            pruning(relationInfo.getCallEdgeScoreThreshold(), relationInfo.getDataEdgeScoreThreshold());
        }
    }

    private void pruning(double callEdgeScoreThreshold, double dataEdgeScoreThreshold) {
        // remove edges contains no dataType above dataTypeFilteThreshold

        double dataTypeFilteThreshold = Setting.idfThreshold;
        this.dataTypeIDFMap = relationInfo.computeDataTypeIDF();
//
        removeDateEdgesContainsNoDataTypeAboveThreshold(dataTypeFilteThreshold);

        removeCallEdgesBelowCallEdgeScoreThreshold(callEdgeScoreThreshold);

//        removeDataEdgesTheyDontLoveEachOther();
//        addUsageEdges();
        removeDataEdgesBelowDataEdgeScoreThreshold(dataEdgeScoreThreshold);

        // only remain the edges(a, b) that all edges of vertex a, the edge score is highest.
        // and all edges of vertex a, the edge score is highest.

        // man loves two women
//        removeDataEdgesTheyDontHaveFeelingsForEachOther();


//        removeDataEdgesNotIncludingHighestScoreVertex();
        createPrunedGraph();
    }

    private void removeDataEdgesNotIncludingHighestScoreVertex() {


    }

    private void removeDataEdgesTheyDontHaveFeelingsForEachOther() {
        Map<CodeEdge, Double> dataEdgeScoreMap = new HashMap<>();
        for (CodeEdge codeEdge : unDirGraph.getEdges()) {
            DataEdge dataEdge = (DataEdge) codeEdge;
            dataEdgeScoreMap.put(dataEdge, computeDataEdgeScore(dataEdge));
        }

        normalize(dataEdgeScoreMap);

        List<CodeEdge> codeEdgesShouldRemain = new ArrayList<>();
        for (CodeVertex codeVertex : unDirGraph.getVertices()) {
            List<CodeEdge> highestTwoEdgesList = getHighestTwoEdgesForCodeVertex(codeVertex, dataEdgeScoreMap);
            for (CodeEdge e : highestTwoEdgesList) {
                codeEdgesShouldRemain.add(e);
            }
        }

        List<CodeEdge> codeEdgesShouldRemove = new ArrayList<>();
        for (CodeEdge dataEdge : dataEdgeScoreMap.keySet()) {
            if (!codeEdgesShouldRemain.contains(dataEdge)) {
                codeEdgesShouldRemove.add(dataEdge);
            }
        }

        for (CodeEdge dataEdge : codeEdgesShouldRemove) {
            unDirGraph.removeEdge(dataEdge);
        }
    }

    private List<CodeEdge> getHighestTwoEdgesForCodeVertex(CodeVertex codeVertex, Map<CodeEdge, Double> dataEdgeScoreMap) {
        List<CodeEdge> highestTwoEdgesList = new ArrayList<>();

        ArrayList<CodeEdge> codeEdgesList = new ArrayList<>(unDirGraph.getInEdges(codeVertex));
        if (codeEdgesList.isEmpty()) return highestTwoEdgesList;

        for (CodeEdge codeEdge : codeEdgesList) {
            codeEdge.setScore(dataEdgeScoreMap.get(codeEdge));
        }

        Collections.sort(codeEdgesList, Collections.reverseOrder());

        if (codeEdgesList.size() >= 1) {
            highestTwoEdgesList.add(codeEdgesList.get(0));
        }

//        if (codeEdgesList.size() >= 2) {
//            highestTwoEdgesList.add(codeEdgesList.get(1));
//        }

        return highestTwoEdgesList;
    }

//    private List<ClosestNeighbour> getFirstTwoClosestNeighbour(CodeVertex codeVertex, Map<CodeEdge, Double> dataEdgeScoreMap) {
//
//        List<ClosestNeighbour> ClosestNeighbourList = new ArrayList<>();
//        ArrayList<CodeEdge> codeEdgesList = new ArrayList<>(unDirGraph.getInEdges(codeVertex));
//        if (codeEdgesList.isEmpty()) return ClosestNeighbourList;
//
//        for (CodeEdge codeEdge : codeEdgesList) {
//            codeEdge.setScore(dataEdgeScoreMap.get(codeEdge));
//        }
//
//        Collections.sort(codeEdgesList, Collections.reverseOrder());
//
//        if (codeEdgesList.get(0)!=null) {
//            CodeVertex theOtherCodeVertex = getTheOtherCodeVertex(codeEdgesList.get(0), codeVertex);
//            ClosestNeighbourList.add(new ClosestNeighbour(theOtherCodeVertex, codeEdgesList.get(0)));
//        }
//
//        if (codeEdgesList.get(1)!=null) {
//            CodeVertex theOtherCodeVertex = getTheOtherCodeVertex(codeEdgesList.get(1), codeVertex);
//            ClosestNeighbourList.add(new ClosestNeighbour(theOtherCodeVertex, codeEdgesList.get(1)));
//        }
//
//        return ClosestNeighbourList;
//    }

    private CodeVertex getTheOtherCodeVertex(CodeEdge codeEdge, CodeVertex codeVertex) {
        CodeVertex cv1 = unDirGraph.getEndpoints(codeEdge).getFirst();
        CodeVertex cv2 = unDirGraph.getEndpoints(codeEdge).getSecond();
        return codeVertex.equals(cv1) ? cv2 : cv1;
    }

    private void addUsageEdges() {
        for (RelationPair usagePair : relationInfo.getUsageRelationPairList()) {
            Integer id = edgeFactory.create();

            CodeVertex source = idCodeVertexMap.get(usagePair.getKey());
            CodeVertex target = idCodeVertexMap.get(usagePair.getValue());

            DataEdge dataEdge = new DataEdge(id, EdgeType.Data, source, target);
            dataEdge.setDataRelationList(relationInfo.getUsageRelationListForRelationPair(usagePair));

            unDirGraph.addEdge(dataEdge, idCodeVertexMap.get(usagePair.getKey()), idCodeVertexMap.get(usagePair.getValue()));
        }
    }

    private void removeDataEdgesTheyDontLoveEachOther() {
        Map<CodeEdge, Double> dataEdgeScoreMap = new HashMap<>();
        for (CodeEdge codeEdge : unDirGraph.getEdges()) {
            DataEdge dataEdge = (DataEdge) codeEdge;
            dataEdgeScoreMap.put(dataEdge, computeDataEdgeScore(dataEdge));
        }

        normalize(dataEdgeScoreMap);

        List<CodeEdge> codeEdgesShouldRemain = new ArrayList<>();
        for (CodeVertex codeVertex : unDirGraph.getVertices()) {
            ClosestNeighbour closestNeighbour = getClosestNeighbour(codeVertex, dataEdgeScoreMap);
            if (closestNeighbour != null) {
                if (codeVertex.equals(getClosestNeighbour(closestNeighbour.codeVertex, dataEdgeScoreMap).codeVertex)) {
                    codeEdgesShouldRemain.add(closestNeighbour.codeEdge);
//                    System.out.println("closestCodeVertexPair " + codeVertex + " " + closestNeighbour.codeVertex);
                }
            }
        }

        List<CodeEdge> codeEdgesShouldRemove = new ArrayList<>();
        for (CodeEdge dataEdge : dataEdgeScoreMap.keySet()) {
            if (!codeEdgesShouldRemain.contains(dataEdge)) {
                codeEdgesShouldRemove.add(dataEdge);
            }
        }

        for (CodeEdge dataEdge : codeEdgesShouldRemove) {
            unDirGraph.removeEdge(dataEdge);
        }
    }

    private ClosestNeighbour getClosestNeighbour(CodeVertex codeVertex, Map<CodeEdge, Double> dataEdgeScoreMap) {
        ArrayList<CodeEdge> codeEdgesList = new ArrayList<>(unDirGraph.getInEdges(codeVertex));
        if (codeEdgesList.isEmpty()) return null;

        CodeEdge highestValueEdge = codeEdgesList.get(0);
        for (CodeEdge edge : codeEdgesList) {
            if (dataEdgeScoreMap.get(edge) > dataEdgeScoreMap.get(highestValueEdge)) {
                highestValueEdge = edge;
            }
        }

//        CodeVertex cv1 = unDirGraph.getEndpoints(highestValueEdge).getFirst();
//        CodeVertex cv2 = unDirGraph.getEndpoints(highestValueEdge).getSecond();
        CodeVertex closestCodeVertex = getTheOtherCodeVertex(highestValueEdge, codeVertex);
        return new ClosestNeighbour(closestCodeVertex, highestValueEdge);
    }

    private void createPrunedGraph() {
        prunedGraph = new UndirectedSparseGraph<>();
        for (CodeVertex vertex : dirGraph.getVertices()) {
            prunedGraph.addVertex(vertex);
        }

        int edgeId = 1;
        for (CodeEdge callEdge : dirGraph.getEdges()) {
            prunedGraph.addEdge(callEdge, callEdge.getSource(), callEdge.getTarget());
            edgeId++;
        }

        // 3.12
        for (CodeEdge dataEdge : unDirGraph.getEdges()) {
            prunedGraph.addEdge(dataEdge, dataEdge.getSource(), dataEdge.getTarget());
            edgeId++;
        }
    }

    private void removeDataEdgesBelowDataEdgeScoreThreshold(double dataEdgeScoreThreshold) {
        dataEdgeScoreMap = new HashMap<>();
        for (CodeEdge codeEdge : unDirGraph.getEdges()) {
            DataEdge dataEdge = (DataEdge) codeEdge;
//            dataEdgeScoreMap.put(dataEdge, computeDataEdgeScore(dataEdge));
            dataEdgeScoreMap.put(dataEdge, computeDataEdgeScoreByIDF(dataEdge));
        }

        normalizeDataEdge(dataEdgeScoreMap);

        analyseDistributed(dataEdgeScoreMap, "data", dataEdgeScoreThreshold);
//        ValuesCountMap(dataEdgeScoreMap);

        for (CodeEdge dataEdge : dataEdgeScoreMap.keySet()) {
            if (dataEdgeScoreMap.get(dataEdge) < dataEdgeScoreThreshold) {
                unDirGraph.removeEdge(dataEdge);
            }
        }
    }

    private void removeCallEdgesBelowCallEdgeScoreThreshold(double callEdgeScoreThreshold) {
        callEdgeScoreMap = new HashMap<>();
        for (CodeEdge codeEdge : dirGraph.getEdges()) {
            CallEdge callEdge = (CallEdge) codeEdge;
            callEdgeScoreMap.put(callEdge, computeCallEdgeScore(callEdge));
        }

        normalizeCallEdge(callEdgeScoreMap);
        analyseDistributed(callEdgeScoreMap, "Call", callEdgeScoreThreshold);


        for (CodeEdge callEdge : callEdgeScoreMap.keySet()) {
            if (callEdgeScoreMap.get(callEdge) < callEdgeScoreThreshold) {
//                if (!isLoopEdge(callEdge)|| !isSingleton(callEdge)) {
//                    dirGraph.removeEdge(callEdge);
//                }
//                if (!isLoopEdge(callEdge)) {
                dirGraph.removeEdge(callEdge);
//                }
            }
        }
    }

    private boolean isLoopEdge(CodeEdge edge) {
        return dirGraph.findEdge(edge.getTarget(), edge.getSource()) != null;
    }

    private boolean isSingleton(CodeEdge edge) {
        return dirGraph.getInEdges(edge.getTarget()) == null;
//                && dirGraph.getOutEdges(edge.getTarget()).size()==0;
    }

    private void analyseDistributed(Map<CodeEdge, Double> codeEdgeScoreMap, String type, double threshold) {
        List<Double> values = new ArrayList<>();
        for (Double v : codeEdgeScoreMap.values()) {
            values.add(v);
        }
        Collections.sort(values, Collections.reverseOrder());

        Map<Double, Integer> valuesCountMap = new LinkedHashMap<>();

        for (Double value : values) {
            if (valuesCountMap.containsKey(value)) {
                valuesCountMap.put(value, valuesCountMap.get(value) + 1);
            } else {
                valuesCountMap.put(value, 1);
            }
        }

        int num = 0;
        int numAboveThreshold = 0;
        int valueNum = 0;
        for (double v : valuesCountMap.keySet()) {
            num += valuesCountMap.get(v);

            if (v > threshold) {
                numAboveThreshold += valuesCountMap.get(v);
                valueNum++;
            }
        }

        // 12.22
//        System.out.println(type + " ValuesCountMap = " + valuesCountMap);
//        System.out.println(" num = " + num);
//        System.out.println(" numAboveThreshold = " + numAboveThreshold);
//        System.out.println("Num Rate " + 1.0 * numAboveThreshold / num);
//        System.out.println(" valueNum = " + valueNum);
//        System.out.println(" valuesCountMap.keySet().size() = " + valuesCountMap.keySet().size());
//        System.out.println("Value Rate " + 1.0 * valueNum / valuesCountMap.keySet().size());
    }

    private void normalizeCallEdge(Map<CodeEdge, Double> codeEdgeScoreMap) {
        callEdgeScoreValues = new ArrayList<>();
        for (Double v : codeEdgeScoreMap.values()) {
            callEdgeScoreValues.add(v);

        }

        Collections.sort(callEdgeScoreValues, Collections.reverseOrder());
// Don't do normalization
//        Double max = callEdgeScoreValues.get(0);
//        Double min = callEdgeScoreValues.get(callEdgeScoreValues.size() - 1);
//
//        for (CodeEdge edge : codeEdgeScoreMap.keySet()) {
//            Double value = codeEdgeScoreMap.get(edge);
//            codeEdgeScoreMap.put(edge, ((value - min) / (max - min)));
//        }
    }

    private void normalizeDataEdge(Map<CodeEdge, Double> codeEdgeScoreMap) {
        dataEdgeScoreValues = new ArrayList<>();
        for (Double v : codeEdgeScoreMap.values()) {
            dataEdgeScoreValues.add(v);

        }

        Collections.sort(dataEdgeScoreValues, Collections.reverseOrder());
// Don't do normalization
//        Double max = dataEdgeScoreValues.get(0);
//        Double min = dataEdgeScoreValues.get(dataEdgeScoreValues.size() - 1);
//
//        for (CodeEdge edge : codeEdgeScoreMap.keySet()) {
//            Double value = codeEdgeScoreMap.get(edge);
//            codeEdgeScoreMap.put(edge, ((value - min) / (max - min)));
//        }
    }


    private void normalize(Map<CodeEdge, Double> codeEdgeScoreMap) {
        List<Double> values = new ArrayList<>();
        for (Double v : codeEdgeScoreMap.values()) {
            values.add(v);

        }
        Collections.sort(values, Collections.reverseOrder());

        Double max = values.get(0);
        Double min = values.get(values.size() - 1);
//        System.out.println(" max = " + max);
//        System.out.println(" min = " + min);

        for (CodeEdge edge : codeEdgeScoreMap.keySet()) {
            Double value = codeEdgeScoreMap.get(edge);
            codeEdgeScoreMap.put(edge, ((value - min) / (max - min)));
        }
    }

    private double computeCallEdgeScore(CallEdge callEdge) {
        int callerOutNum = (dirGraph.getOutEdges(callEdge.getSource())).size();
        int calleeInNum = (dirGraph.getInEdges(callEdge.getTarget())).size();
        //UnDirGraph
//        int callerOutNum = (dirGraph.getNeighborCount(callEdge.getSource()));
//        int calleeInNum = (dirGraph.getNeighborCount(callEdge.getTarget()));
//        return callEdge.getCallRelationSize() * 2.0 / (callerOutNum + calleeInNum);
        return callEdge.getCallRelationSize() * 1.0 / (callerOutNum + calleeInNum);
//        return callEdge.getCallRelationSize() * 1.0 / (callerOutNum + calleeInNum);
    }

    private Double computeDataEdgeScore(DataEdge dataEdge) {
        int callerOutNum = (unDirGraph.getOutEdges(dataEdge.getSource())).size();
        int calleeInNum = (unDirGraph.getInEdges(dataEdge.getTarget())).size();
//        return 1.0 / (callerOutNum + calleeInNum);
        return dataEdge.getDataRelationSizeByUniqueType() * 1.0 / (callerOutNum + calleeInNum);
//        return dataEdge.getDataRelationSize() * 1.0 / (callerOutNum + calleeInNum);
    }

    private Double computeDataEdgeScoreByIDF(DataEdge dataEdge) {
        CodeVertex source = dataEdge.getSource();
        CodeVertex target = dataEdge.getTarget();
        List<String> typesForSource = getTypesInvolvedInCodeVertex(source);
        List<String> typesForTarget = getTypesInvolvedInCodeVertex(target);
        List<String> typesInEdge = getTypesInEdge(dataEdge);

        List<String> commonTypeList = new ArrayList<>();
        for (String s : typesForSource) {
            if (!commonTypeList.contains(s)) {
                commonTypeList.add(s);
            }
        }

        for (String s : typesForTarget) {
            if (!commonTypeList.contains(s)) {
                commonTypeList.add(s);
            }
        }

        double idfSum = 0.0;
        for (String type : typesInEdge) {
            if (dataTypeIDFMap.containsKey(type)) {
                idfSum += dataTypeIDFMap.get(type);
            }
        }

        double score = idfSum * typesInEdge.size() * 1.0 / commonTypeList.size();
//
//        double score = typesInEdge.size() * 1.0 / commonTypeList.size();

//        System.out.println("Edge: " + source.getName() + "_" + target.getName());
//        System.out.println(" typesInEdge.size() = " + typesInEdge.size());
//        System.out.println("typesInEdge: " + typesInEdge);
//        System.out.println(" commonTypeList.size() = " + commonTypeList.size() );
//        System.out.println(" commonTypeList = " + commonTypeList );
//        System.out.println(" typesForSource = " + typesForSource.size());
//        System.out.println(" typesForSource = " + typesForSource );
//        System.out.println(" typesForTarget = " + typesForTarget.size());
//        System.out.println(" typesForTarget = " + typesForTarget );
//        System.out.println(" idfSum = " + idfSum );
//        System.out.println(" score = " + score );
        return score;
    }

    private List<String> getTypesInvolvedInCodeVertex(CodeVertex vertex) {
        Collection<CodeEdge> edges = unDirGraph.getOutEdges(vertex);
        List<String> typesList = new ArrayList<>();
        for (CodeEdge e : edges) {
            for (String type : getTypesInEdge(e)) {
                if (!typesList.contains(type) && dataTypeIDFMap.containsKey(type)) {
                    typesList.add(type);
                }
            }
        }
        return typesList;
    }

    private List<String> getTypesInEdge(CodeEdge e) {
        List<String> types = new ArrayList<>();
        for (DataRelation dr : ((DataEdge) e).getDataRelationList()) {
            if (!types.contains(dr.getType())) {
                types.add(dr.getType());
            }
        }
        return types;
    }

    private void removeDateEdgesContainsNoDataTypeAboveThreshold(double dataTypeFilteThreshold) {
//        for (String s : dataTypeIDFMap.keySet()) {
//            System.out.println(s + " " + dataTypeIDFMap.get(s));
//        }

//        parserIDF(dataTypeIDFMap);
        List<String> dataTypesAboveThreshold = FilteDataTypeByIDF(dataTypeIDFMap, dataTypeFilteThreshold);


        Set<String> dataTypes = dataTypeIDFMap.keySet();
        Set<String> dataTypesToRemove = new HashSet<>();

        for (String type : dataTypes) {
            if (!dataTypesAboveThreshold.contains(type)) {
                dataTypesToRemove.add(type);
            }
        }

        for (String type : dataTypesToRemove) {
            dataTypeIDFMap.remove(type);
        }


        List<CodeEdge> dataEdgesBelowThreshold = new ArrayList<>();
        for (CodeEdge dataEdge : unDirGraph.getEdges()) {
            if (!dataEdgeContainsDataTypeAboveThreshold((DataEdge) dataEdge, dataTypesAboveThreshold)) {
                dataEdgesBelowThreshold.add(dataEdge);
            }
        }


        for (CodeEdge codeEdge : dataEdgesBelowThreshold) {
            unDirGraph.removeEdge(codeEdge);
        }
    }

    private boolean dataEdgeContainsDataTypeAboveThreshold(DataEdge dataEdge, List<String> dataTypesAboveThreshold) {
        for (DataRelation dr : dataEdge.getDataRelationList()) {
            if (dataTypesAboveThreshold.contains(dr.getType())) {
                return true;
            }
        }
        return false;
    }

    private List<String> FilteDataTypeByIDF(Map<String, Double> dataTypeIDFMap, double dataTypeFilteThreshold) {
        List<String> dataTypesAboveThreshold = new ArrayList<>();
        for (String type : dataTypeIDFMap.keySet()) {
            if (dataTypeIDFMap.get(type) >= dataTypeFilteThreshold) {
                dataTypesAboveThreshold.add(type);
            }
        }
        return dataTypesAboveThreshold;
    }

    private void parserIDF(Map<String, Double> dataTypeIDF) {

        for (String s : dataTypeIDF.keySet()) {
            System.out.println(s);
        }
        List<Double> values = new ArrayList<>();
        for (Double v : dataTypeIDF.values()) {
            values.add(v);
        }
        Collections.sort(values, Collections.reverseOrder());

        Map<Double, Integer> valuesCountMap = new LinkedHashMap<>();

        for (Double value : values) {
            if (valuesCountMap.containsKey(value)) {
                valuesCountMap.put(value, valuesCountMap.get(value) + 1);
            } else {
                valuesCountMap.put(value, 1);
            }
        }

        for (Double d : valuesCountMap.keySet()) {
            System.out.println(d + " " + valuesCountMap.get(d));
        }

        System.out.println(" Data type IDF Distributed = " + valuesCountMap);
    }

    @Override
    public int getVertexesNum() {
        return dirGraph.getVertexCount();
    }

    @Override
    public Map<Integer, CodeVertex> getVertexes() {
        return idCodeVertexMap;
    }

    @Override
    public List<CodeVertex> getNeighbours(String vertexName) {
        return null;
    }

    @Override
    public int getNeighboursNum(String vertexName) {
        return 0;
    }

    @Override
    public Collection<CodeEdge> getEdges(String vertexName) {
        List<CodeEdge> edges = new ArrayList<>();
        for (CodeEdge e : getInEdges(vertexName)) {
            edges.add(e);
        }

        for (CodeEdge e : getOutEdges(vertexName)) {
            edges.add(e);
        }

        for (CodeEdge e : unDirGraph.getOutEdges(nameCodeVertexMap.get(vertexName))) {
            edges.add(e);
        }

        return edges;
    }

    public List<CodeEdge> getInEdges(String vertexName) {
        List<CodeEdge> classEdgeList = new ArrayList<>();
        for (CodeEdge e : dirGraph.getInEdges(nameCodeVertexMap.get(vertexName))) {
            classEdgeList.add(e);
        }

        return classEdgeList;
    }

    public List<CodeEdge> getOutEdges(String vertexName) {
        List<CodeEdge> classEdgeList = new ArrayList<>();
        for (CodeEdge e : dirGraph.getOutEdges(nameCodeVertexMap.get(vertexName))) {
            classEdgeList.add(e);
        }
        return classEdgeList;
    }

    @Override
    public List<CodeEdge> getCallEdges() {
        List callEdges = new ArrayList();
        for (CodeEdge edge : dirGraph.getEdges()) {
            callEdges.add(edge);
        }
        return callEdges;
    }

    @Override
    public List<CodeEdge> getDataEdges() {
        List dataEdges = new ArrayList();
        for (CodeEdge edge : unDirGraph.getEdges()) {
            dataEdges.add(edge);
        }
        return dataEdges;
    }

    public List<CodeVertex> getNeighboursByCall(String vertexName) {
        List<CodeVertex> vertexesList = new ArrayList<>();
        CodeVertex vertex = nameCodeVertexMap.get(vertexName);

        for (CodeVertex v : getFathersByCall(vertex.getName())) {
            vertexesList.add(v);
        }

        for (CodeVertex v : getChildrenByCall(vertex.getName())) {
            vertexesList.add(v);
        }
        return vertexesList;
    }


    public List<CodeVertex> getChildrenByCall(String vertexName) {
        List<CodeVertex> vertexesList = new ArrayList<>();
        CodeVertex vertex = nameCodeVertexMap.get(vertexName);
        for (CodeEdge edge : dirGraph.getOutEdges(vertex)) {
            vertexesList.add(dirGraph.getDest(edge));
        }
        return vertexesList;
    }

    private List<CodeVertex> getFathersByCall(String vertexName) {
        List<CodeVertex> vertexesList = new ArrayList<>();
        CodeVertex vertex = nameCodeVertexMap.get(vertexName);

        // attention !!
        if (dirGraph.getInEdges(vertex) == null) {
            return vertexesList;
        }

        for (CodeEdge edge : dirGraph.getInEdges(vertex)) {
            vertexesList.add(dirGraph.getEndpoints(edge).getFirst());
        }
        return vertexesList;
    }

    public void searhNeighbourConnectedGraphByCall(CodeVertex codeVertex, List<CodeVertex> connectedVertexes) {
        searhGraphVertexBelongsTo(codeVertex, connectedVertexes, dirGraph);
        connectedVertexes.remove(codeVertex);
    }

    public void searhNeighbourConnectedGraphByData(CodeVertex codeVertex, List<CodeVertex> connectedVertexes) {
        searhGraphVertexBelongsTo(codeVertex, connectedVertexes, unDirGraph);
        connectedVertexes.remove(codeVertex);
    }

    public void searhNeighbourConnectedGraphByPruning(CodeVertex codeVertex, List<CodeVertex> connectedVertexes) {
        searhGraphVertexBelongsTo(codeVertex, connectedVertexes, prunedGraph);
        connectedVertexes.remove(codeVertex);
    }

    private void searhGraphVertexBelongsTo(CodeVertex codeVertex, List<CodeVertex> connectedVertexes, Graph graph) {
        if (!connectedVertexes.contains(codeVertex)) {
            connectedVertexes.add(codeVertex);
        }

        if (graph.getNeighbors(codeVertex) == null) {
            System.out.println(codeVertex.getName());
        }
        List<CodeVertex> nbs = new ArrayList<>(graph.getNeighbors(codeVertex));
        if (nbs.size() == 0) return;

        for (CodeVertex n : nbs) {
            if (!connectedVertexes.contains(n)) {
                searhGraphVertexBelongsTo(n, connectedVertexes, graph);
            }
        }
    }

    public void searhNeighbourVertexByPruning(CodeVertex codeVertex, List<CodeVertex> connectedVertexes) {
        searhNeighbourVertex(codeVertex, connectedVertexes, prunedGraph);
    }

    public void searhNeighbourVertexByCall(CodeVertex codeVertex, List<CodeVertex> connectedVertexes) {
        searhNeighbourVertex(codeVertex, connectedVertexes, dirGraph);
    }

    public void searhNeighbourVertexByData(CodeVertex codeVertex, List<CodeVertex> connectedVertexes) {
        searhNeighbourVertex(codeVertex, connectedVertexes, unDirGraph);
    }

    private void searhNeighbourVertex(CodeVertex codeVertex, List<CodeVertex> connectedVertexes, Graph graph) {
        if (graph.getNeighbors(codeVertex) == null) {
            System.out.println(codeVertex.getName());
        }

        List<CodeVertex> nbs = new ArrayList<>(graph.getNeighbors(codeVertex));
        if (nbs.size() == 0) return;

        for (CodeVertex n : nbs) {
            if (!connectedVertexes.contains(n)) {
                connectedVertexes.add(n);
            }
        }
    }


    public List<CodeVertex> getNeighboursByData(String vertexName) {
        List<CodeVertex> vertexesList = new ArrayList<>();
        CodeVertex vertex = nameCodeVertexMap.get(vertexName);

        if (unDirGraph.getInEdges(vertex) == null) {
            return vertexesList;
        }

        for (CodeEdge edge : unDirGraph.getInEdges(vertex)) {
            CodeVertex first = unDirGraph.getEndpoints(edge).getFirst();
            CodeVertex second = unDirGraph.getEndpoints(edge).getSecond();
            if (first.equals(vertex)) {
                vertexesList.add(second);
            } else {
                vertexesList.add(first);
            }
        }
        return vertexesList;
    }

    public List<CodeVertex> getStartVertexes() {
        List<CodeVertex> starts = new ArrayList<>();
        for (CodeVertex v : getVertexes().values()) {
            if (dirGraph.getInEdges(v).size() <= 0) {
                starts.add(v);
            }
        }
        return starts;
    }

    public List<CodeVertex> getEndVertexes() {
        List<CodeVertex> ends = new ArrayList<>();
        for (CodeVertex v : getVertexes().values()) {
            if (dirGraph.getOutEdges(v).size() <= 0) {
                ends.add(v);
            }
        }
        return ends;
    }

//    public double getDataEdgeBonusII(String source, String target) {
//
//
//        double sum = 0.0;
//
//        CodeVertex sourceVertex = nameCodeVertexMap.get(source);
//        CodeVertex targetVertex = nameCodeVertexMap.get(target);
//
//        if (unDirGraph.findEdge(sourceVertex, targetVertex) == null) return 0.0;
//
//
//        for (CodeEdge edge : unDirGraph.getInEdges(sourceVertex)) {
//            sum += computeDataBonus(dataEdgeScoreMap.get(edge));
//        }
//
//        CodeEdge edge = unDirGraph.findEdge(sourceVertex, targetVertex);
//
//        return 1.0 * computeDataBonus(dataEdgeScoreMap.get(edge)) / sum;
//    }

    public double getDataEdgeBonus_involveInitialRegion(String source, String target, List<String> firstCut) {
        double sum = 0.0;
        double sumInFirstCut = 0.0;
        CodeVertex sourceVertex = nameCodeVertexMap.get(source);
        CodeVertex targetVertex = nameCodeVertexMap.get(target);

        if (unDirGraph.findEdge(sourceVertex, targetVertex) == null) return 0.0;

        for (CodeEdge edge : unDirGraph.getInEdges(sourceVertex)) {
            String edge_source = edge.getSource().getName();
            String edge_target = edge.getTarget().getName();
            String nb = edge_source.equals(source) ? edge_target : edge_source;

            if (firstCut.contains(nb)) {
                sumInFirstCut += dataEdgeScoreMap.get(edge);
            }
        }


        for (CodeEdge edge : unDirGraph.getInEdges(sourceVertex)) {
            sum += dataEdgeScoreMap.get(edge);
        }

        CodeEdge edge = unDirGraph.findEdge(sourceVertex, targetVertex);


//        return 0.0;
//        return 1.0 * sumInFirstCut / sum/5;
//        System.out.println(" dataEdgeScoreMap.get(edge) = " + dataEdgeScoreMap.get(edge) );
//        System.out.println(" sum = " + sum );
//        System.out.println(1.0 * dataEdgeScoreMap.get(edge) / sum / 5);
        return 1.0 * dataEdgeScoreMap.get(edge) / sum;
    }

    public double getDataEdgeBonusCloseness(String source, String target) {

        CodeVertex sourceVertex = nameCodeVertexMap.get(source);
        CodeVertex targetVertex = nameCodeVertexMap.get(target);

        if (unDirGraph.findEdge(sourceVertex, targetVertex) == null) return 0.0;

        CodeEdge edge = unDirGraph.findEdge(sourceVertex, targetVertex);
        double edgeScore = dataEdgeScoreMap.get(edge);

        return edgeScore;
    }


    public double getDataEdgeBonusBothVertex(String source, String target) {



        double sum = 0.0;

        CodeVertex sourceVertex = nameCodeVertexMap.get(source);
        CodeVertex targetVertex = nameCodeVertexMap.get(target);

        if (unDirGraph.findEdge(sourceVertex, targetVertex) == null) return 0.0;

        for (CodeEdge edge : unDirGraph.getInEdges(sourceVertex)) {
            sum += dataEdgeScoreMap.get(edge);
        }

        for (CodeEdge edge : unDirGraph.getInEdges(targetVertex)) {
            sum += dataEdgeScoreMap.get(edge);
        }

        CodeEdge edge = unDirGraph.findEdge(sourceVertex, targetVertex);

        return 2.0 * dataEdgeScoreMap.get(edge) / sum;
    }

    public double getDataEdgeBonusII(String source, String target) {


        double sum = 0.0;

        CodeVertex sourceVertex = nameCodeVertexMap.get(source);
        CodeVertex targetVertex = nameCodeVertexMap.get(target);

        if (unDirGraph.findEdge(sourceVertex, targetVertex) == null) return 0.0;


        for (CodeEdge edge : unDirGraph.getInEdges(sourceVertex)) {
            sum += dataEdgeScoreMap.get(edge);
        }

        CodeEdge edge = unDirGraph.findEdge(sourceVertex, targetVertex);

        return 1.0 * dataEdgeScoreMap.get(edge) / sum;
    }

//    public double getCallEdgeBonus(String source, String target) {
//
//        double sum = 0.0;
//
//        CodeVertex sourceVertex = nameCodeVertexMap.get(source);
//        CodeVertex targetVertex = nameCodeVertexMap.get(target);
//
//        for (CodeEdge edge : dirGraph.getInEdges(sourceVertex)) {
//            sum += computeCallBonus(callEdgeScoreMap.get(edge));
//        }
//
//        for (CodeEdge edge : dirGraph.getOutEdges(sourceVertex)) {
//            sum += computeCallBonus(callEdgeScoreMap.get(edge));
//        }
//
//        double v = 0.0;
//
//        CodeEdge edge1 = dirGraph.findEdge(sourceVertex, targetVertex);
//        if (edge1 != null) {
//            v += computeCallBonus(callEdgeScoreMap.get(edge1));
//        }
//
//        CodeEdge edge2 = dirGraph.findEdge(targetVertex, sourceVertex);
//        if (edge2 != null) {
//            v += computeCallBonus(callEdgeScoreMap.get(edge2));
//        }
//
//
//        return 1.0 * v / sum;
//    }

    public double getCallEdgeBonus(String source, String target) {

        double sum = 0.0;

        CodeVertex sourceVertex = nameCodeVertexMap.get(source);
        CodeVertex targetVertex = nameCodeVertexMap.get(target);

        for (CodeEdge edge : dirGraph.getInEdges(sourceVertex)) {
            sum += callEdgeScoreMap.get(edge);
        }

        for (CodeEdge edge : dirGraph.getOutEdges(sourceVertex)) {
            sum += callEdgeScoreMap.get(edge);
        }

        double v = 0.0;

        CodeEdge edge1 = dirGraph.findEdge(sourceVertex, targetVertex);
        if (edge1 != null) {
            v += callEdgeScoreMap.get(edge1);
        }

        CodeEdge edge2 = dirGraph.findEdge(targetVertex, sourceVertex);
        if (edge2 != null) {
            v += callEdgeScoreMap.get(edge2);
        }


        return 1.0 * v / sum;
    }

    public double getCallEdgeBonusBothVertexSum(String source, String target) {

        double sum = 0.0;

        CodeVertex sourceVertex = nameCodeVertexMap.get(source);
        CodeVertex targetVertex = nameCodeVertexMap.get(target);

        for (CodeEdge edge : dirGraph.getInEdges(sourceVertex)) {
            sum += callEdgeScoreMap.get(edge);
        }

        for (CodeEdge edge : dirGraph.getOutEdges(sourceVertex)) {
            sum += callEdgeScoreMap.get(edge);
        }

        for (CodeEdge edge : dirGraph.getInEdges(targetVertex)) {
            sum += callEdgeScoreMap.get(edge);
        }

        for (CodeEdge edge : dirGraph.getOutEdges(targetVertex)) {
            sum += callEdgeScoreMap.get(edge);
        }


        double v = 0.0;

        CodeEdge edge1 = dirGraph.findEdge(sourceVertex, targetVertex);
        if (edge1 != null) {
            v += callEdgeScoreMap.get(edge1);
        }

        CodeEdge edge2 = dirGraph.findEdge(targetVertex, sourceVertex);
        if (edge2 != null) {
            v += callEdgeScoreMap.get(edge2);
        }

        return 2.0 * v / sum;
    }

    private double computeDataBonus(Double score) {
        int rank = dataEdgeScoreValues.size();

        for (double i : dataEdgeScoreValues) {
            if (score < i) {
                rank--;
            }
        }
        return 1.0 * rank / dataEdgeScoreValues.size();
    }

//    private double computeDataBonus(Double score) {
//
//        Set<Double> unionValues = new LinkedHashSet<>();
//        for (double i : dataEdgeScoreValues) {
//            unionValues.add(i);
//        }
//
//        int rank = unionValues.size();
//
//        for (double i : unionValues) {
//            if (score < i) {
//                rank--;
//            }
//        }
//        return 1.0 * rank /unionValues.size();
//    }

//    private double computeCallBonus(Double score) {
//
//        Set<Double> unionValues = new LinkedHashSet<>();
//        for (double i : callEdgeScoreValues) {
//            unionValues.add(i);
//        }
//
//        int rank = unionValues.size();
//
//        for (double i : unionValues) {
//            if (score < i) {
//                rank--;
//            }
//        }
//
////        callEdgeScoreValues
//        return 1.0 * rank /unionValues.size();
//    }

//    private double computeCallBonus(Double score, String vertex) {
//
//    }


    private double computeCallBonus(Double score) {

        int rank = callEdgeScoreValues.size();

        for (double i : callEdgeScoreValues) {
            if (score < i) {
                rank--;
            }
        }

        return 1.0 * rank / callEdgeScoreValues.size();
    }

    public double computeCallBonus(String source, String targetINCutGraph, List<String> cutGraph) {
        LinkedList<LinkedList<CodeVertex>> allPath = new LinkedList<>();
        LinkedList<CodeVertex> visited = new LinkedList();
        CodeVertex start = getCodeVertexByName(source);
        CodeVertex end = getCodeVertexByName(targetINCutGraph);
        visited.add(start);

        new PathSearch().breadthFirst(dirGraph, visited, end, allPath);

        TreeSet<Double> bonusSet = new TreeSet<>();

        for (LinkedList<CodeVertex> v : allPath) {
//
            if (connectToEdge(v, cutGraph)) {
                bonusSet.add(computePathBonus(v));
//                System.out.println("Path");
//                for (CodeVertex cv : v) {
//                    System.out.print(cv + " ");
//                }
//                System.out.println();
            }

        }

        return bonusSet.size() != 0 ? bonusSet.last() : 0.0;
    }

    // 如果路径内有两个以上的点在第一刀的子图中，返回 False
    private boolean connectToEdge(LinkedList<CodeVertex> v, List<String> cutGraph) {
        int num = 0;
        for (CodeVertex cv : v) {
            if (cutGraph.contains(cv.getName())) {
                num++;
            }
        }
//        System.out.println(" num = " + num );
        return num == 1 ? true : false;
    }

    public double computeCallBonus(String source, String targetINCutGraph) {
        LinkedList<LinkedList<CodeVertex>> allPath = new LinkedList<>();
        LinkedList<CodeVertex> visited = new LinkedList();
        CodeVertex start = getCodeVertexByName(source);
        CodeVertex end = getCodeVertexByName(targetINCutGraph);
        visited.add(start);

        new PathSearch().breadthFirst(dirGraph, visited, end, allPath);

        TreeSet<Double> bonusSet = new TreeSet<>();

        for (LinkedList<CodeVertex> v : allPath) {
//            for (CodeVertex cv : v) {
//                System.out.print(cv + " ");
//            }
//            System.out.println();
            bonusSet.add(computePathBonus(v));
//            System.out.println(" computePathBonus(v) = " + computePathBonus(v) );
        }

        return bonusSet.size() != 0 ? bonusSet.last() : 0.0;
    }

//    private double computePathBonus(LinkedList<CodeVertex> path) {
//        if (path == null) throw new NoSuchElementException("No Path");
//        if (path.size() == 1) return 0;
//
//        double value = 1.0;
////        double sum = 0.0;
//
//        for (int i = 0; i < path.size() - 1; i++) {
////            double temp = getCallEdgeBonus(path.get(i).getName(), path.get(i + 1).getName());
//            double temp = getCallEdgeBonus(path.get(i + 1).getName(), path.get(i).getName());
//            value *= (1.0 / (1 + Math.pow((i + 1), 2)) + temp * (1.0 - 1.0 / (1 + Math.pow((i + 1), 2))));
////            value *= (1.0 / Math.pow((i + 2), 2) + temp * (1.0 - 1.0 / Math.pow((i + 2), 2)));
////            value *= (0.5 + temp * 0.5);
//        }
//
////        return 1.0 * value / sum /(path.size() - 1);
////        return 1.0 * value / sum / 2;
//        return value * 1.0 /(path.size() - 1);
////        return value * 1.0;
//    }

    // times / sum + subsection
//    private double computePathBonus(LinkedList<CodeVertex> path) {
//        if (path == null) throw new NoSuchElementException("No Path");
//        if (path.size() == 1) return 0;
//        if (path.size() == 2) {
//            double value = getCallEdgeBonusBothVertexSum(path.get(1).getName(), path.get(0).getName());
////            return 0.5 + 0.5 * value;
//            return value;
//        }
//
//        double value = 1.0;
//        double sum = 0.0;
//
//        for (int i = 0; i < path.size() - 1; i++) {
//            value *= getCallEdgeBonusBothVertexSum(path.get(i + 1).getName(), path.get(i).getName());
////            double temp = getCallEdgeBonusBothVertexSum(path.get(i + 1).getName(), path.get(i).getName());
////            value *= getCallEdgeBonus(path.get(i + 1).getName(), path.get(i).getName());
////            value *= (0.5 + temp * 0.5);
////            sum += getCallEdgeBonus(path.get(i + 1).getName(), path.get(i).getName());
//        }
//
////        return 1.0 * value / sum /(path.size() - 1);
////        return 1.0 * value / sum / 2;
////        return value * 1.0 / (path.size() - 1);
//        return value;
//    }

//    times / sum
//    private double computePathBonus(LinkedList<CodeVertex> path) {
//        if (path == null) throw new NoSuchElementException("No Path");
//        if (path.size() == 1) return 0;
//        double value = 1.0;
//        double sum = 0.0;
//
//        for (int i = 0; i < path.size() - 1; i++) {
//            value *= getCallEdgeBonus(path.get(i).getName(),path.get(i + 1).getName());
//            sum += getCallEdgeBonus(path.get(i).getName(), path.get(i + 1).getName());
////            value *= getCallEdgeBonus(path.get(i + 1).getName(), path.get(i).getName());
////            sum += getCallEdgeBonus(path.get(i + 1).getName(), path.get(i).getName());
//        }
//
////        return 0.0;
//        return 1.0 * value / sum;
//    }

    // pow2 sum / number
//    private double computePathBonus(LinkedList<CodeVertex> path) {
//        if (path == null) throw new NoSuchElementException("No Path");
//        if (path.size() == 1) return 0;
//        double value = 1.0;
//
//        for (int i = 0; i < path.size() - 1; i++) {
//            value += Math.pow(getCallEdgeBonus(path.get(i + 1).getName(), path.get(i).getName()), 2);
//        }
//
//        return 1.0 * Math.sqrt(value) / (path.size() - 1);
//    }


    // Harmonic Mean
//    private double computePathBonus(LinkedList<CodeVertex> path) {
//        if (path == null) throw new NoSuchElementException("No Path");
//        if (path.size() == 1) return 0;
//        double sum = 0.0;
//        for (int i = 0; i < path.size() - 1; i++) {
//            sum += (1.0 / getCallEdgeBonusBothVertexSum(path.get(i).getName(), path.get(i + 1).getName()));
//        }
//
//        int n = path.size() - 1;
//        return 1.0 / sum / n;
//    }

//    original
    private double computePathBonus(LinkedList<CodeVertex> path) {
        if (path == null) throw new NoSuchElementException("No Path");
        if (path.size() == 1) return 0;
        double value = 1.0;

        for (int i = 0; i < path.size() - 1; i++) {

//
// value *= getCallEdgeBonus(path.get(i).getName(), path.get(i+1).getName());
//            double temp = getCallEdgeBonusBothVertexSum(path.get(i).getName(), path.get(i + 1).getName());
            double temp = getCallEdgeBonusCloseness(path.get(i).getName(), path.get(i + 1).getName());
//
            if (temp > 1.0) {
//                System.out.println("zzz " + temp);
//                throw new IllegalArgumentException("");
            }
//
//  double alph = 0.1;
//            value *= (alph + (1.0 - alph) * temp);
            value *= temp;
//            value *= getCallEdgeBonusBothVertexSum(path.get(i).getName(), path.get(i + 1).getName());
        }
        return value;
    }

    public double getCallEdgeBonusCloseness(String source, String target) {

        CodeVertex sourceVertex = nameCodeVertexMap.get(source);
        CodeVertex targetVertex = nameCodeVertexMap.get(target);

        double v = 0.0;

        CodeEdge edge1 = dirGraph.findEdge(sourceVertex, targetVertex);
        if (edge1 != null) {
            v += callEdgeScoreMap.get(edge1);
        }

        CodeEdge edge2 = dirGraph.findEdge(targetVertex, sourceVertex);
        if (edge2 != null) {
            v += callEdgeScoreMap.get(edge2);
        }

        return v;
    }

    private CodeEdge getOutCallerEdgeForCallerCallee(CodeVertex caller, CodeVertex callee) {
        Collection<CodeEdge> edges = dirGraph.getOutEdges(caller);
        for (CodeEdge edge : edges) {
            if (edge.getTarget().getName().equals(callee.getName())) {
                return edge;
            }
        }
        _.abort("Found no such edge");
        return null;
    }

    public Map<String, Double> getPageRank() {
        Graph<Integer, Integer> prGraph = new DirectedSparseGraph();
//
        for (int i : vertexIdNameMap.keySet()) {
            prGraph.addVertex(i);
        }

        Map<Integer, Double> outCount = new HashMap<>();
        for (int i = 0; i < callRelationPairList.size(); i++) {
            RelationPair pair = callRelationPairList.get(i);
            Integer callerID = pair.getKey();
            Integer calleeID = pair.getValue();
            String callerName = vertexIdNameMap.get(callerID);
            String calleeName = vertexIdNameMap.get(calleeID);
            CodeEdge edge = getOutCallerEdgeForCallerCallee(nameCodeVertexMap.get(callerName), nameCodeVertexMap.get(calleeName));

            double edgeScore = computeCallEdgeScore((CallEdge) edge);

            if (!outCount.containsKey(pair.getKey())) {
                outCount.put(pair.getKey(), edgeScore);
            } else {
                outCount.put(pair.getKey(), outCount.get(pair.getKey()) + edgeScore);
            }
        }

        Map<Integer, Double> edgeWeights = new HashMap<>();
        int edgeN = 1;
        for (int i = 0; i < callRelationPairList.size(); i++) {
            RelationPair pair = callRelationPairList.get(i);
            prGraph.addEdge(edgeN, pair.getKey(), pair.getValue());

            Integer callerID = pair.getKey();
            Integer calleeID = pair.getValue();
            String callerName = vertexIdNameMap.get(callerID);
            String calleeName = vertexIdNameMap.get(calleeID);

            CodeEdge edge = getOutCallerEdgeForCallerCallee(nameCodeVertexMap.get(callerName), nameCodeVertexMap.get(calleeName));
            double edgeScore = computeCallEdgeScore((CallEdge) edge);

            edgeWeights.put(edgeN, 1.0 * edgeScore / outCount.get(pair.getKey()));
            edgeN++;
        }

        PageRankWithPriors<Integer, Integer> pr = new PageRank<Integer, Integer>(prGraph, MapTransformer.getInstance(edgeWeights), 0);
        pr.evaluate();

        Map<String, Double> vertexWeights = new HashMap<>();

        for (Integer v : prGraph.getVertices()) {
            vertexWeights.put(vertexIdNameMap.get(v), pr.getVertexScore(v));
        }
        return vertexWeights;

    }
}
