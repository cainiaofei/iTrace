package cn.edu.nju.cs.itrace4.relation.io;

import cn.edu.nju.cs.itrace4.io._;
import cn.edu.nju.cs.itrace4.relation.info.CallRelation;
import cn.edu.nju.cs.itrace4.relation.info.CallRelationList;

import java.io.File;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by niejia on 15/2/25.
 */

/*
Parse
 */
public class CallRelationIO implements CallRelationIOInterface{


    public static CallRelationList parser(String callDBPath) {

        File callDBFile = new File(callDBPath);
        if (!callDBFile.exists()) {
            _.abort("Call DB file doesn't exist");
        }

        Connection con;
        Statement stmt;

        CallRelationList callRelationList = new CallRelationList();

        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:" + callDBPath);
            con.setAutoCommit(false);

            System.out.printf("Opened %s successfully\n", callDBFile.getName());
            stmt = con.createStatement();

            String sql = "SELECT * FROM callGraph;";
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String callerInDB = rs.getString("caller").trim();
                String calleeInDB = rs.getString("callee").trim();
                
                /**
                 * @author zzf
                 * @date 2017.11.20
                 * @description there are two formats in call.db  Lorg... and org ...
                 * unify them and transfer Lorg... to org...
                 */
                if(callerInDB.startsWith("L")) {
                	callerInDB = callerInDB.substring(1);
                }
                if(calleeInDB.startsWith("L")) {
                	calleeInDB = calleeInDB.substring(1);
                }
                
                
                String callerMethod = sqlFormatToIdFormatInCallDB(callerInDB);
                String calleeMethod = sqlFormatToIdFormatInCallDB(calleeInDB);
                String callerClass = callerMethod.split("#")[0];
                String calleeClass = calleeMethod.split("#")[0];
                CallRelation cr = new CallRelation(callerClass, calleeClass, callerMethod, calleeMethod);
//                System.out.println(cr);
                callRelationList.add(cr);
            }

            rs.close();

            System.out.println("Table reqs parsed successfully");
            System.out.printf("Closed %s successfully\n", callDBFile.getName());
            stmt.close();
            con.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.printf("Read %d call relations from call.db", callRelationList.size());
        return callRelationList;
    }

    private static String sqlFormatToIdFormatInCallDB(String sqlFormat) {
    	System.out.println(sqlFormat);
        String[] tokens = sqlFormat.split("\\.");

        // find Java method
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].startsWith("MPEGecoder")) continue;
            if (Character.isUpperCase(tokens[i].charAt(0))) {
                StringBuilder sb = new StringBuilder();

//                String s = tokens[i];
//                sb.append(tokens[i]);
                sb.append(tokens[i].split("\\$")[0]);
                sb.append("#");
                sb.append(tokens[i + 1].split("\\(")[0]);
                return sb.toString();
            }
        }

//        find JSP method
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equals("jsp")) {

                for (int j = i; j < tokens.length; j++) {
                    if (tokens[j].endsWith("_jsp")) {

                        for (int k = i + 1; k <= j; k++) {
                            sb.append(tokens[k]);
                            if (k != j) {
                                sb.append(".");
                            }
                        }
                        sb.append("#");
                        sb.append(tokens[j + 1].split("\\(")[0]);
                    }
                }
            }
        }
        // [keng]
        String res = sb.toString().replace("_002d", "-");
        System.out.println("res:"+res);
        return res;
    }

    
    /**
     * @author zzf
     * @date 2017.12.26
     * @description if there are many call dependency a.a --->b.b we regard it 
     * 		as one call dependency. 
     */
    public static CallRelationList parserIgnoreForLoop(String callDBPath) {
    	Set<String> set = new HashSet<String>();
    	
        File callDBFile = new File(callDBPath);
        if (!callDBFile.exists()) {
            _.abort("Call DB file doesn't exist");
        }

        Connection con;
        Statement stmt;

        CallRelationList callRelationList = new CallRelationList();

        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:" + callDBPath);
            con.setAutoCommit(false);

            System.out.printf("Opened %s successfully\n", callDBFile.getName());
            stmt = con.createStatement();

            String sql = "SELECT * FROM callGraph;";
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String callerInDB = rs.getString("caller").trim();
                String calleeInDB = rs.getString("callee").trim();
                
                /**
                 * @author zzf
                 * @date 2017.11.20
                 * @description there are two formats in call.db  Lorg... and org ...
                 * unify them and transfer Lorg... to org...
                 */
                if(callerInDB.startsWith("L")) {
                	callerInDB = callerInDB.substring(1);
                }
                if(calleeInDB.startsWith("L")) {
                	calleeInDB = calleeInDB.substring(1);
                }
                
                
                String callerMethod = sqlFormatToIdFormatInCallDB(callerInDB);
                String calleeMethod = sqlFormatToIdFormatInCallDB(calleeInDB);
                String callerClass = callerMethod.split("#")[0];
                String calleeClass = calleeMethod.split("#")[0];
                
                String label = callerMethod + ";" + calleeMethod + ";" +
                		callerClass + ";" + calleeClass;
                if(!set.contains(label)) {
                	set.add(label);
                	CallRelation cr = new CallRelation(callerClass, calleeClass, callerMethod, calleeMethod);
//                    System.out.println(cr);
                    callRelationList.add(cr);
                }
                
//                CallRelation cr = new CallRelation(callerClass, calleeClass, callerMethod, calleeMethod);
////                System.out.println(cr);
//                callRelationList.add(cr);
            }

            rs.close();

            System.out.println("Table reqs parsed successfully");
            System.out.printf("Closed %s successfully\n", callDBFile.getName());
            stmt.close();
            con.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.printf("Read %d call relations from call.db", callRelationList.size());
        return callRelationList;
    }

    
    
    public static void main(String[] args) {
//        String classDirPath = "data/exp/iTrust/class/code";
//        String methodDirPath = "data/exp/iTrust/method/code";
//        String callDBPath = "data/exp/iTrust/relation/call.db";

        String classDirPath = "data/exp/Gantt/class/code";
        String methodDirPath = "data/exp/Gantt/method/code";
        String callDBPath = "data/exp/Gantt/relation/call.db";

        CallRelationList callRelationList = CallRelationIO.parser(callDBPath);
    }
}
