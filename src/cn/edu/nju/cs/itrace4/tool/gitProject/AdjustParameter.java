package cn.edu.nju.cs.itrace4.tool.gitProject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.relation.RelationInfo;
import cn.edu.nju.cs.itrace4.util.FileParse.project.Gantt;
import cn.edu.nju.cs.itrace4.util.FileParse.project.Itrust;
import cn.edu.nju.cs.itrace4.util.FileParse.project.Maven;
import cn.edu.nju.cs.itrace4.util.FileParse.project.Project;

public class AdjustParameter {
	private Map<String, Double> irPvalueMap = new ConcurrentHashMap<String, Double>();
	private Map<String, Double> udPvalueMap = new ConcurrentHashMap<String, Double>();
	private Map<String, Double> clusterMap = new ConcurrentHashMap<String, Double>();
	
	public void lookForParameter() throws InterruptedException, IOException, ClassNotFoundException {
		System.setProperty("routerLen", 6 + "");
		Project[] projects = { new Gantt() , new Itrust(), /*new Maven()*/};
		String[] models = { "cn.edu.nju.cs.itrace4.core.ir.VSM", "cn.edu.nju.cs.itrace4.core.ir.JSD",
				"cn.edu.nju.cs.itrace4.core.ir.LSI" };
		List<Thread> threadList = new ArrayList<Thread>();
		AtomicInteger ai = new AtomicInteger();

		for (double callThreshold = 0.5; callThreshold < 0.99; callThreshold += 0.1) {
			for (double dataThreshold = 0.5; dataThreshold < 1.00; dataThreshold += 0.1) {
				for (Project project : projects) {
					for (String model : models) {
						TextDataset textDataset = new TextDataset(project.getUcPath(), project.getClassDirPath(),
								project.getRtmClassPath());
						FileInputStream fis = new FileInputStream(project.getClass_RelationInfoPath());
						ObjectInputStream ois = new ObjectInputStream(fis);
						RelationInfo ri = (RelationInfo) ois.readObject();
						ois.close();
						ai.getAndIncrement();
						//System.out.println(ai);
						Executor executor = new Executor(callThreshold, dataThreshold, project, model, irPvalueMap,
								udPvalueMap,clusterMap);
						executor.init(textDataset,ri);
						Thread cur = new Thread(executor);
						cur.start();
						threadList.add(cur);
						if (threadList.size() % 8 == 0) {
							for (Thread thread : threadList) {
								thread.join();
							}
							threadList.clear();
						}
					}
				}
			}
		}

		for (Thread thread : threadList) {
			thread.join();
		}

		try {
			storeExcel();
		} catch (Exception e) {
			e.printStackTrace();
		}
		findBestParameter(0.06);
		System.out.println("---terminal-----");
	}

//	public void findBestParameter(double threshold) {
//		Map<String, List<Double>> irMap = reArrange(irPvalueMap);
//		Map<String, List<Double>> udMap = reArrange(udPvalueMap);
//		Map<String, List<Double>> cluster = reArrange(clusterMap);
//		
//		for (String str : irMap.keySet()) {
//			List<Double> irList = irMap.get(str);
//			List<Double> udList = udMap.get(str);
//			boolean allLessThanThreshold = true;
//			for (double val : irList) {
//				if (val > threshold) {
//					allLessThanThreshold = false;
//					break;
//				}
//			}
//			for (double val : udList) {
//				if (val > threshold) {
//					allLessThanThreshold = false;
//					break;
//				}
//			}
//			if (allLessThanThreshold) {
//				System.out.println(str);
//				System.out.println("--------------");
//				System.out.println(str);
//				System.out.println(irList.toString());
//				System.out.println(udList.toString());
//			}
//		}
//	}
	
	public void findBestParameter(double threshold) {
		Map<String, List<Double>> udMap = reArrange(udPvalueMap);
		Map<String, List<Double>> cluster = reArrange(clusterMap);
		
		for (String str : udMap.keySet()) {
			List<Double> udList = udMap.get(str);
			boolean allLessThanThreshold = true;
			for (double val : udList) {
				if (val > threshold) {
					allLessThanThreshold = false;
					break;
				}
			}
			if (allLessThanThreshold) {
				System.out.println(str);
				System.out.println("--------------");
				System.out.println(udList.toString());
			}
		}
	}

	private boolean mapOk(List<Double> list) {
		double threshold = 0.048;
		for(double ele:list) {
			if(ele>threshold) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @author zzf
	 * @date 2017/10/21
	 * @description find best parameter pair.
	 */
	private Map<String, List<Double>> reArrange(Map<String, Double> pvalueMap) {
		Map<String, List<Double>> map = new HashMap<String, List<Double>>();
		for (String key : pvalueMap.keySet()) {
			String callDataIden = getCallDataIden(key);
			if (!map.containsKey(callDataIden)) {
				map.put(callDataIden, new LinkedList<Double>());
			}
			map.get(callDataIden).add(pvalueMap.get(key));
		}
		return map;
	}

	private String getCallDataIden(String str) {
		int start = str.indexOf('-');
		int end = str.lastIndexOf('-');
		return str.substring(start + 1, end);
	}

	private void storeExcel() throws FileNotFoundException, IOException {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File("clusterMap.out")));
		out.writeObject(clusterMap);
		out = new ObjectOutputStream(new FileOutputStream(new File("udPvalueMap.out")));
		out.writeObject(udPvalueMap);
		out.close();
	}

	/**
	 * readObject 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws ClassNotFoundException 
	 */
	public void readObject() throws FileNotFoundException, IOException, ClassNotFoundException{
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File("clusterMap.out")));
		clusterMap = (Map<String, Double>) in.readObject();
		in = new ObjectInputStream(new FileInputStream(new File("udPvalueMap.out")));
		udPvalueMap = (Map<String, Double>) in.readObject();
		in.close();
	}
	
	public static void main(String[] args) throws InterruptedException, ClassNotFoundException, IOException {
		AdjustParameter tool = new AdjustParameter();
		tool.lookForParameter();
		tool.readObject();
		tool.findBestParameter(0.15);
	}
}
//[0.058532477738937, 5.440001369614869E-4, 0.8007007655983734, 0.12151119191356541, 0.43406576879888925]
