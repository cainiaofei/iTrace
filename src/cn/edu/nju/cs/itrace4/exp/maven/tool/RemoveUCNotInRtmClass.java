//package cn.edu.nju.cs.itrace4.exp.maven.tool;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.util.HashSet;
//import java.util.Set;
//
///**
// * @author zzf
// * @date 2017/10/13
// * @description  
// */
//
//public class RemoveUCNotInRtmClass {
//	
//	public void removeUCNotInRtmClass(String ucPath,String rtmPath) throws Exception {
//		Set<String> ucSet = getUCSet(rtmPath);	
//		File dir = new File(ucPath);
//		File[] files = dir.listFiles();
//		for(File f:files) {
//			String fullName = f.getName();
//			String ucName = fullName.substring(0,fullName.lastIndexOf("."));
//			if(!ucSet.contains(ucName)) {
//				f.delete();
//			}
//		}
//	}
//	
//	private Set<String> getUCSet(String rtmPath) throws Exception {
//		Set<String> set = new HashSet<String>();
//		BufferedReader br = new BufferedReader(new FileReader(new File(rtmPath)));
//		String line;
//		while((line=br.readLine())!=null) {
//			set.add(line.split("\\s")[0]);
//		}
//		br.close();
//		return set;
//	}
//
//	public static void main(String[] args) throws Exception {
//		RemoveUCNotInRtmClass tool = new RemoveUCNotInRtmClass();
//		String ucPath = "D:\\workspace\\eclipse-workspace\\iTrace4\\data\\exp\\Maven\\uc";
//		String rtmPath = "D:\\workspace\\eclipse-workspace\\iTrace4\\data\\exp\\Maven\\rtm\\RTM_CLASS.txt";
//		tool.removeUCNotInRtmClass(ucPath, rtmPath);
//	}
//}
