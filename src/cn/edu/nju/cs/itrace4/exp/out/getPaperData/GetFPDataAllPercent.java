package cn.edu.nju.cs.itrace4.exp.out.getPaperData;


public class GetFPDataAllPercent {
	private GetFPData getFPData;
	
	public GetFPDataAllPercent() {
		System.setProperty("routerLen", "6");
		try {
			getFPData = new GetFPData();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void batchProcess() {
		for(double percent = 0.1; percent<1.1; percent+=0.1) {
			getFPData.setPercent(percent);
			try {
				getFPData.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		GetFPDataAllPercent tool = new GetFPDataAllPercent();
		tool.batchProcess();
	}
}
