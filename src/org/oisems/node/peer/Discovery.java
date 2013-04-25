package org.oisems.node.peer;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.HashMap;

import org.oisems.client.Utils;
import org.oisems.node.Node;
import org.oisems.node.Peer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public class Discovery extends Thread {

	Node node;
	int port;
	
	public Discovery(Node node, int port) {
		this.node = node;
		this.port = port;
	}
	
	public void announce() {
		try {
			
			
			String ip_address = InetAddress.getLocalHost().getHostAddress();
			System.out.println("Announcing prescence (" + ip_address + ") via UDP");
			MulticastSocket mSocket;
			
			MulticastSocket socket = new MulticastSocket(port);
			InetAddress group = InetAddress.getByName("239.1.2.0");
			socket.joinGroup(group);

			HashMap <String, String>response = new HashMap<String, String>();
			response.put("node_id", node.getNodeId().toString());
			response.put("address", ip_address);
			response.put("port", Integer.toString(port));
			String json_string = Utils.mapToJSON(response);
			
			byte[] sbuf = json_string.getBytes();
			DatagramPacket sp = new DatagramPacket(sbuf, sbuf.length, group, port);
			socket.send(sp);

			InetSocketAddress sa = new InetSocketAddress("239.1.2.0", port);
			mSocket = new MulticastSocket(sa);
			mSocket.setTimeToLive(255);
			mSocket.joinGroup(sa.getAddress());
			DatagramPacket packet;
			while(true) {
			    byte[] buf = new byte[512];
			    packet = new DatagramPacket(buf, buf.length);
			    mSocket.receive(packet);
			    String received = new String(packet.getData());
			    System.out.println("received = [" + received + "]");
			    JsonParser parser = new JsonParser();
			    JsonReader reader = new JsonReader(new StringReader(received));
			    JsonObject peerData = parser.parse(reader).getAsJsonObject();
			    BigInteger node_id = peerData.get("node_id").getAsBigInteger();
			    String address = peerData.get("address").getAsString();
			    int port = peerData.get("port").getAsInt();
			    Peer p = node.getPeer(node_id);
			    if (p == null) {
			    	node.addPeer(node_id, address, port);
			    }
			    
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		announce();
	}

}
