package webchat;

import static spark.Spark.webSocket;
import static spark.Spark.init;

/**
 * 
 * @author Aditya
 * @since 24/8/18
 */
final public class Chat {

	public static void main(String[] args) {
		// served at localhost:4567
		webSocket("/chat", ChatWebSocketHandler.class);
		webSocket("/chat1", ChatWebSocketHandler.class);
		init();
	}
}
