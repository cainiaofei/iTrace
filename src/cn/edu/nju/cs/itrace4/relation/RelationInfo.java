package cn.edu.nju.cs.itrace4.relation;


import cn.edu.nju.cs.itrace4.core.document.ArtifactsCollection;
import cn.edu.nju.cs.itrace4.core.type.Granularity;
import cn.edu.nju.cs.itrace4.io.ArtifactsReader;
import cn.edu.nju.cs.itrace4.relation.info.*;
import cn.edu.nju.cs.itrace4.relation.io.CallRelationIO;
import cn.edu.nju.cs.itrace4.relation.io.CallRelationIOForGit;
import cn.edu.nju.cs.itrace4.relation.io.DataRelationIO;
import cn.edu.nju.cs.itrace4.relation.io.DataRelationIOForGit;
import cn.edu.nju.cs.itrace4.util.Setting;
import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import org.apache.commons.collections15.functors.MapTransformer;

import java.io.*;
import java.util.*;


/**
 * @author zzf
 * @date 2017/10/12
 * @description the pp is too big to process based on the method of niejia, We will use our own 
 * 	method to generate DataRelation.
 */
public class RelationInfo implements Serializable {
    // interface
    private List<RelationPair> callRelationPairList;
    private List<RelationPair> dataRelationPairList;
    private List<RelationPair> usageRelationPairList;
    private Map<RelationPair, CallRelationList> pairCallRelationListMap;
    private Map<RelationPair, DataRelationList> pairDataRelationListMap;
    private Map<RelationPair, DataRelationList> pairUsageRelationListMap;
    private StringBuilder relationGraphFile;

    //
    private Map<Integer, String> vertexIdNameMap;
    private Map<String, Integer> vertexNameIdMap;

    private ArtifactsCollection artifactCollection;
    private Granularity granularity;

    private String relationDirPath;
    private boolean isPruning;
    private double callEdgeScoreThreshold;
    private double dataEdgeScoreThreshold;

//    private IDFInfo idfInfo;

//    public RelationInfo() {
//    }

    public Map<Integer, String> getVertexIdNameMap() {
        return vertexIdNameMap;
    }

    public RelationInfo(String artifactCollectionDirPath, String relationDirPath, Granularity granularity) {
         
    	
        String callDBPath = relationDirPath + "/call.db";
        this.relationDirPath = relationDirPath;
        this.isPruning = false;

        pairCallRelationListMap = new LinkedHashMap<>();
        pairDataRelationListMap = new LinkedHashMap<>();
        pairUsageRelationListMap = new LinkedHashMap<>();

        artifactCollection = ArtifactsReader.getCollections(artifactCollectionDirPath, ".txt");

        this.granularity = granularity;

        vertexIdNameMap = new LinkedHashMap<>();
        vertexNameIdMap = new LinkedHashMap<>();
        int id = 1;
        if (granularity.equals(Granularity.CLASS)) {
            for (String name : artifactCollection.keySet()) {
                if (!name.endsWith("_jsp")) {
                    vertexIdNameMap.put(id, name);
                    vertexNameIdMap.put(name, id);
                    id++;
                }
            }

            for (String name : artifactCollection.keySet()) {
                if (name.endsWith("_jsp")) {
                    vertexIdNameMap.put(id, name);
                    vertexNameIdMap.put(name, id);
                    id++;
                }
            }
        } else if (granularity.equals(Granularity.METHOD)) {
            for (String name : artifactCollection.keySet()) {
                if (!name.endsWith("_jspService")) {
                    vertexIdNameMap.put(id, name);
                    vertexNameIdMap.put(name, id);
                    id++;
                }
            }

            for (String name : artifactCollection.keySet()) {
                if (name.endsWith("_jspService")) {
                    vertexIdNameMap.put(id, name);
                    vertexNameIdMap.put(name, id);
                    id++;
                }
            }
        }
        /**
         * @author zzf
         * @date 2017/10/19
         * @description judge whether this project is git. 
         */
       // relationPairParser(CallRelationIO.parser(callDBPath), DataRelationIO.parser(relationDirPath));
        /*
         *@date 2017.12.26
         *@description using only call dependency and ignore data dependency. 
         */
        relationPairParser(CallRelationIO.parser(callDBPath),new DataRelationList());
//        String type = System.getProperty("projectType");
//        if(type!=null && type.equals("git")) {
//        	 relationPairParser(CallRelationIOForGit.parser(callDBPath), DataRelationIOForGit.parser(dataDBPath));
//        }
//        else {
//        	
//        }
    }

