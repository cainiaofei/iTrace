package cn.edu.nju.cs.itrace4.exp.tool;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
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
	
	public void GetSrcBaseRI(String masterPath,String targetPath) throws IOException {
		nameInGraph = getClassInRI(relationPath);
		GetSrcBaseRIFromProject(masterPath, targetPath);
	}
	
	private Set<String> getClassInRI(String relationPath) {
		Set<String> classSet = new HashSet<String>();
		getClassInCallDB(relationPath,"call.db","graph",classSet);
		getClassInTable(relationPath,"FA.db","fieldAccess",classSet);
		getClassInTable(relationPath,"FM.db","fieldModification",classSet);
		getClassInTable(relationPath,"PP.db","parameterPass",classSet);
		return classSet;
	}

	

	private void getClassInCallDB(String relationPath, String dbName, String tableName, 
			Set<String> classSet) {	
		
		SqliteOperation sqlOperate = new SqliteOperation();
		sqlOperate.buildConnection(driver, relationPath+dbName);
		String query = "select * from " + tableName;
		ResultSet rs = sqlOperate.executeQuery(query);
		while(rs.next()) {
			
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
