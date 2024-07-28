package info.preva1l.fadah.microservice;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import info.preva1l.fadah.microservice.clients.Command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;

public class ClientHandler extends Thread {
    private final Socket socket;
    private final MicroService microService;
    private final PrintWriter out;
    private final BufferedReader in;
    private String clientToken;

    public ClientHandler(Socket socket, MicroService microService) throws IOException {
        this.socket = socket;
        this.microService = microService;
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @Override
    public void run() {
        try {
            clientToken = in.readLine();
            if (!microService.isValidToken(clientToken)) {
                out.println("Invalid token");
                socket.close();
                return;
            }

            microService.registerClient(clientToken, this);

            String rawMessage;
            while ((rawMessage = in.readLine()) != null) {
                final JsonObject message = JsonParser.parseString(rawMessage).getAsJsonObject();
                final Command command = Command.valueOf(message.get("command").getAsString());
                switch (command) {
                    case LISTING_GET: microService.getDatabase().getListing(UUID.fromString(message.get("uuid").getAsString()));
                    case LISTING_ADD: ;
                    default: throw new IllegalStateException("Unexpected command %s".formatted(command));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Cleanup on client disconnect
            microService.unregisterClient(clientToken);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}