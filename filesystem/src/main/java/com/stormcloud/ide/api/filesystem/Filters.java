package com.stormcloud.ide.api.filesystem;

/*
 * #%L
 * Stormcloud IDE - API - Filesystem
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

import java.io.File;
import java.io.FilenameFilter;

/**
 *
 * @author martijn
 */
public class Filters {

    public static FilenameFilter getProjectFilter() {

        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File file, String string) {

                // don't show hidden files
                if (string.startsWith(".")) {
                    return false;
                }

                // dont come up with build dirs
                if (string.equals("target")) {
                    return false;
                }

                return true;
            }
        };


        return filter;
    }
}
