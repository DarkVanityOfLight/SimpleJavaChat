package sessionServer;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class SessionServerMain {

	static public int sessionCounter = 0;
	static public int port = 8000;
	//These two maps will work as database
	static public Map<String, String>RecieverMsg = new HashMap<String, String>();//Reciever as key and the msg as value
	static public Map<String, String>MsgSender = new HashMap<String, String>();//Msg as key and sender as value

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO get args
		// TODO maybe multi socket?

		int maxSessions = 4;// maximum session numbers
		SessionSocket server = new SessionSocket();
		ServerSocket serverSocket;

		serverSocket = server.createSocket();
		System.out.printf("Server is now ready and listening for connections on port %d\n", port);

		while (sessionCounter <= maxSessions) {
			Socket client = SessionSocket.createSession(serverSocket);
			if (client != null) {
				SessionHandler sessionHandler = new SessionHandler(client);
				sessionHandler.start();

			} else {
				client = SessionSocket.createSession(serverSocket);
			}

		}

	}

}
