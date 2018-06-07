/**
 * @author zzf
 * @date 2018.6.7
 * @description call graph and data graph as a whole rather than call then data.
 * 	several method to generate code dependency graph, eg. call merge data directly, call then
 * 		extend with data. And some attempts about link with high similarity, like regard the
 * 		IR link with highest similarity as valid. and so on....
 */
package cn.edu.nju.cs.itrace4.core.algo.region.calldata;