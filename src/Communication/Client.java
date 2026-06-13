package Communication;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class Client {

    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket clientSocket;

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
