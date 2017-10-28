package cn.edu.nju.cs.itrace4.demo.multiThread;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import cn.edu.nju.cs.itrace4.relation.RelationInfo;

public class ReadDataTask implements Callable{
	private ObjectInputStream oisForO;
	private CountDownLatch cdl;
	
	public ReadDataTask(ObjectInputStream oisForO,CountDownLatch cdl){
		this.oisForO = oisForO;
		this.cdl = cdl;
	}
	
	@Override
	public RelationInfo call() throws Exception {
		try {
			RelationInfo ri = (RelationInfo) oisForO.readObject();
			cdl.countDown();
			return ri;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
