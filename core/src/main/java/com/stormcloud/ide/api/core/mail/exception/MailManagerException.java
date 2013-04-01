package com.stormcloud.ide.api.core.mail.exception;

/**
 *
 * @author martijn
 */
@SuppressWarnings("serial")
public class MailManagerException extends Exception {

    /**
     * Creates a new instance of
     * <code>MailManagerException</code> without detail message.
     */
    public MailManagerException() {
    }

    /**
     * Constructs an instance of
     * <code>MailManagerException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public MailManagerException(String msg) {
        super(msg);
    }

    public MailManagerException(Throwable thrwbl) {
        super(thrwbl);
    }

    public MailManagerException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }
}
