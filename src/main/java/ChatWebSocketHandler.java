import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.json.JSONObject;

@WebSocket
final public class ChatWebSocketHandler {
	private Map<Session, String> userUsernameMap = new ConcurrentHashMap<>();
	private int nextUserNumber = 1; // Assign to user name for next connecting user
    private static ClientUpgradeRequest request = new ClientUpgradeRequest();
    
	@OnWebSocketConnect
	public void onConnect(Session user) throws Exception {
		request.getHeader("username");
		String username = "User" + nextUserNumber++;
		userUsernameMap.put(user, username);
		broadcastMessage("Server", (username + " joined the chat"));
	}

	@OnWebSocketClose
	public void onClose(Session user, int statusCode, String reason) {
		String username = userUsernameMap.get(user);
		userUsernameMap.remove(user);
		broadcastMessage("Server", (username + " left the chat"));
	}

	@OnWebSocketMessage
	public void onMessage(Session user, String message) {
		broadcastMessage(userUsernameMap.get(user), message);
	}

	// Sends a message from one user to all users, along with a list of current
	// user names
	private void broadcastMessage(String sender, String message) {
		userUsernameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
			try {
				session.getRemote()
						.sendString(String.valueOf(new JSONObject().put("sender", sender)
								.put("timestamp", new SimpleDateFormat("HH:mm:ss").format(new Date()))
								.put("message", message).put("userlist", userUsernameMap.values())));
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
}
