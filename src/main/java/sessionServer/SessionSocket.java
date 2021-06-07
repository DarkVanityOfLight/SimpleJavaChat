package sessionServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SessionSocket {

	public int port;// Create the port var
	public ServerSocket serverSocket;

	SessionSocket() {
		this.port = SessionServerMain.port;// assign the port var to the current
											// port in main
		this.serverSocket = null;

	}

	public ServerSocket createSocket() {
		this.serverSocket = null;

		try {

			// Assign the socket to port
			this.serverSocket = new ServerSocket(this.port);

			return this.serverSocket;
		} catch (IOException e) {
			System.out.println("Socket could not be created: ");
			System.out.println(e);
			serverSocket = null;
		}
		return this.serverSocket;

	}

	public static Socket createSession(ServerSocket serverSocket) {

		// Accept incoming connections and return the sessionSocket(clients)
		Socket client = null;
		try {
			client = serverSocket.accept();
		} catch (IOException e) {
			System.out.println("Could not accept client: ");
			System.out.println(e);
		}

		return client;

	}

}
