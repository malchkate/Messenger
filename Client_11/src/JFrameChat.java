import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class JFrameChat extends JFrame{

    JFrameChat() {
        super("Chat");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new FlowLayout());
        setSize(400, 200);
    }
    public static void showClients(ArrayList<String> listCli){
        for(String str: listCli){
//            System.out.println(str);
        }
    }
}