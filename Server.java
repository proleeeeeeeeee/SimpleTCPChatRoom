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
            serverSocket = new ServerSocket(9999);
            pool = Executors.newCachedThreadPool();
            while(!done) {
                Socket client = serverSocket.accept();
                connectionHandler handler = new connectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (Exception e) {
            shutDown();
        }

    }

    public void shutDown(){
        done = true;
        pool.shutdown();
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
        private PrintWriter out;
        private String Nickname;
        connectionHandler(Socket client){
            this.client = client;
        }
        @Override
        public void run() {
            try{
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out = new PrintWriter (new OutputStreamWriter(client.getOutputStream()), true);

                while(true) {

                    out.println("Please Enter Nickname: ");
                    Nickname = in.readLine();
                    if(Nickname != null && !Nickname.trim().isEmpty()){
                        break;
                    }
                    else{
                        System.out.println("Please re-enter your Nickname");
                    }
                }
                System.out.println(Nickname + " is connected!");
                broadCastMessage(Nickname + " Join the chat!");
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
                            out.println("No nickname Provided");
                        }
                    }
                    else if(msg.startsWith("/quit")){
                        broadCastMessage(Nickname + " left the chat!");
                        shutDown();
                        break;
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
            out.println(msg);
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
                if (in != null) in.close();  // Close BufferedReader
                if (out != null) out.close();  // Close PrintWriter
                if (client != null && !client.isClosed()) {
                    client.close();  // Close socket if it's still open
                }
            } catch (IOException io) {
                // ignore errors during shutdown
            } finally {
                // Remove this connection from the list of active connections
                connections.remove(this);
                System.out.println(Nickname + " has disconnected.");
            }
        }


    }
    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}
