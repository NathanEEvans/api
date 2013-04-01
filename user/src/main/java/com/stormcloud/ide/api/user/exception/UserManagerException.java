package com.stormcloud.ide.api.user.exception;

/**
 *
 * @author martijn
 */
@SuppressWarnings("serial")
public class UserManagerException extends Exception {

    /**
     * Creates a new instance of
     * <code>UserManagerException</code> without detail message.
     */
    public UserManagerException() {
    }

    /**
     * Constructs an instance of
     * <code>UserManagerException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public UserManagerException(String msg) {
        super(msg);
    }

    public UserManagerException(Throwable thrwbl) {
        super(thrwbl);
    }

    public UserManagerException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }
}
