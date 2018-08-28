import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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
		broadcastMessage(userUsernameMap.get(user), message);
	}

	@OnWebSocketMessage
	public void methodName(Session user, byte[] buf, int offset, int length) {
		broadcastBytes(userUsernameMap.get(user), buf);
	}

	// Sends a message from one user to all users, along with a list of current
	// user names
	private void broadcastMessage(String sender, String message) {
		userUsernameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
			try {
				session.getRemote()
						.sendString(String.valueOf(new JSONObject().put("sender", sender).put("type", "text")
								.put("timestamp", new SimpleDateFormat("HH:mm:ss").format(new Date()))
								.put("message", message).put("userlist", userUsernameMap.values())));
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private void broadcastBytes(String sender, byte[] b) {
		userUsernameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
			ByteBuffer buf = ByteBuffer.wrap(b);
			try {
				Future<Void> fut = session.getRemote().sendBytesByFuture(buf);
				// wait for completion (forever)
				fut.get();
			} catch (ExecutionException | InterruptedException e) {
				// Send failed
				e.printStackTrace();
			}
		});
	}
}