    private void relationPairParser(CallRelationList callRelationList, DataRelationList dataRelationList) {

        Set<String> usag = new HashSet<>();
        int usageNum = 0;
        for (DataRelation dataRelation : dataRelationList) {
            if (dataRelation.isUsage()) {
                // the overlap things here??
                if (artifactCollection.containsKey(dataRelation.getCallerClass()) && 
                		artifactCollection.containsKey(dataRelation.getCalleeClass())) {
                	//if(dataRelation.getCallerClass()!=dataRelation.getCalleeClass()) {
                    if (!dataRelation.getCallerClass().equals(dataRelation.getCalleeClass())) {
                        String u = dataRelation.getCallerClass() + "#" + dataRelation.getCalleeClass();
                        if (!usag.contains(u)) {
                            CallRelation cr = new CallRelation(dataRelation.getCallerClass(), dataRelation.getCalleeClass(), 
                            		dataRelation.getCallerMethod(), dataRelation.getCalleeMethod());
                            callRelationList.add(cr);
                            usag.add(u);
                            usageNum++;
                        }
                    }
                }
            }
        }

        relationGraphFile = new StringBuilder();
        relationGraphFile.append("*Vertices");
        relationGraphFile.append(" ");
        relationGraphFile.append(vertexIdNameMap.size());
        relationGraphFile.append("\n");

        for (Integer id : vertexIdNameMap.keySet()) {
            relationGraphFile.append(id);
            relationGraphFile.append(" ");
            relationGraphFile.append(vertexIdNameMap.get(id));
            relationGraphFile.append("\n");
        }

        relationGraphFile.append("*Arcs Call");
        relationGraphFile.append("\n");

        callRelationPairList = new ArrayList<>();
        List<String> callRelationById = new ArrayList<>();
        
        /**
         * call prune
         * 实际发生的调用　--- 所属的依赖类型
         */
        for (CallRelation cr : callRelationList) {//call for
            String caller;
            String callee;
            if (granularity.equals(Granularity.CLASS)) {
                caller = cr.getCallerClass();
                callee = cr.getCalleeClass();
            } else {
                caller = cr.getCallerMethod();
                callee = cr.getCalleeMethod();
            }

            if (artifactCollection.containsKey(caller) && artifactCollection.containsKey(callee)) {
                Integer callerId = vertexNameIdMap.get(caller);
                Integer calleeId = vertexNameIdMap.get(callee);

                String relationIdFormat = callerId + " " + calleeId;

                RelationPair rp = new RelationPair(callerId, calleeId);

                if (callerId != calleeId) {
                    if (pairCallRelationListMap.containsKey(rp)) {
                        CallRelationList callRelationListForPair = pairCallRelationListMap.get(rp);
                        callRelationListForPair.add(cr);
                        pairCallRelationListMap.put(rp, callRelationListForPair);
                    } else {
                        CallRelationList callRelationListForPair = new CallRelationList();
                        callRelationListForPair.add(cr);
                        pairCallRelationListMap.put(rp, callRelationListForPair);
                    }
                }


                if (!callRelationById.contains(relationIdFormat) && callerId != calleeId) {
                    callRelationById.add(relationIdFormat);
                    //System.out.println(getCallRelationPairList());
                    getCallRelationPairList().add(rp);
                    relationGraphFile.append(relationIdFormat);
                    relationGraphFile.append("\n");
                } else {
//                    System.out.println(relationIdFormat + " class call relation is duplicated.");
                }
            }
        }//call for

        relationGraphFile.append("*Arcs Data");
        relationGraphFile.append("\n");

        dataRelationPairList = new ArrayList<>();
        List<String> dataRelationById = new ArrayList<>();

        System.out.println(" dataRelationPairList = " + dataRelationPairList.size() );
        int count = 0;
        
        /**
         * data prune 
         */
        for (DataRelation dr : dataRelationList) {//data for
            if (!dr.isUsage()) {
                String caller;
                String callee;
                if (granularity.equals(Granularity.CLASS)) {
                    caller = dr.getCallerClass();
                    callee = dr.getCalleeClass();
                } else {
                    caller = dr.getCallerMethod();
                    callee = dr.getCalleeMethod();
                }

                if ( artifactCollection.containsKey(caller) && artifactCollection.containsKey(callee)) {
                    Integer callerId = vertexNameIdMap.get(caller);
                    Integer calleeId = vertexNameIdMap.get(callee);

                    String relationIdFormat = callerId + " " + calleeId;
                    RelationPair rp = new RelationPair(callerId, calleeId);


                    if ( callerId != calleeId) {
                        if (pairDataRelationListMap.containsKey(rp)) {
                            DataRelationList dataRelationListForPair = pairDataRelationListMap.get(rp);
                            dataRelationListForPair.add(dr);
                            pairDataRelationListMap.put(rp, dataRelationListForPair);
                        } else {
                            DataRelationList dataRelationListForPair = new DataRelationList();
                            dataRelationListForPair.add(dr);
                            pairDataRelationListMap.put(rp, dataRelationListForPair);
                        }
                    }


                    if ( !dataRelationById.contains(relationIdFormat) && callerId != calleeId) {
                            dataRelationById.add(relationIdFormat);
                            getDataRelationPairList().add(rp);
                            count++;
                            relationGraphFile.append(relationIdFormat);
                            relationGraphFile.append("\n");

                    } else {
//                    System.out.println(relationIdFormat + " class call relation is duplicated.");
                    }


                }
            }
        }//data for
        
        noDuplicatedDataStatic();

        relationGraphFile.append("*Arcs Usage");
        relationGraphFile.append("\n");

        usageRelationPairList = new ArrayList<>();
        List<String> usageRelationById = new ArrayList<>();
        for (DataRelation dr : dataRelationList) {
            if (dr.isUsage()) {
                String caller;
                String callee;
                if (granularity.equals(Granularity.CLASS)) {
                    caller = dr.getCallerClass();
                    callee = dr.getCalleeClass();
                } else {
                    caller = dr.getCallerMethod();
                    callee = dr.getCalleeMethod();
                }

                if (artifactCollection.containsKey(caller) && artifactCollection.containsKey(callee)) {
                    Integer callerId = vertexNameIdMap.get(caller);
                    Integer calleeId = vertexNameIdMap.get(callee);

                    String relationIdFormat = callerId + " " + calleeId;
                    RelationPair rp = new RelationPair(callerId, calleeId);

                    if (callerId != calleeId) {
                        if (pairUsageRelationListMap.containsKey(rp)) {
                            DataRelationList dataRelationListForPair = pairUsageRelationListMap.get(rp);
                            dataRelationListForPair.add(dr);
                            pairUsageRelationListMap.put(rp, dataRelationListForPair);
                        } else {
                            DataRelationList dataRelationListForPair = new DataRelationList();
                            dataRelationListForPair.add(dr);
                            pairUsageRelationListMap.put(rp, dataRelationListForPair);
                        }
                    }

                    if (!usageRelationById.contains(relationIdFormat) && callerId != calleeId) {
                        usageRelationById.add(relationIdFormat);
                        getUsageRelationPairList().add(rp);
                        relationGraphFile.append(relationIdFormat);
                        relationGraphFile.append("\n");
                    } else {
//                    System.out.println(relationIdFormat + " class call relation is duplicated.");
                    }
                }
            }
        }

        System.out.println(" dataRelationPairList = " + dataRelationPairList.size() );



        int overlapNum = 0;
        int uniqueNUm = 0;
        for (String usageRelation : usageRelationById) {
            if (callRelationById.contains(usageRelation)) {
                overlapNum++;
            } else {
                //System.out.println("unique usage relaiton = " + usageRelation);
                uniqueNUm++;
            }
        }

        System.out.println(" Usage uniqueNUm = " + uniqueNUm);
//        System.out.println(" overlapNum = " + overlapNum);
//        System.out.printf("overlap = %f\n", overlapNum / (1.0 * usageRelationById.size()));
//
       // System.out.println(relationGraphFile.toString());
        serialize();

        relationPairAnalysis(callRelationById, dataRelationById, usageRelationById);
    }

