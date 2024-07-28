package info.preva1l.fadah.microservice.clients;

import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

public class ServiceConnection {
    private final String host;
    private final int port;
    private final String token;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public ServiceConnection(String host, int port, String token) {
        this.host = host;
        this.port = port;
        this.token = token;
    }

    public void connect() throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        out.println(token);
        String response = in.readLine();
        // parse json

    }

    public CompletableFuture<JsonObject> getData(String topic) throws IOException {
        // command
        return CompletableFuture.completedFuture(null);
    }

    public void disconnect() throws IOException {
        out.close();
        in.close();
        socket.close();
    }
}