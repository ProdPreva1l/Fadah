package info.preva1l.fadah.microservice;

import info.preva1l.fadah.microservice.storage.Database;
import info.preva1l.fadah.microservice.storage.SQLiteDatabase;
import lombok.Getter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MicroService {
    private final int port;
    private final InetAddress ipAddress;
    private final Map<String, ClientHandler> clients = new HashMap<>();
    private final Map<String, Set<String>> subscriptions = new HashMap<>(); // topic -> set of client tokens
    @Getter private final Database database;

    public MicroService(String ip, int port) throws IOException {
        this.ipAddress = InetAddress.getByName(ip);
        this.port = port;

        this.database = new SQLiteDatabase();
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port, 50, ipAddress)) {
            System.out.println("MicroService started on IP " + ipAddress.getHostAddress() + " and port " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(socket, this).start();
            }
        }
    }

    public synchronized void registerClient(String token, ClientHandler clientHandler) {
        clients.put(token, clientHandler);
    }

    public synchronized void unregisterClient(String token) {
        clients.remove(token);
    }

    public synchronized void subscribe(String token, String topic) {
        subscriptions.computeIfAbsent(topic, k -> new HashSet<>()).add(token);
    }

    public synchronized void publish(String topic, String message) {
        Set<String> clientTokens = subscriptions.get(topic);
        if (clientTokens != null) {
            for (String token : clientTokens) {
                ClientHandler clientHandler = clients.get(token);
                if (clientHandler != null) {
                    clientHandler.sendMessage(message);
                }
            }
        }
    }

    public boolean isValidToken(String token) {
        return false;
    }

    public static void main(String[] args) throws IOException {
        MicroService microService = new MicroService("127.0.0.1", 1212);
        microService.start();
    }
}