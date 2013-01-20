package com.stormcloud.ide.model.factory.exception;

/**
 *
 * @author martijn
 */
public class MavenModelFactoryException extends Exception {

    /**
     * Creates a new instance of
     * <code>MavenModelFactoryException</code> without detail message.
     */
    public MavenModelFactoryException() {
    }

    /**
     * Constructs an instance of
     * <code>MavenModelFactoryException</code> with the specified detail
     * message.
     *
     * @param msg the detail message.
     */
    public MavenModelFactoryException(String msg) {
        super(msg);
    }

    public MavenModelFactoryException(Throwable thrwbl) {
        super(thrwbl);
    }

    public MavenModelFactoryException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }
}
