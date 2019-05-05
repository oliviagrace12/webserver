import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

/**
 * Created by oliviachisman on 2019-05-05
 */
public class MyTelnetClient {

    public static void main(String[] args) {
        String serverName = args.length < 1 ? "localhost" : args[0];
//        String serverName = "condor.depaul.edu";
        int port = 80;

        System.out.println("Olivia Chisman's MyTelnet Client \n");
        System.out.println("Using server: " + serverName + ", port: " + port);

        Socket socket;
        BufferedReader fromServer;
        PrintStream toServer;
        BufferedReader fromConsole = new BufferedReader(new InputStreamReader(System.in));
        try {
            socket = new Socket(serverName, port);
            fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            toServer = new PrintStream(socket.getOutputStream());

            sendData(fromConsole, toServer);

            receiveData(fromServer);

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void receiveData(BufferedReader fromServer) throws IOException {
        String textFromServer;
        for (int i = 1; i <=20; i++){
            textFromServer = fromServer.readLine();
            if (textFromServer != null) System.out.println(textFromServer);
        }
    }

    private static void sendData(BufferedReader fromConsole, PrintStream toServer) throws IOException {
        System.out.print("Enter text to send to server or <stop> to end: ");
        System.out.flush();
        String dataToSend = fromConsole.readLine();
        if (!dataToSend.contains("stop")){
            toServer.println(dataToSend);
            toServer.flush();
            sendData(fromConsole, toServer);
        }
    }

}
