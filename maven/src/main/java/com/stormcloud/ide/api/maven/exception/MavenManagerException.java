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
public class MavenManagerException extends Exception {

    /**
     * Creates a new instance of <code>ProjectManagerException</code> without detail message.
     */
    public MavenManagerException() {
    }

    /**
     * Constructs an instance of <code>ProjectManagerException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public MavenManagerException(String msg) {
        super(msg);
    }

    public MavenManagerException(Throwable thrwbl) {
        super(thrwbl);
    }

    public MavenManagerException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }
}
