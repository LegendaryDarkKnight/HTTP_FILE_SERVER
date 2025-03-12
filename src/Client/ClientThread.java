package Client;

import java.io.*;
import java.net.Socket;

public class ClientThread extends Thread{
    private final Socket socket;
    private final PrintWriter pr;
    private final BufferedReader in;
    private final String fileName;
    public ClientThread(Socket socket,
                        String fileName)  throws IOException{
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.pr = new PrintWriter(this.socket.getOutputStream());
        this.fileName = fileName;
    }
    private void clientWork(){
        File file = new File("./" + fileName);
        String output = "UPLOAD "+this.fileName+"\r\n";
        if(!file.exists()){
            System.out.println("No such Files");
            try{
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        System.out.println(output);
        pr.write(output);
        pr.flush();
        try {
            String feedback = in.readLine();
            if(feedback.startsWith("Invalid")){
                System.out.println("Request Failed");
                return;
            }
        } catch (IOException e) {
            System.out.println("Exception reading the feedback. File upload");

        }

        try (FileInputStream fis = new FileInputStream(file);
             BufferedOutputStream bos = new BufferedOutputStream(this.socket.getOutputStream())) {
            byte[] buffer = new byte[4096]; // Increased buffer size for faster transfers
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead); // Writing bytes to socket output stream
            }
            bos.flush();
            socket.shutdownOutput();
            String responseLine;
            while ((responseLine = in.readLine()) != null) {
                System.out.println(responseLine);
                if (responseLine.isEmpty()) {
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("File upload failed");
        }
    }
    @Override
    public void run(){
        clientWork();
    }
}