    private void noDuplicatedDataStatic() {
        List<RelationPair> dataPairList = new ArrayList<>();


        for (RelationPair rp : getDataRelationPairList()) {
            if (!isPairExist(rp, dataPairList)) {
                dataPairList.add(rp);
            }
        }

        System.out.println("No duplicated dataPairList = " + dataPairList.size() );

    }

    private boolean isPairExist(RelationPair rp, List<RelationPair> dataPairList) {
        for (RelationPair pair : dataPairList) {
            if (pair.getKey() == rp.getKey() && pair.getValue() == rp.getValue()) {
                return true;
            } else if (pair.getKey() == rp.getValue() && pair.getValue() == rp.getKey()) {
                return true;
            }
        }

        return false;
    }

//    public Map<String,Integer> getVertexNameIdMap(){
//    	return vertexNameIdMap;
//    }
    
    private void relationPairAnalysis(List<String> callRelationById, List<String> dataRelationById, List<String> usageRelationById) {
        System.out.println("Call Relation Size = " + callRelationById.size());
        System.out.println("Data Relation Size = " + dataRelationById.size());
        System.out.println("Overlap " + overlapNumBetweenCallAndData(callRelationById, dataRelationById));


        System.out.println("Call Relation Size = " + callRelationById.size());
        System.out.println("Usage Relation Size = " + usageRelationById.size());
        System.out.println("Overlap " + overlapNumBetweenCallAndUsage(callRelationById, usageRelationById));


        System.out.println("Usage Relation Size = " + usageRelationById.size());
        System.out.println("Data Relation Size = " + dataRelationById.size());
        System.out.println("Overlap " + overlapNumBetweenUsageAndData(usageRelationById, dataRelationById));

        List<String> directRelationById = getDirectRelationById(callRelationById, usageRelationById);
        System.out.println("directRelation Size = " + directRelationById.size());
        System.out.println("Data Relation Size = " + dataRelationById.size());
        System.out.println("Overlap " + overlapNumBetweenDirectAndData(directRelationById, dataRelationById));


    }

