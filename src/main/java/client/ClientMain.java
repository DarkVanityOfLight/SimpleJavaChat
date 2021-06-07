package client;

import java.io.IOException;

public class ClientMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Boolean running = true;
		Ui ui = new TerminalUI();
		ClientSocket socket = new ClientSocket(ui);
		socket.connect();
		while (!socket.authenticated) {
			socket.auth();
		}
		System.out.println("Authenticated");
		socket.sendMsg("Hi, I'm a client", "client1");
		while (running) {
			try {
				if (socket.dIn.available() > 0) {
					socket.recvMsg();
				}
			} catch (IOException e) {

				System.out.println("IO error try restarting the script\n");
				System.out.println(e);
			}
		}

	}

}
