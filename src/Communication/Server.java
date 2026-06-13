package Communication;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Server {
    private ServerSocket serverSocket;
    private static final int PORT = 7777;

    static void main() throws IOException {
        Server server = new Server();
        server.start(PORT);
    }

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Communication.Server started");
        Timer schedule = new Timer(true);
        while (true) {
            new ClientHandler(serverSocket.accept(), schedule).start();
        }
    }

    public void stop() throws IOException {
        serverSocket.close();
    }

    private static class ClientHandler extends Thread {
        private final Socket clientSocket;
        private final Timer schedule;

        public ClientHandler(Socket socket, Timer schedule) {
            this.clientSocket = socket;
            this.schedule = schedule;
        }

        @Override
        public void run() {
            System.out.println("Communication.Client connected");
            try {
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

                Object message;
                while ((message = in.readObject()) != null) {
                    if (message instanceof Message n) {
                        System.out.println("Received notification: " + n.getMessage());
                        schedule.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    out.writeObject(n);
                                } catch (IOException e) {
                                    System.out.println("Failed to send a notification to a client: " + e.getMessage());
                                }
                            }
                        }, Date.from(n.getAlertTime()));
                    } else {
                        System.out.println("Communication.Server received an unrecognized message");
                    }
                }

                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Communication.Client disconnected");
            }
            catch (ClassNotFoundException e) {
                System.out.println("Exception reading from the client: " + e.getMessage());
            }
        }
    }
}