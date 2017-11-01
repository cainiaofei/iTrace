package cn.edu.nju.cs.itrace4.relation.io;

import cn.edu.nju.cs.itrace4.io._;
import cn.edu.nju.cs.itrace4.relation.info.*;

import java.io.File;
import java.sql.*;
import java.util.*;

/**
 * Created by niejia on 15/2/27.
 */
public class DataRelationIO implements DataRelationIOInterface{

    private static List<FieldMonitor> faList;
    private static List<FieldMonitor> fmList;
    private static List<FieldMonitor> ppList;


    public static DataRelationList parser(String relationDBDirPath) {

        File relationDBDirFile = new File(relationDBDirPath);
        if (!relationDBDirFile.exists()) {
            _.abort("Data Relation DB dir doesn't exist");
        }

        faList = relationParser(relationDBDirFile.getPath() + "/test1.db", DataRelationType.FieldAccess);
        fmList = relationParser(relationDBDirFile.getPath() + "/test2.db", DataRelationType.FieldModification);
        /**
         * @date 2017/10/31  
         * @description 暂时注释掉
         * */
        ppList = relationParser(relationDBDirFile.getPath() + "/test3.db", DataRelationType.ParameterPass);

        return getDataRelationList(faList, fmList, ppList);
    }

    private static DataRelationList getDataRelationList(List<FieldMonitor> faList, List<FieldMonitor> fmList, List<FieldMonitor> ppList) {

        List<FieldMonitor> fieldMonitorsList = new ArrayList<>();
        for (FieldMonitor fa : faList) {
            fieldMonitorsList.add(fa);
        }

        for (FieldMonitor fm : fmList) {
            fieldMonitorsList.add(fm);
        }

        DataRelationList dataRelationList = new DataRelationList();

        // why ignore test3.db when we compute usage
        for (DataRelation dr : getUsageRelations(fieldMonitorsList)) {
            dataRelationList.add(dr);
        }

        /**
         * @author zzf 
         * @date 2017/10/31
         * @description  暂时注释掉pp!!!!!!!!! 后面记得去掉注释 
         */
        for (FieldMonitor pp : ppList) {
            fieldMonitorsList.add(pp);
        }

        DataRelationList drList = getShareFieldRelations(fieldMonitorsList);
        System.out.println(" dataRelationList = " + dataRelationList.size() );
        System.out.println(" drList = " + drList.size() );
        for (DataRelation dr : drList) {
            dataRelationList.add(dr);
        }
        System.out.println(" dataRelationList = " + dataRelationList.size() );

//        DataRelationList dataRelationList = getShareFieldRelations(fieldMonitorsList);
//        DataRelationList dataRelationList = getUsageRelations(fieldMonitorsList);

        return dataRelationList;
    }



    private static DataRelationList getUsageRelations(List<FieldMonitor> fieldMonitorsList) {
        DataRelationList dataRelationList = new DataRelationList();

        for (FieldMonitor monitor : fieldMonitorsList) {

            DataRelation dr = new DataRelation();
            dr.callerClass = getClassNameFromDBFormat(monitor.getMcSignature());
            dr.calleeClass = getClassNameFromDBFormat(monitor.getfSignature());
            dr.callerMethod = monitor.getMcSignature() + "#" + monitor.getMethodName();
            dr.calleeMethod = "none";
            dr.type = monitor.getfSignature();
            dr.hashcode = monitor.getfHashcode();
            dr.isUsage = true;
            dataRelationList.add(dr);
        }

        return dataRelationList;
    }

