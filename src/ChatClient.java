import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatClient {

    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(System.out, true, "cp866"));
            System.out.println("Client starting...");
            Socket s = new Socket("92.42.26.48", 922);
            System.out.println("Connect to server...");
            Thread threadIn = new Thread(new SocketInputThread(s));
            Thread threadOut = new Thread(new SocketOutputThread(s));
            threadOut.start();
            threadIn.start();
        } catch (UnknownHostException ex) {
            Logger.getLogger(ChatClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ChatClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}



class SocketInputThread implements Runnable
{

    private Socket s = null;
    private Scanner in = null;
    private PrintWriter out = null;
    private boolean exit = true;
    private String inMessage = null;
    private String outMessage = null;

    public SocketInputThread(Socket s) {
        this.s = s;
    }

    @Override
    public void run() {
        System.out.println("SocketInputThread");
        try {
            in = new Scanner(s.getInputStream());
            while(true){
                if(in.hasNext()){
                    inMessage = in.nextLine();
                    System.out.println("This is in:" + inMessage);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(SocketInputThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}




class SocketOutputThread implements Runnable
{

    private Socket s = null;
    private Scanner in = null;
    private PrintWriter out = null;
    private boolean exit = true;
    private String inMessage = null;
    private String outMessage = null;

    public SocketOutputThread(Socket s) {
        this.s = s;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(s.getOutputStream());
            BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in,"windows-1251"));
            while (true) {
                outMessage = buffer.readLine();
                System.out.println("This is out:" + outMessage);
                out.println(outMessage);
                out.flush();
            }
        } catch (IOException ex) {
            Logger.getLogger(SocketOutputThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

