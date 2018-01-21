package cn.edu.nju.cs.refactor.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import cn.edu.nju.cs.refactor.exception.FileException;

/**
 * @author zzf
 * @date 2018.1.19 
 * @description encapsulate some operation used to operate file.
 */
public class FileProcessTool implements Tool{

	/**
	 * @throws FileException 
	 * @throws IOException 
	 * @description return the package name if it is a java file,else throw exception.
	 * @exception FileException 
	 */
	public String getClassPackageName(String fileName) throws FileException, IOException {
		if(fileName==null || !fileName.endsWith(".java")) {
			throw new FileException("it is not a java file");
		}
		else {
			BufferedReader br = null;
			try {
				 br = new BufferedReader(new FileReader(new File(fileName)));
				 String line = null;
				 while((line=br.readLine())!=null) {
					 line = line.trim();
					 if(line.startsWith("package")) {
						 String[] strs = line.split("\\s+");
						 if(strs.length==2) {
							 //package cn..iTrace; ==> cn..iTrace
							 if(strs[1].endsWith(";")) {
								 strs[1] = strs[1].substring(0, strs[1].length()-1);
							 }
							 return strs[1];
						 }
					 }
				 }
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}finally {
				br.close();
			}
			return "";
		}
	}
	
	@Override
	public String description() {
		StringBuilder description = new StringBuilder();
		description.append("1.");
		return description.toString();
	}
	
}
