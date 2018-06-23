package cn.edu.nju.cs.itrace4.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/*
 * 2017/3/24
 * by zhangsan
 * */
public class FindWhoInvokeMe {
	/**
	 * if the file is .java process it else recursive.
	 * @param the url of directory.
	 * */
	public void doTask(String basePath,String target) throws IOException{
		File curFile = new File(basePath);
		if(curFile.isDirectory()){
			File[] files = curFile.listFiles();
			for(File file:files){
				doTask(file.getAbsolutePath(),target);
			}
		}
		else if(!curFile.getName().endsWith(".java")){
			;
		}
		else{
			if(curFile.getName().endsWith(".java")){
				execute(curFile.getAbsolutePath(),target);
			}
		}
	}
	
	/**
	 * @param the absolute path of java file
	 * @throws IOException 
	 **/
	private void execute(String filePath,String target) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		String line;
		while((line=br.readLine())!=null){
			String[] strs = line.split("\\(|\\)|\\s|\\.|;|@|\\<|\\>|\\{|\\}");
			
			for(String str:strs){
				if(str.equals(target)){
					System.out.println(filePath);
					br.close();
					return ;
				}
			}
		}
		br.close();
	}

	public static void main(String[] args) throws IOException{
		String basePath = "./src";
		//String basePath = "Z:\\研二\\iTrace4_icsme\\iTrace4\\src";
		FindWhoInvokeMe tool = new FindWhoInvokeMe();
		tool.doTask(basePath, "StoreCallSubGraph");
	}
	
}
