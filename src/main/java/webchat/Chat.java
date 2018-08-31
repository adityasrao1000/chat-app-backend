package webchat;

/**
 * 
 * @author Aditya
 * @since 24/8/18
 */
import static spark.Spark.webSocket;
import static spark.Spark.init;

final public class Chat {

	public static void main(String[] args) {
		// served at local host:4567
		webSocket("/chat", ChatWebSocketHandler.class);
		webSocket("/chat1", ChatWebSocketHandler.class);
		init();
	}
}
