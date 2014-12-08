import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Cli {
    String nameClient;
    JFrameLogin login;

    Cli() {
        login =  new JFrameLogin();
    }

    void main2() {
        try {
            while (login.nameClient == null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            nameClient = login.nameClient;
            System.setOut(new PrintStream(System.out, true, "utf-8"));
            System.out.println("Client starting..." );
            Socket s = new Socket("25.96.213.238",4445 );   //"25.96.213.238"
            System.out.println("Connect to server...");
            Thread threadIn = new Thread(new SocketInputThread(s));
            Thread threadOut = new Thread(new SocketOutputThread(s, nameClient));
            threadOut.start();
            threadIn.start();
        } catch (UnknownHostException ex) {
            Logger.getLogger(Cli.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Cli.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        Cli cli = new Cli();
        cli.main2();
    }
}



class SocketInputThread implements Runnable{

    private Socket s = null;
    private Scanner in = null;
    private PrintWriter out = null;
    private boolean exit = true;
    private String inMessage = null;
    private String outMessage;
    private ArrayList<String> listClients;

    public SocketInputThread(Socket s ) {
        this.s = s;
        listClients = new ArrayList<String>();
    }

    @Override
    public void run() {
        System.out.println("Введите сообщение:");
        try {
            in = new Scanner(s.getInputStream());
            while(true){
                if(in.hasNext()){
                    inMessage = in.nextLine();
                    if (inMessage.charAt(0) == '*'){
                        inMessage = inMessage.substring(1);
                        if (inMessage.equals("exit")){
                            JFrameChat.showClients(listClients);
                            listClients.clear();
                        }
                        listClients.add(inMessage);
                    }else{
                        System.out.println( inMessage);
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(SocketInputThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}




class SocketOutputThread implements Runnable
{
    private String nameClient = "";
    private Socket s = null;
    private Scanner in = null;
    private PrintWriter out = null;
    private boolean exit = true;
    private String inMessage = null;
    private String outMessage = null;
    private boolean firstMess = true;

    public SocketOutputThread(Socket s, String name) {
        this.s = s;
        nameClient = name;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(s.getOutputStream());
            BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in,"utf-8"));
            while (true) {
                if(firstMess){
                    outMessage = "*" + nameClient;
                    firstMess = false;
                }else{
                outMessage = buffer.readLine();
                outMessage = nameClient + ":" + outMessage;

                System.out.println( outMessage);
                }
                out.println(outMessage);
                out.flush();
            }
        } catch (IOException ex) {
            Logger.getLogger(SocketOutputThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

