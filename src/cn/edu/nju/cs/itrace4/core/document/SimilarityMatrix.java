package cn.edu.nju.cs.itrace4.core.document;

import java.io.*;
import java.util.*;

/**
 * Created by niejia on 15/2/10.
 */

public class SimilarityMatrix implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//能够保证顺序
    protected Map<String, Map<String, Double>> matrix = new LinkedHashMap<>();

    protected double threshold;
    protected int cutN;
    protected String name;
    protected double variableCut;
    protected double variableThreshold;
    protected double scaleThreshold;

    public SimilarityMatrix() {
        threshold = 0.0;
        cutN = 0;
        name = "";
        variableCut = 1.0;
        variableThreshold = 0.0;
        scaleThreshold = 0.0;
    }

    public SimilarityMatrix(SimilarityMatrix inMatrix) {
        this.matrix = inMatrix.matrix;
        this.name = inMatrix.name;
        this.threshold = inMatrix.threshold;
        this.cutN = inMatrix.cutN;
        this.variableCut = inMatrix.variableCut;
        this.variableThreshold = inMatrix.variableThreshold;
        this.scaleThreshold = inMatrix.scaleThreshold;
    }

    public void setCutN(Integer value) {
        if (value != null) {
            this.cutN = value;
        }
    }

    public int getCutN() {
        return this.cutN;
    }

    public void setVariableCut(Double value) {
        if (value != null) {
            this.variableCut = value;
        }
    }

    public double getVariableCut() {
        return variableCut;
    }

    public void setThreshold(Double value) {
        if (value != null) {
            this.threshold = value;
        }
    }

    public double getThreshold() {
        return threshold;
    }

    public void setVariableThreshold(Double value) {
        if (value != null) {
            this.variableThreshold = value;
        }
    }

    public double getVariableThreshold() {
        return variableThreshold;
    }

    public void setScaleThreshold(Double value) {
        if (value != null) {
            this.scaleThreshold = value;
        }
    }

    public double getScaleThreshold() {
        return scaleThreshold;
    }

    public StringHashSet sourceArtifactsIds() {
        StringHashSet hs = new StringHashSet();
        for (String s : matrix.keySet()) {
            hs.add(s);
        }
        return hs;
    }

    public StringHashSet targetArtifactsIds() {
        StringHashSet hs = new StringHashSet();
        for (SingleLink link : allLinks()) {
//            System.out.println(link.getTargetArtifactId());
            hs.add(link.getTargetArtifactId());
        }
        return hs;
    }

    public Double getScoreForLink(String sourceArtfactId, String targetArtfactId) {
        Double retVal = 0.0;////
        Map<String, Double> links = matrix.get(sourceArtfactId);
        if (links != null) {
            retVal = links.get(targetArtfactId);
        }
        if(retVal==null){
        	System.out.println(retVal);
        }
        return retVal;
    }

    public void setScoreForLink(String sourceArtfactId, String targetArtfactId, Double score) {
        if (matrix.get(sourceArtfactId).get(targetArtfactId) != null) {
            matrix.get(sourceArtfactId).put(targetArtfactId, score);
        } else {
            System.out.println("Target link not Found, Update score failed.");
        }
    }

    public LinksList allLinks() {
        LinksList allLinks = new LinksList();
        for (Map.Entry<String, Map<String, Double>> sourceArtifact : matrix.entrySet()) {
            String sourceArtifactId = sourceArtifact.getKey();
            Map<String, Double> sourceArtifactLinks = sourceArtifact.getValue();

            for (Map.Entry<String, Double> targetArtifact : sourceArtifactLinks.entrySet()) {
                String targetArtifactId = targetArtifact.getKey();
                Double score = targetArtifact.getValue();

                allLinks.add(new SingleLink(sourceArtifactId, targetArtifactId, score));
            }
        }
        return allLinks;
    }

    public void addLink(String sourceArtifactId, String targetArtifactId, Double score) {
        Map<String, Double> links = matrix.get(sourceArtifactId);
        if (links == null) {
            links = new LinkedHashMap<>();
            matrix.put(sourceArtifactId, links);
        }

        if (links.containsKey(targetArtifactId) == false) {
            links.put(targetArtifactId, score);
        } else {
            if (score != links.get(targetArtifactId)) {
                throw new IllegalArgumentException(String.format("Link for source artifact %s and target artifact %s has already been added to the spare matrix", sourceArtifactId,
                        targetArtifactId));
            }
        }
    }

    public Map<String, Double> getLinksForSourceId(String sourceArtifactId) {
        return matrix.get(sourceArtifactId);
    }

    public int count() {
        int totalCount = 0;
        for (Map<String, Double> links : matrix.values()) {
            totalCount += links.size();
        }
        return totalCount;
    }

    public boolean isLinkAboveThreshold(String sourceArtifactId, String targetArtifactId) {
        boolean retVal = false;
        Map<String, Double> links = matrix.get(sourceArtifactId);
        if (links != null) {
            Double score = links.get(targetArtifactId);
            if (score != null) {//下面的判断让人费解   但是这个函数return true的话 说明oracle里确实存在这个链接关系
                retVal = (score >= threshold);
            }
        }
        return retVal;
    }

    public boolean isLinkInCutN(String sourceArtifactId, String targetArtifactId) {
        boolean retVal = false;
        LinkedHashMap<String, Double> links = (LinkedHashMap) matrix.get(sourceArtifactId);
        if (links != null) {
            List<String> linksInCutN = new ArrayList<>();
            int i = 0;
            for (String key : links.keySet()) {
                if (i < cutN & links.get(targetArtifactId) > 0.0) {
                    linksInCutN.add(key);
                    i++;
                }
            }
            retVal = linksInCutN.contains(targetArtifactId);
        }
        return retVal;
    }

    public boolean isLinkInVariableCut(String sourceArtifactId, String targetArtifactId) {
        boolean retVal = false;

        double tmp = threshold;
        threshold = 0.0;
        LinkedHashMap<String, Double> links = (LinkedHashMap) matrix.get(sourceArtifactId);
        int cutNumInVariableCut = (int) (getCountOfLinksAboveThresholdForSourceArtifact(sourceArtifactId) * variableCut);
        if (links != null) {

            List<String> linksInVariableCut = new ArrayList<>();
            int i = 0;
            for (String key : links.keySet()) {
                if (i < cutNumInVariableCut) {
                    linksInVariableCut.add(key);
                    i++;
                }
            }
            retVal = linksInVariableCut.contains(targetArtifactId);
        }

        // recover threshold
        threshold = tmp;
        return retVal;
    }

    public boolean isLinkAboveVariableThreshold(String sourceArtifactId, String targetArtifactId) {
        boolean retVal = false;

        LinkedHashMap<String, Double> links = (LinkedHashMap) matrix.get(sourceArtifactId);
        double maxSimilarity = getMaxSimilarityForSourceArtifact(sourceArtifactId);
        double minSimilarity = getMinSimilarityForSourceArtifact(sourceArtifactId);
        double thresholdAtVariable = minSimilarity + (maxSimilarity - minSimilarity) * variableThreshold;
//        System.out.println(" thresholdAtVariable = " + thresholdAtVariable );
        if (links != null) {
            Double score = links.get(targetArtifactId);
            if (score != null) {
                retVal = (score > thresholdAtVariable);
            }
        }
        return retVal;
    }

    public boolean isLinkAboveScaleThreshold(String sourceArtifactId, String targetArtifactId) {
        boolean retVal = false;

        LinkedHashMap<String, Double> links = (LinkedHashMap) matrix.get(sourceArtifactId);
        double maxSimilarity = getMaxSimilarityForSourceArtifact(sourceArtifactId);
        double thresholdAtScale = maxSimilarity * scaleThreshold;
//        System.out.println(" thresholdAtScale = " + thresholdAtScale );
        if (links != null) {
            Double score = links.get(targetArtifactId);
            if (score != null) {
                retVal = (score > thresholdAtScale);
            }
        }
        return retVal;
    }

    private double getMaxSimilarityForSourceArtifact(String sourceArtifactId) {
        double tmp = threshold;
        threshold = 0.0;
        LinksList links = getLinksAboveThresholdForSourceArtifact(sourceArtifactId);
        threshold = tmp;
        if (links.size() > 0) {
            return links.get(0).getScore();
        } else {
            return 0.0;
        }
    }

    public double getMinSimilarityForSourceArtifact(String sourceArtifactId) {
        LinkedHashMap<String, Double> links = (LinkedHashMap) matrix.get(sourceArtifactId);
        double tmp = threshold;
        threshold = 0.0;
        LinksList linksAboveThreshold = getLinksAboveThresholdForSourceArtifact(sourceArtifactId);
        threshold = tmp;
        if (links.size() == linksAboveThreshold.size()) {
            return linksAboveThreshold.get(linksAboveThreshold.size() - 1).getScore();
        } else {
            return 0.0;
        }
    }

    public StringHashSet getSetOfTargetArtifactIdsAboveThresholdForSourceArtifact(String sourceArtifactId) {
        StringHashSet linksForSourceArtifact = new StringHashSet();
        linksForSourceArtifact = new StringHashSet();

        Map<String, Double> links = matrix.get(sourceArtifactId);
        if (links != null) {
            for (String targetArtfactId : links.keySet()) {
                if (links.get(targetArtfactId) > threshold) {
                    linksForSourceArtifact.add(targetArtfactId);
                }
            }
        }
        return linksForSourceArtifact;
    }

    public StringHashSet getSetOfTargetArtifactIdsInCutNForSourceArtifact(String sourceArtifactId) {
        StringHashSet linksForSourceArtifact = new StringHashSet();
        linksForSourceArtifact = new StringHashSet();

        Map<String, Double> links = matrix.get(sourceArtifactId);
        if (links != null) {
            for (String targetArtifactId : links.keySet()) {
                if (isLinkInCutN(sourceArtifactId, targetArtifactId)) {
                    linksForSourceArtifact.add(targetArtifactId);
                }
            }
        }
        return linksForSourceArtifact;
    }

    public StringHashSet getSetOfTargetArtifactIdsInVariableCutForSourceArtifact(String sourceArtifactId) {
        StringHashSet linksForSourceArtifact = new StringHashSet();
        linksForSourceArtifact = new StringHashSet();
        Map<String, Double> links = matrix.get(sourceArtifactId);
        if (links != null) {
            for (String targetArtifactId : links.keySet()) {
                if (isLinkInVariableCut(sourceArtifactId, targetArtifactId)) {
                    linksForSourceArtifact.add(targetArtifactId);
                }
            }
        }
        return linksForSourceArtifact;
    }

    public StringHashSet getSetOfTargetArtifactIdsAboveVariableThresholdForSourceArtifact(String sourceArtifactId) {
        StringHashSet linksForSourceArtifact = new StringHashSet();
        linksForSourceArtifact = new StringHashSet();
        Map<String, Double> links = matrix.get(sourceArtifactId);
        if (links != null) {
            for (String targetArtifactId : links.keySet()) {
                if (isLinkAboveVariableThreshold(sourceArtifactId, targetArtifactId)) {
                    linksForSourceArtifact.add(targetArtifactId);
                }
            }
        }
        return linksForSourceArtifact;
    }

    public StringHashSet getSetOfTargetArtifactIdsAboveScaleThresholdForSourceArtifact(String sourceArtifactId) {
        StringHashSet linksForSourceArtifact = new StringHashSet();
        linksForSourceArtifact = new StringHashSet();

        Map<String, Double> links = matrix.get(sourceArtifactId);
        if (links != null) {
            for (String targetArtifactId : links.keySet()) {
                if (isLinkAboveScaleThreshold(sourceArtifactId, targetArtifactId)) {
                    linksForSourceArtifact.add(targetArtifactId);
                }
            }
        }
        return linksForSourceArtifact;
    }


    public LinksList getLinksAboveThresholdForSourceArtifact(String sourceArtifactId) {
        LinksList linksForSourceArtifact = new LinksList();
        linksForSourceArtifact = new LinksList();

        Map<String, Double> links = matrix.get(sourceArtifactId);
        if (links != null) {
            for (String targetArtifactId : links.keySet()) {
                if (isLinkAboveThreshold(sourceArtifactId, targetArtifactId)) {
                    linksForSourceArtifact.add(new SingleLink(sourceArtifactId, targetArtifactId,
                            links.get(targetArtifactId)));
                }
            }
        }
        return linksForSourceArtifact;
    }

    public LinksList getLinksInCutNForSourceArtifact(String sourceArtifactId) {
        LinksList linksForSourceArtifact = new LinksList();
        linksForSourceArtifact = new LinksList();

        Map<String, Double> links = matrix.get(sourceArtifactId);
        if (links != null) {
            for (String targetArtifactId : links.keySet()) {
                if (isLinkInCutN(sourceArtifactId, targetArtifactId)) {
                    linksForSourceArtifact.add(new SingleLink(sourceArtifactId, targetArtifactId,
                            links.get(targetArtifactId)));
                }
            }
        }
        return linksForSourceArtifact;
    }

    public LinksList getLinksInVariableCutForSourceArtifact(String sourceArtifactId) {
        LinksList linksForSourceArtifact = new LinksList();
        linksForSourceArtifact = new LinksList();

        Map<String, Double> links = matrix.get(sourceArtifactId);
        if (links != null) {
            for (String targetArtifactId : links.keySet()) {
                if (isLinkInVariableCut(sourceArtifactId, targetArtifactId)) {
                    linksForSourceArtifact.add(new SingleLink(sourceArtifactId, targetArtifactId,
                            links.get(targetArtifactId)));
                }
            }
        }
        return linksForSourceArtifact;
    }

    public LinksList getLinksAboveVariableThresholdForSourceArtifact(String sourceArtifactId) {
        LinksList linksForSourceArtifact = new LinksList();
        linksForSourceArtifact = new LinksList();

        Map<String, Double> links = matrix.get(sourceArtifactId);
        if (links != null) {
            for (String targetArtifactId : links.keySet()) {
                if (isLinkAboveVariableThreshold(sourceArtifactId, targetArtifactId)) {
                    linksForSourceArtifact.add(new SingleLink(sourceArtifactId, targetArtifactId,
                            links.get(targetArtifactId)));
                }
            }
        }
        return linksForSourceArtifact;
    }

    public LinksList getLinksAboveScaleThresholdForSourceArtifact(String sourceArtifactId) {
        LinksList linksForSourceArtifact = new LinksList();
        linksForSourceArtifact = new LinksList();

        Map<String, Double> links = matrix.get(sourceArtifactId);
        if (links != null) {
            for (String targetArtifactId : links.keySet()) {
                if (isLinkAboveScaleThreshold(sourceArtifactId, targetArtifactId)) {
                    linksForSourceArtifact.add(new SingleLink(sourceArtifactId, targetArtifactId,
                            links.get(targetArtifactId)));
                }
            }
        }
        return linksForSourceArtifact;
    }

    public int getCountOfLinksAboveThresholdForSourceArtifact(String sourceArtifactId) {
        return getSetOfTargetArtifactIdsAboveThresholdForSourceArtifact(sourceArtifactId).size();
    }

    public int getCountOfLinksInCutNForSourceArtifact(String sourceArtifactId) {
        return getSetOfTargetArtifactIdsInCutNForSourceArtifact(sourceArtifactId).size();
    }

    public int getCountOfLinksInVariableCutForSourceArtifact(String sourceArtifactId) {
        return getSetOfTargetArtifactIdsInVariableCutForSourceArtifact(sourceArtifactId).size();
    }

    public int getCountOfLinksAboveVariableThresholdForSourceArtifact(String sourceArtifactId) {
        return getSetOfTargetArtifactIdsAboveVariableThresholdForSourceArtifact(sourceArtifactId).size();
    }

    public int getCountOfLinksAboveScaleThresholdForSourceArtifact(String sourceArtifactId) {
        return getSetOfTargetArtifactIdsAboveScaleThresholdForSourceArtifact(sourceArtifactId).size();
    }

    public LinksList getLinksAboveThreshold() {
        LinksList allLinks = allLinks();
        LinksList linksAboveThreshold = new LinksList();
        for (SingleLink link : allLinks) {
        	/**
        	 * replace more than with no less than
        	 * */
            if (link.getScore() >= threshold) {
                linksAboveThreshold.add(link);
            }
        }
        return linksAboveThreshold;
    }

    public LinksList getLinksInCutN() {
        LinksList linksInCutN = new LinksList();
        for (String sourceArtifactId : matrix.keySet()) {
            LinksList linksInCutNForSourceId = getLinksInCutNForSourceArtifact(sourceArtifactId);
            for (SingleLink link : linksInCutNForSourceId) {
                linksInCutN.add(link);
            }
        }
        return linksInCutN;
    }

    public LinksList getLinksInVariableCut() {
        LinksList linksInVariableCut = new LinksList();
        for (String sourceArtifactId : matrix.keySet()) {
            LinksList linksInVariableCutForSourceId = getLinksInVariableCutForSourceArtifact(sourceArtifactId);
            for (SingleLink link : linksInVariableCutForSourceId) {
                linksInVariableCut.add(link);
            }
        }
        return linksInVariableCut;
    }

    public LinksList getLinksAboveVariableThreshold() {
        LinksList linksAboveVariableThreshold = new LinksList();
        for (String sourceArtifactId : matrix.keySet()) {
            LinksList linksAboveVariableThresholdForSourceId = getLinksAboveVariableThresholdForSourceArtifact(sourceArtifactId);
            for (SingleLink link : linksAboveVariableThresholdForSourceId) {
                linksAboveVariableThreshold.add(link);
            }
        }
        return linksAboveVariableThreshold;
    }

    public LinksList getLinksAboveScaleThreshold() {
        LinksList linksAboveScaleThreshold = new LinksList();
        for (String sourceArtifactId : matrix.keySet()) {
            LinksList linksAboveScaleThresholdForSourceId = getLinksAboveScaleThresholdForSourceArtifact(sourceArtifactId);
            for (SingleLink link : linksAboveScaleThresholdForSourceId) {
                linksAboveScaleThreshold.add(link);
            }
        }
        return linksAboveScaleThreshold;
    }

    public List<String> getRankedTarget(String source) {
        Map<String, Double> links = getLinksForSourceId(source);

        List<String> targetList = new ArrayList<>();
        List<Double> scoreList = new ArrayList<>();

        for (String target : links.keySet()) {
            double score = getScoreForLink(source, target);
            targetList.add(target);
//            System.out.println(" target = " + target );
            scoreList.add(score);
//            System.out.println(" score = " + score );
        }

        return targetList;
    }

    public String getFirstMaxValueTarget(String source) {
        Map<String, Double> links = getLinksForSourceId(source);

        List<String> targetList = new ArrayList<>();
        List<Double> scoreList = new ArrayList<>();

        for (String target : links.keySet()) {
            double score = getScoreForLink(source, target);
            targetList.add(target);
            scoreList.add(score);
        }

        return targetList.get(0);
    }

    public String getSecondMaxValueTarget(String source, List firstPiecesCode) {
        Map<String, Double> links = getLinksForSourceId(source);

        for (String target : links.keySet()) {
            if (!firstPiecesCode.contains(target)) {
                return target;
            }
//            System.out.println(target + " " + getScoreForLink(source, target));
        }
        throw new NoSuchElementException("Can't find second highest point");
    }


    public LinksList getQualityLinks() {
        LinksList qualityLinks = new LinksList();

        for (String source : sourceArtifactsIds()) {
            Map<String, Double> links = getLinksForSourceId(source);
//            double maxDist = 0.0;
//            String qualityTarget = "";
//
//            double preScore = 0.0;
//            String preTarget = "";
            List<String> targetList = new ArrayList<>();
            List<Double> scoreList = new ArrayList<>();

            for (String target : links.keySet()) {
                double score = getScoreForLink(source, target);
                targetList.add(target);
                scoreList.add(score);
            }

            String qualityTarget = targetList.get(0);
            double maxDist = 0.0;
            double baseScore = 0.0;
            int size = targetList.size();
            for (int i = 1; i < size; i++) {
                double dist = scoreList.get(i - 1) - scoreList.get(i);

                if (dist > maxDist) {
                    maxDist = dist;

                    qualityTarget = targetList.get(i - 1);
                    baseScore = scoreList.get(i - 1);
                }
            }

            for (String target : links.keySet()) {
                double score = getScoreForLink(source, target);
                if (score >= baseScore) {
                    qualityLinks.add(new SingleLink(source, target, score));
                }
            }
//            System.out.println(" qualityTarget = " + source + " " + qualityTarget + " " + baseScore);
        }
//        System.out.println(qualityLinks);
//        System.out.println(qualityLinks.size());
        return qualityLinks;
    }

    public String toString() {
        return allLinks().toString();
    }

    public LinksList getHighestLinks() {
        LinksList highestLinks = new LinksList();

        for (String source : sourceArtifactsIds()) {
            LinksList links = getLinksAboveThresholdForSourceArtifact(source);
            Collections.sort(links, Collections.reverseOrder());
            highestLinks.add(links.get(0));
        }
        return highestLinks;
    }


}

