package com.ays.javachat.server.database;

import com.ays.javachat.common.datatypes.IgnoredUsers2;
import com.ays.javachat.common.datatypes.LoginData;
import com.ays.javachat.common.datatypes.UserDetails;
import com.ays.javachat.common.globalconsts.Net;
import com.ays.javachat.server.interfaces.ServerDatabaseCapables;

import java.io.*;
import java.util.Properties;
import java.util.Vector;


/**
 * Stores all user data. See ServerDatabaseCapables interface for more information *
 */
public class ServerDatabase implements ServerDatabaseCapables {
    private String UsersFileName = null;
    private String IgnoreFileName = null;
    private Properties p = new Properties();

    private final String DATA_DIR = "./DATA/";

    public ServerDatabase() {
    }

    private String getUsersFileName() {
        // is DATA_DIR exists ?
        File dataDir = new File(DATA_DIR);
        if (!dataDir.isDirectory()) {
            // trying to create it...
            try {
                if (!dataDir.mkdirs()) {
                    return UsersFileName;
                }
            } catch (Exception e) {
                return UsersFileName;
            }
        }

        return DATA_DIR + UsersFileName;
    }

    private String getIgnoreFileName() {
        return DATA_DIR + IgnoreFileName;
    }

    private void createFileIfNotExists(String aFileName) {
        File f = new File(aFileName);
        if (!f.exists()) {
            try {
                FileOutputStream stream = new FileOutputStream(aFileName);
                stream.close();
            }
            catch (Exception e) {

            }
        }
    }


    public void setDatabaseParams(String aParams[]) {
        if (aParams != null) {
            if (aParams.length >= 1)
                UsersFileName = aParams[0];
            if (aParams.length >= 2)
                IgnoreFileName = aParams[1];
        }
    }

    public int checkUserNamePassword(LoginData aLogin) {
        try {
            if (!aLogin.isDataValid())
                return Net.INVALID_USERNAME_PASS;

            createFileIfNotExists(getUsersFileName());

            p.load(new FileInputStream(getUsersFileName()));
            String s = p.getProperty(aLogin.UserName);
            if (s == null)
                return Net.NO_SUCH_USER;
            if (s.equals(aLogin.Password))
                return Net.OK;
            else
                return Net.BAD_PASSWORD;
        }
        catch (Exception e) {
            System.out.print(e);
            return Net.INTERNAL_ERROR;
        }
    }

    public int changePassword(LoginData aLogin) {
        if (!aLogin.isDataValid())
            return Net.INVALID_USERNAME_PASS;

        createFileIfNotExists(getUsersFileName());

        try {
            p.load(new FileInputStream(getUsersFileName()));
            String s = p.getProperty(aLogin.UserName);
            if (s == null)
                return Net.USER_DOESNT_EXISTS;

            p.setProperty(aLogin.UserName, aLogin.Password);
            p.store(new FileOutputStream(getUsersFileName()), null);

            return Net.OK;
        }
        catch (Exception e) {
            return Net.INTERNAL_ERROR;
        }
    }

    public String getPassword(String aUserName) {
        try {
            createFileIfNotExists(getUsersFileName());

            p.load(new FileInputStream(getUsersFileName()));
            return p.getProperty(aUserName);
        }
        catch (Exception e) {
            System.out.print(e);
            return "";
        }
    }

    public int addUser(LoginData aLogin, UserDetails aDetails) {
        try {
            if (!aLogin.isDataValid())
                return Net.INVALID_USERNAME_PASS;
            if (!aDetails.isDataValid())
                return Net.BAD_USER_DETAILS;

            createFileIfNotExists(getUsersFileName());

            int iResult = updateUserDetails(aLogin.UserName, aDetails);
            if (iResult != Net.OK)
                return iResult;

            p.load(new FileInputStream(getUsersFileName()));
            String s = p.getProperty(aLogin.UserName);
            if (s != null)
                return Net.USER_ALREADY_EXISTS;

            p.setProperty(aLogin.UserName, aLogin.Password);
            p.store(new FileOutputStream(getUsersFileName()), null);

            return Net.OK;
        }
        catch (Exception e) {
            return Net.INTERNAL_ERROR;
        }
    }

