//package cn.edu.nju.cs.itrace4.exp.maven.test;
//
//import cn.edu.nju.cs.itrace4.exp.maven.tool.GetUC;
//
//public class GetUCTest {
//	GetUC getUC = new GetUC();
//	public void filterTest(String str) {
//		System.out.println(getUC.filter(str));
//	}
//	
//	public static void main(String[] args) {
//		GetUCTest test = new GetUCTest();
//		String text = "[MNG-895] Profile resources not merged with main POM<p>Profile resources are not merged with resources specified in the main POM, e.g.:</p>\r\n" + 
//				"\r\n" + 
//				"<p>&lt;project&gt;<br/>\r\n" + 
//				" ...<br/>\r\n" + 
//				" &lt;build&gt;<br/>\r\n" + 
//				"   &lt;resources&gt;<br/>\r\n" + 
//				"     &lt;resource&gt;a&lt;/resource&gt;<br/>\r\n" + 
//				"   &lt;/resources&gt;<br/>\r\n" + 
//				" &lt;/build&gt;<br/>\r\n" + 
//				" &lt;profiles&gt;<br/>\r\n" + 
//				"   &lt;profile&gt;<br/>\r\n" + 
//				"     ...<br/>\r\n" + 
//				"     &lt;build&gt;<br/>\r\n" + 
//				"       &lt;resources&gt;<br/>\r\n" + 
//				"         &lt;resource&gt;b&lt;/resource&gt;<br/>\r\n" + 
//				"       &lt;/resources&gt;<br/>\r\n" + 
//				"     &lt;/build&gt;<br/>\r\n" + 
//				"     ...<br/>\r\n" + 
//				"   &lt;/profile&gt;<br/>\r\n" + 
//				" &lt;/profiles&gt;<br/>\r\n" + 
//				" ...<br/>\r\n" + 
//				"&lt;/project&gt;</p>\r\n" + 
//				"\r\n" + 
//				"<p>The effective resources block should consist of both 'a' and 'b' when the profile is activated, but currently 'b' overrides 'a'.</p>";
//		test.filterTest(text);
//	}
//}
