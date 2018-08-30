package webchat;

import com.google.gson.Gson;

public final class Singleton {
	static private Gson gson = null;

	private Singleton() {

	}

	public static Gson gson() {
		if (gson == null) {
			gson = new Gson();
		}
		return gson;
	}
}
