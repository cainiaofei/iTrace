package cn.edu.nju.cs.itrace4.demo.getPaperData;

import java.util.ArrayList;
import java.util.List;

public class GetAPAndMAPAllPercent {
	
	public void bactchProcess(String basePath) throws InterruptedException {
		List<Thread> threads = new ArrayList<Thread>();
		for(double percent=0.2; percent<=0.5;percent+=0.2) {
			GetApAndMap task = new GetApAndMap(percent,basePath);
			Thread thread = new Thread(task);
			thread.start();
			threads.add(thread);
//			threads.add(new Thread(task));
//			System.out.println(percent+":outer");
			if (threads.size()%4 == 0) {
				for (Thread thread1 : threads) {
					thread1.join();
					System.out.println(threads.size());
				}
				threads.clear();
			}
		}
		
		for(Thread thread:threads) {
			thread.join();
		}
		System.out.println("----end-----");
	}
	
	public static void main(String[] args) throws InterruptedException {
		GetAPAndMAPAllPercent batchTool = new GetAPAndMAPAllPercent();
		batchTool.bactchProcess("./finalData");
	}
}
