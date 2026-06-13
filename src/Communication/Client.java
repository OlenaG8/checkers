package Communication;

import Logic.GameState;
import Logic.MoveResult;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class Client {

    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket clientSocket;

    private GameState state = new GameState(GameState.StartPosition.VANILLA_ON_BOTTOM);

    public Client() {
    }

    public void start(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        out = new ObjectOutputStream(clientSocket.getOutputStream());
        in = new ObjectInputStream(clientSocket.getInputStream());
        new ServerHandler().start();
    }

    public void sendMessage(Message notif) throws IOException {
        out.writeObject(notif);
    }

    public void close() throws IOException {
        out.close();
        in.close();
        clientSocket.close();
    }

    public void moveCheckerOnce(int selectedRow, int selectedCol, int row, int col) {
        MoveResult res = state.moveCheckerOnce(selectedRow, selectedCol, row, col);
        if (res == MoveResult.INVALID_MOVE) {
            throw new IllegalStateException("Unexpected move result: " + res);
        }
    }

    private class ServerHandler extends Thread {

        public ServerHandler() {
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                Object message;
                while ((message = in.readObject()) != null) {
                    if (message instanceof Message n) {
                        System.out.println("Received an unrecognized message");
                    } else {
                        System.out.println("Received an unrecognized message");
                    }
                }
                in.close();
            } catch (SocketException e) {
                System.out.println("Communication.Server disconnected");
            }
            catch (IOException | ClassNotFoundException e) {
                System.out.println("Exception reading from the server: " + e.getMessage());
            }
        }
    }

}
