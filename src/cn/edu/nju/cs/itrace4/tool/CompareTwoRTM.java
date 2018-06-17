package cn.edu.nju.cs.tool;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CompareTwoRTM {
	private String originRTMPath = "/home/zzf/workspace/iTrace4/data/exp/Maven/rtm/RTM_CLASS.txt";
	private String nowRTMPath = "/home/zzf/geek/iTrace4/data/exp/Maven/rtm/RTM_CLASS.txt";
	
	private void compare() {
		Set<String> originSet = getSetFromFile(originRTMPath);
		Set<String> nowSet = getSetFromFile(nowRTMPath);
		Iterator<String> ite = nowSet.iterator();
		
		while(ite.hasNext()) {
			String str = ite.next();
			
			if(originSet.contains(str)) {
				ite.remove();
				originSet.remove(str);
			}
		}
		
		System.out.println("*****************************************");
		for(String str:originSet) {
			System.out.println("originRTM:"+str);
		}
		
		System.out.println("-----------------------------------------");
		for(String str:nowSet) {
			System.out.println("nowRTM:"+str);
		}
	}
	
	private Set<String> getSetFromFile(String rtmPath) {
		Set<String> set = new HashSet<String>();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(rtmPath));
			String line = null;
			while((line=br.readLine())!=null) {
				String className = line.split("\\s+")[1];
				set.add(className);
			}
			br.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return set;
	}

	public static void main(String[] args) {
		CompareTwoRTM tool = new CompareTwoRTM();
		tool.compare();
	}
}
