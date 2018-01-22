package cn.edu.nju.cs.refactor.util;

public interface FileWrite extends Tool{
	public void createFile(String filePath);
	public void writeLine(String line);
	public void close();
}
