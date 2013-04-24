package org.oisems.node;

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
	public void onClose(WebSocket arg0, int arg1, String arg2, boolean arg3) {
		// TODO Auto-generated method stub
		
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
		if (command.equals("REGISTER")) {
			System.out.println("REGISTER");
			String session_id = DigestUtils.sha256Hex(Double.toString(Math.random()));
			try {
				HashMap <String,String>response = new HashMap<String,String>();
				System.out.println("session id sent");
				response.put("session_id", Base64.encodeBase64String(encrypt(device_id, session_id)));
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
		ByteBuffer buffer = ByteBuffer.allocate(128);
		PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(
				new X509EncodedKeySpec(Base64.decodeBase64(key)));
		Cipher cipher = Cipher
				.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte message_bytes[] = message.getBytes(Charset.forName("UTF-8")); 
		buffer.putInt(message_bytes.length);
		buffer.put(message_bytes);
		
		byte[] result =  OisemsMessage.blockCipher(cipher,buffer.array(),Cipher.ENCRYPT_MODE);
		return result;
	}
	
	@Override
	public void onOpen(WebSocket socket, ClientHandshake handshake) {
		System.out.println("Device connected");
		HashMap <String,String>response = new HashMap<String,String>();
		response.put("version", "1");
		response.put("node_id", Long.toString(node.getNodeId()));
		
		System.out.println("device_id = " + handshake.getFieldValue("device_id"));
		String responseString = Utils.mapToJSON(response);
		socket.send(responseString.getBytes(Charset.forName("UTF-8")));
	}
	
}