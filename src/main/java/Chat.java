import static spark.Spark.*;

public class Chat {

	// this map is shared between sessions and threads, so it needs to be
	// thread-safe (http://stackoverflow.com/a/2688817)
	

	public static void main(String[] args) {
		// staticFiles.location("/public"); // index.html is served at localhost:4567
		// (default port)
		// staticFiles.expireTime(600);
		webSocket("/chat", ChatWebSocketHandler.class);
		webSocket("/chat1", ChatWebSocketHandler.class);
		init();
	}
}
