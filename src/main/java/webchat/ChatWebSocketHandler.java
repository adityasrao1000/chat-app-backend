package webchat;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.tika.Tika;
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * @author Aditya
 * @since 24/8/18
 */
@WebSocket(maxTextMessageSize = 1000000, maxBinaryMessageSize = 15728640)
final public class ChatWebSocketHandler {

	// this map is shared between sessions and threads
	private final Map<Session, String> userUsernameMap = new ConcurrentHashMap<>();
	private final Map<Session, String> emptyMap = new ConcurrentHashMap<>();
	private final Tika tika = new Tika();

	private final ApplicationContext context = new ClassPathXmlApplicationContext("spring-jdbc.xml");
	private final ChatUserJDBCTemplate imageJDBCTemplate = (ChatUserJDBCTemplate) context.getBean("ImageJDBCTemplate");

	/**
	 * 
	 * @param user
	 */
	@OnWebSocketConnect
	public void onConnect(Session user) {

		String username = user.getUpgradeRequest().getParameterMap().get("username").get(0);
		String password = user.getUpgradeRequest().getParameterMap().get("password").get(0);
		List<UserDetails> result = imageJDBCTemplate.isAuthenticated(username, password);

		String name = null;
		if (result.size() > 0) {
			name = result.get(0).getUsername();
		}

		if (name != null) {
			if (!userUsernameMap.containsValue(name)) {
				user.setIdleTimeout(0);
				userUsernameMap.put(user, name);
				broadcastMessage("Server", userUsernameMap, (name + " joined the chat"), "text", userUsernameMap);
			}
		} else {
			final Map<Session, String> userMap = new ConcurrentHashMap<>();
			userMap.put(user, "");
			broadcastMessage("Server", userMap, "You are not allowed to join this chat", "text", emptyMap);
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
			broadcastMessage("Server", userUsernameMap, (username + " left the chat"), "text", userUsernameMap);
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
		if (userUsernameMap.containsKey(user)) {
			broadcastMessage(userUsernameMap.get(user), userUsernameMap, message, "text", userUsernameMap);
		} else {
			user.close();
		}
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
		if (userUsernameMap.containsKey(user)) {
			createBinary(content, user);
		} else {
			user.close();
		}
	}

	/**
	 * Sends a message from one user to all users, along with a list of current
	 * 
	 * @param sender
	 * @param message
	 * @param type
	 */
	private void broadcastMessage(String sender, Map<Session, String> sessions, String message, String type,
			Map<Session, String> userlist) {

		String content = String.valueOf(new JSONObject().put("sender", sender).put("type", type)
				.put("timestamp", LocalDateTime.now()).put("message", message).put("userlist", userlist.values()));
		sessions.keySet().stream().filter(Session::isOpen).forEach(session -> {
			session.getRemote().sendStringByFuture(content);
		});
	}

	/**
	 * 
	 * @param content
	 * @param user
	 */
	protected void createBinary(byte[] content, Session user) {
		Optional<String> mimeType = Optional.ofNullable(tika.detect(content));
		
		if (mimeType.isPresent()) {
			String encodedBase64Image = Base64.getEncoder().encodeToString(content);
			broadcastMessage(userUsernameMap.get(user), userUsernameMap, encodedBase64Image, mimeType.get(),
					userUsernameMap);
		} else {
			Map<Session, String> map = new ConcurrentHashMap<Session, String>();
			map.put(user, user.getUpgradeRequest().getParameterMap().get("name").get(0));
			broadcastMessage("Server", map, "file type is not supported", "text", userUsernameMap);
		}
	}
}
