package cn.edu.nju.cs.itrace4.exp.tool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author zzf
 * @date 2017.11.12
 * @description prune RTM based on ri, About class in RTM, if it not appear in ri,delete it.
 *  Then as soon as the RTM change. Then we need to prune code/uc based rtm
 */
public class PruneBaseRI {
	public static void pruneBaseRT(String basePath,Set<String> riClassSet) throws IOException {
		String rtmPath = basePath+File.separator+
				"rtm"+File.separator+"RTM_CLASS.txt";
		BufferedReader br = new BufferedReader(new FileReader(rtmPath));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while((line=br.readLine())!=null) {
			String className = line.split("\\s+")[1];
			if(riClassSet.contains(className)) {
				sb.append(line+"\n");
			}
			else {
				System.out.println("************  "+className+"  not in ri **************");
			}
		}
		br.close();
		writeToFile(rtmPath,sb.toString());
		//prune code/uc based on rtm
		String ucPath = basePath + File.separator + "uc";
		String codePath = basePath + File.separator + "class" + File.separator + "code";
		/**
		 * rtm line example: UC4 SearchUsersAction 1.0
		 * the first element in line represent uc, the second represent class name. 
		 */
		deleteNotInRTM(ucPath,rtmPath,0);
		deleteNotInRTM(codePath,rtmPath,1);
	}

	private static void deleteNotInRTM(String path, String rtmPath, int index) {
		Set<String> nameSet = null;
		try {
			nameSet = getNameSet(rtmPath,index);
		} catch (Exception e) {
			e.printStackTrace();
		}
		File dir = new File(path);
		File[] files = dir.listFiles();
		for(File file:files) {
			String fileFullName = file.getName();
			String fileName = fileFullName.substring(0,fileFullName.lastIndexOf("."));
			if(!nameSet.contains(fileName)) {
				file.delete();
			}
		}
	}

	private static Set<String> getNameSet(String rtmPath, int index) throws Exception {
		Set<String> set = new HashSet<String>();
		BufferedReader br = new BufferedReader(new FileReader(rtmPath));
		String line;
		while((line=br.readLine())!=null) {
			set.add(line.split("\\s+")[index]);
		}
		br.close();
		return set;
	}

	private static void writeToFile(String rtmPath, String content) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(rtmPath));
		bw.write(content);
		bw.close();
	}
}








