package cn.edu.nju.cs.presentation.tmp;
import java.util.ArrayList;
    import java.util.List;
import java.util.Set;

import javax.swing.text.AttributeSet;
    import javax.swing.text.BadLocationException;
    import javax.swing.text.DefaultStyledDocument;
    import javax.swing.text.Style;

    public class KeywordStyledDocument extends DefaultStyledDocument  {
        private static final long serialVersionUID = 1L;
        private Style _defaultStyle;
        private Style _cwStyle;

        private static Set<String> keyWordSet;
        
        public KeywordStyledDocument(Style defaultStyle, Style cwStyle,Set<String> keyWordSet) {
            _defaultStyle =  defaultStyle;
            _cwStyle = cwStyle;
            this.keyWordSet = keyWordSet;
        }

         public void insertString (int offset, String str, AttributeSet a) throws BadLocationException {
             super.insertString(offset, str, a);
             refreshDocument();
         }

         public void remove (int offs, int len) throws BadLocationException {
             super.remove(offs, len);
             refreshDocument();
         }

         private synchronized void refreshDocument() throws BadLocationException {
             String text = getText(0, getLength());
             final List<HiliteWord> list = processWords(text);

             setCharacterAttributes(0, text.length(), _defaultStyle, true);   
             for(HiliteWord word : list) {
                 int p0 = word._position;
                 setCharacterAttributes(p0, word._word.length(), _cwStyle, true);
             }
         }       

         
         
         private static  List<HiliteWord> processWords(String content) {
             content += " ";
             List<HiliteWord> hiliteWords = new ArrayList<HiliteWord>();
             int lastWhitespacePosition = 0;
             String word = "";
             char[] data = content.toCharArray();

             for(int index=0; index < data.length; index++) {
                 char ch = data[index];
                 if(!(Character.isLetter(ch) || Character.isDigit(ch) || ch == '_')||
                		 (word.length()>0&&(upperAndUpper(word.charAt(0),ch)))) {
                     lastWhitespacePosition = index;
                     if(word.length() > 0) {
                         if(isReservedWord(word.trim())) {
                        	 System.out.println(word);
                             hiliteWords.add(new HiliteWord(word,(lastWhitespacePosition - word.length())));
                         }
                         //word="";
                         word = "";
                         if(Character.isLetter(ch)) {
                        	 word = "" + ch;
                         }
                     }
                 }
                 else {
                     word += ch;
                 }
            }
             
             if(word.length() > 0) {
                 if(isReservedWord(word.trim())) {
                	 System.out.println(word);
                     hiliteWords.add(new HiliteWord(word,(lastWhitespacePosition - word.length())));
                 }
             } 
             
            return hiliteWords;
         }

         private static boolean upperAndUpper(char ch1, char ch2) {
			return (Character.isUpperCase(ch1)&&Character.isUpperCase(ch2))||
					(Character.isLowerCase(ch1)&&Character.isUpperCase(ch2));
		}

		private static final boolean isReservedWord(String word) {
        	 return keyWordSet.contains(word.toUpperCase());
        }
    }