    private List<String> getDirectRelationById(List<String> callRelationById,  List<String> usageRelationById) {
        List<String> result = new ArrayList<>();
        for (String s : callRelationById) {
            if (!result.contains(s)) {
                result.add(s);
            }
        }

        for (String s : usageRelationById) {
            if (!result.contains(s)) {
                result.add(s);
            }
        }

        return result;
    }

    private int overlapNumBetweenDirectAndData(List<String> directRelationById, List<String> dataRelationById) {
        int overlap = 0;

        for (String cr : directRelationById) {
            String cr_verse = cr.split(" ")[1] +" "+ cr.split(" ")[0];
            if (dataRelationById.contains(cr)||dataRelationById.contains(cr_verse)) {
                overlap++;
            }
        }

        return overlap;
    }

    private int overlapNumBetweenUsageAndData(List<String> usageRelationById, List<String> dataRelationById) {
        int overlap = 0;

        for (String ua : usageRelationById) {
            String cr_verse = ua.split(" ")[1] +" "+ ua.split(" ")[0];
            if (dataRelationById.contains(ua)||dataRelationById.contains(cr_verse)) {
                overlap++;
                String caller = ua.split(" ")[0];
                String callee = ua.split(" ")[1];
                String s1 = vertexIdNameMap.get(Integer.parseInt(caller));
                String s2 = vertexIdNameMap.get(Integer.parseInt(callee));
                //System.out.println(s1 + " " + s2);
            } else {

            }
        }

        return overlap;
    }

    private int overlapNumBetweenCallAndData(List<String> callRelationById, List<String> dataRelationById) {
        int overlap = 0;

        for (String cr : callRelationById) {
            String cr_verse = cr.split(" ")[1] +" "+ cr.split(" ")[0];
            if (dataRelationById.contains(cr)||dataRelationById.contains(cr_verse)) {
                overlap++;
            }
        }

        return overlap;
    }

