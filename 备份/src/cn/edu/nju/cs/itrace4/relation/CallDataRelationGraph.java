package cn.edu.nju.cs.itrace4.relation;

import cn.edu.nju.cs.itrace4.core.algo.icse.MapUtil;
import cn.edu.nju.cs.itrace4.core.algo.legacy.PathSearch;
import cn.edu.nju.cs.itrace4.core.document.LinksList;
import cn.edu.nju.cs.itrace4.io._;
import cn.edu.nju.cs.itrace4.relation.graph.*;
import cn.edu.nju.cs.itrace4.relation.info.CallRelation;
import cn.edu.nju.cs.itrace4.relation.info.DataRelation;
import cn.edu.nju.cs.itrace4.relation.info.DataRelationList;
import cn.edu.nju.cs.itrace4.relation.info.RelationPair;
import cn.edu.nju.cs.itrace4.util.PauTa;
import cn.edu.nju.cs.itrace4.util.Setting;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import javafx.util.Pair;

import java.util.*;

/**
 * Created by niejia on 15/3/2.
 */
public class CallDataRelationGraph extends RelationGraph {

    public Graph<CodeVertex, CodeEdge> dirGraph;
    protected Graph<CodeVertex, CodeEdge> unDirGraph;

    private Graph<CodeVertex, CodeEdge> prunedGraph;
    private Map<String, Double> dataTypeIDFMap;

    public Map<CodeEdge, Double> callEdgeScoreMap;
    public Map<CodeEdge, Double> dataEdgeScoreMap;

    public Map<String, Double> callEdgeScoreMapByString;
    public Map<String, Double> dataEdgeScoreMapByString;

    private List<Double> callEdgeScoreValues;
    private List<Double> dataEdgeScoreValues;

    private CallDataRelationGraph allDependenciesGraph;

    public boolean useNormalize = true;

