package cn.edu.nju.cs.itrace4.exp.tool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.Set;

import cn.edu.nju.cs.itrace4.relation.RelationInfo;

public class TransferTXT {
	
	//Set<String> set = new HashSet<String>();
	
	public TransferTXT() /*throws IOException, ClassNotFoundException*/ {
//		 FileInputStream fis = new FileInputStream("data/exp/iTrust/relation/CLASS_relationInfo_whole.ser");
//	     ObjectInputStream ois = new ObjectInputStream(fis);
//	     RelationInfo ri = (RelationInfo) ois.readObject();
//	     ois.close();
//	     set = LookForBug.getClassFromRI(ri);
//	     for(String str:set) {
//	    	 System.out.println(str);
//	     }
//	     System.out.println("---------------------");
	}
	
	public void transferTXT(String origin,String target) throws IOException {
		File dir = new File(origin);
		File[] childs = dir.listFiles();
		for(File child:childs) {
			if(child.getName().endsWith(".java")||child.getName().endsWith(".jsp")) {
				copy(child,target);
			}
		}
	}
	
	private void copy(File child, String target) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(child));
		//String name = getFileName(child.getName());
		String name = child.getName();
		name = name.replace("тАР", "-");
		if(name.endsWith(".jsp")) {
			name = name.substring(0, name.lastIndexOf("."))+"_jsp.txt";
		}
		else {
			name = name.substring(0, name.lastIndexOf("."))+".txt";
		}
		BufferedWriter bw = new BufferedWriter(new FileWriter(target+File.separator+name));
		String line = null;
		while((line=br.readLine())!=null) {
			bw.write(line);
			bw.newLine();
		}
		br.close();
		bw.close();
	}

	private String getFileName(String name) {
		return name.substring(0,name.lastIndexOf("."))+".txt";
	}

	public void printFileName(String basePath) {
		File dir = new File(basePath);
		for(File file:dir.listFiles()) {
			if(file.getName().endsWith("updateLabProc_jsp.txt")) {
				System.out.println(file.getName().replace("тАР", "-"));
			}
		}
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		TransferTXT tool = new TransferTXT();
		String origin = "data/exp/iTrust/src";
		String target = "data/exp/iTrust/class/code";
		tool.transferTXT(origin, target);
		//tool.printFileName(target);
	}
}
