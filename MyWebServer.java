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

    // content type codes
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
                // reading the first line sent from the client
                String line = in.readLine();
                if (line == null) {
                    throw new UnsupportedOperationException();
                }
                // if the line contains cgi, then it should be handled as an addition request. Otherwise, it should
                // be handled as a file or directory request
                if (line.contains("cgi")) {
                    handleAsAdd(out, line);
                } else {
                    handleAsFile(out, line);
                }

            } catch (IOException e) {
                // if any exceptions are thrown during the reading process, they will be printed here
                System.out.println("Server read error");
                System.out.println(e.getMessage());
            }
            // closing the connection with the client
            socket.close();
        } catch (IOException | UnsupportedOperationException e) {
            // if any exceptions are thrown during the socket operations (getting the input and output streams,
            // closing the socket), they will be printed here
            if (e.getMessage() != null) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void handleAsAdd(PrintStream out, String line) {
        // getting portion of GET line that has cgi request
        String cgi = line.split(" ")[1];
        // getting portion of cgi request that has the fields and values
        String fieldLine = cgi.split("\\?")[1];
        // getting each field-value pair
        String[] pairs = fieldLine.split("&");
        String person = "";
        long num1 = 0;
        long num2 = 0;

        // for each field-value pair, parse based on the field name
        for (int i = 0; i < pairs.length; i++) {
            String str = pairs[i];
            if (str.contains("person")) {
                person = str.split("=")[1];
            } else if (str.contains("num1")) {
                num1 = Long.valueOf(str.split("=")[1]);
            } else if (str.contains("num2")) {
                num2 = Long.valueOf(str.split("=")[1]);
            }
        }

        // sending the headers of the http response to the client
        out.println("HTTP/1.1 200 OK");
        // telling the client how long the response will be
        out.println("Content-Length: " + 200);
        // tells the client how to interpret the response. For this response we are choosing html
        out.println("Content-Type: " + textHtml);
        // formatting response
        out.println();
        // send payload, which tells the person's name, the numbers they entered and the sum of those numbers
        out.println(person + ", the sum of " + num1 + " and " + num2 + " is " + (num1 + num2));
        // flush the stream so that the client receives the response right away
        out.flush();
    }

    private void handleAsFile(PrintStream out, String line) throws IOException {
        // splitting the line on the space character, to separate out the elements
        String[] array = line.split(" ");
        // if the array does not have more than one element, ignore this request
        if (array.length < 2) {
            throw new UnsupportedOperationException();
        }
        // the file name is the second element in the first line of the request
        String fileName = array[1];
        // we want to ignore these requests, default from firefox
        if (fileName.contains("favicon")) {
            throw new UnsupportedOperationException();
        }

        // retrieve the desired file
        File file = new File("./" + fileName);
        // if the file is a directory, display the files in the directory. Otherwise, display the file if the
        // content type is supported
        if (file.isDirectory()) {
            sendDirToClient(out, file);
        } else {
            // parse the content type from the file name
            String contentType = getContentType(fileName);
            sendFileToClient(out, file, contentType);
        }
    }

    private void sendDirToClient(PrintStream out, File file) {
        // This gets all of the files in the directory
        List<File> filesAndDirs = Arrays.asList(file.listFiles());

        // sending the headers of the http response to the client
        out.println("HTTP/1.1 200 OK");
        // telling the client how long the response will be
        out.println("Content-Length: " + file.getTotalSpace());
        // tells the client how to interpret the response, here we want it to be interpreted as html
        out.println("Content-Type: " + textHtml);
        // more configs and formatting
        out.println("Connection: close");
        out.println();
        // beginning of payload, which is dynamically generated html
        out.println("<pre>");
        // title of page, which tells the directory name
        out.println("<h1>Index of " + file.getName() + "</h1>");
        // list the parent directory as "Parent Directory". This can be used to navigate one level up in the
        // directory structure
        out.println("<a href=\"../\">Parent Directory/</a> <br>");

        // list out all of the files in the directory as hyperlinks
        filesAndDirs.forEach(f -> {
            if (f.isDirectory()) {
                out.println("<a href=\"" + f.getName() + "/\">" + f.getName() + "/</a><br>");
            } else {
                out.println("<a href=\"" + f.getName() + "\">" + f.getName() + "</a> <br>");
            }
        });
    }

    private void sendFileToClient(PrintStream out, File file, String contentType) throws IOException {
        // creating a reader to read the contents of the file
        BufferedReader fileReader = new BufferedReader(new FileReader(file));

        // sending the headers of the http response to the client
        out.println("HTTP/1.1 200 OK");
        // telling the client how long the response will be
        out.println("Content-Length: " + file.getTotalSpace());
        // tells the client how to interpret the response. For this server, the responses will be either text or html
        out.println("Content-Type: " + contentType);
        // formatting response
        out.println();

        // reading each line of the file and sending it to the client
        String fileLine;
        while ((fileLine = fileReader.readLine()) != null) {
            out.println(fileLine);
        }
        // making sure everything gets sent to the client right away
        out.flush();
    }

    private String getContentType(String fileName) {
        // decide what type of file we are dealing with based on the file extension
        if (fileName.contains(".txt")) {
            return textPlain;
        } else if (fileName.contains(".html")) {
            return textHtml;
        } else {
            return null;
        }
    }
}

