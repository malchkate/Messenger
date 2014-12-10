import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class JFrameChat extends JFrame{

    static JTextArea jTextAreaListOfLogin;
    static JTextArea jTextAreaListOfMessage;
    static JTextField jTextFieldMessage;

    JFrameChat() {
        super("Chat");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(true);
        setLayout(new FlowLayout());
        setSize(400, 300);

        jTextAreaListOfMessage = new JTextArea(10, 20);
        jTextAreaListOfLogin = new JTextArea(10, 8);
        jTextFieldMessage = new JTextField(15);

        add(jTextAreaListOfMessage);
        add(jTextAreaListOfLogin);
        add(jTextFieldMessage);
    }

    public static void showClients(ArrayList<String> listCli){
        while(true) {
            try {
                Thread.sleep(1000);
                jTextAreaListOfLogin.setText(null);
                for (String str : listCli) {
                    jTextAreaListOfLogin.append(str + "\n");
                    System.out.println(str);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void showMessage() {
        while(true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            jTextAreaListOfMessage.append(SocketOutputThread.getoutMessage() + "\n");
        }
    }
}