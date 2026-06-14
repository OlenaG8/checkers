package Communication;

import Communication.Messages.Move;
import Logic.GameState;
import Logic.MoveResult;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket serverSocket;
    private static final int PORT = 7777;

    private final GameState state = new GameState(GameState.StartPosition.VANILLA_ON_BOTTOM);

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start(PORT);
    }

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started");
        while (true) {
            new ConnectionHandler(serverSocket.accept(), state).start();
        }
    }

    public void stop() throws IOException {
        serverSocket.close();
    }

    private static class ConnectionHandler extends Thread {
        private final Socket clientSocket;
        private final GameState state;

        public ConnectionHandler(Socket socket, GameState state) {
            this.clientSocket = socket;
            this.state = state;
        }

        @Override
        public void run() {
            System.out.println("Client connected");
            try {
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

                Object message;
                while ((message = in.readObject()) != null) {
                    if (message instanceof Move move) {
                        System.out.println("Received move: " + move);
                        MoveResult res = state.move(
                                move.getFrom().getRow(),
                                move.getFrom().getCol(),
                                move.getTo().getRow(),
                                move.getTo().getCol()
                        );
                        sendMessage(out, res);
                    } else {
                        System.out.println("Server received an unrecognized message");
                    }
                }

                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Client disconnected");
            }
            catch (ClassNotFoundException e) {
                System.out.println("Exception reading from the client: " + e.getMessage());
            }
        }

        private void sendMessage(ObjectOutputStream out, Object msg) {
            try {
                out.writeObject(msg);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}