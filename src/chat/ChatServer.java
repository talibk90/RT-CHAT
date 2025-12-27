package chat;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static int clientCount = 0;
    private static Map<PrintWriter, String> clientNames = new HashMap<>();
    private static Set<PrintWriter> clientWriters = new HashSet<>();

    public static void main(String[] args) {
        System.out.println("Chat Server is running on port " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addClient(String name, PrintWriter writer) {
        synchronized (clientWriters) {
            clientWriters.add(writer);
            clientNames.put(writer, name);
        }
    }

    public static void removeClient(PrintWriter writer) {
        synchronized (clientWriters) {
            String name = clientNames.get(writer);
            if (name != null) {
                broadcast("LEFT:" + name, writer);
                System.out.println(name + " has left.");
            }
            clientWriters.remove(writer);
            clientNames.remove(writer);
        }
    }

    public static List<String> getOtherClientNames(PrintWriter excludeWriter) {
        List<String> names = new ArrayList<>();
        synchronized (clientWriters) {
            for (PrintWriter writer : clientWriters) {
                if (writer != excludeWriter) {
                    names.add(clientNames.get(writer));
                }
            }
        }
        return names;
    }

    public static void broadcast(String message, PrintWriter excludeWriter) {
        synchronized (clientWriters) {
            for (PrintWriter writer : clientWriters) {
                if (writer != excludeWriter) {
                    writer.println(message);
                }
            }
        }
    }

    public static void sendPrivate(String targetName, String message, PrintWriter senderWriter) {
        synchronized (clientWriters) {
            for (Map.Entry<PrintWriter, String> entry : clientNames.entrySet()) {
                if (entry.getValue().equals(targetName)) {
                    PrintWriter targetWriter = entry.getKey();
                    // Send to Target
                    targetWriter.println(message);
                    // Echo back to Sender (so they see what they sent)
                    senderWriter.println(message);
                    return;
                }
            }
        }
    }
}
