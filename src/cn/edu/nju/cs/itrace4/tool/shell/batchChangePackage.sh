#!/bin/sh
for f in ../*.java; do 
	echo $f | xargs sed -i 's/package cn.edu.nju.cs.tool;/package cn.edu.nju.cs.itrace4.tool;/g'
done
