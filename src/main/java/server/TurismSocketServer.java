package server;

import service.TurismServerService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TurismSocketServer {
    private final int port;
    private final TurismServerService service;
    private final List<ClientConnection> clients = new CopyOnWriteArrayList<>();

    public TurismSocketServer(int port, TurismServerService service) {
        this.port = port;
        this.service = service;
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, service, this);
                clients.add(clientHandler);
                Thread thread = new Thread(clientHandler, "client-" + clientSocket.getPort());
                thread.start();
            }
        }
    }

    public void broadcastTripsUpdated() {
        for (ClientConnection client : clients) {
            client.sendTripsUpdatedNotification();
        }
    }

    public void removeClient(ClientConnection client) {
        clients.remove(client);
    }
}
