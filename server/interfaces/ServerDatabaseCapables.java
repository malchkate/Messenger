package com.ays.javachat.server.interfaces;

import com.ays.javachat.common.datatypes.LoginData;
import com.ays.javachat.common.datatypes.UserDetails;

import java.util.Vector;

/**
 * Server storage classes must implement this interface which permits to store user's data *
 */
public interface ServerDatabaseCapables {
    /**
     * Settings up database startup parameters.<br> In case of using of the files ( instead of database ) you can pass as a aParams[] file names for store *
     */
    public void setDatabaseParams(String aParams[]);

    /**
     * Validates user name and password.<br> Returns 0 in OK case, and error code <0 in case of errors*
     */
    public int checkUserNamePassword(LoginData aLogin);

    /**
     * Changing password.<br>. Returns 0 in OK case, and error code <0 in case of errors *
     */
    public int changePassword(LoginData aLogin);

    /**
     * Returns password for aUserName.<br> Returns 0 in OK case, and error code <0 in case of errors *
     */
    public String getPassword(String aUserName);

    /**
     * Adds new user to the database.<br> Returns 0 in OK case, and error code <0 in case of errors *
     */
    public int addUser(LoginData aLogin, UserDetails aDetails);

    /**
     * Deletes user from the database.<br> Returns 0 in OK case, and error code <0 in case of errors *
     */
    public int deleteUser(String aUserName);

    /**
     * Updates user information in the database.<br> Returns 0 in OK case, and error code <0 in case of errors*
     */
    public int updateUserDetails(String aUserName, UserDetails aDetails);

    /**
     * Returns user information from the database.<br> Returns 0 in OK case, and error code <0 in case of errors*
     */
    public int getUserDetails(String aUserName, UserDetails aUserDetails);

    // must be synch !!!!
    /**
     * Stores ingnore list to the database.<br>Note : this functions must be sync.<br> Returns 0 in OK case, and error code <0 in case of errors *
     */
    public int setIgnoreUsersList(String aUserName, Vector aIgnoredUsersList, boolean OverwriteExistingList);

    /**
     * Loads ingnore list from the database.<br>Note : this functions must be sync.<br> Returns 0 in OK case, and error code <0 in case of errors *
     */
    public int getIgnoreUsersList(String aUserName, Vector aIgnoredUsersList);
}
