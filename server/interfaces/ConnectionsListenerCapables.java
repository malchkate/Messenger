package com.ays.javachat.server.interfaces;

/**
 * Starts and stops listen for incoming connections *
 */
public interface ConnectionsListenerCapables {
    /**
     * Starts listen for incoming connections *
     */
    public boolean startListen(int aPort);

    public void stopListen();
}
