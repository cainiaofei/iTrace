package cn.edu.nju.cs.itrace4.exp.tool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class TransferTXT {
	
	public void transferTXT(String origin,String target) throws IOException {
		File dir = new File(origin);
		File[] childs = dir.listFiles();
		for(File child:childs) {
			copy(child,target);
		}
	}
	
	private void copy(File child, String target) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(child));
		String name = getFileName(child.getName());
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

	public static void main(String[] args) throws IOException {
		TransferTXT tool = new TransferTXT();
		String origin = "D:\\workspace\\eclipse-workspace\\iTrace4\\data\\exp\\Maven\\src";
		String target = "D:\\workspace\\eclipse-workspace\\iTrace4\\data\\exp\\Maven\\class\\code";
		tool.transferTXT(origin, target);
	}
}
