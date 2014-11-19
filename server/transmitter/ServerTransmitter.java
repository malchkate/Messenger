package com.ays.javachat.server.transmitter;

import com.ays.javachat.common.globalconsts.Net;
import com.ays.javachat.server.interfaces.TransmitterCallback;
import com.ays.javachat.server.interfaces.TransmitterCapables;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * This class is responsible about data exchange between client and server. See ServerTransmiterCapables for more information *
 */
public class ServerTransmitter extends Thread implements TransmitterCapables {
    private String UserName = null; // null means that user didn't do login
    private Socket socket = null;
    private TransmitterCallback transmitterCallback = null;
    private ObjectOutputStream outputStream = null;

    /**
     * Creator of this class must pass a pointer to a object which implemented interface TransmitterCallback *
     */
    public ServerTransmitter(TransmitterCallback aTranasmitterCallback) {
        transmitterCallback = aTranasmitterCallback;
    }

    public void startDataExchange() { // !!! NOTE !!! you can't call by this func more than 1nce !!! ( thread wil not started )
        if (socket == null)
            return;

        start();
    }

    // returns user name accociated with this socket
    // null means that user not logged in
    public synchronized String getUserName() {
        return UserName;
    }

    public synchronized void setUserName(String aUserName) {
        UserName = aUserName;
    }

    public void setupConnectionBridgeObject(Object aObject) {
        // in this content aObject must be a Socket object
        if (!(aObject instanceof Socket))
            return;

        socket = (Socket) aObject;
        try {
            outputStream = new ObjectOutputStream(socket.getOutputStream());
        }
        catch (Exception e) {
            // save_log() ; can''t create object output stream
            socket = null;
            return;
        }
    }

    public int sendObject(Object o) {
        if (socket == null)
            return Net.NO_TCP_CONNECTION;

        try {
            outputStream.writeObject(o);
        }
        catch (Exception e) {
            // save_log() ;
            return Net.CONNECTION_PROBLEM;
        }

        return Net.OK;
    }

    public void disconnect() {
        if (socket != null)
            try {
                outputStream.close();
                socket.close();
            }
            catch (Exception e) {
            }

        stop();
    }

    public void run() {
        if (transmitterCallback == null)
            ; // save_log() ;

        if (socket == null)
            return;

        Object o;
        ObjectInputStream objectInputStream;

        try {
            objectInputStream = new ObjectInputStream(socket.getInputStream());
        }
        catch (Exception e) {
            // save_log() ;
            return;
        }

        while (true) {
            try {
                o = objectInputStream.readObject();
            }
            catch (Exception e) {
                // save_log() ;
                transmitterCallback.connectionDown(this);
                return;
            }

            transmitterCallback.receiveObject(this, o);
        }
    }

    public String getIP() {
        if (socket == null)
            return "";
        else
            return socket.getInetAddress().toString();
    }


    public int getPort() {
        if (socket == null)
            return 0;
        else
            return socket.getPort();
    }
}
