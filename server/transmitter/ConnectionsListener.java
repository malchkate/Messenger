package com.ays.javachat.server.transmitter;
// this class listens for incoming connection

import com.ays.javachat.server.interfaces.ConnectionsListenerCallback;
import com.ays.javachat.server.interfaces.ConnectionsListenerCapables;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class listens for incoming TCP connections. See ConnectionsListenerCapables for more information *
 */
public class ConnectionsListener extends Thread implements ConnectionsListenerCapables {
    private int Port = 0;
    private ServerSocket serverSocket = null;
    private ConnectionsListenerCallback connectionsListenerCallback;

    /**
     * Creator of this class must pass a pointer to the object which implemented interface ConnectionsListenerCallback *
     */
    public ConnectionsListener(ConnectionsListenerCallback aConnectionsListenerCallback) {
        connectionsListenerCallback = aConnectionsListenerCallback;
    }

    public boolean startListen(int aPort) { // NOTE !!! you can't user by this func more than 1nce !!!
        Port = aPort;

        try {
            serverSocket = new ServerSocket(Port);
            start();
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public void stopListen() {
        stop();
        try {
            serverSocket.close();
        }
        catch (Exception e) {
            // save_log() ;
        }
    }

    public void run() {
        Socket socket;
        while (true)
            try {
                socket = serverSocket.accept();
                connectionsListenerCallback.someoneConnected(socket);
            }
            catch (Exception e) {
                // save_log() ;
                return;
            }
    }
}
