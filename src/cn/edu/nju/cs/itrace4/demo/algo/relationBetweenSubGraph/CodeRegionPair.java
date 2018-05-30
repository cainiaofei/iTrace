package cn.edu.nju.cs.itrace4.demo.algo.relationBetweenSubGraph;

/**
 * @date 2018.5.30
 * @author zzf
 * @description  two id of code region and closeness between them.
 */
public class CodeRegionPair {
	private int formerId;
	private int latterId;
	private double closeness;
	
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
}
