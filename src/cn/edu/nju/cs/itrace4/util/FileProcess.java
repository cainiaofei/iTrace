package cn.edu.nju.cs.itrace4.util;

import java.io.IOException;

import cn.edu.nju.cs.itrace4.util.exception.FileException;

public interface FileProcess extends Tool{
	public String getFileConent(String filePath) throws FileException, IOException;
	public String getClassPackageName(String fileName) throws FileException, IOException;
	public void writeFile(String filePath,String text);
}
