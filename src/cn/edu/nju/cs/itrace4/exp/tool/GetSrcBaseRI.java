package cn.edu.nju.cs.itrace4.exp.tool;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import cn.edu.nju.cs.itrace4.preprocess.rawdata.db.SqliteOperation;

public class GetSrcBaseRI extends GetSrc{
	private Set<String> nameInGraph;
	private String relationPath;
	private String driver = "org.sqlite.JDBC";
	
	public GetSrcBaseRI(String relationPath) {
		super();
		this.relationPath = relationPath;
	}
	
	@Override
	public void getSrcBaseRI(String masterPath,String targetPath) throws IOException {
		try {
			nameInGraph = getClassInRI(relationPath);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		GetSrcBaseRIFromProject(masterPath, targetPath);
	}
	
	private Set<String> getClassInRI(String relationPath) throws SQLException {
		Set<String> classSet = new HashSet<String>();
		getClassInCallDB(relationPath,"call.db","callGraph",classSet);
		getClassInTable(relationPath,"test1.db","fieldAccess",classSet);
		getClassInTable(relationPath,"test2.db","fieldModification",classSet);
		getClassInTable(relationPath,"test3.db","parameterPass",classSet);
		return classSet;
	}

	private void getClassInTable(String relationPath, String dbName, String tableName, 
			Set<String> classSet) throws SQLException {
		SqliteOperation sqlOperate = new SqliteOperation();
		sqlOperate.buildConnection(driver, relationPath+File.separator+dbName);
		String query = "select * from " + tableName;
		ResultSet rs = sqlOperate.executeQuery(query);
		while(rs.next()) {
			String fullName = rs.getString("McSignature");
			if(!fullName.contains("tutorial")) {
        		String className = getNameFromFullClassName(fullName);
        		classSet.add(className);
        	}
        	
		}
	}

	private void getClassInCallDB(String relationPath, String dbName, String tableName, 
			Set<String> classSet) throws SQLException {	
		SqliteOperation sqlOperate = new SqliteOperation();
		sqlOperate.buildConnection(driver, relationPath+File.separator+dbName);
		String query = "select * from " + tableName;
		ResultSet rs = sqlOperate.executeQuery(query);
		while(rs.next()) {
			String former = rs.getString("caller");//source
        	String latter = rs.getString("callee");//sink
        	if(!former.contains("tutorial")) {
        		String formerName = getNameFromFullClassName(former);
        		classSet.add(formerName);
        	}
        	
        	if(!latter.contains("tutorial")) {
        		String latterName = getNameFromFullClassName(latter);
        		classSet.add(latterName);
        	}
        }
	}

	private void GetSrcBaseRIFromProject(String originPath,String targetPath) throws IOException {
		File dir = new File(originPath);
		if(dir.isDirectory()) {
			if(!dir.getName().equals("test")) {
				File[] childs = dir.listFiles();
				for(File child:childs) {
					GetSrcBaseRIFromProject(child.getAbsolutePath(), targetPath);
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

	private String getNameFromFullClassName(String fullName) {
		if(fullName.startsWith("L")) {
			fullName = fullName.substring(1);
		}
		//String[] args = fullName.split("\\.|\\_|\\$");
		String[] args = fullName.split("\\.|\\_|\\$|/|\\s+|;");
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
	
	public static void main(String[] args) throws IOException {
//		GetSrcBaseRI tool = new GetSrcBaseRI();
//		String originPath = "data\\exp\\Infinispan\\infinispan-master";
//		String targetPath = "data\\exp\\Infinispan\\src";
//		tool.GetSrcBaseRIFromProject(originPath, targetPath);
//		String dbPath = "data\\exp\\Infinispan\\relation\\call.db";
//		Set<String> set = tool.getGraphRelevantCode(dbPath);
//		System.out.println(set.size());
	}
}
