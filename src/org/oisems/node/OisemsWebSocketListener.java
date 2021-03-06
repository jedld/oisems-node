package org.oisems.node;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.oisems.client.Utils;
import org.oisems.client.message.OisemsMessage;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;


public class OisemsWebSocketListener  extends WebSocketServer {
	
	Node node;
	
	public OisemsWebSocketListener(Node node, int port) throws UnknownHostException {
		super(new InetSocketAddress( port ));
		System.out.println("listening on websocket port");
		this.node = node;
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		System.out.println("Device disconnected.");
		node.removeDeviceBySocket(conn);
	}

	@Override
	public void onError(WebSocket arg0, Exception arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessage(WebSocket socket, String message) {
		System.out.println("onMessage");
		JsonParser parser = new JsonParser();
		JsonElement element = parser.parse(message);
		String device_id = element.getAsJsonObject().get("oisems_id").getAsString();
		String command = element.getAsJsonObject().get("cmd").getAsString();
		if (command.equals("SENDMESSAGE")) {
			System.out.println("SENDMESSAGE");
			//Check if it has a valid session
			DeviceInfo device = node.deviceList.get(device_id);
			String session_id = element.getAsJsonObject().get("session_id").getAsString();
			if (device.getSessionId().equals(session_id)) {
				String msgb64 = element.getAsJsonObject().get("message").getAsString();
				byte message_bytes[] = Base64.decodeBase64(msgb64);
				
				OisemsMessage msg = new OisemsMessage();
				msg.fromBytesPartial(message_bytes);
				System.out.println("Sending to " + msg.getRecipient());
				
				//Check if message is available locally
				DeviceInfo dev_info = node.getDevice(msg.getRecipient());
				if (!dev_info.getSocket().isClosed()) {
					WebSocket recipient = dev_info.getSocket();
					HashMap <String,String>response = new HashMap<String,String>();
					response.put("cmd", "RECEIVEMESSAGE");
					response.put("message", msgb64);
					recipient.send(Utils.mapToJSON(response));
				}
			}
		} else
		if (command.equals("REGISTER")) {
			System.out.println("REGISTER");
			String session_id = DigestUtils.sha256Hex(Double.toString(Math.random()));
			try {
				HashMap <String,String>response = new HashMap<String,String>();
				System.out.println("session id sent");
				response.put("cmd", "SESSION");
				response.put("session_id", Base64.encodeBase64String(encrypt(device_id, session_id)));
				response.put("domain", "public");
				response.put("node_id", node.getNodeId().toString());
				node.addDevice(device_id, session_id, socket);
				socket.send(Utils.mapToJSON(response));
			} catch (NotYetConnectedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalBlockSizeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BadPaddingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidKeySpecException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchPaddingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		};
	}

	private byte[] encrypt(String key, String message) throws IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(
				new X509EncodedKeySpec(Base64.decodeBase64(key)));
		Cipher cipher = Cipher
				.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte message_bytes[] = message.getBytes(Charset.forName("UTF-8")); 
		return OisemsMessage.blockCipher(cipher,message_bytes,Cipher.ENCRYPT_MODE);
	}
	
	@Override
	public void onOpen(WebSocket socket, ClientHandshake handshake) {
		System.out.println("Device connected");
		HashMap <String,String>response = new HashMap<String,String>();
		response.put("version", "1");
		response.put("node_id", node.getNodeId().toString());
		
		System.out.println("device_id = " + handshake.getFieldValue("device_id"));
		String responseString = Utils.mapToJSON(response);
		socket.send(responseString.getBytes(Charset.forName("UTF-8")));
	}
	
}