package cn.edu.nju.cs.itrace4.core.algo.region.relation;

/**
 * @author zzf <tiaozhanzhe>
 * @date 2017.11.16
 * @description remove data/call edge which less than threshold, then data region appear naturally,
 * alike call region, we use dfs to get data region.
 * its parent class <code>StoreSubGraphInfoByThreshold</code> has get region after prune, so here 
 * we do nothing and use its parent method <code></code> directly.
 */
public class StoreDataSubGraphRemoveEdge extends StoreSubGraphInfoByThreshold{
}
