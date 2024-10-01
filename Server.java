import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server implements Runnable {
    private ArrayList<connectionHandler> connections;
    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(8888);
            Socket client = serverSocket.accept();
            connectionHandler handler = new connectionHandler(client);
            connections.add(handler);
        } catch (IOException e) {
            // TODO: handle
        }

    }

    class connectionHandler implements Runnable{
        private Socket client;
        private BufferedReader in;
        private BufferedWriter out;
        private String Nickname;
        connectionHandler(Socket client){
            this.client = client;
        }
        @Override
        public void run() {
            try{
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                while(true) {
                    System.out.print("Please Enter Nickname: ");
                    Nickname = in.readLine();
                    if(Nickname != null && !Nickname.trim().isEmpty()){
                        break;
                    }
                    else{
                        System.out.println("Please re-enter your Nickname");
                    }
                }
                System.out.println(Nickname + " is connected!");
                broadCastMessage(Nickname + "Join the chat!");

            }catch(IOException io){
                // TODO: handle
            }
        }

        public void sendMessage(String msg){
            try {
                out.write(msg);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void broadCastMessage(String msg){
            for(connectionHandler ch: connections){
                if(ch!=null){
                    ch.sendMessage(msg);
                }
            }
        }
    }
}
