package cn.edu.nju.cs.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class GetCodeLineAndClassCount {
	
	public int process(String basePath) throws IOException {
		File dir = new File(basePath);
		int res = 0;
		if(dir.isDirectory() && !dir.getName().equals("test")) {
			File[] files = dir.listFiles();
			for(File file:files) {
				res += process(file.getPath());
			}
		}
		else {
			
			if(dir.isFile() && dir.getName().endsWith(".java")) {
				res += codeLines(dir);
			}
		}
		return res;
	}
	
	public int codeLines(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		int count = 0;
		String line = null;
		while((line=br.readLine())!=null) {
			if(line.length()!=0) {
				count++;
			}
		}
		br.close();
		return count;
	}
	
	public static void main(String[] args) throws IOException {
		String path = "./data/exp/Maven/maven-master";
		GetCodeLineAndClassCount tool = new GetCodeLineAndClassCount();
		int res = tool.process(path);
		System.out.println(res*1.0/1000);
	}
}
