import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.json.JSONObject;

@WebSocket(maxTextMessageSize = 1048576, maxBinaryMessageSize = 10485760)
final public class ChatWebSocketHandler {
	private Map<Session, String> userUsernameMap = new ConcurrentHashMap<>();

	@OnWebSocketConnect
	public void onConnect(Session user) throws Exception {
		List<String> name = user.getUpgradeRequest().getParameterMap().get("name");
		if (name.get(0) != null) {
			if (userUsernameMap.containsValue(name.get(0))) {
				user.close();
			} else {
				userUsernameMap.put(user, name.get(0));
				broadcastMessage("Server", (name.get(0) + " joined the chat"));
			}
		}
	}

	@OnWebSocketClose
	public void onClose(Session user, int statusCode, String reason) {
		String username = userUsernameMap.get(user);
		if (username != null) {
			userUsernameMap.remove(user);
			broadcastMessage("Server", (username + " left the chat"));
		}
	}

	@OnWebSocketError
	public void onError(Throwable error) {
		error.printStackTrace();
	}

	@OnWebSocketMessage
	public void onMessage(Session user, String message) {
		if (userUsernameMap.containsKey(user)) {
			broadcastMessage(userUsernameMap.get(user), message);
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
	private void broadcastMessage(String sender, String message) {
		userUsernameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
			try {
				session.getRemote()
						.sendString(String.valueOf(new JSONObject().put("sender", sender).put("type", "text")
								.put("timestamp", LocalDateTime.now())
								.put("message", message).put("userlist", userUsernameMap.values())));
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

	protected String getSaltString(int length) {
		String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
		StringBuilder salt = new StringBuilder();
		Random rnd = new Random();
		while (salt.length() < length) { // length of the random string.
			int index = (int) (rnd.nextFloat() * SALTCHARS.length());
			salt.append(SALTCHARS.charAt(index));
		}
		String saltStr = salt.toString();
		return saltStr;
	}
}
