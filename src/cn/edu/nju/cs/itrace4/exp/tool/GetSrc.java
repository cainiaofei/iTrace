package cn.edu.nju.cs.itrace4.exp.tool;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GetSrc {
	private Set<String> nameInGraph;
	private String rtmPath;
	
	public GetSrc(String rtmPath) {
		this.rtmPath = rtmPath;
	}
	
	
	public void getSrcFromMasterBasedOnGraphDB(String masterPath,String targetPath, String dbPath) throws IOException {
		nameInGraph = getGraphRelevantCode(dbPath);
		getSrcFromProject(masterPath, targetPath);
	}
	
	public void getSrcFromMasterBasedOnGraphDBNewFormatDB(String masterPath,String targetPath, String dbPath,
			String tableName,String caller,String callee) throws IOException {
		//nameInGraph = getGraphRelevantCodeFromNewFormatDB(dbPath,tableName,caller,callee);
		nameInGraph = getGraphRelevantCodeFromNewFormatDB(rtmPath,"rtm","file_path");
		getSrcFromProject(masterPath, targetPath);
	}
	
	private Set<String> getGraphRelevantCode(String dbPath){
		Set<String> set = new HashSet<String>();
		Connection con;
        Statement stmt;

        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            con.setAutoCommit(false);
            stmt = con.createStatement();
            
            String sql = "select * from callGraph";
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
            	String former = rs.getString("source");//source
            	String latter = rs.getString("sink");//sink
            	if(!former.contains("tutorial")) {
            		String formerName = getNameFromFullClassName(former);
            		set.add(formerName);
            	}
            	
            	if(!latter.contains("tutorial")) {
            		String latterName = getNameFromFullClassName(latter);
                	set.add(latterName);
            	}
            }
            
            stmt.close();
            con.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
		return set;
	}
	
	private Set<String> getGraphRelevantCodeFromNewFormatDB(String dbPath,String tableName,String colName){
		Set<String> set = new HashSet<String>();
		Connection con;
        Statement stmt;

        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            con.setAutoCommit(false);
            stmt = con.createStatement();
            
            String sql = "select * from "+tableName;
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
            	String classNameCombine = rs.getString(colName);//source
            	/**
            	 * @author zzf
            	 * @date 2018.1.11
            	 */
            	String[] nameList = getClassNameList(classNameCombine);
            	for(String name:nameList) {
            		//exclude test class
            		if(name.endsWith("Test")) {
            			continue;
            		}
            		else {
            			set.add(name);
            		}
            	}
            }
            stmt.close();
            con.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
		return set;
	}
	
	
	private Set<String> getGraphRelevantCodeFromNewFormatDB(String dbPath,
			String tableName,String caller,String callee){
		Set<String> set = new HashSet<String>();
		Connection con;
        Statement stmt;

        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            con.setAutoCommit(false);
            stmt = con.createStatement();
            
            String sql = "select * from "+tableName;
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
            	String former = rs.getString(caller);//source
            	String latter = rs.getString(callee);//sink
            	/**
            	 * @author zzf
            	 * @date 2017.11.20
            	 * @description  there exists two class formats, Lorg.. and org.. Unify these two formats and 
            	 * 	transfer Lorg.. to org.. 
            	 */
            	if(former.startsWith("L")) {
            		former = former.substring(1);
            	}
            	if(latter.startsWith("L")) {
            		latter = latter.substring(1);
            	}
            	
            	if(!former.contains("tutorial")) {
            		String formerName = getNameFromFullClassName(former);
            		set.add(formerName);
            	}
            	
            	if(!latter.contains("tutorial")) {
            		String latterName = getNameFromFullClassName(latter);
                	set.add(latterName);
            	}
            }
            
            stmt.close();
            con.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
		return set;
	}
	
	/**
	 * @author zzf
	 * @date 2018.1.11
	 * @description parse the file_path in rtm into several class 
	 */
	private static String[] getClassNameList(String strs) {
		Set<String> set = new HashSet<String>();
		String[] strArr = strs.split("和");
		for(int i = 0; i < strArr.length;i++) {
			//System.out.println(strArr[i]);
			strArr[i] = strArr[i].replace("/", ".");
			strArr[i] = strArr[i].substring(0, strArr[i].lastIndexOf("."));
			strArr[i] = strArr[i].substring(strArr[i].lastIndexOf(".")+1);
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
	
	
	private Set<String> getAllGraphRelevantCodeFromNewFormatDB(String basePath,
			String tableName,String caller,String callee){
		Set<String> set = new HashSet<String>();
		Connection con;
        Statement stmt;

        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:" + basePath+File.separator+"call.db");
            con.setAutoCommit(false);
            stmt = con.createStatement();
            
            String sql = "select * from "+tableName;
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
            	String former = rs.getString(caller);//source
            	String latter = rs.getString(callee);//sink
            	/**
            	 * @author zzf
            	 * @date 2017.11.20
            	 * @description  there exists two class formats, Lorg.. and org.. Unify these two formats and 
            	 * 	transfer Lorg.. to org.. 
            	 */
            	if(former.startsWith("L")) {
            		former = former.substring(1);
            	}
            	if(latter.startsWith("L")) {
            		latter = latter.substring(1);
            	}
            	
            	if(!former.contains("tutorial")) {
            		String formerName = getNameFromFullClassName(former);
            		set.add(formerName);
            	}
            	
            	if(!latter.contains("tutorial")) {
            		String latterName = getNameFromFullClassName(latter);
                	set.add(latterName);
            	}
            }
            
            stmt.close();
            con.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
		return set;
	}
	
	
	private String getNameFromFullClassName(String fullName) {
		String[] args = fullName.split("\\.|\\_|\\$");
		for(String arg:args) {
			if(arg.charAt(0)>='A' && arg.charAt(0)<='Z') {
				return arg;
			}
		}
		
		System.out.println(fullName);
		System.err.println("---err--methodName:getNameFromFullClassName()："+args[args.length-2]);
		//System.exit(-1);
		/**
		 * org.apache.pig.piggybank.evaluation.math.toDegrees.-init-()V
		 */
		return args[args.length-2];
	}

	private void getSrcFromProject(String originPath,String targetPath) throws IOException {
		File dir = new File(originPath);
		if(dir.isDirectory()) {
			if(!dir.getName().equals("test")) {
				File[] childs = dir.listFiles();
				for(File child:childs) {
					getSrcFromProject(child.getAbsolutePath(), targetPath);
				}
			}
		}
		else {
			String fileName = dir.getName();
			if(fileName.endsWith(".java")) {
				String className = fileName.substring(0,fileName.lastIndexOf("."));
				if(nameInGraph.contains(className)) {
					writeFile(dir,targetPath);
				}
			}
		}
	}
	
	private void writeFile(File javaFile, String targetPath) throws IOException {
		String name = javaFile.getName();
		BufferedReader br = new BufferedReader(new FileReader(javaFile));
		BufferedWriter bw = new BufferedWriter(new FileWriter(targetPath+File.separator+name));
		
		String line;
		while((line=br.readLine())!=null) {
			bw.write(line);
			bw.newLine();
		}
		br.close();
		bw.close();
	}

	public static void main(String[] args) throws IOException {
//		GetSrc tool = new GetSrc();
//		String originPath = "data\\exp\\Infinispan\\infinispan-master";
//		String targetPath = "data\\exp\\Infinispan\\src";
//		tool.getSrcFromProject(originPath, targetPath);
//		String dbPath = "data\\exp\\Infinispan\\relation\\call.db";
//		Set<String> set = tool.getGraphRelevantCode(dbPath);
//		System.out.println(set.size());
	}
}
