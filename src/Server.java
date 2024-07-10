import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static final int PORT = 8080;
    private static List<PrintWriter> clientOutputs = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        ServerSocket listener = new ServerSocket(PORT);
        System.out.println("Serwer oczekuje na połączenia...");
        try {
            while (true) {
                new ClientHandler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                synchronized (clientOutputs) {
                    clientOutputs.add(out);
                }

                while (true) {
                    String input = in.readLine();
                    if (input == null || input.equals("exit")) {
                        break;
                    }

                    if (input.equals("update")) {
                        notifyClients();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void notifyClients() {
            synchronized (clientOutputs) {
                for (PrintWriter writer : clientOutputs) {
                    writer.println("update");
                }
            }
        }
    }
}
