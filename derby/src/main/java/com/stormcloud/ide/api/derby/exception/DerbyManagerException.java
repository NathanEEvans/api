package com.stormcloud.ide.api.derby.exception;

/**
 *
 * @author martijn
 */
public class DerbyManagerException extends Exception {

    /**
     * Creates a new instance of
     * <code>MavenManagerException</code> without detail message.
     */
    public DerbyManagerException() {
    }

    /**
     * Constructs an instance of
     * <code>MavenManagerException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public DerbyManagerException(String msg) {
        super(msg);
    }

    public DerbyManagerException(Throwable thrwbl) {
        super(thrwbl);
    }

    public DerbyManagerException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }
}
