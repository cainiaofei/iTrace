#!/bin/sh
pwd=~/workspace/iTrace4/src
find . -name '*java'|xargs sed -i 's/cn.edu.nju.cs.itrace4.core.algo.None_CSTI/cn.edu.nju.cs.itrace4.core.algo.prealgo.None_CSTI/'
find . -name '*java'|xargs sed -i 's/cn.edu.nju.cs.itrace4.core.algo.O_CSTI/cn.edu.nju.cs.itrace4.core.algo.prealgo.O_CSTI/'
find . -name '*java'|xargs sed -i 's/cn.edu.nju.cs.itrace4.core.algo.PageRank_CSTI/cn.edu.nju.cs.itrace4.core.algo.prealgo.PageRank_CSTI/'
find . -name '*java'|xargs sed -i 's/cn.edu.nju.cs.itrace4.core.algo.UseEdge/cn.edu.nju.cs.itrace4.core.algo.prealgo.UseEdge/'

