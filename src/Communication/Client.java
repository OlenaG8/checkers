package Communication;

import Communication.Messages.GameStarted;
import Communication.Messages.Move;
import Communication.Messages.Position;
import Logic.GameState;
import Logic.MoveResult;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.function.Consumer;

public class Client {
    private static final int PORT = 7777;

    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket clientSocket;

    private Consumer<GameStarted> onGameStarted;
    private Consumer<MoveResult> onMoveResult;
    private Consumer<Move> onOpponentMove;

    public Client(String ip) throws IOException {
        clientSocket = new Socket(ip, PORT);
        out = new ObjectOutputStream(clientSocket.getOutputStream());
        in = new ObjectInputStream(clientSocket.getInputStream());

        new ConnectionHandler().start();
    }

    public void move(int selectedRow, int selectedCol, int row, int col) {
        Move move = new Move(new Position(selectedRow, selectedCol), new Position(row, col));
        sendMessage(move);
    }

    public void onGameStarted(Consumer<GameStarted> handler) {
        onGameStarted = handler;
    }

    public void onMoveResult(Consumer<MoveResult> handler) {
        onMoveResult = handler;
    }

    public void onOpponentMove(Consumer<Move> handler) {
        onOpponentMove = handler;
    }

    private void sendMessage(Object msg) {
        try {
            out.writeObject(msg);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() throws IOException {
        out.close();
        in.close();
        clientSocket.close();
    }

    private class ConnectionHandler extends Thread {
        @Override
        public void run() {
            try {
                Object message;
                while ((message = in.readObject()) != null) {
                    if (message instanceof MoveResult res) {
                        System.out.println("Received move result: " + res);
                        if (onMoveResult != null) {
                            onMoveResult.accept(res);
                        }
                    } else if (message instanceof Move move) {
                        System.out.println("Received opponent move: " + move);
                        if (onOpponentMove != null) {
                            onOpponentMove.accept(move);
                        }
                    } else if (message instanceof GameStarted started) {
                        System.out.println("Received game started: " + started);
                        if (onGameStarted != null) {
                            onGameStarted.accept(started);
                        }
                    } else {
                        System.out.println("Client received an unrecognized message");
                    }
                }

                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Server disconnected");
            } catch (ClassNotFoundException e) {
                System.out.println("Exception reading from the server: " + e.getMessage());
            }
        }
    }
}
