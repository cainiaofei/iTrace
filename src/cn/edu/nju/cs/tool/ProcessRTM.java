package cn.edu.nju.cs.tool;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProcessRTM {
	
	public void process(String rtmPath) throws IOException {
		Map<String,Set<String>> map = new HashMap<String,Set<String>>();
		BufferedReader br = new BufferedReader(new FileReader(rtmPath));
		String line = null;
		while((line=br.readLine())!=null) {
			String[] strs = line.split("\\s+");
			if(!map.containsKey(strs[0])) {
				map.put(strs[0], new HashSet<String>());
			}
			map.get(strs[0]).add(strs[1]);
		}
		br.close();
		printInfo(map);
	}
	
	private void printInfo(Map<String, Set<String>> map) {
		int all = 0;
		int max = 0;
		int min = Integer.MAX_VALUE;
		int count = 0;
		for(String req:map.keySet()) {
			count += map.get(req).size();
			int cur = map.get(req).size();
			all += cur;
			min = Math.min(min, cur);
			max = Math.max(max, cur);
		}
		System.out.println("min:"+min);
		System.out.println("max:"+max);
		System.out.println("mean:"+(all*1.0/map.size()));
		System.out.println("count:"+count);
	}

	public static void main(String[] args) throws IOException {
		String rtmPath = "./data/exp/iTrust/rtm/RTM_CLASS.txt"; 
		ProcessRTM tool = new ProcessRTM();
		tool.process(rtmPath);
	}
}
