package fr.lelouet.tools.communication.http;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * opens a given port, and wait for incoming connection ; then retrieve the next
 * request and uses the get data. Useful for discussions between JVMs
 */
@SuppressWarnings("restriction")
public abstract class AHttpReceiver {

	public final int port;
	HttpServer server;
	public final String basePath;

	public AHttpReceiver(int port, String basePath) {
		this.port = port;
		this.basePath = basePath;
		try {
			server = HttpServer.create(new InetSocketAddress(port), port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		server.createContext(basePath, mHandler);
		server.setExecutor(null);
		server.start();
	}

	public AHttpReceiver(int port) {
		this(port, "/");
	}

	public abstract String onReceivedString(String received);

	private String lastString = null;

	public void clean() {
		lastString = null;
	}

	public String getString() {
		return lastString;
	}

	public String nextStr() {
		clean();
		String ret = null;
		while ((ret = getString()) == null) {
			Thread.yield();
		}
		return ret;
	}

	protected void setString(String str) {
		lastString = str;
	}

	private HttpHandler mHandler = new HttpHandler() {

		public void handle(HttpExchange exchange) throws IOException {
			String message = exchange.getRequestURI().getQuery();
			setString(message);
			String response = onReceivedString(message);
			exchange.sendResponseHeaders(200, response.length());
			exchange.getResponseBody().write(response.getBytes());
			exchange.getResponseBody().close();
		}

	};

	public static void main(String args[]) {
		int port = Integer.parseInt(args[0]);
		final AHttpReceiver http = new AHttpReceiver(port, "/") {

			@Override
			public String onReceivedString(String received) {
				return "handled";
			}

		};
		Runnable run = new Runnable() {

			@Override
			public void run() {
				while (true) {
					System.out.println("waitin for the string");
					String str = http.nextStr();
					System.out.println(" received " + str + "; looping");
				}
			}
		};

		new Thread(run).start();
	}

}
