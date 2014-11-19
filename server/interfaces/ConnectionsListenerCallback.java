package com.ays.javachat.server.interfaces;
/**
 *
 * <p>Title: </p>
 *
 * <p>Description: Provides for global interfaces</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */


/**
 * This interface is using to pass communication object ( socket for example ) when someone connected to the server.<br>
 * For example : object #1 listens for incoming connections. When someone connected, object #1
 * calls someoneConnected() func from object #2 and passes connectionBridgeObject  *
 */
public interface ConnectionsListenerCallback {
    public void someoneConnected(Object aConnectionBrigdeObject); // in case of the sockets, aConnectionBridgeObject is a Socket object
}
