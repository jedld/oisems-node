package org.oisems.node;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Random;

import org.java_websocket.WebSocket;
import org.oisems.client.Client;
import org.oisems.client.OisemsClientDevice;

class DeviceInfo {
	
	String sessionId;
	String oisemsId;
	long lastHeartBeat;
	WebSocket socket;
	
	public long getLastHeartBeat() {
		return lastHeartBeat;
	}
	public void setLastHeartBeat(long lastHeartBeat) {
		this.lastHeartBeat = lastHeartBeat;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public String getOisemsId() {
		return oisemsId;
	}
	public void setOisemsId(String oisemsId) {
		this.oisemsId = oisemsId;
	}
	public WebSocket getSocket() {
		return socket;
	}

	public void setSocket(WebSocket socket) {
		this.socket = socket;
	}
	
	public long hash() {
		  long h = 1125899906842597L; // prime
		  int len = oisemsId.length();

		  for (int i = 0; i < len; i++) {
		    h = 31*h + oisemsId.charAt(i);
		  }
		  return h;
		}
}

public class Node {
	long nodeId;
	
	HashMap <String, DeviceInfo> deviceList = new HashMap<String, DeviceInfo>();
	
	public Node() {
		Random random = new Random();
		nodeId = random.nextLong();
	}

	public long getNodeId() {
		return nodeId;
	}

	public void setNodeId(long nodeId) {
		this.nodeId = nodeId;
	}
	
	public void addDevice(String oisems_id, String session_id, WebSocket socket) {
		DeviceInfo info = new DeviceInfo();
		info.setOisemsId(oisems_id);
		info.setSessionId(session_id);
		info.setLastHeartBeat(System.currentTimeMillis());
		info.setSocket(socket);
		System.out.println("Device " + info.hash() + " added. with session_id " + session_id);
		deviceList.put(oisems_id, info);
	}
	
	public void start() {
		try {
			OisemsWebSocketListener listener = new OisemsWebSocketListener(this, 44444);
			listener.start();
			System.out.println("waiting for connections");
			while(true) {
				try {
					Thread.sleep(1000, 0);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
