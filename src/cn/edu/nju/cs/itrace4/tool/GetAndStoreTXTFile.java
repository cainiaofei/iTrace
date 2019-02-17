package cn.edu.nju.cs.itrace4.tool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class GetAndStoreTXTFile {
	String dirPath;
	String targetPath;
	
	public GetAndStoreTXTFile(String dirPath,String targetPath){
		this.dirPath = dirPath;
		this.targetPath = targetPath;
	}
	
	public void doTask() throws IOException{
		File dir = new File(dirPath);
		File[] files = dir.listFiles();
		for(File file:files){
			cpy(file,targetPath);
		}
	}
	
	private void cpy(File file, String basePath) throws IOException {
		String fileName = file.getName();
		String newFileName = fileName.substring(0,fileName.indexOf('.'))+".txt";
		BufferedReader br = new BufferedReader(new FileReader(file));
		BufferedWriter bw = new BufferedWriter(new FileWriter(basePath+File.separator+newFileName));
		String line = null;
		while((line=br.readLine())!=null){
			bw.write(line);
			bw.newLine();
		}
		bw.close();
		br.close();
	}
	
	public static void main(String[] args) throws IOException{
		String dirPath = "Z:\\研二\\iTrace4_icsme\\iTrace4\\data\\exp\\JHotDraw\\src";
		String targetPath = "Z:\\研二\\iTrace4_icsme\\iTrace4\\data\\exp\\JHotDraw\\class\\graph\\code";
		GetAndStoreTXTFile getAndStoreTXTFile = new GetAndStoreTXTFile(dirPath,targetPath);
		getAndStoreTXTFile.doTask();
	}
}
