package webchat;

import java.util.Map;

import org.eclipse.jetty.websocket.api.Session;

/**
 * 
 * @author Aditya
 *
 */
final public class PendingMessages {
	protected String sender;
	protected Map<Session, String> session;
	protected String message;
	protected String type;

	PendingMessages(String sender, Map<Session, String>  session, String message, String type) {
		this.sender = sender;
		this.session = session;
		this.message = message;
		this.type = type;
	}
}
