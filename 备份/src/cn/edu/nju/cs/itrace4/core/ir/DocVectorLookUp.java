package cn.edu.nju.cs.itrace4.core.ir;

import cn.edu.nju.cs.itrace4.core.document.TermDocumentMatrix;

import java.util.NoSuchElementException;

/**
 * Created by niejia on 15/5/20.
 */
public class DocVectorLookUp {

    private TermDocumentMatrix query;

    public DocVectorLookUp(TermDocumentMatrix query) {
        this.query = query;
    }

    public double[] getVectorForDoc(String target) {
        double[] doc = query.getDocument(target);
        if (doc==null) throw new NoSuchElementException("Target Doc not In TermDocumentMatrix");
        return query.getDocument(target);
    }
}
