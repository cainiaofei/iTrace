package cn.edu.nju.cs.itrace4.demo.batch;

import cn.edu.nju.cs.refactor.exp.input.ModelFactory;
import cn.edu.nju.cs.refactor.exp.input.ModelFactoryImp;
import cn.edu.nju.cs.refactor.exp.input.ProjectFactory;
import cn.edu.nju.cs.refactor.exp.input.ProjectFactoryImp;
import cn.edu.nju.cs.refactor.util.FileProcess;
import cn.edu.nju.cs.refactor.util.FileProcessTool;
import cn.edu.nju.cs.refactor.util.FileWrite;
import cn.edu.nju.cs.refactor.util.FileWriterImp;

public class BatchExecuteFP {
	private String projectPath = "";
	private String modelPath = "";
	private String template = "";
	
	private String targetPath = "";
	
	private ProjectFactory projectFactory = new ProjectFactoryImp();
	private ModelFactory modelFactory = new ModelFactoryImp();
	private FileProcess fileProcess = new FileProcessTool();
	private FileWrite fileWrite = new FileWriterImp();
	
	public BatchExecuteFP() {}
	
	public void getFPData() {
		
	}
}
