package cn.edu.nju.cs.itrace4.io;

import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.type.Granularity;
import cn.edu.nju.cs.itrace4.io._;
import cn.edu.nju.cs.itrace4.parser.RTMParser;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author zzf <tiaozhanzhe668@163.com>
 * @date 2017/10/12
 * @description make some change based on the code of niejia, because the rtm format of maven is 
 * 	different with iTrust/Gantt/jHotDraw. 
 */
public class SqliteIOForGit implements SqliteIOInterface{

	private static String ucPath;
	
	public static void setUCPath(String ucPath) {
		SqliteIOForGit.ucPath = ucPath;
	}
	
    public static SimilarityMatrix readRTMFromDB(String path, Granularity granularity) {
        File dbFile = new File(path);
        if (!dbFile.exists()) {
            _.abort("DB file doesn't exist");
        }

        Connection con;
        Statement stmt;
        List<String> columnsList = new ArrayList<>();
        String contents = null;

        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:" + path);
            con.setAutoCommit(false);

           // System.out.printf("Opened %s successfully\n", dbFile.getName());
            stmt = con.createStatement();

            // Store columns from table reqs
            columnsList = getColumnsName();
            /*
             * Store trace links from RTM in text format, such as
             * UC1 AuthDAO
             */
            contents = getTracesInRTM(stmt, columnsList);

            System.out.println("Table reqs parsed successfully");
            System.out.printf("Closed %s successfully\n", dbFile.getName());
//            System.out.println(contents);
            stmt.close();
            con.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("RTM Info: ");
        System.out.printf("columns num: %d\n", columnsList.size());
        System.out.printf("records num: %d\n", contents.split("\n").length);
        
        /**
         * @date 2017/10/12 
         * @description get cloumnsList from contents because 
         */ 
        columnsList = getFromContents(contents);
        SimilarityMatrix sm = RTMParser.createSimilarityMatrix(columnsList, contents, granularity);

        // Export the parsed RTM if you want
        _.writeFile(sm.toString(), dbFile.getParent() + "/RTM_" + granularity + ".txt");
        return sm;
    }
    
    public static List<String> getFromContents(String strs) {
    	Set<String> ucNameSet = new LinkedHashSet<String>();
    	String[] strArr = strs.split("\n");
    	for(String str:strArr) {
    		ucNameSet.add(str.split(" ")[0].trim());
    	}
    	
    	List<String> res = new ArrayList<String>();
    	for(String str:ucNameSet) {
    		res.add(str);
    	}
    	return res;
    }
    
    /**
     * @author zzf 
     * @date 2017/10/12
     * @description return rtm, the format is: uc className 1.0
     */
    private static String getTracesInRTM(Statement stmt, List<String> columnsList) {
    	StringBuilder sb = new StringBuilder();
    	String sql = "select request as uc, file_path as className from rtm";
    	int number = 1;
    	try {
    		ResultSet rs = stmt.executeQuery(sql);
    		while(rs.next()) {
//    			boolean hasRelevantFuncClass = false;
    			String ucName = "req"+number;
    			String classNameStrs = rs.getString("className");
 //   			System.out.println(ucName+"=>"+classNameStrs);
    			String[] classList = getClassNameList(classNameStrs);
    			for(String className:classList) {//for
    				className = className.replace(" ", "");
    				//this is a bug, the req in uc directory will not response with rtm
//    				if(className.endsWith("Test") || className.endsWith("TestCase")) {
//    					continue;
//    				}
    				sb.append(ucName+" "+className+" "+"1.0"+" ");
    				sb.append("\n");
//    				hasRelevantFuncClass = true;
    			}//for
//    			if(hasRelevantFuncClass==false) {
//    				continue;
//    			}
    			number++;
    		}
    	}catch(SQLException e) {
    		e.printStackTrace();
    	}
    	return sb.toString();
    }
    

    /**
     * @date 2017/10/16
     * @description ensure no duplicate. 
     */
    private static String[] getClassNameList(String strs) {
    	Set<String> set = new HashSet<String>();
    	String[] strArr = strs.split("和");
		for(int i = 0; i < strArr.length;i++) {
			//System.out.println(strArr[i]);
			strArr[i] = strArr[i].replace("/", ".");
			strArr[i] = strArr[i].substring(0, strArr[i].lastIndexOf("."));
			set.add(strArr[i]);
		}
		strArr = new String[set.size()];
		int index = 0;
		for(String str:set) {
			strArr[index] = str;
			index++;
		}
		return strArr;
	}
    /**
     * @author zzf
     * @date 2017/10/12
     * @description get uc name list
     */
    private static List<String> getColumnsName() {
    	File ucDir = new File(ucPath);
    	File[] ucFiles = ucDir.listFiles();
    	List<String> ucNameList = new LinkedList<String>();
    	for(File ucFile:ucFiles) {
    		String ucFullName = ucFile.getName();
    		String ucName = ucFullName.substring(0,ucFullName.lastIndexOf("."));
    		ucNameList.add(ucName);
    	}
    	return ucNameList;
    }

}
