package com.ays.javachat.server.interfaces;


/**
 * All communication objects must implemet this interface ( i.e. what communication object can do ) *
 */
public interface TransmitterCapables {
    /**
     * Setups communication object. For example : socket*
     */
    public void setupConnectionBridgeObject(Object aObject); // for example, if you are using sockets, you need to pass socket object

    /**
     * Sends data ( object ) *
     */
    public int sendObject(Object o);

    /**
     * Terminates connections *
     */
    public void disconnect();

    /**
     * Returns IP address of the current connection *
     */
    public String getIP();

    /**
     * Returns Port of the current connection *
     */
    public int getPort();

    /**
     * Start to listen for incoming data *
     */
    public void startDataExchange();

    /**
     * transmitter object stores user name which who has linked. User setuserName()/getUserName() functions to get/set this value *
     */
    public String getUserName();

    /**
     * transmitter object stores user name which who has linked. User setuserName()/getUserName() functions to get/set this value *
     */
    public void setUserName(String aUserName);
}