    /**
    private static DataRelationList getShareFieldRelations(List<FieldMonitor> fieldMonitorsList) {
        // find method access same Field
        Map<String, List<String>> methodAccessSameFieldMap = new LinkedHashMap<>();
        for (FieldMonitor monitor : fieldMonitorsList) {
            if (!monitor.getfHashcode().equals("null")
                    && !monitor.getfHashcode().equals("primitive")
                  ) {

                String accessFieldIdentify;

                // handle the static
                if (monitor.getfHashcode().equals("static")) {
                    if (monitor.getType().equals(DataRelationType.FieldAccess)) {
                        accessFieldIdentify = "static_"+getClassNameFromDBFormat(((FieldAccess) monitor).getcSignature()) + "_" + getClassNameFromDBFormat(monitor.getfSignature()) + "#" + monitor.getfName();
                    } else if (monitor.getType().equals(DataRelationType.FieldModification)) {
                        accessFieldIdentify = "static_"+getClassNameFromDBFormat(((FieldModification) monitor).getcSignature()) + "_" + getClassNameFromDBFormat(monitor.getfSignature()) + "#" + monitor.getfName();
                    } else {
                        accessFieldIdentify = "";
                        _.abort("Find static field in test3 ???");
                    }

//                    System.out.println("static accessFieldIdentify = " + accessFieldIdentify );
                } else {
                    accessFieldIdentify = getClassNameFromDBFormat(monitor.getfSignature()) + "#" + monitor.getfHashcode();
                }

                String methodIdentify = getClassNameFromDBFormat(monitor.getMcSignature()) + "#" + monitor.getMethodName();

                if (!methodAccessSameFieldMap.containsKey(accessFieldIdentify)) {
                    List<String> methodIdentifyList = new ArrayList<>();
                    methodIdentifyList.add(methodIdentify);
                    methodAccessSameFieldMap.put(accessFieldIdentify, methodIdentifyList);
                } else {
                    List<String> methodIdentifyList = methodAccessSameFieldMap.get(accessFieldIdentify);
                    if (!methodIdentifyList.contains(methodIdentify)) {
                        methodIdentifyList.add(methodIdentify);
                    }
                    methodAccessSameFieldMap.put(accessFieldIdentify, methodIdentifyList);
                }
            }


        }

        for (FieldMonitor monitor : fieldMonitorsList) {
            if (monitor.getType().equals(DataRelationType.FieldModification)) {
                String newValue = ((FieldModification) monitor).getNewValue();

                if (!monitor.getfHashcode().equals("primitive") && newValue != null && !newValue.equals("write") && !newValue.equals("null")) {
                    String accessFieldIdentify = getClassNameFromDBFormat(monitor.getfSignature()) + "#" + newValue;
                    String methodIdentify = getClassNameFromDBFormat(monitor.getMcSignature()) + "#" + monitor.getMethodName();

                    if (!methodAccessSameFieldMap.containsKey(accessFieldIdentify)) {
                        List<String> methodIdentifyList = new ArrayList<>();
                        methodIdentifyList.add(methodIdentify);
                        methodAccessSameFieldMap.put(accessFieldIdentify, methodIdentifyList);
                    } else {
                        List<String> methodIdentifyList = methodAccessSameFieldMap.get(accessFieldIdentify);
                        if (!methodIdentifyList.contains(methodIdentify)) {
                            methodIdentifyList.add(methodIdentify);
                        }
                        methodAccessSameFieldMap.put(accessFieldIdentify, methodIdentifyList);
                    }
                }

            }
        }

        System.out.println(" methodAccessSameFieldMap = " + methodAccessSameFieldMap.size() );
        System.out.println("-----------------------------------" );

        Set<String> staticFields = new HashSet<>();
        for (String s : methodAccessSameFieldMap.keySet()) {
            if (s.startsWith("static")) {
//                methodAccessSameFieldMap.remove(s);
                staticFields.add(s);
            }
//            System.out.println("haha " + s + " " + methodAccessSameFieldMap.get(s).size());
        }

        for (String s : staticFields) {
//            methodAccessSameFieldMap.remove(s);
        }

        System.out.println("-----------------------------------" );

        DataRelationList dataRelationList = new DataRelationList();
        for (String accessFieldIdentify : methodAccessSameFieldMap.keySet()) {
//            System.out.println(" accessFieldIdentify = " + accessFieldIdentify );
            if (methodAccessSameFieldMap.get(accessFieldIdentify).size() > 1) {
                for (DataRelation dr : getDataRelationAccessListSameFiled(accessFieldIdentify, methodAccessSameFieldMap.get(accessFieldIdentify))) {
                    if (!dr.getCalleeMethod().equals(dr.getCallerMethod())) {
                        dataRelationList.add(dr);
                    }
                }
            }
        }

        return dataRelationList;
    }
     */

