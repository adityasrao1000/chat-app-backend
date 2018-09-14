package webchat.tests;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import org.eclipse.jetty.websocket.api.BatchMode;
import org.eclipse.jetty.websocket.api.CloseStatus;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.SuspendToken;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.api.UpgradeResponse;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.eclipse.jetty.websocket.api.extensions.ExtensionConfig;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import webchat.ChatWebSocketHandler;

class mockUpgradeRequest implements UpgradeRequest {

	@Override
	public void addExtensions(ExtensionConfig... arg0) {

	}

	@Override
	public void addExtensions(String... arg0) {

	}

	@Override
	public void clearHeaders() {

	}

	@Override
	public List<HttpCookie> getCookies() {

		return null;
	}

	@Override
	public List<ExtensionConfig> getExtensions() {

		return null;
	}

	@Override
	public String getHeader(String arg0) {

		return null;
	}

	@Override
	public int getHeaderInt(String arg0) {

		return 0;
	}

	@Override
	public Map<String, List<String>> getHeaders() {

		return null;
	}

	@Override
	public List<String> getHeaders(String arg0) {

		return null;
	}

	@Override
	public String getHost() {

		return null;
	}

	@Override
	public String getHttpVersion() {

		return null;
	}

	@Override
	public String getMethod() {

		return null;
	}

	@Override
	public String getOrigin() {

		return null;
	}

	@Override
	public Map<String, List<String>> getParameterMap() {
		Map<String, List<String>> map = new HashMap<>();
		List<String> username = new LinkedList<>();
		username.add("aditya");
		List<String> password = new LinkedList<>();
		password.add("123456");
		map.put("username", username);
		map.put("password", password);
		return map;
	}

	@Override
	public String getProtocolVersion() {

		return null;
	}

	@Override
	public String getQueryString() {

		return null;
	}

	@Override
	public URI getRequestURI() {

		return null;
	}

	@Override
	public Object getSession() {

		return null;
	}

	@Override
	public List<String> getSubProtocols() {

		return null;
	}

	@Override
	public Principal getUserPrincipal() {

		return null;
	}

	@Override
	public boolean hasSubProtocol(String arg0) {

		return false;
	}

	@Override
	public boolean isOrigin(String arg0) {

		return false;
	}

	@Override
	public boolean isSecure() {

		return false;
	}

	@Override
	public void setCookies(List<HttpCookie> arg0) {

	}

	@Override
	public void setExtensions(List<ExtensionConfig> arg0) {

	}

	@Override
	public void setHeader(String arg0, List<String> arg1) {

	}

	@Override
	public void setHeader(String arg0, String arg1) {

	}

	@Override
	public void setHeaders(Map<String, List<String>> arg0) {

	}

	@Override
	public void setHttpVersion(String arg0) {

	}

	@Override
	public void setMethod(String arg0) {

	}

	@Override
	public void setRequestURI(URI arg0) {

	}

	@Override
	public void setSession(Object arg0) {

	}

	@Override
	public void setSubProtocols(List<String> arg0) {

	}

	@Override
	public void setSubProtocols(String... arg0) {

	}

}

class mockRemoteEndpoint implements RemoteEndpoint {

	@Override
	public void flush() throws IOException {

	}

	@Override
	public BatchMode getBatchMode() {
		return null;
	}

	@Override
	public InetSocketAddress getInetSocketAddress() {
		return null;
	}

	@Override
	public void sendBytes(ByteBuffer arg0) throws IOException {

	}

	@Override
	public void sendBytes(ByteBuffer arg0, WriteCallback arg1) {

	}

	@Override
	public Future<Void> sendBytesByFuture(ByteBuffer arg0) {
		return null;
	}

	@Override
	public void sendPartialBytes(ByteBuffer arg0, boolean arg1) throws IOException {

	}

	@Override
	public void sendPartialString(String arg0, boolean arg1) throws IOException {

	}

	@Override
	public void sendPing(ByteBuffer arg0) throws IOException {

	}

	@Override
	public void sendPong(ByteBuffer arg0) throws IOException {

	}

	@Override
	public void sendString(String arg0) throws IOException {

	}

	@Override
	public void sendString(String arg0, WriteCallback arg1) {

	}

	@Override
	public Future<Void> sendStringByFuture(String arg0) {
		return null;
	}

	@Override
	public void setBatchMode(BatchMode arg0) {

	}

}

class mockSession implements Session {

	@Override
	public void close() {

	}

	@Override
	public void close(CloseStatus arg0) {

	}

	@Override
	public void close(int arg0, String arg1) {

	}

	@Override
	public void disconnect() throws IOException {

	}

	@Override
	public long getIdleTimeout() {
		return 0;
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		return new InetSocketAddress(0);
	}

	@Override
	public WebSocketPolicy getPolicy() {
		return null;
	}

	@Override
	public String getProtocolVersion() {
		return null;
	}

	@Override
	public RemoteEndpoint getRemote() {
		return null;
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return null;
	}

	@Override
	public UpgradeRequest getUpgradeRequest() {
		return new mockUpgradeRequest();
	}

	@Override
	public UpgradeResponse getUpgradeResponse() {
		return null;
	}

	@Override
	public boolean isOpen() {
		return false;
	}

	@Override
	public boolean isSecure() {
		return false;
	}

	@Override
	public void setIdleTimeout(long arg0) {

	}

	@Override
	public SuspendToken suspend() {
		return null;
	}

}

public class ChatWebSocketHandlerTest {

	@Mock
	Session s = null;
	RemoteEndpoint r = null;
	ChatWebSocketHandler handler = null;

	@Before
	public void initialize() {
		this.s = new mockSession();
		this.r = new mockRemoteEndpoint();
		this.handler = new ChatWebSocketHandler();
	}

	@Test
	public void onConnect() {
		handler.onConnect(s);
	}

	@Test
	public void sendStringMessage() {
		handler.onMessage(s, "Hi");
	}

	/**
	 * first connect to the chat so the session is added to the userUsernameMap map.
	 * Then createBinary() method should be invoked from the onStreamMessage()
	 * method.
	 */
	@Test
	public void sendStreamMessage() {
		handler.onConnect(s);
		handler.onStreamMessage(s, new byte[0], 0, 0);
	}
}
