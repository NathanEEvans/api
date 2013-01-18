package com.stormcloud.ide.api.git;

/*
 * #%L
 * Stormcloud IDE - API - Git
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
import com.stormcloud.ide.api.git.exception.GitManagerException;
import com.stormcloud.ide.api.git.model.IndexState;
import java.io.File;
import org.eclipse.jgit.dircache.DirCache;

/**
 *
 * @author martijn
 */
public interface IGitManager {

    String cloneRemoteRepository(String uri) throws GitManagerException;

    IndexState getIndexState(String repository) throws GitManagerException;

    String getStatus(String file, String userHome) throws GitManagerException;

    String getStatus(File file, String userHome) throws GitManagerException;

    void commit(String repository, String message, String[] files, boolean all) throws GitManagerException;

    DirCache add(String repository, String pattern) throws GitManagerException;

    void log(String repository) throws GitManagerException;
}
