package cn.edu.nju.cs.refactor.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author zzf
 * @date 2018.1.22
 * @description some common operation when write file.  
 */
public class FileWriterImp implements FileWrite{
	private BufferedWriter bw;
	@Override
	public String description() {
		StringBuilder sb = new StringBuilder();
		sb.append("encapsulate some operation of write file contains open file, write"
				+ " and close and so on.,,,");
		return null;
	}
	
	/**
	 * @date 2018.1.22
	 * @author zzf
	 * @description if there already exist this file, this method will overwrite it. 
	 */
	@Override
	public void createFile(String filePath) {
		try {
			bw = new BufferedWriter(new FileWriter(new File(filePath)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void writeLine(String line) {
		try {
			bw.write(line);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void newLine() {
		try {
			bw.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