    private int overlapNumBetweenCallAndUsage(List<String> callRelationById, List<String> usageRelationById) {
        int overlap = 0;

//        relations only in usage
        for (String ur : usageRelationById) {
            if (callRelationById.contains(ur)) {
                overlap++;
            } else {
                String caller = ur.split(" ")[0];
                String callee = ur.split(" ")[1];
                String s1 = vertexIdNameMap.get(Integer.parseInt(caller));
                String s2 = vertexIdNameMap.get(Integer.parseInt(callee));
//                System.out.println(s1 + " " + s2);
            }
        }

        // relations only in call
//        for (String cr : callRelationById) {
//            if (usageRelationById.contains(cr)) {
//                overlap++;
//            } else {
//                String caller = cr.split(" ")[0];
//                String callee = cr.split(" ")[1];
//                String s1 = vertexIdNameMap.get(Integer.parseInt(caller));
//                String s2 = vertexIdNameMap.get(Integer.parseInt(callee));
//                System.out.println(s1 + " " + s2);
//            }
//        }
        return overlap;
    }

    public String getRelationGraphFile() {
        return relationGraphFile.toString();
    }

    public CallRelationList getCallRelationListForRelationPair(RelationPair pair) {
        return pairCallRelationListMap.get(pair);
    }

    public DataRelationList getDataRelationListForRelationPair(RelationPair pair) {
        return pairDataRelationListMap.get(pair);
    }

    public DataRelationList getUsageRelationListForRelationPair(RelationPair pair) {
        return pairUsageRelationListMap.get(pair);
    }

    public Map<Integer, String> getVertexes() {
        return vertexIdNameMap;
    }

    public Integer getVertexIdByName(String vertexName) {
        return vertexNameIdMap.get(vertexName);
    }

    public String getVertexNameById(Integer id) {
        return vertexIdNameMap.get(id);
    }

    public void showMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(granularity + " relation graph contains ");
        sb.append(getCallRelationPairList().size() + " call relation pairs, ");
        sb.append(getDataRelationPairList().size() + " data relations pairs and ");
        sb.append(getUsageRelationPairList().size() + " usage relations pairs.");

