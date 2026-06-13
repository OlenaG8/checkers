package Communication;

import Communication.Messages.Move;
import Communication.Messages.Position;
import Logic.MoveResult;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class Client {
    private static final int PORT = 7777;

    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket clientSocket;

    public Client(String ip) throws IOException {
        clientSocket = new Socket(ip, PORT);
        out = new ObjectOutputStream(clientSocket.getOutputStream());
        in = new ObjectInputStream(clientSocket.getInputStream());
    }

    public void move(int selectedRow, int selectedCol, int row, int col) {
        Move move = new Move(new Position(selectedRow, selectedCol), new Position(row, col));
        sendMessage(move);

        MoveResult res = readMessage(MoveResult.class);
        if (res == MoveResult.INVALID_MOVE) {
            throw new IllegalStateException("Unexpected move result: " + res);
        }
    }

    private void sendMessage(Object msg) {
        try {
            out.writeObject(msg);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T readMessage(Class<T> clazz) {
        try {
            Object message = in.readObject();
            if (!clazz.isInstance(message)) {
                throw new RuntimeException(clazz.getSimpleName() + " is not a " + message.getClass().getSimpleName());
            }
            return clazz.cast(message);
        } catch (SocketException e) {
            throw new RuntimeException("Server disconnected", e);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Exception reading from the server: " + e.getMessage(), e);
        }
    }

    public void close() throws IOException {
        out.close();
        in.close();
        clientSocket.close();
    }
}
