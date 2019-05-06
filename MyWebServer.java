import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

public class MyWebServer {

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
            new Worker(socket).start();
        }
    }
}

class Worker extends Thread {

    private String textPlain = "text/plain";
    private String textHtml = "text/html";

    private Socket socket;

    public Worker(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            // creating a reader to read the data coming in to the socket from the client
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // creating a stream to write to the socket, sending data back to the client
            PrintStream out = new PrintStream(socket.getOutputStream());

            try {
                // reading the first line sent from the client, which contains the GET request we want to parse as the
                // second element
                String fileName = in.readLine().split(" ")[1];
                // we want to ignore these requests, default from firefox
                if (fileName.contains("favicon")) {
                    return;
                }
                // parse the content type from the file name
                String contentType = getContentType(fileName);
                // retrieve the desired file
                File file = new File("./" + fileName);

                // if the content type is not null, this means that the file requested was either a .txt or a .html file
                // and we should send back its contents. Otherwise we will assume it is a directory.
                if (contentType != null) {
                    sendFileToClient(out, file, contentType);
                } else {
                    sendDirToClient(out, file);
                }

            } catch (IOException e) {
                // if any exceptions are thrown during the reading process, they will be printed here
                System.out.println("Server read error");
                System.out.println(e.getMessage());
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

    private void sendDirToClient(PrintStream out, File file) {
        // This gets all of the files in the directory
        List<File> filesAndDirs = Arrays.asList(file.listFiles());

        out.println("HTTP/1.1 200 OK");
        out.println("Content-Length: " + 200);
        out.println("Content-Type: " + textHtml);
        out.println();
        out.println("<pre>");
        out.println("<h1>Index of " + file.getName() + "</h1>");
        out.println("<a href=\"" + file.getParent() + "\">Parent Directory</a> <br>");

        filesAndDirs.forEach(f -> {
            out.println("<a href=\"" + f.getName() + "\">" + f.getName() + "</a> <br>");
        });
    }

    private void sendFileToClient(PrintStream out, File file, String contentType) throws IOException {
        BufferedReader fileReader = new BufferedReader(new FileReader(file));

        out.println("HTTP/1.1 200 OK");
        out.println("Content-Length: " + 200);
        out.println("Content-Type: " + contentType);
        out.println();

        String fileLine;
        while ((fileLine = fileReader.readLine()) != null) {
            out.println(fileLine);
        }
        out.flush();
    }

    private String getContentType(String fileName) {
        if (fileName.contains(".txt")) {
            return textPlain;
        } else if (fileName.contains(".html")) {
            return textHtml;
        } else {
            return null;
        }
    }
}

