package client;

import java.util.Scanner;

public class TerminalUI implements Ui {

    public String getAddress() {
        return "127.0.0.1";
    }


    public int getPort() {

        return 8000;
    }

    public String getUsername() {
        System.out.println("Whats your name: ");
        Scanner input = new Scanner(System.in);
        String name = input.next();
        input.close();
        return name;
    }

    public void displayMsg(String sender, String msg) {
        System.out.printf("%s: %s\n", sender, msg);
    }
}
