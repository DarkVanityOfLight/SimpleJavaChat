package sessionServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map.Entry;

@SuppressWarnings("unused")
public class SessionHandler extends Thread {

	private final Socket client;
	private DataInputStream dIn;
	private DataOutputStream dOut;
	private String clientName;
	private boolean sessionIsRunning;
	private Boolean authenticated = false;

	SessionHandler(Socket client) {
		super();// Init the Thread class

		this.client = client;// Get the client obj and assign it to this
								// sessionHandler
		this.dIn = null;
		this.dOut = null;

		this.clientName = null;
		this.sessionIsRunning = true;
		this.authenticated = false;
	}

	// ___________________________________________________SetUp___________________________________________________________
	// Set the in and output streams
	// So that the msg can be received and sent
	private void inOut() throws IOException {

		this.dIn = new DataInputStream(this.client.getInputStream());
		this.dOut = new DataOutputStream(this.client.getOutputStream());

	}

	private void auth() throws IOException {
		byte dataByte;
		String dataString;

		// Get and set the name
		// TODO check if server side is really ok
		this.dOut.writeByte(0);// All is ok so send 0, look at bottom
		this.dOut.flush();// Send the byte
		dataByte = this.dIn.readByte();// Read the answer
		// If we got anything unexpected exit
		if (dataByte == 0) {// Client sends Ok answer with a get name byte
			this.dOut.writeByte(5);// Get the client name
			this.dOut.flush();// Send the byte

			dataByte = this.dIn.readByte();
			if (dataByte == 6) {// Next the name follows in UTF
				dataString = this.dIn.readUTF();// dataString is now the user name
				this.clientName = dataString;// Set the session clientName on the one we got

				this.authenticated = true;// user is now authenticated
				this.dOut.writeByte(14);
				this.dOut.flush();
				// TODO authenticate the user
			} else {
				cancel();
			}
			// Client says abort
			// TODO maybe get some logs
		// Client wants to exit
		} else {
			cancel();
		}

	}

	// ___________________________________________________________________________________________________________________

	// ___________________________________________________SendAndRecv_____________________________________________________
	private void recvMsg() {
		// Recv a msg from the client and if it is valid post it to the database
		byte dataByte;
		String dataString;
		String msg;
		String reciver;

		try {
			dataByte = this.dIn.readByte();

			// If we get smth unkown exit
			if (dataByte == 10) {// We get an msg
				dataByte = this.dIn.readByte();// Get the next byte
				switch (dataByte) {
					case 11:// Msg follows
						msg = this.dIn.readUTF();// read the msg
						dataByte = this.dIn.readByte();// Get the next byte
						switch (dataByte) {
							case 12:// We get the reciver
								reciver = this.dIn.readUTF(); // read the reciver
								SessionServerMain.MsgSender.put(msg, this.clientName);
								SessionServerMain.RecieverMsg.put(reciver, msg);
								this.dOut.writeByte(0);// Msg was recived, send Ok back
								// to the client
								System.out.println("Msg recieved");
								break;

							case 13:// Msg abort
								break;

							default:// If we get smth unkown exit
								cancel();
								break;
						}
						break;

					case 12:// The receiver follows
						reciver = this.dIn.readUTF();// Read the reciver
						dataByte = this.dIn.readByte();// Read the next byte
						switch (dataByte) {
							case 11:// We get the msg
								msg = this.dIn.readUTF();// Read the msg
								SessionServerMain.MsgSender.put(msg, this.clientName);
								SessionServerMain.RecieverMsg.put(reciver, msg);
								this.dOut.writeByte(0);// Msg was received, send Ok back
								System.out.println("Msg received");
							case 13:// Abort msg sending
								break;
							default:// If we get smth unknown exit
								cancel();
								break;
						}
						break;

					case 13:// abort msg sending
						break;

					default:// If we get smth unknown exit
						cancel();
						break;
				}
			} else {
				cancel();
			}

		} catch (IOException e) {
			System.out.println("Could not recv msg");
			System.out.println(e);
		}

	}

	// Send msg back to the client from the database if available
	private void sendMsg(String msg, String sender) {
		byte dataByte = 3;

		try {
			this.dOut.writeByte(7);// Set the msg flag
			this.dOut.writeByte(8);// Set the msgSender flag
			this.dOut.writeUTF(sender);// write the Sender
			this.dOut.writeByte(9);// Set the msgMsg flag
			this.dOut.writeUTF(msg);// write the msg
			this.dOut.flush();// Send all
			dataByte = this.dIn.readByte();// Check if the msg was send
											// correctly
		} catch (IOException e) {
			System.out.println("Msg could not be deliverd");
			System.out.println(e);
			cancel();
		}

		// check if msg is delivered
		// If we get smth unknown exit
		if (dataByte == 0) {// All is ok the msg was received
			SessionServerMain.MsgSender.remove(msg);//We delete the msg from our database
			SessionServerMain.RecieverMsg.remove(this.clientName);
			// Some error
		} else {
			cancel();
		}
		
		

	}

	// ___________________________________________________________________________________________________________________

	// ___________________________________________________Systemfunctions_________________________________________________
	// Stop the thread and msg the main thread so
	// there can a new session be created
	private void cancel() {
		SessionServerMain.sessionCounter--;// Reduce the number of threads//
											// running
		this.sessionIsRunning = false;// End the session loop, (No while true
										// loops today ;[)
		interrupt();// Close this thread

	}

	// Run methode for the thread
	@Override
	public void run() {

		// Set up the Input and Output Stream
		try {
			this.inOut();
		} catch (IOException e) {
			// Error handling
			System.out.printf("Could not setup Input/Output Stream for %s",
					this.client);
			System.out.println(e);
			cancel();
		}

		while (sessionIsRunning) {
			if (this.authenticated) {
				// Check if there are messages available
				try {
					if (this.dIn.available() > 0/* Check if bytes are available */) {
						recvMsg();// Recv a msg from the client

					} else {
						for (Entry<String, String> entry : SessionServerMain.RecieverMsg
								.entrySet()) {
							
							System.out.println(entry.getKey());
							System.out.println(this.clientName);
							if (entry.getKey().equals(this.clientName)) {
								System.out.println("Found a msg");
								String msg = SessionServerMain.RecieverMsg
										.get(this.clientName);
								// TODO make each msg unique
								String sender = SessionServerMain.MsgSender
										.get(msg);
								System.out.println("Trying to send msg");
								this.sendMsg(msg, sender);

							}
						}

					}
				} catch (IOException e) {
					System.out.println("Could not read Bytes");
					System.out.println(e);
				}

			} else {
				try {
					this.auth();
				} catch (IOException e) {
					System.out.println("Could not auth user");
					System.out.println(e);
					cancel();
				}
			}

		}

	}
	// ___________________________________________________________________________________________________________________
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
// --> 12 the receiver follows
// 13 Abort msg sending
// 14 authenticated
// 15 auth fail
// 16 resend 


