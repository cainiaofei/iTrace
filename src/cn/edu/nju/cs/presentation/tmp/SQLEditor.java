package cn.edu.nju.cs.presentation.tmp;
import java.awt.BorderLayout;
    import java.awt.Color;
    import java.awt.Font;
    import javax.swing.JFrame;
    import javax.swing.JScrollPane;
    import javax.swing.JTextPane;
    import javax.swing.text.BadLocationException;
    import javax.swing.text.Style;
    import javax.swing.text.StyleConstants;
    import javax.swing.text.StyleContext;

    public class SQLEditor extends JFrame {
        private static final long serialVersionUID = 1L;

        public SQLEditor() {
            StyleContext styleContext = new StyleContext();
            Style defaultStyle = styleContext.getStyle(StyleContext.DEFAULT_STYLE);
            Style cwStyle = styleContext.addStyle("ConstantWidth", null);
            StyleConstants.setForeground(cwStyle, Color.BLUE);
            StyleConstants.setBold(cwStyle, true);

            final JTextPane pane = new JTextPane(new KeywordStyledDocument(defaultStyle, cwStyle));
            pane.setFont(new Font("Courier New", Font.PLAIN, 12));

            JScrollPane scrollPane = new JScrollPane(pane);
            getContentPane().add(scrollPane, BorderLayout.CENTER);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(375, 400);      
        }

        public static void main(String[] args) throws BadLocationException {
            SQLEditor app = new SQLEditor();
            app.setVisible(true);
        }
    }