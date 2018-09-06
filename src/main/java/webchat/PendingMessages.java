package webchat;

import org.eclipse.jetty.websocket.api.Session;

/**
 * 
 * @author Aditya
 *
 */
final public class PendingMessages {
	protected String sender;
	protected Session session;
	protected String message;
	protected String type;

	PendingMessages(String sender, Session session, String message, String type) {
		this.sender = sender;
		this.session = session;
		this.message = message;
		this.type = type;
	}
}
