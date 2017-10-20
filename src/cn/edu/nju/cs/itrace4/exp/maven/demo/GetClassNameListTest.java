package cn.edu.nju.cs.itrace4.exp.maven.demo;

public class GetClassNameListTest {
	private static String[] getClassNameList(String strs) {
		String[] strArr = strs.split("和");
		for(int i = 0; i < strArr.length;i++) {
			strArr[i] = strArr[i].replace("/", ".");
			strArr[i] = strArr[i].substring(0, strArr[i].lastIndexOf("."));
			System.out.println(strArr[i]);
		}
		return strArr;
	}
	
	public static void main(String[] args) {
		String origin = "artifact/src/main/java/org/apache/maven/artifact/metadata/"
				+ "AbstractVersionArtifactMetadata.java和maven-artifact/src/main/java/org/apache"
				+ "/maven/artifact/metadata/ArtifactMetadata.java和"
				+ "maven-artifact/src/main/java/org/apache/maven/artifact/metadata/"
				+ "ReleaseArtifactMetadata.java";
		String[] strs = getClassNameList(origin);
		for(String str:strs) {
			System.out.println(str);
		}
	}
}
