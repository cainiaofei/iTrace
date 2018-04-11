package cn.edu.nju.cs.presentation;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class JTextPanelTest extends JFrame {
	    private JTextPane textPanel;
	private StyleContext styleContext;
	private DefaultStyledDocument doc;

	public JTextPanelTest() {
		initCompoment();
	}

	public void initCompoment() {
		styleContext = new StyleContext();
		doc = new DefaultStyledDocument(styleContext);
		textPanel = new JTextPane(doc); // 设置绿色文本
		final Style greenStyle = styleContext.addStyle("ConstantWidth", null);
		StyleConstants.setFontFamily(greenStyle, "monospaced");
		StyleConstants.setForeground(greenStyle, Color.green); // 设置黄色文本
		final Style yellowStyle = styleContext.addStyle("ConstantWidth", null);
		StyleConstants.setFontFamily(yellowStyle, "monospaced");
		StyleConstants.setForeground(yellowStyle, Color.yellow); // 设置蓝色文本
		final Style blueStyle = styleContext.addStyle("ConstantWidth", null);
		StyleConstants.setFontFamily(blueStyle, "monospaced");
		StyleConstants.setForeground(blueStyle, Color.blue);

		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					try {
						// 将文本添加到文档中
						doc.insertString(0, text, null);
						// 设置第一段为绿色
						doc.setParagraphAttributes(0, 1, greenStyle, false);
						// 设置第二段为黄色
						doc.setParagraphAttributes(12, 1, yellowStyle, false);
						// 设置第三四段为蓝色
						doc.setParagraphAttributes(25, 13, blueStyle, false);
					} catch (BadLocationException e) {
					}
				}
			});
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		this.add(new JScrollPane(textPanel));
	}

	public static final String text = "Hello World\n" + "Hello World\n" + "Hello World\n" + "Hello World\n"
			+ "Hello World\n";

	public static void main(String[] args) {
		JTextPanelTest frame = new JTextPanelTest();
		frame.setSize(400, 300);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}