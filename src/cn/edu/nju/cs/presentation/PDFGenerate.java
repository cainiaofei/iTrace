package cn.edu.nju.cs.presentation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;


public class PDFGenerate {

	public void saveAsPDF(String targetPath,String[][] data) {
		if(!targetPath.endsWith(".pdf")) {
			targetPath += ".pdf";
		}
		File file = new File(targetPath);
		Document document = new Document();
	    try {
			PdfWriter.getInstance(document, new FileOutputStream(targetPath));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	    document.open();
	    PdfPTable table = new PdfPTable(data[0].length);
	    try {
			table.setWidths(new float[] {1,5,3});
		} catch (DocumentException e1) {
			e1.printStackTrace();
		}
	    for(int row = 0; row < data.length;row++) {
	    	for(int col=0; col < data[row].length;col++) {
	    		table.addCell(data[row][col]);
	    	}
	    }
	    try {
			document.add(table);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
        document.close();
	}
}