    private static DataRelationList getShareFieldRelations(List<FieldMonitor> fieldMonitorsList) {
        // find method access same Field
        Map<String, List<String>> methodAccessSameFieldMap = new LinkedHashMap<>();
        for (FieldMonitor monitor : fieldMonitorsList) {
            if (!monitor.getfHashcode().equals("null")
                    && !monitor.getfHashcode().equals("static")
                    && !monitor.getfHashcode().equals("primitive")) {
                String accessFieldIdentify = getClassNameFromDBFormat(monitor.getfSignature()) + "#" + monitor.getfHashcode();
//                accessFieldIdentify = DAOFactory#3387681
//                System.out.println(" accessFieldIdentify = " + accessFieldIdentify );

                String methodIdentify = getClassNameFromDBFormat(monitor.getMcSignature()) + "#" + monitor.getMethodName();

                if (!methodAccessSameFieldMap.containsKey(accessFieldIdentify)) {
                    List<String> methodIdentifyList = new ArrayList<>();
                    methodIdentifyList.add(methodIdentify);
                    methodAccessSameFieldMap.put(accessFieldIdentify, methodIdentifyList);
                } else {
                    List<String> methodIdentifyList = methodAccessSameFieldMap.get(accessFieldIdentify);
                    if (!methodIdentifyList.contains(methodIdentify)) {
                        methodIdentifyList.add(methodIdentify);
                    }
                    methodAccessSameFieldMap.put(accessFieldIdentify, methodIdentifyList);
                }
            }
        }


        System.out.println(" methodAccessSameFieldMap = " + methodAccessSameFieldMap.size());

//        DataRelationList dataRelationList = new DataRelationList();
        DataRelationList dataRelationList = new DataRelationList();
        for (String accessFieldIdentify : methodAccessSameFieldMap.keySet()) {
//            System.out.println(" accessFieldIdentify = " + accessFieldIdentify );
            if (methodAccessSameFieldMap.get(accessFieldIdentify).size() > 1) {
                for (DataRelation dr : getDataRelationAccessListSameFiled(accessFieldIdentify, 
                		methodAccessSameFieldMap.get(accessFieldIdentify))) {
                    if (!dr.getCalleeMethod().equals(dr.getCallerMethod())) {
                        dataRelationList.add(dr);
                    }
                }
            }
        }
        return dataRelationList;
    }

    private static DataRelationList getDataRelationAccessListSameFiled(String accessFieldIdentify, List<String> methodList) {
        DataRelationList dataRelationList = new DataRelationList();


        String type = accessFieldIdentify.split("#")[0];
        String hashcode = accessFieldIdentify.split("#")[1];

        for (int i = 0; i < methodList.size(); i++) {
            for (int j = i + 1; j < methodList.size(); j++) {
                DataRelation dr = new DataRelation();

                dr.callerClass = (methodList.get(i).split("#")[0]);
                dr.calleeClass = (methodList.get(j).split("#")[0]);
                dr.callerMethod = (methodList.get(i));
                dr.calleeMethod = (methodList.get(j));
                dr.type = type;
                dr.hashcode = hashcode;
                dr.isUsage = false;
                dataRelationList.add(dr);
            }
        }

        return dataRelationList;
    }

    private static List<FieldMonitor> relationParser(String relationDBPath, DataRelationType dataRelationType) {

        File relationDBFile = new File(relationDBPath);
        if (!relationDBFile.exists()) {
            _.abort("Data Relation DB file doesn't exist");
        }

        Connection con;
        Statement stmt;

        List<FieldMonitor> fieldMonitorList = new ArrayList<>();

        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:" + relationDBPath);
            con.setAutoCommit(false);

            System.out.printf("Opened %s successfully\n", relationDBFile.getName());
            stmt = con.createStatement();

            String dbFileName = "";
            if (dataRelationType.equals(DataRelationType.FieldAccess)) {
                dbFileName = "fieldAccess";
            } else if (dataRelationType.equals(DataRelationType.FieldModification)) {
                dbFileName = "fieldModification";
            } else if (dataRelationType.equals(DataRelationType.ParameterPass)) {
                dbFileName = "parameterPass";
            }

            String sql = "SELECT * FROM " + dbFileName + ";";
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                if (dataRelationType.equals(DataRelationType.FieldAccess)) {
                    FieldAccess fa = new FieldAccess();
                    fa.setcSignature(rs.getString("cSignature").trim());
                    fa.setoHashcode(rs.getString("oHashcode").trim());
                    fa.setfSignature(rs.getString("fSignature").trim());
                    fa.setfHashcode(rs.getString("fHashcode").trim());
                    fa.setfName(rs.getString("fName").trim());
                    fa.setMcSignature(rs.getString("McSignature").trim());
                    fa.setMethodName(rs.getString("methodName").trim());
                    fa.setMethodSignature(rs.getString("methodSignature").trim());
                    fa.setType(DataRelationType.FieldAccess);
                    fieldMonitorList.add(fa);
                } else if (dataRelationType.equals(DataRelationType.FieldModification)) {
                    FieldModification fm = new FieldModification();
                    fm.setcSignature(rs.getString("cSignature").trim());
                    fm.setoHashcode(rs.getString("oHashcode").trim());
                    fm.setfSignature(rs.getString("fSignature").trim());
                    fm.setfHashcode(rs.getString("fHashcode").trim());
                    fm.setfName(rs.getString("fName").trim());
                    fm.setMcSignature(rs.getString("McSignature").trim());
                    fm.setMethodName(rs.getString("methodName").trim());
                    fm.setMethodSignature(rs.getString("methodSignature").trim());
                    fm.setNewValue(rs.getString("newValue").trim());
                    fm.setType(DataRelationType.FieldModification);
                    fieldMonitorList.add(fm);
                } else if (dataRelationType.equals(DataRelationType.ParameterPass)) {
                    ParameterPass pp = new ParameterPass();
                    pp.setfSignature(rs.getString("fSignature").trim());
                    pp.setfHashcode(rs.getString("fHashcode").trim());
                    pp.setfName(rs.getString("fName").trim());
                    pp.setMcSignature(rs.getString("McSignature").trim());
                    pp.setMethodName(rs.getString("methodName").trim());
                    pp.setMethodSignature(rs.getString("methodSignature").trim());
                    pp.setType(DataRelationType.ParameterPass);
                    fieldMonitorList.add(pp);
                }
            }

