package cn.edu.nju.cs.tool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.edu.nju.cs.itrace4.util.FileProcess;
import cn.edu.nju.cs.itrace4.util.FileProcessTool;
import cn.edu.nju.cs.itrace4.util.exception.FileException;

public class DataProcess {
	private FileProcess fp = new FileProcessTool();
	
	private String filePath = "/home/zzf/gaokao/qufushifan.txt";
	
	public void removeBlankLine() throws FileException, IOException {
		String content = fp.getFileConent(filePath);
		String[] arrs = content.split("\n");
		List<String> list = new ArrayList<String>();
		for(String str:arrs) {
			if(str.length()==0) {
				continue;
			}
			list.add(str);
		}
		int number = 0;
		for(String str:list) {
			if(number%5==0) {
				System.out.println();
			}
			System.out.print(str+" ");
			number++;
			number = number%5;
		}
	}
	
	
	public void process() throws FileException, IOException {
		String content = fp.getFileConent(filePath);
		String[] arrs = content.split("\n");
		for(String str:arrs) {
			String[] records = str.split("\\s+");
			if(records[0].startsWith("山东") &&records[2].equals("文史") ) {
				print(records);
			}
		}
	}
	
	private void print(String[] records) {
		for(int i = 0; i < records.length;i++) {
			if(i==3) {
				continue;
			}
			String record = records[i].split("\\.")[0];
			System.out.print(record+"|");
		}
		System.out.print(483+"|");
		double minScore = Double.valueOf(records[4].split("\\.")[0]);
		double threshold = 483;
		System.out.print(minScore-threshold+" ");
		System.out.println();
		
	}

	public static void main(String[] args) throws FileException, IOException {
		DataProcess dp = new DataProcess();
		dp.process();
		//dp.removeBlankLine();
	}
}
