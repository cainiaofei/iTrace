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
	
	
	public void getSrcFromMasterBasedOnGraphDB(String masterPath,String targetPath, String dbPath) throws IOException {
		nameInGraph = getGraphRelevantCode(dbPath);
		getSrcFromProject(masterPath, targetPath);
	}
	
	
	public Set<String> getGraphRelevantCode(String dbPath){
		Set<String> set = new HashSet<String>();
		Connection con;
        Statement stmt;

        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            con.setAutoCommit(false);
            stmt = con.createStatement();
            
            String sql = "select * from graph";
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
            	String former = rs.getString("source");
            	String latter = rs.getString("sink");
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
		System.err.println("---err--");
		System.exit(-1);
		return null;
	}

	public void getSrcFromProject(String originPath,String targetPath) throws IOException {
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
		GetSrc tool = new GetSrc();
		String originPath = "data\\exp\\Infinispan\\infinispan-master";
		String targetPath = "data\\exp\\Infinispan\\src";
		tool.getSrcFromProject(originPath, targetPath);
//		String dbPath = "data\\exp\\Infinispan\\relation\\call.db";
//		Set<String> set = tool.getGraphRelevantCode(dbPath);
//		System.out.println(set.size());
	}
}
