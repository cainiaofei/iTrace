package cn.edu.nju.cs.itrace4.tool;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

public class PrintPageRankResult {
	class Node{
		private String name;
		private double weight;
		public Node(String name,double weight){
			this.name = name;
			this.weight = weight;
		}
	}
	public void doTask(Map<String,Number> map){
		Node[] nodes = new Node[map.size()];
		Iterator<String> ite = map.keySet().iterator();
		int index = 0;
		while(ite.hasNext()){
			String name = ite.next();
			nodes[index] = new Node(name,map.get(name).doubleValue());
			index++;
		}
		Arrays.sort(nodes,new Comparator<Node>(){
			@Override
			public int compare(Node o1, Node o2) {
				double dist = o2.weight - o1.weight;
				if(dist>0){
					return 1;
				}
				else if(dist<0){
					return -1;
				}
				else{
					return 0;
				}
			}
		});
		for(int i = 0; i < nodes.length;i++){
			System.out.println(nodes[i].name+" "+nodes[i].weight);
		}
	}
	
	public static void main(String[] args){
		PrintPageRankResult printResult = new PrintPageRankResult();
		
	}
}
