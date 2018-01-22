package cn.edu.nju.cs.refactor.util;

import java.io.IOException;

import cn.edu.nju.cs.refactor.exception.FileException;

public interface FileProcess extends Tool{
	public String getFileConent(String filePath) throws FileException, IOException;
	public String getClassPackageName(String fileName) throws FileException, IOException;
}
