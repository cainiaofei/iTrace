package cn.edu.nju.cs.itrace4.util;

public interface FileWrite extends Tool{
	public void createFile(String filePath);
	public void writeLine(String line);
	public void close();
	public void writeContent(String content);
	public void newLine();
}
