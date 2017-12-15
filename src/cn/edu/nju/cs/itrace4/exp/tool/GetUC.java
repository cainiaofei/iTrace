package cn.edu.nju.cs.itrace4.exp.tool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public class GetUC {
	
	public void getUCFromDB(String targetPath,String dbPath) throws Exception {
		Connection con;
		Statement stmt;
		Class.forName("org.sqlite.JDBC");
		con = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
		con.setAutoCommit(false);
		stmt = con.createStatement();
		String sql = "select request as requirement from rtm";
		ResultSet rs = stmt.executeQuery(sql);
		int number = 1;
		while(rs.next()) {
			String requirement = rs.getString("requirement");
			writeUCFile(requirement,number,targetPath);
			number++;
		}
		con.close();
	}
	
	public void writeUCFile(String text,int index,String targetPath) throws IOException {
		text = filter(text);
		BufferedWriter bw = new BufferedWriter(new FileWriter(targetPath+File.separator+"req"+index+".txt"));
		bw.write(text);
		bw.close();
	}
	
	/**
	 * @author zzf <tiaozhanzhe668@163.com>
	 * @date 2017/10/15
	 * @description process the text of raw issue.
	 * step1: remove the header example: [mng-123] xbdh ==> xbdh
	 * step2: remove <a>xxx</a> example: content is url<a href=www.baidu.com>www.jd.com</a>university 
	 * 			==> content is url university  note: space
	 * step3: remove exception info, [info]
	 * 	 	  example: 
	 * 	       xxx
	 * 		   [INFO] The following files have been resolved:
	 *		   [INFO]    org.apache.Infinispan:foo:test-jar:tests:1.0:test
	 *		   Caused by: org.apache.Infinispan.plugin.PluginExecutionException: Execution assembly-zip of goal org.apache.Infinispan.plugins:Infinispan-assembly-plugin:2.6:single failed.
	 *				at org.apache.Infinispan.plugin.DefaultBuildPluginManager.executeMojo(DefaultBuildPluginManager.java:145)
	 *				at org.apache.Infinispan.lifecycle.internal.MojoExecutor.execute(MojoExecutor.java:207)
	 *			... 11 more
	 *		   [error] cc
	 *  ==>  xxx      
	 */
	public String filter(String text) {
		String[] specialStr = {"[", "at org", "at java","..."};
		text = removeHeader(text);
		//text = removeLineStartWithSpecifyStr(text,specialStr);
		text = removeTagA(text);
		text = removeTag(text);
		text = removeLineStartWithSpecifyStr(text,specialStr);
		return text;
	}
        
//	public String filter(String text) {
//		String content = text.split("\n")[0];
//		return content.substring(content.indexOf("]")+1);
//	}

	private String removeTag(String text) {
		while(true) {
			int left = text.indexOf("<");
			int right = text.indexOf(">");
			if(left==-1 || right==-1) {
				return text;
			}
			else if(left>right) {
				text = text.substring(0,right) + " " + text.substring(right+1);
			}
			else {
				text = text.substring(0, left) + " " + text.substring(right+1);
			}
		}
	}

	private String removeLineStartWithSpecifyStr(String text, String[] specialStr) {
		//fillSet(specialStrSet,specialStr);
		String[] contents = text.split("\n");
		StringBuilder sb = new StringBuilder();
		for(String content:contents) {
			content = content.trim();
			if(content.length()==0 || startWith(content,specialStr)) {
				continue;
			}
			else {
				sb.append(content);
				sb.append("\n");
			}
		}
		
		return sb.toString();
	}

	private boolean startWith(String content, String[] specialStr) {
		for(String str:specialStr) {
			if(content.startsWith(str)) {
				return true;
			}
		}
		return false;
	}

	private void fillSet(Set<String> specialStrSet, String[] specialStr) {
		for(String str:specialStr) {
			specialStrSet.add(str);
		}
		
	}

	private String removeTagA(String text) {
		while(true) {
			int left = text.indexOf("<a");
			int right = text.indexOf("/a>");
			if(left==-1 || right==-1) {
				return text;
			}
			else {
				text = text.substring(0, left) + " " + text.substring(right+3);
			}
		}
	}

	private String removeHeader(String text) {
		return text.substring(text.indexOf("]")+1);
	}

//	public static void main(String[] args) throws Exception {
//		GetUC tool = new GetUC();
//		//String targetPath = "D:\\workspace\\eclipse-workspace\\iTrace4\\data\\exp\\Infinispan\\uc";
//		String targetPath = "D:\\workspace\\eclipse-workspace\\iTrace4\\data\\exp\\Infinispan\\test\\uc";
//		tool.getUCFromDB(targetPath,"");
//	}
}
