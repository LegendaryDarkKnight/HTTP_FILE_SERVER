package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final int PORT = 5033;
    public static void main(String[] args) throws IOException {
        while (true){
            Scanner scanner = new Scanner(System.in);
            String[] input = scanner.nextLine().split(" ");
            if(input.length < 1){
                System.out.println("Invalid Input");
                continue;
            }
            for(String i:input){
                if(i.equalsIgnoreCase("Upload")) continue;
                Socket socket = new Socket("localhost",PORT);
                ClientThread clientThread = new ClientThread(socket,
                        i);
                clientThread.start();
            }
        }
    }
}
