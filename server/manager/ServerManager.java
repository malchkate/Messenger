package com.ays.javachat.server.manager;

import com.ays.javachat.common.datatypes.UserDetails;
import com.ays.javachat.common.globalconsts.Net;
import com.ays.javachat.common.messages.*;
import com.ays.javachat.server.database.ServerDatabase;
import com.ays.javachat.server.interfaces.ConnectionsListenerCallback;
import com.ays.javachat.server.interfaces.TransmitterCallback;
import com.ays.javachat.server.transmitter.ConnectionsListener;
import com.ays.javachat.server.transmitter.ServerTransmitter;

import java.net.Socket;
import java.util.Vector;


/**
 * Manages of all server's logic : creates neccessay classes, proccessings events, sends/receives/stores data<br>
 * See ConnectionsListenerCallback, TransmitterCallback  for more information *
 */
public class ServerManager implements ConnectionsListenerCallback, TransmitterCallback {
    private Vector clients = new Vector();
    private ConnectionsListener connectionsListener;
    private ServerDatabase serverDatabase;

    public ServerManager() {
        report("=================================================================");
       // report("Chat server by Yevgeny Sergeyev");
        report("Ver 1.1");
        //report("Contacts : tel(Israel). : +974-547945462. ICQ : 123845810. e-mail : yevgeny.sergeyev@gmail.com");
        report("=================================================================");

        serverDatabase = new ServerDatabase();
        String Params[] = {"user.ini", "ignores.dat"}; // ini file name
        serverDatabase.setDatabaseParams(Params);
        connectionsListener = new ConnectionsListener(this);
        if (connectionsListener.startListen(Net.DEFAULT_PORT))
            report("Server is started...");
        else
            report("Can't start server. Possible server is already started...");
    }

    private void report(String aString) {
        System.out.println(aString);
    }

