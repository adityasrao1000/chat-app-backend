package webchat;

public class BinaryMessage {
	private String type;
	private String file;

	protected void setType(String type) {
		this.type = type;
	}

	protected String getType() {
		return type;
	}

	protected void setFile(String file) {
		this.file = file;
	}

	protected String getFile() {
		return file;
	}
}
