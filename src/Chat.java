import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Chat{



    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(System.out, true, "cp866"));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Program starting...");
        try {
            ServerSocket ss = new ServerSocket(922);
            System.out.println("Server starting...");
            while(true){
                Socket s = ss.accept();
                SocketThread socketThread = new SocketThread(s);
                Thread t = new Thread(socketThread);
                t.start();
            }
        } catch (IOException ex) {
            Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}



class ListSocket
{

    private static ArrayList<Socket> listSocket = new ArrayList<Socket>();

    public synchronized static ArrayList<Socket> getListSocket() {
        return ListSocket.listSocket;
    }

    public synchronized static void addSocketToList(Socket s) {
        ListSocket.listSocket.add(s);
    }
    public synchronized static void removeSocketWithList(Socket s) {
        ListSocket.listSocket.remove(s);
    }
}




class SocketThread implements Runnable {

    private Socket s = null;
    private Scanner in = null;
    private PrintWriter out = null;
    private boolean exit = true;
    private String inMessage = null;
    private String outMessage = null;
    private ArrayList<Socket> listSocket = null;

    public SocketThread(Socket s) {
        this.s = s;
    }

    @Override
    public void run() {
        try {
            System.out.println("User connect...");
            ListSocket.addSocketToList(s);
            in = new Scanner(s.getInputStream());
            while (exit) {
                inMessage = in.nextLine();
                System.out.println("Server in:" + inMessage);
                listSocket = ListSocket.getListSocket();
                for (Socket socket : listSocket) {
                    if (!socket.equals(s)) {
                        out = new PrintWriter(socket.getOutputStream());
                        out.println(inMessage);
                        out.flush();
                    }
                }
                if (inMessage.trim().equals("exit")) {
                    exit = false;
                }
            }
            ListSocket.removeSocketWithList(s);
            System.out.println("User disconnect...");
            in.close();
            out.close();
            s.close();
        } catch (IOException ex) {
            Logger.getLogger(SocketThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