    private void processReceivedObject(ServerTransmitter t, Message aMsg) {
        // LOGIN
        if (aMsg instanceof Login) {
            Login msg = (Login) aMsg;

            report("Someone from IP " + t.getIP() + ":" + t.getPort() + " trying to login as " + msg.login.UserName);

            ReplyLogin reply = new ReplyLogin(msg.login, Net.OK);

            // checking if this user was logged in...
            String s = t.getUserName();
            if (t.getUserName() != null) {
                reply.Status = Net.NOT_LOGGEDOUT;
                t.sendObject(reply);
                return;
            }

            // checking if user another user logged in with this user name
            ServerTransmitter transmitter;
            for (int i = 0; i < clients.size(); i++) {
                transmitter = (ServerTransmitter) clients.elementAt(i);
                if (transmitter.getUserName() != null)
                    if (transmitter.getUserName().equals(msg.login.UserName)) {
                        reply.Status = Net.SOMEONE_LOGGEDIN_ASIT;
                        t.sendObject(reply);
                        report("Someone from IP " + transmitter.getIP() + ":" + transmitter.getPort() + " already logged in as " + transmitter.getUserName());
                        return;
                    }
            }

            t.setUserName(null);

            reply.Status = serverDatabase.checkUserNamePassword(msg.login);
            if (reply.Status != Net.OK) {
                t.sendObject(reply);
                report("Not passed login/password validation");
                return;
            }

            // user successfully logged in
            t.setUserName(msg.login.UserName);
            t.sendObject(reply);

            report("Logged in successfully");

            // notifying all clients about new login
            UpdateUsersList updateUsersList = new UpdateUsersList(t.getUserName(), Net.USER_JOINED);
            for (int i = 0; i < clients.size(); i++) {
                transmitter = (ServerTransmitter) clients.elementAt(i);
                if (transmitter.getUserName() != null)
                    if (!transmitter.getUserName().equals(t.getUserName()))
                        transmitter.sendObject(updateUsersList);
            }


        }

        // LOGOUT
        if (aMsg instanceof Logout) {
            Logout msg = (Logout) aMsg;

            ReplyLogout reply = new ReplyLogout(Net.OK);

            if (t.getUserName() == null) {
                reply.Status = Net.NOT_LOGGED_IN;
                t.sendObject(reply);
                return;
            }

            report("User " + t.getUserName() + " from IP " + t.getIP() + ":" + t.getPort() + " logging out");

            // notifying all clients about user logout
            ServerTransmitter transmitter;
            UpdateUsersList updateUsersList = new UpdateUsersList(t.getUserName(), Net.USER_LEFT);
            for (int i = 0; i < clients.size(); i++) {
                transmitter = (ServerTransmitter) clients.elementAt(i);
                if (transmitter.getUserName() != null)
                    transmitter.sendObject(updateUsersList);
            }

            t.setUserName(null);
            t.sendObject(reply);
        }

        // REGISTER
        if (aMsg instanceof Register) {
            Register msg = (Register) aMsg;
            int iStatus;

            report("Someone from IP " + t.getIP() + ":" + t.getPort() + " trying to register user " + msg.login.UserName);

            // checking for data
            iStatus = serverDatabase.addUser(msg.login, msg.details);

            report("Register status : " + Net.describeMessage(iStatus));

            ReplyRegister reply = new ReplyRegister(msg.login, msg.details, iStatus);
            t.sendObject(reply);
        }

        // UNREGISTER
        if (aMsg instanceof UnRegister) {
            UnRegister msg = (UnRegister) aMsg;
            int iStatus;

            if (t.getUserName() == null)
                iStatus = Net.NOT_LOGGED_IN;
            else
                iStatus = serverDatabase.deleteUser(t.getUserName());

            report("Someone from IP " + t.getIP() + ":" + t.getPort() + " trying to unregister");
            report("Unregister status : " + Net.describeMessage(iStatus));

            ReplyUnregister reply = new ReplyUnregister(iStatus);
            t.sendObject(reply);
        }

        // GETUSERDETAILS
        if (aMsg instanceof GetUserDetails) {
            GetUserDetails msg = (GetUserDetails) aMsg;

            int iStatus;
            UserDetails details = new UserDetails();

            // if msg.UserName is null -> returnin details of the current user
            String sUserName = t.getUserName();
            if (msg.UserName != null)
                if (!msg.UserName.equals(""))
                    sUserName = msg.UserName;


            if (t.getUserName() == null)
                iStatus = Net.NOT_LOGGED_IN;
            else
                iStatus = serverDatabase.getUserDetails(sUserName, details);

            report("User " + t.getUserName() + " from IP " + t.getIP() + ":" + t.getPort() + " with user name " + t.getUserName() + " trying to get details of user " + msg.UserName);

            report("GetUserDetails status : " + Net.describeMessage(iStatus));

            ReplyGetUserDetails reply = new ReplyGetUserDetails(sUserName, details, iStatus);
            reply.InternalFlag = msg.InternalFlag;
            t.sendObject(reply);
        }

        // SETUSERDETAILS
        if (aMsg instanceof SetUserDetails) {
            SetUserDetails msg = (SetUserDetails) aMsg;

            ReplySetUserDetails reply = new ReplySetUserDetails(msg.details, Net.OK);

            if (t.getUserName() == null) {
                reply.Status = Net.NOT_LOGGED_IN;
                t.sendObject(reply);
                return;
            }

            report("User " + t.getUserName() + " from IP " + t.getIP() + ":" + t.getPort() + " with user name " + t.getUserName() + " trying to update his details");

            if (!serverDatabase.getPassword(t.getUserName()).equals(msg.Password)) {
                reply.Status = Net.BAD_PASSWORD;
                t.sendObject(reply);
                return;
            }

            reply.Status = serverDatabase.updateUserDetails(t.getUserName(), msg.details);

            report("Update status : " + Net.describeMessage(reply.Status));

            t.sendObject(reply);
        }

        // GET ONLINE USERS LIST
        if (aMsg instanceof GetOnlineUsersList) {
            GetOnlineUsersList msg = (GetOnlineUsersList) aMsg;

            ReplyGetOnlineUsersList reply = new ReplyGetOnlineUsersList(Net.OK);

            if (t.getUserName() == null) {
                reply.Status = Net.NOT_LOGGED_IN;
                t.sendObject(reply);
                return;
            }

            report("User " + t.getUserName() + " from IP " + t.getIP() + ":" + t.getPort() + " with user name " + t.getUserName() + " trying to get OnlineUsersList");

            // filling online users list array
            int iSize = 0;
            for (int i = 0; i < clients.size(); i++)
                if (((ServerTransmitter) clients.elementAt(i)).getUserName() != null)
                    iSize++;

            reply.array = new String[iSize];
            int k = 0;
            for (int i = 0; i < clients.size(); i++)
                if (((ServerTransmitter) clients.elementAt(i)).getUserName() != null)
                    reply.array[k++] = ((ServerTransmitter) clients.elementAt(i)).getUserName();

            t.sendObject(reply);
            report("GetOnlieUsersList status : " + Net.describeMessage(reply.Status));
        }

        // ClientText
        if (aMsg instanceof ClientText) {
            ClientText msg = (ClientText) aMsg;

            ReplyClientText reply = new ReplyClientText(t.getUserName(), msg.Text, 0);

            if (t.getUserName() == null) {
                reply.Status = Net.NOT_LOGGED_IN;
                t.sendObject(reply);
                report("Unknown user from IP " + t.getIP() + ":" + t.getPort() + " sent text : " + msg.Text);
                return;
            }

            String s = " to user " + msg.UserName;
            if (msg.UserName == null)
                s = " to the general room";
            report("User " + t.getUserName() + " from IP " + t.getIP() + ":" + t.getPort() + " sent text : " + msg.Text + s);

            ServerTransmitter transmitter;
            ServerText st = new ServerText();
            st.FromUser = t.getUserName();
            st.Text = msg.Text;
            if (msg.UserName == null) { // sending text to general room for all clients
                st.IsPrivate = false;
                for (int i = 0; i < clients.size(); i++) {
                    transmitter = (ServerTransmitter) (clients.elementAt(i));
                    if (transmitter.getUserName() != null)
                        if ((!transmitter.getUserName().equals(t.getUserName())) &&
                                (t.getUserName() != null))
                            transmitter.sendObject(st);
                }
                reply.Status = Net.OK;
                t.sendObject(reply);
            } else { // sending message to the one user ( private )
                st.IsPrivate = true;
                // searching for user...
                boolean bUserFound = false;
                for (int i = 0; i < clients.size(); i++) {
                    transmitter = (ServerTransmitter) clients.elementAt(i);
                    if (transmitter.getUserName().equals(msg.UserName)) {
                        // checking if your wants to speak with you
                        if (isUserIgnored(transmitter.getUserName(), t.getUserName())) {
                            reply.Status = Net.USER_DONTWANT_TOTALK;
                            t.sendObject(reply);
                            report("Text wasn't sent to the user. Reason : inogred");
                            return;
                        }
                        // sending text...
                        bUserFound = true;
                        reply.Status = transmitter.sendObject(st);
                        t.sendObject(reply);
                        break;
                    }
                }
                if (!bUserFound) {
                    reply.Status = Net.NO_SUCH_USER;
                    report("Text wasn't sent to the user. Reason : tarhet user is offline or doesn't exists");
                    t.sendObject(reply);
                }

            }

        }

        // GetUsersIgnoredByMe
        if (aMsg instanceof GetUsersIgnoredByMe) {
            GetUsersIgnoredByMe msg = (GetUsersIgnoredByMe) aMsg;

            ReplyGetUsersIgnoredByMe reply;
            if (t.getUserName() == null) {
                reply = new ReplyGetUsersIgnoredByMe(null, Net.NOT_LOGGED_IN);
                t.sendObject(reply);
                return;
            }

            report("User " + t.getUserName() + " from IP " + t.getIP() + ":" + t.getPort() + " trying to ignore list");

            Vector ignoredUsersList = new Vector();
            int iStatus = serverDatabase.getIgnoreUsersList(t.getUserName(), ignoredUsersList);

            reply = new ReplyGetUsersIgnoredByMe(ignoredUsersList, iStatus);
            t.sendObject(reply);
        }

        // IgnoreUsers
        if (aMsg instanceof IgnoreUsers) {
            IgnoreUsers msg = (IgnoreUsers) aMsg;

            ReplyIgnoreUsers reply;

            if (t.getUserName() == null) {
                reply = new ReplyIgnoreUsers(msg.ignoredUsersList, msg.OverwriteExistingList, Net.NOT_LOGGED_IN);
                t.sendObject(reply);
                return;
            }

            report("User " + t.getUserName() + " from IP " + t.getIP() + ":" + t.getPort() + " trying to ignore some another user(s)");

            int iStatus = serverDatabase.setIgnoreUsersList(t.getUserName(), msg.ignoredUsersList, msg.OverwriteExistingList);

            reply = new ReplyIgnoreUsers(msg.ignoredUsersList, msg.OverwriteExistingList, iStatus);
            t.sendObject(reply);
        }
    }

