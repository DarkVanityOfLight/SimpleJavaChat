package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientSocket {

	private int port;
	private String address;
	private String clientName;
	private Socket clientSocket;
	public DataInputStream dIn;
	private DataOutputStream dOut;
	public boolean authenticated;

	ClientSocket() {
		this.address = Ui.getAddress();
		this.port = Ui.getPort();
		this.clientName = Ui.getUsername();
		this.clientSocket = null;
		this.dIn = null;
		this.dOut = null;
		this.authenticated = false;

	}

	/**
	 * Connect to a server using a socket
	 */
	public void connect() {
		try {
			this.clientSocket = new Socket(this.address, this.port);
			this.inOut();
		} catch (UnknownHostException e) {
			System.out
					.println("Unkown host, are u sure the server is running there?\n");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Could not connect\n");
			e.printStackTrace();
		}

	}


	/**
	 * Get the input and output streams
	 */
	private void inOut() {
		if (this.clientSocket != null) {
			try {
				this.dIn = new DataInputStream(
						this.clientSocket.getInputStream());
				this.dOut = new DataOutputStream(
						this.clientSocket.getOutputStream());

			} catch (IOException e) {
				System.out.println("Could not setup In/Out Stream\n");
				e.printStackTrace();
				System.out.println("We will try again\n");
				this.inOut();
			}
		} else {
			System.out.println("Socket was null");
			this.connect();
		}

	}

	/**
	 * Authenticate at the server
	 */
	public void auth() {
		byte dataByte;

		try {
			dataByte = this.dIn.readByte();

			switch (dataByte) {
			case 0:// Ok
					// TODO check if all is ok
				this.dOut.writeByte(0);
				this.dOut.flush();
				dataByte = this.dIn.readByte();
				switch (dataByte) {
				case 5:// Server wants name
					this.dOut.writeByte(6);// Write the send name byte
					this.dOut.writeUTF(this.clientName);// Write the name
					this.dOut.flush();// send the packet
					dataByte = this.dIn.readByte();

					switch (dataByte) {
					case 14:// auth done
						this.authenticated = true;
						break;

					case 15:// auth fail
						this.auth();
						break;

					default:
						break;
					}
				}

				break;
			// TODO create a cancel() function
			case 1:// Server aborts
				break;

			case 3:// Server exits
				break;

			default:
				break;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Send a message
	 * @param msg The message to send
	 * @param receiver The receiver name of this message
	 */
	public void sendMsg(String msg, String receiver) {
		byte dataByte;

		try {
			this.dOut.writeByte(10);// Show the server that a msg follows
			this.dOut.writeByte(11);// Show the server that the msg body follows
			this.dOut.writeUTF(msg);// Write the msg body
			this.dOut.writeByte(12);// Show the server that the receiver follows
			this.dOut.writeUTF(receiver);// Write the receiver
			this.dOut.flush();// Send the packet

			dataByte = this.dIn.readByte();
			switch (dataByte) {
			case 0:
				System.out.println("Msg got send");
				break;

			case 1:// Server aborts
				break;

			case 3:// Server exits
				break;

			default:
				break;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Msg could not be send");
			e.printStackTrace();
		}

	}

	/**
	 * Receive a message from the server
	 * @throws IOException
	 */
	public void recvMsg() throws IOException {// 7 8 9 back: 0
		byte dataByte;
		String sender;
		String msg;

		dataByte = this.dIn.readByte();// recv msg flag
		switch (dataByte) {
		case 1:
			break;
		case 3:
			break;

		case 7:// We get a msg
			dataByte = this.dIn.readByte();// recv the next byte
			switch (dataByte) {
			case 1:
				break;
			case 3:
				break;
			case 8:
				sender = this.dIn.readUTF();// We get the sender
				dataByte = this.dIn.readByte();// Get next byte
				switch (dataByte) {
				case 1:
					break;
				case 3:
					break;
				case 9:
					msg = this.dIn.readUTF();// get the msg
					this.dOut.writeByte(0);// Send ok back
					Ui.printMsg(sender, msg);// print the msg
					break;
				default:
					break;
				}

				break;

			case 9:
				msg = this.dIn.readUTF();// get the msg
				dataByte = this.dIn.readByte();// recv the next byte
				switch (dataByte) {
				case 1:
					break;
				case 3:
					break;
				case 8:
					sender = this.dIn.readUTF();// We get the sender
					this.dOut.writeByte(0);// Send ok back
					Ui.printMsg(sender, msg);// print the msg
					break;
				default:
					break;

				}
				break;
			default:
				break;

			}

			break;
		default:
			break;
		}

	}
}

// 0 Ok
// 1 Abort server side
// 2 Abort client side
// 3 Server side exits
// 4 Client side exits
// 5 Wants client name
// 6 Gets client name
// 7 Server sends text msg
// --> 8 The sender follows
// --> 9 The msg follows
// 10 Client sends text msg
// --> 11 the msg follows
// --> 12 the reciver follows
// 13 Abort msg sending