            rs.close();

            System.out.println("Table " + dataRelationType.toString() + " parsed successfully");
            System.out.printf("Closed %s successfully\n", dataRelationType.toString());
            stmt.close();
            con.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.printf("Read %d " + dataRelationType + " field relations from data.db\n", fieldMonitorList.size());

        return fieldMonitorList;
    }


    private static String getClassNameFromDBFormat(String dbFormat) {
        String className = "";
        if (!dbFormat.endsWith("_jsp;")) {
            // Ledu/ncsu/csc/itrust/dao/DAOFactory; -> DAOFactory
            String tokens[] = dbFormat.split("/");
            className = tokens[tokens.length - 1].split(";")[0];
        } else {
            // Lorg/apache/jsp/auth/patient/viewVisitedHCPs_jsp -> auth.patient.viewVisitedHCPs_jsp
            dbFormat = dbFormat.replace("_002d", "-");
            className = dbFormat.split("/jsp/")[1].replace("/", ".").split(";")[0];
        }

//        System.out.println(" className = " + className.split("\\$")[0] );
        return className.split("\\$")[0];
    }

    public static void main(String[] args) {
        String relationDirPath = "data/exp/iTrust/relation";
        DataRelationIO dataRelationIO = new DataRelationIO();
        DataRelationList dataRelationList = dataRelationIO.parser(relationDirPath);
    }
}

//            Set<String> newValueSet = new HashSet<>();
//            handle the new value
//            if (monitor.getType().equals(DataRelationType.FieldModification)) {
//                String newValue = ((FieldModification) monitor).getNewValue();
//                if (!monitor.getfHashcode().equals("primitive") && newValue != null && !newValue.equals("write") && !newValue.equals("null")) {
//                    String accessFieldIdentify = getClassNameFromDBFormat(monitor.getfSignature()) + "#" + newValue;
//                    String methodIdentify = getClassNameFromDBFormat(monitor.getMcSignature()) + "#" + monitor.getMethodName();
////
//                    if (!methodAccessSameFieldMap.containsKey(accessFieldIdentify)) {
//                        List<String> methodIdentifyList = new ArrayList<>();
//                        methodIdentifyList.add(methodIdentify);
////                        newValueSet.add(accessFieldIdentify);
//////                        System.out.println("new value  accessFieldIdentify = " + accessFieldIdentify);
//////                        methodAccessSameFieldMap.put(accessFieldIdentify, methodIdentifyList);
//                    } else {
//                        List<String> methodIdentifyList = methodAccessSameFieldMap.get(accessFieldIdentify);
//                        if (!methodIdentifyList.contains(methodIdentify)) {
////                            System.out.println("new value added for " + accessFieldIdentify + " " + methodIdentify);
//                            methodIdentifyList.add(methodIdentify);
//                        }
//////                        System.out.println("existed value  accessFieldIdentify = " + accessFieldIdentify);
//////                        methodAccessSameFieldMap.put(accessFieldIdentify, methodIdentifyList);
//                    }
//                }
//            }