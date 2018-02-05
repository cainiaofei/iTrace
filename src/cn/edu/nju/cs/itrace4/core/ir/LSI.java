package cn.edu.nju.cs.itrace4.core.ir;

import cn.edu.nju.cs.itrace4.core.document.*;
import cn.edu.nju.cs.itrace4.core.ir.lsi.SVD;
import cn.edu.nju.cs.itrace4.util.Setting;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.Collections;
import java.util.List;

/**
 * Created by niejia on 16/3/9.
 */
public class LSI implements IRModel {
    private TermDocumentMatrix queries;
    private TermDocumentMatrix documents;
    private int k = Setting.LSI_K;

    public SimilarityMatrix Compute(ArtifactsCollection source, ArtifactsCollection target) {
    	/**
    	 * @date 2018.1.26
    	 * @author zzf
    	 * @description change k value based on projectName
    	 */
    	String projectName = System.getProperty("projectName");
    	if(projectName!=null && (projectName.equalsIgnoreCase("infinispan")||
    			projectName.equalsIgnoreCase("pig"))) {
    		k = 200;
    		System.out.println("lsi_k-------------------"+k);
    	}
    	else {
    		System.out.println("lsi_k-------------------"+k);
    	}
    	
        ArtifactsCollection bothSourceAndTarget = new ArtifactsCollection();
        bothSourceAndTarget.putAll(source);
        bothSourceAndTarget.putAll(target);

        return Compute(new TermDocumentMatrix(source), new TermDocumentMatrix(target), new TermDocumentMatrix(bothSourceAndTarget));
    }

    private SimilarityMatrix Compute(TermDocumentMatrix source, TermDocumentMatrix target, TermDocumentMatrix both) {

        TermDocumentMatrix TF = ComputeTF(both);
        double[] IDF = ComputeIDF(ComputeDF(both), both.NumDocs());
        TermDocumentMatrix TFIDF_Origin = ComputeTFIDF(TF, IDF);

        TermDocumentMatrix TFIDF_svd = svd(TFIDF_Origin);
        TermDocumentMatrix sourceIDs = ComputeIdentities(source);
        TermDocumentMatrix targetIDs = ComputeIdentities(target);

        TermDocumentMatrix sourceWithTFIDF = ReplaceIDWithTFIDF(sourceIDs, TFIDF_svd);
        TermDocumentMatrix targetWithTFIDF = ReplaceIDWithTFIDF(targetIDs, TFIDF_svd);

        return ComputeSimilarities(sourceWithTFIDF, targetWithTFIDF);
    }

    private TermDocumentMatrix svd(TermDocumentMatrix tfidf_origin) {

        RealMatrix realMatrix = convertTermDocumentMatrixToRealMatrix(tfidf_origin);
        RealMatrix rebuildMatrix = SVD.compute(realMatrix, k);
        TermDocumentMatrix tfidf_svd = convertRealMatrixToTermDocumentMatrix(rebuildMatrix, tfidf_origin);
        return tfidf_svd;
    }


    private RealMatrix convertTermDocumentMatrixToRealMatrix(TermDocumentMatrix tfidf_origin) {
        double[][] dates = new double[tfidf_origin.NumTerms()][tfidf_origin.NumDocs()];

        for (int i = 0; i < tfidf_origin.NumTerms(); i++) {
            for (int j = 0; j < tfidf_origin.NumDocs(); j++) {
                dates[i][j] = tfidf_origin.getValue(j, i);
            }
        }

        RealMatrix realMatrix = MatrixUtils.createRealMatrix(dates);
        return realMatrix;
    }

    private TermDocumentMatrix convertRealMatrixToTermDocumentMatrix(RealMatrix rebuildMatrix, TermDocumentMatrix tfidf_origin) {


        for (int i = 0; i < rebuildMatrix.getRowDimension(); i++) {
            for (int j = 0; j < rebuildMatrix.getColumnDimension(); j++) {
                if (rebuildMatrix.getEntry(i, j) > 0.0) {
                    tfidf_origin.setValue(j, i, rebuildMatrix.getEntry(i, j));
                }
            }
        }
        return tfidf_origin;
    }

