package webchat;

import static spark.Spark.*;

public class Chat {

	public static void main(String[] args) {
		// served at localhost:4567
		webSocket("/chat", ChatWebSocketHandler.class);
		webSocket("/chat1", ChatWebSocketHandler.class);
		init();
	}
}
