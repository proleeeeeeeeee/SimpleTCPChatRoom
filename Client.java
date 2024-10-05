import java.io.*;
import java.net.Socket;

public class Client implements Runnable{
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;

    private boolean done;
    @Override
    public void run() {
        try{
            client = new Socket("localhost", 9999);
            out = new PrintWriter(new OutputStreamWriter(client.getOutputStream()), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            InputHandler inHandler = new InputHandler();
            Thread t = new Thread(inHandler);
            t.start();

            String inMsg;
            while((inMsg = in.readLine()) != null){
                System.out.println(inMsg);
            }
        }catch(IOException io){

        }
    }



    class InputHandler implements Runnable{

        @Override
        public void run() {
            try{
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while(!done){
                    String msg = inReader.readLine();
                    if(msg.startsWith("/quit")){
                        out.println(msg);
                        shutDown();
                        inReader.close();
                    }
                    else{
                        out.println(msg);
                    }
                }
            }catch(IOException io){
                shutDown();
            }
        }
        public void shutDown(){
            done = true;
            try{
                if (in != null) in.close();  // Close BufferedReader
                if (out != null) out.close();  // Close PrintWriter
                if (client != null && !client.isClosed()) {
                    client.close();  // Close socket if it's still open
                }
            }catch(IOException io){
                // ignore
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
