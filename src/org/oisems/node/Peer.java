package org.oisems.node;

import java.math.BigInteger;

public class Peer {

	BigInteger nodeId;
	String address;
	int port;
	
	public BigInteger getNodeId() {
		return nodeId;
	}
	public void setNodeId(BigInteger nodeId) {
		this.nodeId = nodeId;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
}
