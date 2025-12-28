package chat;

import java.io.*;
import java.net.*;

public class ClientHandler extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Read name first
            String nameLine = in.readLine();
            String myName = "Unknown";
            if (nameLine != null && nameLine.startsWith("NAME:")) {
                myName = nameLine.substring(5);
            }

            ChatServer.addClient(myName, out);
            out.println("ID:" + myName);

            // Notify me about others
            for (String otherName : ChatServer.getOtherClientNames(out)) {
                out.println("PRESENT:" + otherName);
            }

            // Send history
            for (String hist : ChatServer.getHistory()) {
                out.println(hist);
            }

            // Notify others about me
            ChatServer.broadcast("JOIN:" + myName, out);

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Received from " + myName + ": " + message);
                if (message.startsWith("PM:")) {
                    // Format: PM:TargetName:MessageContent
                    String[] parts = message.split(":", 3);
                    if (parts.length >= 3) {
                        String target = parts[1];
                        String msgContent = parts[2];
                        ChatServer.sendPrivate(target, "PM:" + myName + ":" + msgContent, out);
                    }
                } else {
                    // Broadcast as Global Message
                    // Format: MSG:SenderName:Content
                    ChatServer.broadcast("MSG:" + myName + ":" + message, out);
                }
            }
        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ChatServer.removeClient(out);
        }
    }
}
