package Communication;

import Communication.Messages.GameStarted;
import Communication.Messages.Move;
import Logic.GameState;
import Logic.MoveResult;
import Logic.PlayerColor;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket serverSocket;
    private static final int PORT = 7777;

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start(PORT);
    }

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started");

        while (true) {
            try {
                GameState state = new GameState(GameState.StartPosition.VANILLA_ON_BOTTOM);

                ConnectionHandler conn1 = new ConnectionHandler(serverSocket.accept(), state, PlayerColor.VANILLA);
                System.out.println("Client connected (player 1)");

                ConnectionHandler conn2 = new ConnectionHandler(serverSocket.accept(), state, PlayerColor.CHOCOLATE);
                System.out.println("Client connected (player 2), starting game");

                conn1.opponent = conn2;
                conn2.opponent = conn1;

                conn1.start();
                conn2.start();
            } catch (IOException e) {
                System.out.println("Failed to start game");
            }
        }
    }

    public void stop() throws IOException {
        serverSocket.close();
    }

    private static class ConnectionHandler extends Thread {
        private final Socket clientSocket;
        private final GameState state;
        private final PlayerColor color;

        private final ObjectOutputStream out;
        private final ObjectInputStream in;
        private ConnectionHandler opponent;

        public ConnectionHandler(Socket socket, GameState state, PlayerColor color) throws IOException {
            this.clientSocket = socket;
            this.state = state;
            this.color = color;
            this.out = new ObjectOutputStream(clientSocket.getOutputStream());
            this.in = new ObjectInputStream(clientSocket.getInputStream());
        }

        @Override
        public void run() {
            try {
                out.writeObject(new GameStarted(color));

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
                        out.writeObject(res);
                        if (res != MoveResult.INVALID_MOVE) {
                            opponent.out.writeObject(move); // notify opponent of the move
                        }
                    } else {
                        System.out.println("Server received an unrecognized message");
                    }
                }

                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Client disconnected");
            } catch (ClassNotFoundException e) {
                System.out.println("Exception reading from the client: " + e.getMessage());
            }
        }
    }
}