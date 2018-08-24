import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONObject;
import static spark.Spark.*;

public class Chat {

	// this map is shared between sessions and threads, so it needs to be
	// thread-safe (http://stackoverflow.com/a/2688817)
	static Map<Session, String> userUsernameMap = new ConcurrentHashMap<>();
	static int nextUserNumber = 1; // Assign to user name for next connecting user

	public static void main(String[] args) {
		// staticFiles.location("/public"); // index.html is served at localhost:4567
		// (default port)
		// staticFiles.expireTime(600);
		webSocket("/chat", ChatWebSocketHandler.class);
		init();
	}

	// Sends a message from one user to all users, along with a list of current
	// user names
	public static void broadcastMessage(String sender, String message) {
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
