package cn.edu.nju.cs.itrace4.tool;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class CountOfClass {
	
	public int getCount(String path) throws IOException{
		Set<String> set = new HashSet<String>();
		BufferedReader br = new BufferedReader(new FileReader(path));
		String line = null;
		while((line=br.readLine())!=null){
			String str = line.split(" ")[1].trim();
			set.add(str);
		}
		br.close();
		return set.size();
	}
	
	public static void main(String[] args) throws IOException{
		CountOfClass countOfClass = new CountOfClass();
		String path = "Z:\\研二\\iTrace4_icsme\\iTrace4\\data\\exp\\Gantt\\rtm\\RTM_CLASS.txt";
		System.out.println(countOfClass.getCount(path));
	}
}
