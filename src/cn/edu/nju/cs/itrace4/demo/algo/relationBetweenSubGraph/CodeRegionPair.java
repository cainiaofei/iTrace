package cn.edu.nju.cs.itrace4.demo.algo.relationBetweenSubGraph;

import java.util.HashMap;
import java.util.Map;

/**
 * @date 2018.5.30
 * @author zzf
 * @description  two id of code region and closeness between them.
 */
public class CodeRegionPair {
	private int formerId;
	private int latterId;
	private double closeness;
	private Map<String,Double> validPortionWithReqFromerRegion = new HashMap<String,Double>();
	private Map<String,Double> validPortionWithReqLatterRegion = new HashMap<String,Double>();

	
	public void setValidPortionFormerRegion(String req,double portion) {
		validPortionWithReqFromerRegion.put(req, portion);
	}
	
	public void setValidPortionLatterRegion(String req,double portion) {
		validPortionWithReqLatterRegion.put(req, portion);
	}
	
	
	public CodeRegionPair(int formerId,int latterId,double similarity) {
		this.formerId = formerId;
		this.latterId = latterId;
		this.closeness = similarity;
	}
	
	public int getFormerId() {
		return formerId;
	}

	public void setFormerId(int formerId) {
		this.formerId = formerId;
	}

	public int getLatterId() {
		return latterId;
	}

	public void setLatterId(int latterId) {
		this.latterId = latterId;
	}

	public double getCloseness() {
		return closeness;
	}

	public void setCloseness(double closeness) {
		this.closeness = closeness;
	}
	
	public Map<String, Double> getValidPortionWithReqFromerRegion() {
		return validPortionWithReqFromerRegion;
	}

	public void setValidPortionWithReq(Map<String, Double> validPortionWithReq) {
		this.validPortionWithReqFromerRegion = validPortionWithReq;
	}
	
	public Map<String, Double> getValidPortionWithReqLatterRegion() {
		return validPortionWithReqLatterRegion;
	}

	public void setValidPortionWithReqLatterRegion(Map<String, Double> validPortionWithReqLatterRegion) {
		this.validPortionWithReqLatterRegion = validPortionWithReqLatterRegion;
	}

}
