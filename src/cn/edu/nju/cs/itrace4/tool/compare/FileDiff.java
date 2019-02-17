package cn.edu.nju.cs.itrace4.tool.compare;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import cn.edu.nju.cs.itrace4.util.FileProcess;
import cn.edu.nju.cs.itrace4.util.FileProcessTool;
import cn.edu.nju.cs.itrace4.util.exception.FileException;

public class FileDiff {
	private FileProcess fileProcess = new FileProcessTool();
	
	public void compare(String curPath,String targetPath) throws FileException,
		IOException {
		String filesInMaster = fileProcess.getFileConent(curPath);
		String filesInTarget = fileProcess.getFileConent(targetPath);
		filesWhichNotInTarget(filesInMaster,filesInTarget);
	}
	
	
	private void filesWhichNotInTarget(String filesInMaster, String filesInTarget) {
		Set<String> nameInCur = fileSet(filesInMaster);
		Set<String> nameInTarget = fileSet(filesInTarget);
		System.out.println("---------diff------------");
		for(String name:nameInCur) {
			if(!nameInTarget.contains(name)) {
				System.out.println(name);
			}
		}
	}

	private Set<String> fileSet(String content){
		Set<String> set = new HashSet<String>();
		String[] nameArr = content.split("\n");
		for(String name:nameArr) {
			set.add(name);
		}
		return set;
	}

	public static void main(String[] args) throws FileException, IOException {
		String curPath = "/home/zzf/demo/master.txt";
		String targetPath = "/home/zzf/demo/branch_36.txt";
		FileDiff fileDiff = new FileDiff();
		fileDiff.compare(curPath, targetPath);
	}
}
