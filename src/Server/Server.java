package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int PORT = 5033;
    private static PrintWriter logWriter;

    // Synchronized method to write logs
    public synchronized static void writeInLog(String text) {
        if (logWriter != null) {
            logWriter.println(text);
            logWriter.flush();
        }
    }

    public static void main(String[] args) {
        try {
            // Open log file
            logWriter = new PrintWriter(new BufferedWriter(new FileWriter("log.txt", true)));

            ServerSocket serverSocket = new ServerSocket(PORT);
            File uploadDir = new File("./root","/uploaded");
            if (!uploadDir.exists()) uploadDir.mkdir();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (logWriter != null) {
                    logWriter.close();
                }
            }));

            while (true) {
                System.out.println("Waiting for connection...");
                Socket socket = serverSocket.accept();
                ServerThread t = new ServerThread(socket);
                t.start();
            }
        } catch (IOException e) {
            writeInLog("Error starting server: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            System.out.println("Server Stopped");
        }
    }
}
