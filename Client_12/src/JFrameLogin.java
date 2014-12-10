import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class JFrameLogin extends JFrame implements ActionListener {
    String nameClient;
    JTextField jtextLogin;
    JFrameChat jFrameChat;

    JFrameLogin() {
        super("Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(true);
        setLayout(new FlowLayout());
        setSize(400, 200);

        jtextLogin = new JTextField(20);
        add(jtextLogin);

        jFrameChat = new JFrameChat();

        jtextLogin.addActionListener(this);
        jtextLogin.setActionCommand("jtextLoginClicked");

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getActionCommand().equals("jtextLoginClicked")) {
//            nameClient = jtextLogin.getText();
            nameClient = jtextLogin.getText();
            setVisible(false);
            jFrameChat.setVisible(true);
        }
    }

    public static void main(String[] args) {
        JFrameLogin jFrameLogin = new JFrameLogin();
    }
}