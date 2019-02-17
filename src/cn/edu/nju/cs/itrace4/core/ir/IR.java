package cn.edu.nju.cs.itrace4.core.ir;

import cn.edu.nju.cs.itrace4.core.algo.prealgo.CSTI;
import cn.edu.nju.cs.itrace4.core.dataset.TextDataset;
import cn.edu.nju.cs.itrace4.core.document.SimilarityMatrix;
import cn.edu.nju.cs.itrace4.core.document.SingleLink;
import cn.edu.nju.cs.itrace4.core.metrics.MetricComputation;
import cn.edu.nju.cs.itrace4.core.metrics.Result;
import cn.edu.nju.cs.itrace4.core.metrics.cut.CutStrategy;

/**
 * Created by niejia on 15/2/23.
 */
public class IR {

    public static Result compute(TextDataset textDataset, String modelType, CSTI algorithm) {
        Result result = null;

        try {
            Class modelTypeClass = Class.forName(modelType);
            IRModel irModel = (IRModel) modelTypeClass.newInstance();
            
            SimilarityMatrix similarityMatrix = irModel.Compute(textDataset.getSourceCollection(),
            		textDataset.getTargetCollection());
            
            SimilarityMatrix matrix_improve = algorithm.improve(similarityMatrix, textDataset, similarityMatrix);
            SimilarityMatrix union_matrix = new SimilarityMatrix();

            for (SingleLink link : matrix_improve.allLinks()) {
                String source = link.getSourceArtifactId();
                String target = link.getTargetArtifactId();

                //涓や袱涔嬮棿閮界粡杩囩浉浼煎害璁＄畻浜�   鎵�浠ュソ鍍忓簲璇ヤ笉浼氬瓨鍦ㄦ紡鎺夌殑鎯呭喌
                if (textDataset.getRtm().sourceArtifactsIds().contains(source) && textDataset.getRtm().
                		targetArtifactsIds().contains(target)) {
                    union_matrix.addLink(source, target, link.getScore());
                }
            }
            
            /*
             * add a code line 
             */
            
            matrix_improve = union_matrix;
            
            MetricComputation metricComputation = new MetricComputation(matrix_improve, textDataset.getRtm());

            result = metricComputation.compute(CutStrategy.CONSTANT_THRESHOLD);
            result.setAlgorithmName(algorithm.getAlgorithmName());
            result.setCorrectImprovedTargetsList(algorithm.getCorrectImprovedTargetsList());
            result.setAlgorithmParameters(algorithm.getAlgorithmParameters());
            result.setLog(algorithm.getDetails());
        } catch (ClassNotFoundException e) {
            System.out.println("No such IR model exists");
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        result.setModel(modelType.toString());
//        result.setAlgorithmName(algorithm.getAlgorithmName());
//        result.setAlgorithmParameters(algorithm.getAlgorithmParameters());
//        result.setLog(algorithm.getDetails());

        return result;
    }

    public static DocVectorLookUp getDocVectorLookUp(TextDataset textDataset, String modelType, CSTI algorithm) {

        try {
            Class modelTypeClass = Class.forName(modelType);
            IRModel irModel = (IRModel) modelTypeClass.newInstance();

            SimilarityMatrix similarityMatrix = irModel.Compute(textDataset.getSourceCollection(), textDataset.getTargetCollection());
            return new DocVectorLookUp(irModel.getTermDocumentMatrixOfQueries());

        } catch (ClassNotFoundException e) {
            System.out.println("No such IR model exists");
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }
}
