package cn.edu.nju.cs.itrace4.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author zzf
 * @date 2017/10/17
 * @description get merge case. 
 */
public class MergeProcess {
	
	public void doTask(String rtmPath,int threshold) throws IOException {
		List<String> reqList = getReqRelevantClassMoreThanSpecificNum(rtmPath,threshold);
		printList(reqList);
	}
	
	public void printList(List<String> list) {
		for(String str:list) {
			System.out.println(str);
		}
	}
	
	public List<String> getReqRelevantClassMoreThanSpecificNum(String rtmPath,int threshold) 
			throws IOException{
		List<String> reqList = new LinkedList<String>();
		Map<String,Integer> reqMapClassNum = new HashMap<String,Integer>();
		BufferedReader br = new BufferedReader(new FileReader(new File(rtmPath)));
		String line = null;
		while((line=br.readLine())!=null) {
			String[] strs = line.split("\\s+");
			String reqName = strs[0].trim();
			if(!reqMapClassNum.containsKey(reqName)) {
				reqMapClassNum.put(reqName, 0);
			}
			reqMapClassNum.put(reqName, reqMapClassNum.get(reqName)+1);
		}
		br.close();
		
	    for(String reqName:reqMapClassNum.keySet()) {
	    	if(reqMapClassNum.get(reqName)>=threshold) {
	    		reqList.add(reqName);
	    	}
	    }
		return reqList;
	}
	
	public static void main(String[] args) throws IOException {
		MergeProcess mergeProcess = new MergeProcess();
		String rtmPath = "data\\exp\\Maven\\rtm\\RTM_CLASS.txt";
		int threshold = 10;
		mergeProcess.doTask(rtmPath, threshold);
	}
}
