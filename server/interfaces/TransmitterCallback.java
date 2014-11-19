package com.ays.javachat.server.interfaces;

/**
 * Using by communication object ( transmitter ).<br>*
 */
public interface TransmitterCallback {
    /**
     * When transmitter got new data ( object ), who calls for this func.<br>aTransmitter - is a pointer to the self ( it can be a socket for example ) aData - is a received data *
     */
    public void receiveObject(Object aTransmitter, Object aData); // aTransmitter - is the pointer to this ( from which transmitter data was received )

    /**
     * Calls when exception occured during listening for the data *
     */
    public void connectionDown(Object aTransmitter); // aTransmitter - is the pointer to this ( from which transmitter data was received )
}
