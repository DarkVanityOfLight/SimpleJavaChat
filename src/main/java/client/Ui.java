package client;

import java.util.Scanner;


public class Ui {

	public static String getAddress(){
		
		return "127.0.0.1";
	}
	
	
	public static int getPort(){
		
		return 8000;
	}
	
	public static String getUsername(){
		System.out.println("Whats your name: ");
		Scanner input = new Scanner(System.in);
		String name = input.next();
		input.close();
		return name;
	}
	
	public static void printMsg(String sender, String msg){
		System.out.printf("%s: %s\n", sender, msg);
		return;
	}
}
