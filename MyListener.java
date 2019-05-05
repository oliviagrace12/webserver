import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class MyListener {

    public static void main(String[] args) throws IOException {
        int qLen = 6;
        // defining the port on which to start the server socket
        int port = 2540;

        // creating a server socket
        ServerSocket serverSocket = new ServerSocket(port, qLen);

        // letting the user know that the server has started, and on which port
        System.out.println("Olivia Chisman's Lister server starting up, listening to port " + port + ".\n");

        // program runs in infinite loop, unless exception is thrown
        while (true) {
            // if any clients try to connect, the serverSocket will accept their connections and return a socket
            Socket socket = serverSocket.accept();
            // this socket is passed to a new thread that is spawned (an instance of the Worker class) and start
            // is called on this thread, kicking off the processes in the Worker class run method.
            new ListenerWorker(socket).start();
        }
    }
}

class ListenerWorker extends Thread {

    private Socket socket;

    public ListenerWorker(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            // creating a reader to read the data coming in to the socket from the client
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // creating a stream to write to the socket, sending data back to the client
            PrintStream out = new PrintStream(socket.getOutputStream());

            try {
                // reading a line from the socket (sent from the client) and printing to console
                String line;
                while ((line = in.readLine()) != null && !line.isEmpty()) {
                    System.out.println(line);
                }
                out.println("Server received message");
                out.flush();
            
            } catch (IOException e) {
                // if any exceptions are thrown during the reading process, they will be printed here
                System.out.println("Server read error");
                e.printStackTrace();
            }
            // closing the connection with the client
            socket.close();
        } catch (IOException e) {
            // if any exceptions are thrown during the socket operations (getting the input and output streams,
            // closing the socket), they will be printed here
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}

