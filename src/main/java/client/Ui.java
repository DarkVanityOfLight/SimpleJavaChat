package client;

import java.util.Scanner;


interface Ui {

	String getAddress();
	int getPort();
	String getUsername();

	void displayMsg(String sender, String msg);
}
