package webchat;

import static spark.Spark.webSocket;

import java.util.ArrayList;

import static spark.Spark.init;

/**
 * 
 * @author Aditya
 * @since 24/8/18
 */
final public class Chat {

	public static void main(String[] args) {
		// served at localhost:4567
		ArrayList<String> chatrooms = new ArrayList<String>();
		chatrooms.add("chat");
		for (int i = 0; i < 200; i++) {
			chatrooms.add("chat" + i);
		}
		for (String s : chatrooms) {
			webSocket("/" + s, ChatWebSocketHandler.class);
		}
		init();
	}
}
