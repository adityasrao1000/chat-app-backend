package webchat;

import com.google.gson.Gson;

/**
 * 
 * @author Aditya
 * @since 24/8/18
 */
public final class Singleton {
	static private Gson gson = null;

	private Singleton() {

	}

	/**
	 * returns a single instance of the gson object, if the instance does not exist
	 * it first creates one
	 * 
	 * @return gson object;
	 */
	public static Gson gson() {
		if (gson == null) {
			gson = new Gson();
		}
		return gson;
	}
}
