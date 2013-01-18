package com.stormcloud.ide.api.maven.exception;

/*
 * #%L
 * Stormcloud IDE - API - Maven
 * %%
 * Copyright (C) 2012 - 2013 Stormcloud IDE
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

/**
 *
 * @author martijn
 */
public class LogManagerException extends Exception {

    /**
     * Creates a new instance of <code>MavenManagerException</code> without detail message.
     */
    public LogManagerException() {
    }

    /**
     * Constructs an instance of <code>MavenManagerException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public LogManagerException(String msg) {
        super(msg);
    }

    public LogManagerException(Throwable thrwbl) {
        super(thrwbl);
    }

    public LogManagerException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }
}
