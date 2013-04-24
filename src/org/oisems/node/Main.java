package org.oisems.node;

public class Main {

	public static void main(String args[]) {
		System.out.println("oisems node v.1.0 ");
		Node node = new Node();
		System.out.println("node ID " + node.getNodeId());
		node.start();
	}
	
}
