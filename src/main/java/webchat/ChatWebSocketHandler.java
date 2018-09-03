package webchat;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.json.JSONObject;
import com.google.gson.Gson;

/**
 * 
 * @author Aditya
 * @since 24/8/18
 */
@WebSocket(maxTextMessageSize = 15728640, maxBinaryMessageSize = 15728640)
final public class ChatWebSocketHandler {
	// this map is shared between sessions and threads, so it needs to be
	// thread-safe
	private final Map<Session, String> userUsernameMap = new ConcurrentHashMap<>();
	private Map<Session, String> sessions = null;
	Queue<PendingMessages> queue = new LinkedList<PendingMessages>();
	private final Executor executor = Executors.newCachedThreadPool();
	boolean running = false;
	QueueScheduler scheduler = new QueueScheduler();

	// Initialize queue scheduler thread
	{
		if (!running) {
			new Thread(scheduler).start();
			running = true;
		}
	}

	/**
	 * 
	 * @param user
	 */
	@OnWebSocketConnect
	public void onConnect(Session user) {

		String name = user.getUpgradeRequest().getParameterMap().get("name").get(0);
		if (name != null) {
			if (userUsernameMap.containsValue(name)) {
				user.close();
			} else {
				user.setIdleTimeout(0);
				userUsernameMap.put(user, name);
				sessions = new HashMap<Session, String>(userUsernameMap);
				broadcastMessage("Server", (name + " joined the chat"), "text");
			}
		}
	}

	/**
	 * If a user closes his connection to the socket the user is removed from the
	 * session map
	 * 
	 * @param user
	 * @param statusCode
	 * @param reason
	 */
	@OnWebSocketClose
	public void onClose(Session user, int statusCode, String reason) {
		String username = userUsernameMap.get(user);
		if (username != null) {
			userUsernameMap.remove(user);
			sessions = new HashMap<Session, String>(userUsernameMap);
			broadcastMessage("Server", (username + " left the chat"), "text");
		}
	}

	/**
	 * prints the stack trace in case of error
	 * 
	 * @param error
	 */
	@OnWebSocketError
	public void onError(Throwable error) {
		error.printStackTrace();
	}

	/**
	 * 
	 * @param user
	 * @param message
	 */
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

	/**
	 * Sends a message from one user to all users, along with a list of current
	 * 
	 * @param sender
	 * @param message
	 * @param type
	 */
	private void broadcastMessage(String sender, String message, String type) {

		userUsernameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
			executor.execute(() -> {
				try {
					session.getRemote()
							.sendString(String.valueOf(new JSONObject().put("sender", sender).put("type", type)
									.put("timestamp", LocalDateTime.now()).put("message", message)
									.put("userlist", sessions.values())));
				} catch (Exception e) {
					e.printStackTrace();
					queue.offer(new PendingMessages(sender, session, message, type));

				}
			});
		});
	}

	/**
	 * 
	 * @author Aditya
	 *
	 */
	private class QueueScheduler implements Runnable {
		public void run() {
			while (true) {
				if (!queue.isEmpty()) {
					PendingMessages m = queue.remove();
					broadcastMessage(m.sender, m.session, m.message, m.type);
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 
	 * @param sender
	 * @param session
	 * @param message
	 * @param type
	 */
	public void broadcastMessage(String sender, Session session, String message, String type) {
		if (session.isOpen()) {
			executor.execute(() -> {
				try {
					session.getRemote()
							.sendString(String.valueOf(new JSONObject().put("sender", sender).put("type", type)
									.put("timestamp", LocalDateTime.now()).put("message", message)
									.put("userlist", sessions.values())));
				} catch (Exception e) {
					e.printStackTrace();
					queue.offer(new PendingMessages(sender, session, message, type));

				}
			});
		}
	}
}
