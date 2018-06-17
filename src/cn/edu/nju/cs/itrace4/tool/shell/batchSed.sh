#!/bin/sh

pwd=~/workspace/iTrace4/src

find . -name '*java'|xargs sed -i 's/cn.edu.nju.cs.itrace4.core.algo.None_CSTI/cn.edu.nju.cs.itrace4.core.algo.prealgo.None_CSTI/'
find . -name '*java'|xargs sed -i 's/cn.edu.nju.cs.itrace4.core.algo.O_CSTI/cn.edu.nju.cs.itrace4.core.algo.prealgo.O_CSTI/'
find . -name '*java'|xargs sed -i 's/cn.edu.nju.cs.itrace4.core.algo.PageRank_CSTI/cn.edu.nju.cs.itrace4.core.algo.prealgo.PageRank_CSTI/'
find . -name '*java'|xargs sed -i 's/cn.edu.nju.cs.itrace4.core.algo.UseEdge/cn.edu.nju.cs.itrace4.core.algo.prealgo.UseEdge/'


find . -name '*java'|xargs sed -i 's/cn.edu.nju.cs.itrace4.core.algo.UD_CSTI/cn.edu.nju.cs.itrace4.core.algo.prealgo.UD_CSTI/'
find . -name '*java'|xargs sed -i 's/cn.edu.nju.cs.itrace4.demo.algo.UD_CallSubGraph_Then_DataSubGraph_Closeness/cn.edu.nju.cs.itrace4.core.algo.region.callthendata.UD_CallSubGraph_Then_DataSubGraph_Closeness/'
find . -name '*java'|xargs sed -i 's/cn.edu.nju.cs.itrace4.demo.algo.UD_CallThenDataWithBonusForLone/cn.edu.nju.cs.itrace4.core.algo.region.callthendata.UD_CallThenDataWithBonusForLone/'
find . -name '*java'|xargs sed -i 's/cn.edu.nju.cs.itrace4.demo.cdgraph.temp.UD_CallDataTreatEqualCountTemp/cn.edu.nju.cs.itrace4.core.algo.region.calldata.UD_CallDataTreatEqualCountTemp/'
find . -name '*java'|xargs sed -i 's/cn.edu.nju.cs.itrace4.demo.cdgraph.UD_CallDataTreatEqualCount/cn.edu.nju.cs.itrace4.core.algo.region.calldata.UD_CallDataTreatEqualCount/'
find . -name '*java'|xargs sed -i 's/cn.edu.nju.cs.itrace4.demo.cdgraph.UD_CallDataTreatEqual/cn.edu.nju.cs.itrace4.core.algo.region.calldata.UD_CallDataTreatEqual/'
find . -name '*java'|xargs sed -i 's/cn.edu.nju.cs.itrace4.demo.cdgraph.UD_CallDataDynamic/cn.edu.nju.cs.itrace4.core.algo.region.calldata.UD_CallDataDynamic/'
find . -name '*java'|xargs sed -i 's/cn.edu.nju.cs.itrace4.demo.cdgraph.UD_CallDataTreatEqual/cn.edu.nju.cs.itrace4.core.algo.region.calldata.UD_CallDataTreatEqual/'
find . -name '*java'|xargs sed -i 's/cn.edu.nju.cs.itrace4.demo.remainPR.UD_CSTI_First_Count_Percent_Remain/cn.edu.nju.cs.itrace4.core.algo.prealgo.remain.UD_CSTI_First_Count_Percent_Remain/'
find . -name '*java'|xargs sed -i 's/cn.edu.nju.cs.itrace4.demo.algo.UD_SubGraphWithBonusForLone/cn.edu.nju.cs.itrace4.core.algo.region.callthendata.UD_SubGraphWithBonusForLone/'
find . -name '*java'|xargs sed -i 's/cn.edu.nju.cs.itrace4.demo.algo.UD_SubGraph_Closeness/cn.edu.nju.cs.itrace4.core.algo.region.callthendata.UD_SubGraph_Closeness/'
find . -name '*java'|xargs sed -i 's/cn.edu.nju.cs.itrace4.demo.algo.SortBySubGraph/cn.edu.nju.cs.itrace4.core.algo.region.util.sort.SortBySubGraph/'
find . -name '*java'|xargs sed -i 's/cn.edu.nju.cs.itrace4.demo.algo.SortVertexByScore/cn.edu.nju.cs.itrace4.core.algo.region.util.sort.SortVertexByScore/'
find . -name '*java'|xargs sed -i 's/cn.edu.nju.cs.itrace4.core.algo.CSTI/cn.edu.nju.cs.itrace4.core.algo.prealgo.CSTI/'


find . -name '*java'|xargs sed -i 's/cn.edu.nju.cs.itrace4.demo.tool.AnalyzeResult/cn.edu.nju.cs.itrace4.tool.AnalyzeResult/'


find . -name '*java'|xargs sed -i 's/cn.edu.nju.cs.itrace4.demo.algo.coderegion.UD_CodeTextAsWholeInRegion/cn.edu.nju.cs.itrace4.core.algo.region.UD_CodeTextAsWholeInRegion/'
find . -name '*java'|xargs sed -i 's/cn.edu.nju.cs.itrace4.demo.algo.coderegion.UD_MergeCodeTXTAndNewRepresentElement/cn.edu.nju.cs.itrace4.core.algo.region.UD_MergeCodeTXTAndNewRepresentElement/'
find . -name '*java'|xargs sed -i 's/cn.edu.nju.cs.itrace4.demo.cdgraph.inneroutter.UD_InnerAndOuterSeq/cn.edu.nju.cs.itrace4.core.algo.region.calldata.innerBonus.UD_InnerAndOuterSeq/'
find . -name '*java'|xargs sed -i 's/cn.edu.nju.cs.tool.NegativeLinkAnalyze/cn.edu.nju.cs.itrace4.tool.NegativeLinkAnalyze/'
find . -name '*java'|xargs sed -i 's/cn.edu.nju.cs.tool.ResultAnalyze/cn.edu.nju.cs.itrace4.tool.ResultAnalyze/'


find . -name '*java'|xargs sed -i 's///'