        System.out.println(sb.toString());
    }

    public List<RelationPair> getCallRelationPairList() {
        return callRelationPairList;
    }

    public List<RelationPair> getDataRelationPairList() {
        return dataRelationPairList;
    }

    public List<RelationPair> getUsageRelationPairList() {
        return usageRelationPairList;
    }
    
    private void serialize() {
        try {
            FileOutputStream fs = new FileOutputStream(relationDirPath + "/" + granularity + "_"+ Setting.serializeName);
            ObjectOutputStream os = new ObjectOutputStream(fs);
            os.writeObject(this);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    

//    private void serialize() {
//        try {
//            FileOutputStream fs = new FileOutputStream(relationDirPath + "/" + granularity + "_"+ Setting.serializeName);
//            ObjectOutputStream os = new ObjectOutputStream(fs);
//            os.writeObject(this);
//            
//            FileOutputStream fs2 = new FileOutputStream(relationDirPath + "/" + granularity + "_"+ "relationInfo.ser");
//            ObjectOutputStream os2 = new ObjectOutputStream(fs2);
//            os2.writeObject(this);
//            
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


    public void setPruning(double callEdgeScoreThreshold, double dataEdgeScoreThreshold) {
        this.isPruning = true;
        this.callEdgeScoreThreshold = callEdgeScoreThreshold;
        this.dataEdgeScoreThreshold = dataEdgeScoreThreshold;
    }

    public boolean isPruning() {
        return isPruning;
    }

    public double getCallEdgeScoreThreshold() {
        return callEdgeScoreThreshold;
    }

    public double getDataEdgeScoreThreshold() {
        return dataEdgeScoreThreshold;
    }

    public Map<String, Double> computeDataTypeIDF() {
//        return idfInfo.computeDataTypeIDF();
        List<String> typesList = new ArrayList<>();
        Map<String, Double> dataTypeIDF = new LinkedHashMap<>();
        int dataEdgeSize = getDataRelationPairList().size();

        for (RelationPair edgePair : getDataRelationPairList()) {
            for (DataRelation dataRelation : getDataRelationListForRelationPair(edgePair)) {
                if (!dataRelation.isUsage()) {
                    String type = dataRelation.getType();
                    if (!typesList.contains(type)) {
                        typesList.add(type);
                    }
                }
            }
        }

        for (String type : typesList) {
            int edgeContainsThisTypeNum = 0;
            for (RelationPair edgePair : getDataRelationPairList()) {
                for (DataRelation dataRelation : getDataRelationListForRelationPair(edgePair)) {
                    if (!dataRelation.isUsage()) {
                        if (dataRelation.getType().equals(type)) {
                            edgeContainsThisTypeNum++;
                            break;
                        }
                    }
                }
            }

            dataTypeIDF.put(type, logBase10(dataEdgeSize / (1.0 * edgeContainsThisTypeNum)));
        }
        return dataTypeIDF;
    }

    private double logBase10(double value) {
        return Math.log(value) / Math.log(10);
    }

    public Map<String, Double> computeCallMethodlIDF() {
//        return idfInfo.computeCallMethodlIDF();
        List<String> methodsList = new ArrayList<>();
        Map<String, Double> methodCallIDF = new LinkedHashMap<>();
        int callEdgeSize = getCallRelationPairList().size();

        for (RelationPair edgePair : getCallRelationPairList()) {
            for (CallRelation callRelation : getCallRelationListForRelationPair(edgePair)) {
                String methodIdentity = callRelation.getCalleeMethod();
                if (!methodsList.contains(methodIdentity)) {
                    methodsList.add(methodIdentity);
                }
            }
        }

        for (String methodIdentity : methodsList) {
            int edgeContainsThisMethodNum = 0;
            for (RelationPair edgePair : getCallRelationPairList()) {
                for (CallRelation callRelation : getCallRelationListForRelationPair(edgePair)) {
                    if (callRelation.getCalleeMethod().equals(methodIdentity)) {
                        edgeContainsThisMethodNum++;
                        break;
                    }
                }
            }

            methodCallIDF.put(methodIdentity, Math.log(callEdgeSize / (1.0 * edgeContainsThisMethodNum)));
        }
        return methodCallIDF;
    }

    public Map<String, Number> getPageRank() {
        Graph<Integer, Integer> graph = new DirectedSparseGraph();

        for (int i : vertexIdNameMap.keySet()) {
            graph.addVertex(i);
        }

        //outPut记录每个边的权重
        //callRelationPairList其值什么时候放进去的呢。。
        Map<Integer, Integer> outCount = new HashMap<>();
        for (int i = 0; i < callRelationPairList.size(); i++) {
            RelationPair pair = callRelationPairList.get(i);
            if (!outCount.containsKey(pair.getKey())) {
                outCount.put(pair.getKey(), 1);
            } else {
                outCount.put(pair.getKey(), outCount.get(pair.getKey()) + 1);
            }
        }

        //大概的理解 往外出的每条边的值
        Map<Integer, Number> outPossible = new HashMap<>();
        for (Integer i : outCount.keySet()) {
            outPossible.put(i, 1.0 / outCount.get(i));
        }

        Map<Integer, Number> edgeWeights = new HashMap<>();
        int edgeN = 1;
        //每条边编号  并知每条边的值是多少
        for (int i = 0; i < callRelationPairList.size(); i++) {
            graph.addEdge(edgeN, callRelationPairList.get(i).getKey(), callRelationPairList.get(i).getValue());
            edgeWeights.put(edgeN, outPossible.get(callRelationPairList.get(i).getKey()));
            edgeN++;
        }

        PageRankWithPriors<Integer, Integer> pr = new PageRank<Integer, Integer>(graph, 
        		MapTransformer.getInstance(edgeWeights), 0);
       
        //核心方法
        pr.evaluate();

        Map<String, Number> vertexWeights = new HashMap<>();

        for (Integer v : graph.getVertices()) {

            vertexWeights.put(vertexIdNameMap.get(v), pr.getVertexScore(v));
        }
        return vertexWeights;
    }

    public static void main(String[] args) {
        String classDirPath = "data/exp/iTrust/class/code";
//        String methodDirPath = "data/exp/iTrust/method/code";
        String relationDirPath = "data/exp/iTrust/relation";

//        RelationInfo rg = new RelationInfo(methodDirPath, relationDirPath, Granularity.METHOD);
        RelationInfo rg = new RelationInfo(classDirPath, relationDirPath, Granularity.CLASS);


//        for (String s : rg.computeDataTypeIDF().keySet()) {
//            System.out.println(" s = " + s );
//        }



//        System.out.println(rg.getRelationGraphFile());
//        CallRelationList crl = rg.getCallRelationListForRelationPair(new RelationPair(15, 16));
        rg.showMessage();
    }
}
