package com.stormcloud.ide.api.core.dao.exception;

/**
 *
 * @author martijn
 */
public class StormcloudDaoException extends Exception {

    /**
     * Creates a new instance of
     * <code>StormcloudDaoException</code> without detail message.
     */
    public StormcloudDaoException() {
    }

    /**
     * Constructs an instance of
     * <code>StormcloudDaoException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public StormcloudDaoException(String msg) {
        super(msg);
    }

    public StormcloudDaoException(Throwable thrwbl) {
        super(thrwbl);
    }

    public StormcloudDaoException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }
}
