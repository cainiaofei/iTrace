package cn.edu.nju.cs.refactor.exp.input;

public class ModelFactoryImp implements ModelFactory{

	@Override
	public String description() {
		StringBuilder sb = new StringBuilder();
		sb.append("produce model full name based on model short name");
		return sb.toString();
	}

	@Override
	public String generate(String modelName) {
		String modelFullName = null;
		if(modelName.compareToIgnoreCase("vsm")==0) {
			modelFullName = "cn.edu.nju.cs.itrace4.core.ir.VSM";
		}else if(modelName.compareToIgnoreCase("lsi")==0) {
			modelFullName = "cn.edu.nju.cs.itrace4.core.ir.LSI";
		}else if(modelName.compareToIgnoreCase("js")==0) {
			modelFullName = "cn.edu.nju.cs.itrace4.core.ir.JSD";
		}else {
			System.out.println("there dont exist this type of model ----"
					+ "cn.edu.nju.cs.refactor.exp.input.ModelFactoryImp");
		}
		return modelFullName;
	}

}
