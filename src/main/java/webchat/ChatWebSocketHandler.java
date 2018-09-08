package webchat;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.tika.Tika;
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.json.JSONObject;

/**
 * 
 * @author Aditya
 * @since 24/8/18
 */
@WebSocket(maxTextMessageSize = 1728640, maxBinaryMessageSize = 15728640)
final public class ChatWebSocketHandler {

	// this map is shared between sessions and threads
	private final Map<Session, String> userUsernameMap = new ConcurrentHashMap<>();
	ConcurrentLinkedDeque<PendingMessages> queue = new ConcurrentLinkedDeque<PendingMessages>();
	private final Executor executor = Executors.newCachedThreadPool();
	final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	// Initialize queue scheduler thread
	{
		scheduler.schedule(new Runnable() {

			@Override
			public void run() {
				while (true) {
					if (!queue.isEmpty()) {
						PendingMessages m = queue.remove();
						broadcastMessage(m.sender, m.session, m.message, m.type);
					}
				}
			}
		}, 100, TimeUnit.SECONDS);
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
				broadcastMessage("Server", userUsernameMap, (name + " joined the chat"), "text");
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
			broadcastMessage("Server", userUsernameMap, (username + " left the chat"), "text");
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
		broadcastMessage(userUsernameMap.get(user), userUsernameMap, message, "text");
	}

	/**
	 * 
	 * @param user
	 * @param content
	 * @param offset
	 * @param length
	 */
	@OnWebSocketMessage
	public void onStreamMessage(Session user, byte content[], int offset, int length) {
		createBinary(content, user);
	}

	/**
	 * Sends a message from one user to all users, along with a list of current
	 * 
	 * @param sender
	 * @param message
	 * @param type
	 */
	private void broadcastMessage(String sender, Map<Session, String> sessions, String message, String type) {

		String content = String.valueOf(new JSONObject().put("sender", sender).put("type", type)
				.put("timestamp", LocalDateTime.now()).put("message", message).put("userlist", sessions.values()));
		userUsernameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
			executor.execute(() -> {
				try {
					session.getRemote().sendStringByFuture(content);
				} catch (Exception e) {
					e.printStackTrace();
					Map<Session, String> m = new ConcurrentHashMap<Session, String>();
					m.put(session, session.getUpgradeRequest().getParameterMap().get("name").get(0));
					queue.offer(new PendingMessages(sender, m, message, type));
				}
			});
		});
	}

	

	/**
	 * 
	 * @param content
	 * @param user
	 */
	private void createBinary(byte[] content, Session user) {
		Tika tika = new Tika();
		Optional<String> mimeType = Optional.ofNullable(tika.detect(content));

		if (mimeType.isPresent()) {

			String encodedBase64Image = Base64.getEncoder().encodeToString(content);
			broadcastMessage(userUsernameMap.get(user), userUsernameMap, encodedBase64Image, mimeType.get());

		} else {
			Map<Session, String> m = new ConcurrentHashMap<Session, String>();
			m.put(user, user.getUpgradeRequest().getParameterMap().get("name").get(0));
			queue.offer(new PendingMessages("Server", m, "file type is not supported", "text"));
		}
	}
}
