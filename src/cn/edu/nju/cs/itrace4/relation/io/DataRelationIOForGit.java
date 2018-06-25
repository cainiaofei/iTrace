package cn.edu.nju.cs.itrace4.relation.io;

import cn.edu.nju.cs.itrace4.relation.info.*;
import cn.edu.nju.cs.itrace4.util.io._;

import java.io.File;
import java.sql.*;
import java.util.*;

/**
 * Created by niejia on 15/2/27.
 */
public class DataRelationIOForGit implements DataRelationIOInterface {
	static int count = 0;
    public static DataRelationList parser(String relationDBDirPath) {

        File relationDBDirFile = new File(relationDBDirPath);
        if (!relationDBDirFile.exists()) {
            _.abort("Data Relation DB dir doesn't exist");
        }

        return getDataRelationList(relationDBDirPath);
    }

    
    private static DataRelationList getDataRelationList(String relationDBPath) {
    	File relationDBFile = new File(relationDBPath);
        if (!relationDBFile.exists()) {
            _.abort("Data Relation DB file doesn't exist");
        }

        Connection con;
        Statement stmt;
        DataRelationList dataRelationList = new DataRelationList();
        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:" + relationDBPath);
            con.setAutoCommit(false);

            System.out.printf("Opened %s successfully\n", relationDBFile.getName());
            stmt = con.createStatement();


            String sql = "select source as caller, sink as callee, "
            		+ "edgeData as shareData from graph where edgeData!=\"call\"";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
            	String caller = rs.getString("caller");
            	String callee = rs.getString("callee");
            	String callerClassName = getClassName(caller);
            	String calleeClassName = getClassName(callee);
            	String callerClassMethodName = getMethodName(caller);
            	String calleeClassMethodName = getMethodName(callee);
            	
            	if((callerClassName+"*"+callerClassMethodName).
            			equals(calleeClassName+"*"+calleeClassMethodName)) {
            		continue;
            	}
            	
            	String data = rs.getString("shareData");
            	Map<String,String> shareDataList = getShareDataList(data);
            	for(String shareDataType:shareDataList.keySet()) {
            		DataRelation dr = new DataRelation();
            		dr.setCalleeClass(calleeClassName);
            		dr.setCalleeMethod(calleeClassName+"#"+calleeClassMethodName);
            		dr.setCallerClass(callerClassName);
            		dr.setCallerMethod(callerClassName+"#"+calleeClassMethodName);
            		dr.setType(shareDataType);
            		dr.setHashcode(shareDataList.get(shareDataType));
            		dataRelationList.add(dr);
            	}
            	//System.out.println(callerClassName+"---->"+callerClassMethodName);
            }
        }catch (Exception e) {
               e.printStackTrace();
        }
        
        return dataRelationList;
	}




    /*
     * Ljava/io/File;:250630415#Ljava/util/List;:253878322#remote/ProcessRemoteResourcesMojo;:542791257.256346753.425015667
     * ==> File  250630415, List 253878322, ProcessRemoteResourcesMojo 542791257
     */
    public static Map<String,String> getShareDataList(String data){
    	Map<String,String> map = new HashMap<String,String>();
    	String[] dataTypeArr = data.split("#");
    	for(String dataType:dataTypeArr) {
    		//filter dataType:[C:1190143242.1259537789.104727
    		if(!dataType.contains(";")) {
    			count++;
    			continue;
    		}
    		if(dataType.split(";:").length<2) {
    			count++;
    			continue;
    		}
    		String dataTypeName = getDataTypeName(dataType);
    		String hashCode = getHashCode(dataType);
    		map.put(dataTypeName, hashCode);
    	}
    	return map;
    }
    
    
    
    /**
     * @author zzf
     * @date 2017/10/13
     * remote/ProcessRemoteResourcesMojo;:542791257.256346753.425015667 ==> 542791257
     */
    private static String getHashCode(String dataType) {
    	//System.out.println("dataType:"+dataType);
    	return dataType.split(";:")[1].split("\\.")[0];
	}


	private static String getDataTypeName(String dataType) {
		String fullName = dataType.split(";")[0];
		String[] strs = fullName.split("/");
		return strs[strs.length-1];
	}
    
    
//	private static Set<String> getShareDataList(String data) {
//		Set<String> set = new HashSet<String>();
//		String[] strs = data.split(":");
//		String dataTypeFullName = strs[0];
//		String[] temp = dataTypeFullName.split("/");
//		String dataTypeName = temp[temp.length-1].substring(0, temp[temp.length-1].length()-1);
//		
//		String hashCodeStr = strs[1];
//		String[] hashCodeArr = hashCodeStr.split("\\.");
//		
//		for(String hashCode : hashCodeArr) {
//			set.add(dataTypeName+"#"+hashCode);
//		}
//		return set;
//	}




	private static String getClassName(String str) {
		int pos = str.indexOf("(");
		String[] strs = str.substring(0, pos).split("\\.");
		return strs[strs.length-2];
	}

	private static String getMethodName(String str) {
		int pos = str.indexOf("(");
		String[] strs = str.substring(0, pos).split("\\.");
		return strs[strs.length-1];
	}
	
    public static void main(String[] args) {
        String relationDirPath = "data\\exp\\Maven\\relation\\call.db";
        DataRelationIO dataRelationIO = new DataRelationIO();
        DataRelationList dataRelationList = dataRelationIO.parser(relationDirPath);
        System.out.println(count);
    }
}