    public void someoneConnected(Object aConnectionBridgeObject) {
        // checking for object : must be a socket object
        if (!(aConnectionBridgeObject instanceof Socket))
            return;

        // now we have socket object
        // creating new transmitter
        ServerTransmitter serverTransmitter = new ServerTransmitter(this);
        clients.add(serverTransmitter);
        serverTransmitter.setupConnectionBridgeObject(aConnectionBridgeObject);

        report("Connection accepted from IP " + serverTransmitter.getIP() + ":" + serverTransmitter.getPort());

        serverTransmitter.startDataExchange();
    }

    public void receiveObject(Object aTransmitter, Object aData) {
        if (!(aTransmitter instanceof ServerTransmitter))
            return;

        if (!(aData instanceof Message))
            return;

        processReceivedObject((ServerTransmitter) aTransmitter, (Message) aData);
    }

    public void connectionDown(Object aTransmitter) {
        report("Connection was terminated");

        // searching & removing from client list
        for (int i = 0; i < clients.size(); i++)
            if (aTransmitter == clients.elementAt(i)) {
                ServerTransmitter t = (ServerTransmitter) clients.elementAt(i);
                // removing from list
                clients.remove(i);
                // notifiying all clients
                UpdateUsersList updateUsersList = new UpdateUsersList(t.getUserName(), Net.USER_LEFT);
                for (int j = 0; j < clients.size(); j++)
                    ((ServerTransmitter) clients.elementAt(j)).sendObject(updateUsersList);

                return;
            }
    }

    private boolean isUserIgnored(String aReceiver, String aSender) {
        Vector v = new Vector();
        serverDatabase.getIgnoreUsersList(aReceiver, v);
        for (int i = 0; i < v.size(); i++)
            if (v.get(i).equals(aSender))
                return true;

        return false;
    }

}
