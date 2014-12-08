import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Chat{

    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(System.out, true, "utf-8"));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Program starting...");
        try {
            ServerSocket ss = new ServerSocket(4444);
            System.out.println("Server starting...");
            while(true){
                Socket s = ss.accept();
                Scanner in = new Scanner(s.getInputStream());
                String nameClient = in.nextLine();
                SocketThread socketThread = new SocketThread(s, nameClient);
                Thread t = new Thread(socketThread);
                t.start();
            }
        } catch (IOException ex) {
            Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

class ListSocket {
    private static HashMap<Socket,String> listSocket = new HashMap<Socket, String>();

    public synchronized static HashMap<Socket,String> getListSocket() {
        return ListSocket.listSocket;
    }

    public synchronized static void addSocketToList(Socket s, String name) {
        ListSocket.listSocket.put(s, name);
    }

    public synchronized static void removeSocketWithList(Socket s) {
        ListSocket.listSocket.remove(s);
    }
}

class SocketThread implements Runnable {

    private Socket s = null;
    private boolean except;
    private String nameClient ;
    private Scanner in = null;
    private PrintWriter out = null;
    private boolean exit = true;
    private String inMessage = null;
    private String outMessage = null;
    private HashMap<Socket, String> listSocket = null;

    public SocketThread(Socket s, String nameClient) {
        this.s = s;
        this.nameClient = nameClient;
    }

    @Override
    public void run() {
        try {
            System.out.println("User connect...");
            ListSocket.addSocketToList(s,nameClient);
            in = new Scanner(s.getInputStream());
            ListOutputThread listOutputThread = new ListOutputThread(s);
            Thread t = new Thread(listOutputThread);
            t.start();

            while (exit) {
                inMessage = in.nextLine();
                if (inMessage.equals(nameClient.substring(1) + ":" + "null")) {
                    break;
                }
                System.out.println("Server in:" + inMessage);
                listSocket = ListSocket.getListSocket();
                for (Socket socket : listSocket.keySet()) {
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
//            t.interrupt();
            in.close();
            out.close();
            s.close();
        } catch (IOException ex) {
            Logger.getLogger(SocketThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchElementException ex) {
            ListSocket.removeSocketWithList(s);
            except = true;
        }
    }
}

class ListOutputThread implements Runnable {

    private Socket s = null;
    private Scanner in = null;
    private PrintWriter out = null;
    private boolean exit = true;
    private HashMap<Socket, String> listOfClients;
    private String outMessage = null;

    ListOutputThread(Socket s) {
        this.s = s;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(s.getOutputStream());
            while(true) {
                listOfClients = ListSocket.getListSocket();
                for (Socket socket : listOfClients.keySet()) {
                    outMessage = listOfClients.get(socket);
                    out.println(outMessage);
                    out.flush();
                }
                outMessage = "*exit";
                out.println(outMessage);
                out.flush();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
//            out.close();
        } catch(IOException ex) {
            Logger.getLogger(SocketOutputThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}