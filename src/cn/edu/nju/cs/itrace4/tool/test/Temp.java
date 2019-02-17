package cn.edu.nju.cs.itrace4.tool.test;

public class Temp {
	
	private String getNameFromFullClassName(String fullName) {
		if(fullName.startsWith("L")) {
			fullName = fullName.substring(1);
		}
		String[] args = fullName.split("\\.|\\_|\\$|/|\\s+|;");
		for(String arg:args) {
			if(arg.charAt(0)>='A' && arg.charAt(0)<='Z') {
				return arg;
			}
		}
		System.out.println(fullName);
		System.err.println("---err--methodName:getNameFromFullClassName()："+args[args.length-2]);
		return args[args.length-2];
	}
	
	
	public static void main(String[] args) {
		Temp temp = new Temp();
		String line = "Lorg/apache/maven/cli/CliRequest;";
		String className = temp.getNameFromFullClassName(line);
		System.out.println(className);
	}
}