    private TermDocumentMatrix ReplaceIDWithTFIDF(TermDocumentMatrix ids, TermDocumentMatrix tfidf) {

//        double[][] idsMatrix = ids.getMatrix();
//        List<String> idsDocIndex = ids.getDocIndex();
//        Map<String, Integer> idsDocIndexLookup = new HashMap<>(ids.getDocIndex());
//
//        ids.getMatrix() = new double[tfidf.NumDocs()][];
//
        for (int i = 0; i < ids.NumDocs(); i++) {
            for (int j = 0; j < ids.NumTerms(); j++) {
                ids.setValue(i, j, tfidf.getValue(ids.getDocumentName(i), ids.getTermName(j)));
            }
        }
//
        return ids;
//        List<TermDocumentMatrix> matrices = TermDocumentMatrix.Equalize(ids, tfidf);
//        return matrices.get(0);

    }

    private TermDocumentMatrix ComputeTFIDF(TermDocumentMatrix tf, double[] idf) {
        for (int i = 0; i < tf.NumDocs(); i++) {
            for (int j = 0; j < tf.NumTerms(); j++) {
                tf.setValue(i, j, tf.getValue(i, j) * idf[j]);
            }
        }
        return tf;
    }

    private double[] ComputeIDF(double[] df, int numDocs) {
        double[] idf = new double[df.length];
        for (int i = 0; i < df.length; i++) {
            if (df[i] <= 0.0) {
                idf[i] = 0.0;
            } else {
                idf[i] = Math.log(numDocs / df[i]);
            }
        }
        return idf;
    }

    private double[] ComputeDF(TermDocumentMatrix matrix) {
        double[] df = new double[matrix.NumTerms()];
        for (int j = 0; j < matrix.NumTerms(); j++) {
            df[j] = 0.0;
            for (int i = 0; i < matrix.NumDocs(); i++) {
                df[j] += (matrix.getValue(i, j) > 0.0) ? 1.0 : 0.0;
            }
        }
        return df;
    }

    private TermDocumentMatrix ComputeTF(TermDocumentMatrix matrix) {
        for (int i = 0; i < matrix.NumDocs(); i++) {
            double max = 0.0;
            for (int k = 0; k < matrix.NumTerms(); k++) {
                max += matrix.getValue(i, k);
            }

            for (int j = 0; j < matrix.NumTerms(); j++) {
                matrix.setValue(i, j, (matrix.getValue(i, j) / max));
            }
        }
        return matrix;
    }

    private TermDocumentMatrix ComputeIdentities(TermDocumentMatrix matrix) {
        for (int i = 0; i < matrix.NumDocs(); i++) {
            for (int j = 0; j < matrix.NumTerms(); j++) {
                matrix.setValue(i, j, ((matrix.getValue(i, j) > 0.0) ? 1.0 : 0.0));
            }
        }
        return matrix;
    }

    private SimilarityMatrix ComputeSimilarities(TermDocumentMatrix ids, TermDocumentMatrix tfidf) {
        SimilarityMatrix sims = new SimilarityMatrix();
        List<TermDocumentMatrix> matrices = TermDocumentMatrix.Equalize(ids, tfidf);

        queries = matrices.get(0);
        documents = matrices.get(1);

        for (int i = 0; i < ids.NumDocs(); i++) {
            LinksList links = new LinksList();
            for (int j = 0; j < tfidf.NumDocs(); j++) {
                double product = 0.0;
                double asquared = 0.0;
                double bsquared = 0.0;
                for (int k = 0; k < matrices.get(0).NumTerms(); k++) {
                    double a = matrices.get(0).getValue(i, k);
                    double b = matrices.get(1).getValue(j, k);
                    product += (a * b);
                    asquared += Math.pow(a, 2);
                    bsquared += Math.pow(b, 2);
                }
                double cross = Math.sqrt(asquared) * Math.sqrt(bsquared);
                if (cross == 0.0) {
                    links.add(new SingleLink(ids.getDocumentName(i).trim(), tfidf.getDocumentName(j).trim(), 0.0));
                } else {
                    links.add(new SingleLink(ids.getDocumentName(i), tfidf.getDocumentName(j), product / cross));
                }
            }

            Collections.sort(links, Collections.reverseOrder());

            for (SingleLink link : links) {
                sims.addLink(link.getSourceArtifactId(), link.getTargetArtifactId(), link.getScore());
            }
        }

        return sims;
    }

    @Override
    public TermDocumentMatrix getTermDocumentMatrixOfQueries() {
        return queries;
    }

    @Override
    public TermDocumentMatrix getTermDocumentMatrixOfDocuments() {
        return documents;
    }
}
