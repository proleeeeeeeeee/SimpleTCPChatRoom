import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    private ArrayList<connectionHandler> connections;
    private ServerSocket serverSocket;
    private boolean done;
    private ExecutorService pool;
    public Server(){
        connections = new ArrayList<>();
        done = false;
    }
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(8888);
            pool = Executors.newCachedThreadPool();
            while(!done) {
                Socket client = serverSocket.accept();
                connectionHandler handler = new connectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (IOException e) {
            shutDown();
        }

    }

    public void shutDown(){
        done = true;
        if(!serverSocket.isClosed()){
            try {
                serverSocket.close();
            }catch(IOException io){
                // ignore
            }
        }
        for(connectionHandler ch: connections){
            ch.shutDown();
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
                String msg;
                while((msg = in.readLine()) != null){
                    if (msg.startsWith("/nick")){
                        String[] msgSplit = msg.split(" ", 2);
                        if(msgSplit.length == 2){
                            broadCastMessage(Nickname + " renamed their to " + msg);
                            Nickname = msgSplit[1];
                            System.out.println("Successfully change nickname to " + Nickname);
                        }
                        else{
                            out.write("No nickname Provided");
                        }
                    }
                    else if(msg.startsWith("/quit")){
                        broadCastMessage(Nickname + " left the chat!");
                        shutDown();
                    }
                    else {
                        broadCastMessage(Nickname + ": " + msg);
                    }
                }

            }catch(IOException io){
                shutDown();
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

        public void shutDown(){
            try {
                in.close();
                out.close();
                if (!client.isClosed()) {
                    client.close();
                }
            }catch(IOException io){
                // ignore
            }
        }


    }
    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}