    public CallDataRelationGraph(RelationInfo relationInfo) {
         super(relationInfo);

        dirGraph = new DirectedSparseGraph<>();
        idCodeVertexMap = new LinkedHashMap<>();
        nameCodeVertexMap = new LinkedHashMap<>();
        callEdgeScoreMap = new HashMap<>();
        dataEdgeScoreMap = new HashMap<>();
        
        for (Integer i : relationInfo.getVertexes().keySet()) {
            CodeVertex cv = new CodeVertex(i, relationInfo.getVertexNameById(i));
            idCodeVertexMap.put(i, cv);
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

        System.out.println("");
    }

    public CallDataRelationGraph(RelationInfo relationInfo, boolean useNormalize) {
        super(relationInfo);

        dirGraph = new DirectedSparseGraph<>();
        idCodeVertexMap = new LinkedHashMap<>();
        nameCodeVertexMap = new LinkedHashMap<>();
        callEdgeScoreMap = new HashMap<>();
        dataEdgeScoreMap = new HashMap<>();

        for (Integer i : relationInfo.getVertexes().keySet()) {
            CodeVertex cv = new CodeVertex(i, relationInfo.getVertexNameById(i));
            idCodeVertexMap.put(i, cv);
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

        this.useNormalize = useNormalize;

        if (relationInfo.isPruning()) {
            pruning(relationInfo.getCallEdgeScoreThreshold(), relationInfo.getDataEdgeScoreThreshold());
        }
        System.out.println("");
    }

    private void pruning(double callEdgeScoreThreshold, double dataEdgeScoreThreshold) {
        // remove edges contains no dataType above dataTypeFilteThreshold
        double dataTypeFilteThreshold = Setting.idfThreshold;
        if (allDependenciesGraph == null) {
            this.dataTypeIDFMap = relationInfo.computeDataTypeIDF();
        } else {
            this.dataTypeIDFMap = allDependenciesGraph.relationInfo.computeDataTypeIDF();
        }
        Map<String, Double> sortedDataTypeIDFMap = MapUtil.sortByValue(dataTypeIDFMap);

        List<Double> sortList = new ArrayList<>();
        for (String s : dataTypeIDFMap.keySet()) {
            sortList.add(dataTypeIDFMap.get(s));
        }

        Collections.sort(sortList);
        removeDateEdgesContainsNoDataTypeAboveThreshold(dataTypeFilteThreshold);

        int orginCallNum = dirGraph.getEdgeCount();
        removeCallEdgesBelowCallEdgeScoreThreshold(callEdgeScoreThreshold);

        int afterCallNum = dirGraph.getEdgeCount();

        int originDataNum = unDirGraph.getEdgeCount();
        removeDataEdgesBelowDataEdgeScoreThreshold(dataEdgeScoreThreshold);

        int afterDataNum = unDirGraph.getEdgeCount();

        createPrunedGraph();

        callEdgeScoreMapByString = new HashMap<>();
        for (CodeEdge codeEdge : callEdgeScoreMap.keySet()) {
            String source = codeEdge.getSource().getName();
            String target = codeEdge.getTarget().getName();
            callEdgeScoreMapByString.put(source + "#" + target, callEdgeScoreMap.get(codeEdge));
        }

        dataEdgeScoreMapByString = new HashMap<>();
        for (CodeEdge codeEdge : dataEdgeScoreMap.keySet()) {
            String source = codeEdge.getSource().getName();
            String target = codeEdge.getTarget().getName();
            dataEdgeScoreMapByString.put(source + "#" + target, dataEdgeScoreMap.get(codeEdge));
        }

        System.out.println("");
    }

    private void showDataTypeIDTFInConsole(Map<String, Double> sortedDataTypeIDFMap) {
        for (String s : sortedDataTypeIDFMap.keySet()) {
            System.out.println(s);
            System.out.println(sortedDataTypeIDFMap.get(s));
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

    private ClosestNeighbour getClosestNeighbour(CodeVertex codeVertex, Map<CodeEdge, Double> dataEdgeScoreMap) {
        ArrayList<CodeEdge> codeEdgesList = new ArrayList<>(unDirGraph.getInEdges(codeVertex));
        if (codeEdgesList.isEmpty()) return null;

        CodeEdge highestValueEdge = codeEdgesList.get(0);
        for (CodeEdge edge : codeEdgesList) {
            if (dataEdgeScoreMap.get(edge) > dataEdgeScoreMap.get(highestValueEdge)) {
                highestValueEdge = edge;
            }
        }

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
            if (allDependenciesGraph == null) {
                dataEdgeScoreMap.put(dataEdge, computeDataEdgeScoreByIDF(dataEdge));
            } else {
                String data1 = dataEdge.getSource().getName() + "#" + dataEdge.getTarget().getName();
                String data2 = dataEdge.getTarget().getName() + "#" + dataEdge.getSource().getName();
                if (allDependenciesGraph.dataEdgeScoreMapByString.get(data1) != null) {
                    dataEdgeScoreMap.put(dataEdge, allDependenciesGraph.dataEdgeScoreMapByString.get(data1));
                } else if (allDependenciesGraph.dataEdgeScoreMapByString.get(data2) != null) {
                    dataEdgeScoreMap.put(dataEdge, allDependenciesGraph.dataEdgeScoreMapByString.get(data2));
                } else {
                    //System.out.println(dataEdge);
                }
            }

        }

        System.out.println("");

        double upBound = compute3Alpha(dataEdgeScoreMap);

        // 3.16
        if (useNormalize == true) {
            if (upBound == -1) {
                normalizeDataEdge(dataEdgeScoreMap);
            } else {
                normalizeDataEdge(dataEdgeScoreMap, upBound);
            }
        }

        analyseDistributed(dataEdgeScoreMap, "data", dataEdgeScoreThreshold);

        for (CodeEdge dataEdge : dataEdgeScoreMap.keySet()) {
            if (dataEdgeScoreMap.get(dataEdge) < dataEdgeScoreThreshold) {
                unDirGraph.removeEdge(dataEdge);
            }
        }
    }

    private void normalizeDataEdge(Map<CodeEdge, Double> codeEdgeScoreMap, double upBound) {
        dataEdgeScoreValues = new ArrayList<>();
        for (Double v : codeEdgeScoreMap.values()) {
            dataEdgeScoreValues.add(v);

        }

        Collections.sort(dataEdgeScoreValues, Collections.reverseOrder());

        double max = 0.0;
        for (double d : dataEdgeScoreValues) {
            if (d < upBound) {
                max = d;
                break;
            }
        }

        Double min = dataEdgeScoreValues.get(dataEdgeScoreValues.size() - 1);

        for (CodeEdge edge : codeEdgeScoreMap.keySet()) {
            Double value = codeEdgeScoreMap.get(edge);
            if (value > upBound) {
                codeEdgeScoreMap.put(edge, 1.0);
            } else {
                codeEdgeScoreMap.put(edge, ((value - min) / (max - min)));
            }
        }
    }

    private void removeCallEdgesBelowCallEdgeScoreThreshold(double callEdgeScoreThreshold) {
        callEdgeScoreMap = new HashMap<>();
        for (CodeEdge codeEdge : dirGraph.getEdges()) {
            CallEdge callEdge = (CallEdge) codeEdge;
            if (allDependenciesGraph == null) {
                callEdgeScoreMap.put(callEdge, computeCallEdgeScore(callEdge));
            } else {
                String call = callEdge.getSource().getName() + "#" + callEdge.getTarget().getName();
                callEdgeScoreMap.put(callEdge, allDependenciesGraph.callEdgeScoreMapByString.get(call));
            }
        }


        System.out.println("");
        double upBound = compute3Alpha(callEdgeScoreMap);

       // 3.17
        if (useNormalize == true) {
            if (upBound == -1) {
                normalizeCallEdge(callEdgeScoreMap);
            } else {
                normalizeCallEdge(callEdgeScoreMap, upBound);
            }
        }

        analyseDistributed(callEdgeScoreMap, "Call", callEdgeScoreThreshold);

        for (CodeEdge callEdge : callEdgeScoreMap.keySet()) {
            if (callEdgeScoreMap.get(callEdge) < callEdgeScoreThreshold) {
                 dirGraph.removeEdge(callEdge);
            }
        }
    }

    private double compute3Alpha(Map<CodeEdge, Double> edgeScoreMap) {
        Collection<Double> values = edgeScoreMap.values();
        double[] scores = new double[values.size()];
        int i=0;
        for (Double d : values) {
            scores[i] = d;
            i++;
        }

        PauTa pauTa = new PauTa(scores);

        Set<Double> exceptionValue = pauTa.getExceptionValue();
        List<Double> ev = new LinkedList<>();
        for (Double d : exceptionValue) {
            ev.add(d);
        }
        Collections.sort(ev);

        if (ev.size() > 0) {
            return ev.get(0);
        } else {
            return -1;
        }

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

    }

    // max-min
    private void normalizeCallEdge(Map<CodeEdge, Double> codeEdgeScoreMap) {
        callEdgeScoreValues = new ArrayList<>();
        for (Double v : codeEdgeScoreMap.values()) {
            callEdgeScoreValues.add(v);
        }

        Collections.sort(callEdgeScoreValues, Collections.reverseOrder());

        Double max = callEdgeScoreValues.get(0);
        Double min = callEdgeScoreValues.get(callEdgeScoreValues.size() - 1);

        for (CodeEdge edge : codeEdgeScoreMap.keySet()) {
            Double value = codeEdgeScoreMap.get(edge);
            codeEdgeScoreMap.put(edge, ((value - min) / (max - min)));
        }

    }

    private void normalizeCallEdge(Map<CodeEdge, Double> codeEdgeScoreMap, double upBound) {
        callEdgeScoreValues = new ArrayList<>();
        for (Double v : codeEdgeScoreMap.values()) {
            callEdgeScoreValues.add(v);
        }

        Collections.sort(callEdgeScoreValues, Collections.reverseOrder());

        double max = 0.0;
        for (double d : callEdgeScoreValues) {
            if (d < upBound) {
                max = d;
                break;
            }
        }

        Double min = callEdgeScoreValues.get(callEdgeScoreValues.size() - 1);

        for (CodeEdge edge : codeEdgeScoreMap.keySet()) {
            Double value = codeEdgeScoreMap.get(edge);
            if (value > upBound) {
                codeEdgeScoreMap.put(edge, 1.0);
            } else {
                codeEdgeScoreMap.put(edge, ((value - min) / (max - min)));
            }
        }

    }


    // max min
    private void normalizeDataEdge(Map<CodeEdge, Double> codeEdgeScoreMap) {
        dataEdgeScoreValues = new ArrayList<>();
        for (Double v : codeEdgeScoreMap.values()) {
            dataEdgeScoreValues.add(v);

        }

        Collections.sort(dataEdgeScoreValues, Collections.reverseOrder());

        Double max = dataEdgeScoreValues.get(0);
        Double min = dataEdgeScoreValues.get(dataEdgeScoreValues.size() - 1);

        for (CodeEdge edge : codeEdgeScoreMap.keySet()) {
            Double value = codeEdgeScoreMap.get(edge);
            codeEdgeScoreMap.put(edge, ((value - min) / (max - min)));
        }
    }


    private void normalize(Map<CodeEdge, Double> codeEdgeScoreMap) {
        List<Double> values = new ArrayList<>();
        for (Double v : codeEdgeScoreMap.values()) {
            values.add(v);

        }
        Collections.sort(values, Collections.reverseOrder());

        Double max = values.get(0);
        Double min = values.get(values.size() - 1);
        for (CodeEdge edge : codeEdgeScoreMap.keySet()) {
            Double value = codeEdgeScoreMap.get(edge);
            codeEdgeScoreMap.put(edge, ((value - min) / (max - min)));
        }
    }


    private double computeCallEdgeScore(CallEdge callEdge) {
        int callerOutNum = (dirGraph.getOutEdges(callEdge.getSource())).size();
        int calleeInNum = (dirGraph.getInEdges(callEdge.getTarget())).size();

        CodeVertex caller = callEdge.getSource();
        CodeVertex callee = callEdge.getTarget();

        int calllerWeight = 0;

        for (CodeEdge codeEdge : dirGraph.getOutEdges(caller)) {
            CallEdge ce = (CallEdge) codeEdge;
            calllerWeight += ce.getCallRelationSize();
        }

        int callleeWeight = 0;

        for (CodeEdge codeEdge : dirGraph.getInEdges(callee)) {
            CallEdge ce = (CallEdge) codeEdge;
            callleeWeight += ce.getCallRelationSize();
        }

        int frequency = callEdge.getCallRelationSize();

        double score = 2.0 * frequency / (calllerWeight + callleeWeight);
        return score;
    }



    private Double computeDataEdgeScore(DataEdge dataEdge) {
        int callerOutNum = (unDirGraph.getOutEdges(dataEdge.getSource())).size();
        int calleeInNum = (unDirGraph.getInEdges(dataEdge.getTarget())).size();
        return dataEdge.getDataRelationSizeByUniqueType() * 1.0 / (callerOutNum + calleeInNum);
    }

    private Double computeDataEdgeScoreByIDF(DataEdge dataEdge) {
        CodeVertex source = dataEdge.getSource();
        CodeVertex target = dataEdge.getTarget();
        List<String> typesForSource = getTypesInvolvedInCodeVertex(source);
        List<String> typesForTarget = getTypesInvolvedInCodeVertex(target);
        List<String> typesInEdge = getTypesInEdge(dataEdge);

        double sumOfCommonType= 0.0;
        List<String> commonTypeList = new ArrayList<>();
        for (String s : typesForSource) {
            if (!commonTypeList.contains(s)) {
                commonTypeList.add(s);
                sumOfCommonType += dataTypeIDFMap.get(s);
            }
        }

        for (String s : typesForTarget) {
            if (!commonTypeList.contains(s)) {
                commonTypeList.add(s);
                sumOfCommonType += dataTypeIDFMap.get(s);
            }
        }

        double idfSum = 0.0;
        for (String type : typesInEdge) {
            if (dataTypeIDFMap.containsKey(type)) {
                idfSum += dataTypeIDFMap.get(type);
            }
        }

        double score = 1.0 * idfSum / sumOfCommonType;
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
        if (useNormalize == true) {
        }///////////////////这个语句没有任何意义
        //过滤掉了一些数据类型
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

        //System.out.println("DataTypeSize = " + dataTypeIDFMap.size());

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

    private void normalizeDataType(Map<String, Double> dataTypeIDFMap) {
        dataEdgeScoreValues = new ArrayList<>();
        for (Double v : dataTypeIDFMap.values()) {
            dataEdgeScoreValues.add(v);
        }

        Collections.sort(dataEdgeScoreValues, Collections.reverseOrder());

        Double max = dataEdgeScoreValues.get(0);
        Double min = dataEdgeScoreValues.get(dataEdgeScoreValues.size() - 1);

        for (String type : dataTypeIDFMap.keySet()) {
            Double value = dataTypeIDFMap.get(type);
            dataTypeIDFMap.put(type, ((value - min) / (max - min)));
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
            //System.out.println(d + " " + valuesCountMap.get(d));
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
        //增加代码
        if(vertex==null){
        	return vertexesList;
        }
        //增加代码
        for (CodeVertex v : getFathersByCall(vertex.getName())) {
            vertexesList.add(v);
        }

        for (CodeVertex v : getChildrenByCall(vertex.getName())) {
            vertexesList.add(v);
        }
        return vertexesList;
    }

    public List<String> getCallEdgeInfo(String v1, String v2) {

        StringBuffer sb = new StringBuffer();

        CodeVertex vertex1 = nameCodeVertexMap.get(v1);
        CodeVertex vertex2 = nameCodeVertexMap.get(v2);

        if (vertex1 == null) {
            _.abort("Can't find vertedx: " + vertex1);
        }

        if (vertex2 == null) {
            _.abort("Can't find vertedx: " + vertex2);
        }

        CodeEdge e1 = dirGraph.findEdge(vertex1, vertex2);
        CodeEdge e2 = dirGraph.findEdge(vertex2, vertex1);

        if (e1 != null) {
            CallEdge ce = (CallEdge) e1;
            for (CallRelation cr : ce.getCallRelationList()) {
                //System.out.println(cr.toString());
            }
        } else if (e2 != null) {
            System.out.println("Find call relation");
            System.out.println("caller: " + vertex2);
            System.out.println("callee: " + vertex1);
            CallEdge ce = (CallEdge) e2;
            System.out.println("call frequency: " + ce.getCallRelationSize());
        } else {
            System.out.println("No call relation be found");
        }
        System.out.println("-----------------");
        return null;
    }

    public void printCallEdgeInfo(String v1, String v2) {
        CodeVertex vertex1 = nameCodeVertexMap.get(v1);
        CodeVertex vertex2 = nameCodeVertexMap.get(v2);

        if (vertex1 == null) {
            _.abort("Can't find vertedx: " + vertex1);
        }

        if (vertex2 == null) {
            _.abort("Can't find vertedx: " + vertex2);
        }

        CodeEdge e1 = dirGraph.findEdge(vertex1, vertex2);
        CodeEdge e2 = dirGraph.findEdge(vertex2, vertex1);

        //System.out.println("-----------------");
        if (e1 != null) {
            System.out.println("Find call relation");
//            System.out.println("caller: " + vertex1);
//            System.out.println("callee: " + vertex2);
            CallEdge ce = (CallEdge) e1;
            System.out.println("call frequency: " + ce.getCallRelationSize());
            for (CallRelation cr : ce.getCallRelationList()) {
                System.out.println(cr.toString());
            }
        } else if (e2 != null) {
            System.out.println("Find call relation");
//            System.out.println("caller: " + vertex2);
//            System.out.println("callee: " + vertex1);
            CallEdge ce = (CallEdge) e2;
            System.out.println("call frequency: " + ce.getCallRelationSize());
        } else {
            System.out.println("No call relation be found");
        }
        System.out.println("-----------------");
    }

    public void printDataEdgeInfo(String v1, String v2) {
        CodeVertex vertex1 = nameCodeVertexMap.get(v1);
        CodeVertex vertex2 = nameCodeVertexMap.get(v2);

        if (vertex1 == null) {
            _.abort("Can't find vertedx: " + vertex1);
        }

        if (vertex2 == null) {
            _.abort("Can't find vertedx: " + vertex2);
        }

        CodeEdge e = unDirGraph.findEdge(vertex1, vertex2);

        System.out.println("-----------------");
        if (e != null) {
            System.out.println("Find Data relation");
            DataEdge de = (DataEdge) e;

            Set<String> fieldsList = new HashSet<>();
            DataRelationList dataRelations = de.getDataRelationList();
            for (DataRelation dr : dataRelations) {
                fieldsList.add(dr.getType() + "#" + dr.getHashcode());
            }
            System.out.println("shared data type: " + fieldsList.size());
            for (String s : fieldsList) {
                System.out.println(s);
            }
        } else {
            System.out.println("No data relation be found");
        }
        System.out.println("-----------------");
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

    @SuppressWarnings("unchecked")
	private void searhGraphVertexBelongsTo(CodeVertex codeVertex, List<CodeVertex> connectedVertexes, Graph graph) {
        if (!connectedVertexes.contains(codeVertex)) {
            connectedVertexes.add(codeVertex);
        }

        
        //修改的代码
        if (codeVertex!=null&&graph.getNeighbors(codeVertex) == null) {
            System.out.println(codeVertex.getName());
        }
        if(codeVertex==null){
        	return ;
        }
        ////////////////////////////////上面是修改的代码
        
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

    public double getDataEdgeBonusII(String source, String target) {
        double sum = 0.0;
        CodeVertex sourceVertex = nameCodeVertexMap.get(source);
        CodeVertex targetVertex = nameCodeVertexMap.get(target);

        if (unDirGraph.findEdge(sourceVertex, targetVertex) == null) return 0.0;
        for (CodeEdge edge : unDirGraph.getInEdges(sourceVertex)) {
            sum += computeDataBonus(dataEdgeScoreMap.get(edge));
        }
        CodeEdge edge = unDirGraph.findEdge(sourceVertex, targetVertex);
        return 1.0 * computeDataBonus(dataEdgeScoreMap.get(edge)) / sum;
    }

    public double getCallEdgeBonusII(String source, String target) {
        double sum = 0.0;
        CodeVertex sourceVertex = nameCodeVertexMap.get(source);
        CodeVertex targetVertex = nameCodeVertexMap.get(target);
        for (CodeEdge edge : dirGraph.getInEdges(sourceVertex)) {
            sum += computeCallBonus(callEdgeScoreMap.get(edge));
        }

        for (CodeEdge edge : dirGraph.getOutEdges(sourceVertex)) {
            sum += computeCallBonus(callEdgeScoreMap.get(edge));
        }

        double v = 0.0;

        CodeEdge edge1 = dirGraph.findEdge(sourceVertex, targetVertex);
        if (edge1 != null) {
            v += computeCallBonus(callEdgeScoreMap.get(edge1));
        }

        CodeEdge edge2 = dirGraph.findEdge(targetVertex, sourceVertex);
        if (edge2 != null) {
            v += computeCallBonus(callEdgeScoreMap.get(edge2));
        }
        return 1.0 * v / sum;
    }


    private double computeDataBonus(Double score) {
        int rank = dataEdgeScoreValues.size();
        for (double i : dataEdgeScoreValues) {
            if (score < i) {
                rank--;
            }
        }
        return 1.0 * rank /dataEdgeScoreValues.size();
    }

    private double computeCallBonus(Double score) {
        int rank = callEdgeScoreValues.size();
        for (double i : callEdgeScoreValues) {
            if (score < i) {
                rank--;
            }
        }
        System.out.println(" rank = " + rank );
        return 1.0 * rank /callEdgeScoreValues.size();
    }

    public double computeRelinkCallBonus(String source, String targetINCutGraph, List<String> cutGraph) {
        LinkedList<LinkedList<CodeVertex>> allPath = new LinkedList<>();
        LinkedList<CodeVertex> visited = new LinkedList();
        CodeVertex start = getCodeVertexByName(source);
        CodeVertex end = getCodeVertexByName(targetINCutGraph);
        visited.add(start);
        new PathSearch().breadthFirst(dirGraph, visited, end, allPath);
        TreeSet<Double> bonusSet = new TreeSet<>();
        for (LinkedList<CodeVertex> v : allPath) {
            if (connectToEdge(v, cutGraph)) {
                if (isOneWayDirectionPath(v)) {
                    bonusSet.add(computePathBonus(v));
                }
            }
        }
        return bonusSet.size() != 0 ? bonusSet.last() : 0.0;
    }

    private boolean isOneWayDirectionPath(LinkedList<CodeVertex> path) {
        if (path.size() == 0||path.size()==1) return true;
        String direction = getDirection(path.get(0), path.get(1));
        for (int i = 1; i < path.size() - 1; i++) {
            CodeVertex source = path.get(i);
            CodeVertex target = path.get(i + 1);
            String dir = getDirection(source, target);
            if (!direction.equals(dir)) {
                return false;
            }
        }
        return true;
    }

    private String getDirection(CodeVertex source, CodeVertex target) {
        if (dirGraph.findEdge(source, target) != null) {
            return "caller";
        } else if (dirGraph.findEdge(target, source) != null) {
            return "callee";
        }
        return null;
    }

    // 3.25
    public LinkedList<String> computeShortestPath(String source, String targetINCutGraph, List<String> cutGraph) {
        LinkedList<LinkedList<CodeVertex>> allPath = new LinkedList<>();
        LinkedList<CodeVertex> visited = new LinkedList();
        CodeVertex start = getCodeVertexByName(source);
        CodeVertex end = getCodeVertexByName(targetINCutGraph);
        visited.add(start);

        new PathSearch().breadthFirst(dirGraph, visited, end, allPath);

        Map<LinkedList<CodeVertex>, Integer> pathSizeMap = new LinkedHashMap<>();

        for (LinkedList<CodeVertex> v : allPath) {
            if (connectToEdge(v, cutGraph)) {
                if (v.size() >= 2) {
                    pathSizeMap.put(v, v.size());
                }
            }
        }

        Map<LinkedList<CodeVertex>, Integer> sorted = _.sortValueByAscending(pathSizeMap);

        for (LinkedList<CodeVertex> path : sorted.keySet()) {

            LinkedList<String> result = new LinkedList<>();
            for (CodeVertex v : path) {
                result.add(v.getName());
            }

            return result;
        }

        return null;
    }

    private boolean connectToEdge(LinkedList<CodeVertex> v, List<String> cutGraph) {
        int num = 0;
        for (CodeVertex cv : v) {
            if (cutGraph.contains(cv.getName())) {
                num++;
            }
        }
        return num == 1 ? true : false;
    }

    // 3.23
    private double computePathBonus(LinkedList<CodeVertex> path) {
        if (path == null) throw new NoSuchElementException("No Path");
        if (path.size() == 1) return 0;

        double value = 1.0;

            // 从内到外

            double k = 0.1;
        double times = 1.0;
            int n = 1;

//        return 1.0 / Math.pow(path.size(), 2);


            for (int i = path.size() - 1; i > 0; i--) {
                String source = path.get(i - 1).getName();
                String target = path.get(i).getName();
                double temp = getCallEdgeBonusCloseness(path.get(i - 1).getName(), path.get(i).getName());

                value *= temp;
                n++;
            }
        double geometricMean = Math.pow(value, 1.0 / (path.size() - 1));
        return geometricMean;
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

    public double getDataEdgeBonusCloseness(String source, String target) {

        CodeVertex sourceVertex = nameCodeVertexMap.get(source);
        CodeVertex targetVertex = nameCodeVertexMap.get(target);

        if (unDirGraph.findEdge(sourceVertex, targetVertex) == null) return 0.0;

        CodeEdge edge = unDirGraph.findEdge(sourceVertex, targetVertex);
        double edgeScore = dataEdgeScoreMap.get(edge);

        return edgeScore;
    }

    public Object getCallFrequency(String v1, String v2) {
        CodeVertex vertex1 = nameCodeVertexMap.get(v1);
        CodeVertex vertex2 = nameCodeVertexMap.get(v2);

        if (vertex1 == null) {
            _.abort("Can't find vertedx: " + vertex1);
        }

        if (vertex2 == null) {
            _.abort("Can't find vertedx: " + vertex2);
        }

        CodeEdge e1 = dirGraph.findEdge(vertex1, vertex2);
        CodeEdge e2 = dirGraph.findEdge(vertex2, vertex1);

        if (e1 != null) {
            CallEdge ce = (CallEdge) e1;
            return ce.getCallRelationSize();
        } else if (e2 != null) {
            CallEdge ce = (CallEdge) e2;
        } else {
            System.out.println("No call relation be found");
        }
        return 0;
    }

    public String getDataTypeInEdge(String v1, String v2) {
        CodeVertex vertex1 = nameCodeVertexMap.get(v1);
        CodeVertex vertex2 = nameCodeVertexMap.get(v2);

        if (vertex1 == null) {
            _.abort("Can't find vertedx: " + vertex1);
        }

        if (vertex2 == null) {
            _.abort("Can't find vertedx: " + vertex2);
        }

        CodeEdge e = unDirGraph.findEdge(vertex1, vertex2);
        StringBuffer sb = new StringBuffer();
        if (e != null) {
            DataEdge de = (DataEdge) e;

            Set<String> fieldsList = new HashSet<>();
            Set<String> dataType = new LinkedHashSet<>();
            DataRelationList dataRelations = de.getDataRelationList();
            for (DataRelation dr : dataRelations) {
                fieldsList.add(dr.getType() + "#" + dr.getHashcode());
                dataType.add(dr.getType());
            }
            for (String s : fieldsList) {
//                System.out.println(s);
//                sb.append(s);
//                sb.append(" ");
            }

            int count = 0;
            for (String dt : dataType) {
                if (dataTypeIDFMap.get(dt) != null) {
                    count++;
                }
            }

            sb.append(count);
            sb.append(" ");
            for (String s : dataType) {
                // 16.3.16
                if (dataTypeIDFMap.get(s) != null) {
                    sb.append(s);
                    sb.append("_");
                    sb.append(dataTypeIDFMap.get(s));
                    sb.append(" ");
                }


            }
        } else {
//            System.out.println("No data relation be found");
            return null;
        }
        return sb.toString();
    }

    public Pair<String, Double> computeFormerIRValue(String source, String targetINCutGraph, List<String> cutGraph, LinksList linksList, String uc) {

        LinkedList<LinkedList<CodeVertex>> allPath = new LinkedList<>();
        LinkedList<CodeVertex> visited = new LinkedList();
        CodeVertex start = getCodeVertexByName(source);
        CodeVertex end = getCodeVertexByName(targetINCutGraph);
        visited.add(start);

        new PathSearch().breadthFirst(dirGraph, visited, end, allPath);

        Map<String, Double> IRValueMap = new LinkedHashMap<>();

        for (LinkedList<CodeVertex> v : allPath) {
            if (connectToEdge(v, cutGraph)) {
                if (v.size() >= 2) {
                    IRValueMap.put(v.get(1).getName(), computeIRValue(v, linksList, uc));
                }
            }
        }

        Map<String, Double> sortedIRValueMap = _.sortValueByDescending(IRValueMap);

        if (sortedIRValueMap.size() == 0) {
            return null;
        } else {
            for (String v : sortedIRValueMap.keySet()) {
                return new Pair<>(v, sortedIRValueMap.get(v));
            }
        }

        return null;
    }

    private Double computeIRValue(LinkedList<CodeVertex> path, LinksList linksList, String uc) {
        if (path == null) throw new NoSuchElementException("No Path");
        if (path.size() == 1) return 0.0;

        // 从内到外

        int n = 1;

        double topIRValue = linksList.getScore(uc, path.get(path.size() - 1).getName());

        for (int i = path.size() - 1; i > 1; i--) {
            String source = path.get(i - 1).getName();
            String target = path.get(i).getName();
            double temp = getCallEdgeBonusCloseness(path.get(i - 1).getName(), path.get(i).getName());

            double currentIRValue = linksList.getScore(uc, path.get(i - 1).getName());

            if (topIRValue > currentIRValue) {
                topIRValue = currentIRValue + temp * (topIRValue - currentIRValue);
            }

            n++;
        }

        return topIRValue;
    }



    public CallDataRelationGraph(RelationInfo relationInfo, boolean useNormalize, RelationGraph allDependencies) {
        super(relationInfo);

        this.allDependenciesGraph = (CallDataRelationGraph) allDependencies;
        dirGraph = new DirectedSparseGraph<>();
        idCodeVertexMap = new LinkedHashMap<>();
        nameCodeVertexMap = new LinkedHashMap<>();
        callEdgeScoreMap = new HashMap<>();
        dataEdgeScoreMap = new HashMap<>();

        for (Integer i : relationInfo.getVertexes().keySet()) {
            CodeVertex cv = new CodeVertex(i, relationInfo.getVertexNameById(i));
            idCodeVertexMap.put(i, cv);
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

        this.useNormalize = useNormalize;

        if (relationInfo.isPruning()) {
            pruning(relationInfo.getCallEdgeScoreThreshold(), relationInfo.getDataEdgeScoreThreshold());
        }


        System.out.println("");
    }

    public void searhFathersByCall(CodeVertex codeVertex, List<CodeVertex> connectedVertexes) {
        searhFatherVertexBelongsTo(codeVertex, connectedVertexes, dirGraph);
        connectedVertexes.remove(codeVertex);
    }


    public void searhChildrenByCall(CodeVertex codeVertex, List<CodeVertex> connectedVertexes) {
        searhChildrenVertexBelongsTo(codeVertex, connectedVertexes, dirGraph);
        connectedVertexes.remove(codeVertex);
    }

    private void searhFatherVertexBelongsTo(CodeVertex codeVertex, List<CodeVertex> connectedVertexes, Graph graph) {
        if (!connectedVertexes.contains(codeVertex)) {
            connectedVertexes.add(codeVertex);
        }

        Collection<CodeEdge> inEdges = graph.getInEdges(codeVertex);
        Set<CodeVertex> fatherVertexes = new LinkedHashSet<>();
        for (CodeEdge edge : inEdges) {
            fatherVertexes.add(edge.getSource());
        }

        if (fatherVertexes.size() == 0) {
//            System.out.println(codeVertex.getName());
            return;
        }

        for (CodeVertex n : fatherVertexes) {
            if (!connectedVertexes.contains(n)) {
                searhFatherVertexBelongsTo(n, connectedVertexes, graph);
            }
        }
    }

    
    public Graph<CodeVertex, CodeEdge> getPrunedGraph(){
    	return prunedGraph;
    }
    
    
    private void searhChildrenVertexBelongsTo(CodeVertex codeVertex, List<CodeVertex> connectedVertexes, Graph graph) {
        if (!connectedVertexes.contains(codeVertex)) {
            connectedVertexes.add(codeVertex);
        }

        Collection<CodeEdge> outEdges = graph.getOutEdges(codeVertex);
        Set<CodeVertex> childrenVertexes = new LinkedHashSet<>();
        for (CodeEdge edge : outEdges) {
            childrenVertexes.add(edge.getTarget());
        }

        if (childrenVertexes.size() == 0) {
//            System.out.println(codeVertex.getName());
            return;
        }

        for (CodeVertex n : childrenVertexes) {
            if (!connectedVertexes.contains(n)) {
                searhChildrenVertexBelongsTo(n, connectedVertexes, graph);
            }
        }
    }
}

class ClosestNeighbour {
    public CodeVertex codeVertex;
    public CodeEdge codeEdge;

    public ClosestNeighbour(CodeVertex codeVertex, CodeEdge codeEdge) {
        this.codeVertex = codeVertex;
        this.codeEdge = codeEdge;
    }
}