package webchat;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.json.JSONObject;
import com.google.gson.Gson;

@WebSocket(maxTextMessageSize = 15728640, maxBinaryMessageSize = 15728640)
final public class ChatWebSocketHandler {
	// this map is shared between sessions and threads, so it needs to be
	// thread-safe (http://stackoverflow.com/a/2688817)
	private Map<Session, String> userUsernameMap = new ConcurrentHashMap<>();

	@OnWebSocketConnect
	public void onConnect(Session user) throws Exception {
		String name = user.getUpgradeRequest().getParameterMap().get("name").get(0);
		if (name != null) {
			if (userUsernameMap.containsValue(name)) {
				user.close();
			} else {
				user.setIdleTimeout(0);
				userUsernameMap.put(user, name);
				broadcastMessage("Server", (name + " joined the chat"), "text");
			}
		}
	}

	@OnWebSocketClose
	public void onClose(Session user, int statusCode, String reason) {
		String username = userUsernameMap.get(user);
		if (username != null) {
			userUsernameMap.remove(user);
			broadcastMessage("Server", (username + " left the chat"), "text");
		}
	}

	@OnWebSocketError
	public void onError(Throwable error) {
		error.printStackTrace();
	}

	@OnWebSocketMessage
	public void onMessage(Session user, String message) {
		Gson gson = Singleton.gson();
		if (userUsernameMap.containsKey(user)) {
			try {
				BinaryMessage type = gson.fromJson(message, BinaryMessage.class);
				System.out.println(type.getType());
				broadcastMessage(userUsernameMap.get(user), type.getFile(), type.getType());
			} catch (Exception e) {
				broadcastMessage(userUsernameMap.get(user), message, "text");
			}
		}
	}

	@OnWebSocketMessage
	public void methodName(Session user, byte[] buf, int offset, int length) {
		if (userUsernameMap.containsKey(user)) {
			broadcastBytes(userUsernameMap.get(user), buf);
		}
	}

	// Sends a message from one user to all users, along with a list of current
	// user names
	private void broadcastMessage(String sender, String message, String type) {
		userUsernameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
			try {
				session.getRemote()
						.sendString(String.valueOf(new JSONObject().put("sender", sender).put("type", type)
								.put("timestamp", LocalDateTime.now()).put("message", message)
								.put("userlist", userUsernameMap.values())));
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private void broadcastBytes(String sender, byte[] b) {
		userUsernameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {

			try {
				String s = Base64.getEncoder().encodeToString(b);
				session.getRemote()
						.sendString(String.valueOf(new JSONObject().put("sender", sender).put("type", "image")
								.put("timestamp", LocalDateTime.now()).put("message", s)
								.put("userlist", userUsernameMap.values())));
			} catch (IOException e) {
				// Send failed
				e.printStackTrace();
			}
		});
	}
}