    public int deleteUser(String aUserName) {
        return Net.OK;
    }

    public int updateUserDetails(String aUserName, UserDetails aDetails) {
        try {
            FileOutputStream fileStream = new FileOutputStream(DATA_DIR + aUserName);
            ObjectOutputStream objectStream = new ObjectOutputStream(fileStream);
            objectStream.writeObject(aDetails);
            objectStream.flush();
            objectStream.close();
        }
        catch (Exception e) {
            return Net.INTERNAL_ERROR;
        }
        return Net.OK;
    }

    public int getUserDetails(String aUserName, UserDetails aUserDetails) {
        try {
            FileInputStream fileStream = new FileInputStream(DATA_DIR + aUserName);
            ObjectInputStream objectStream = new ObjectInputStream(fileStream);
            UserDetails d = (UserDetails) objectStream.readObject();
            aUserDetails.copy(d);
            objectStream.close();
        }
        catch (Exception e) {
            return Net.INTERNAL_ERROR;
        }
        return Net.OK;
    }

    // must be synch !!!!
    public synchronized int setIgnoreUsersList(String aUserName, Vector aUsersList, boolean aOverwriteExistingList) {

        if (aUsersList == null)
            return Net.OK;

        IgnoredUsers2 ignoredUsers;

        createFileIfNotExists(getIgnoreFileName());

        // trying to load existing data
        try {
            FileInputStream fileStream = new FileInputStream(getIgnoreFileName());
            ObjectInputStream objectStream = new ObjectInputStream(fileStream);
            ignoredUsers = (IgnoredUsers2) objectStream.readObject();
            objectStream.close();
        }
        catch (Exception e) {
            // no data. creating new data object ( kotoriy doljen bil zagruzitsa if file )
            ignoredUsers = new IgnoredUsers2();
        }

        // getting users which places to the ignore list
        Vector v = (Vector) ignoredUsers.users.get(aUserName);

        if (v == null) { // ignore list is empty : creating
            v = new Vector();
            ignoredUsers.users.put(aUserName, v); // adding
        } else if (aOverwriteExistingList)
            v.clear();

        // copying ignore list from the source message
        boolean b;
        int j;
        for (int i = 0; i < aUsersList.size(); i++) {
            b = false;
            for (j = 0; j < v.size(); j++)
                if (v.get(j).equals(aUsersList.get(i))) { // checking for dublicate values
                    b = true;
                    break; ////
                }

            if (!b) // if there are no dublicates - adding
                v.add(aUsersList.get(i));
        }

        // now saving...
        try {
            FileOutputStream fileStream = new FileOutputStream(getIgnoreFileName());
            ObjectOutputStream objectStream = new ObjectOutputStream(fileStream);
            objectStream.writeObject(ignoredUsers);
            objectStream.flush();
            objectStream.close();
        }
        catch (Exception e) {
            return Net.INTERNAL_ERROR;
        }

        return Net.OK;
    }

    // must be synch !!!!
    // vector object must created before
    public int getIgnoreUsersList(String aUserName, Vector aUsersList) {

        if (aUsersList == null)
            return Net.OK;

        aUsersList.clear();

        IgnoredUsers2 ignoredUsers;

        try {
            ignoredUsers = null;
            FileInputStream fileStream = new FileInputStream(getIgnoreFileName());
            ObjectInputStream objectStream = new ObjectInputStream(fileStream);
            ignoredUsers = (IgnoredUsers2) objectStream.readObject();
            objectStream.close();
        }
        catch (Exception e) {
            ignoredUsers = new IgnoredUsers2();
        }

        Vector v = (Vector) ignoredUsers.users.get(aUserName);

        if (v == null)
            return Net.OK;

        for (int i = 0; i < v.size(); i++)
            aUsersList.add(v.get(i));

        return Net.OK;
    }
}
