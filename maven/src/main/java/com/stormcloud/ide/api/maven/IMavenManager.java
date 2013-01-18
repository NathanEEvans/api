package com.stormcloud.ide.api.maven;

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

import com.stormcloud.ide.api.core.entity.ArchetypeCatalog;
import com.stormcloud.ide.api.maven.exception.MavenManagerException;
import com.stormcloud.ide.model.maven.Project;

/**
 * The ProjectManager manages all interaction with the files on disk in a maven
 * project.
 *
 *
 * @author martijn
 */
public interface IMavenManager {

    ArchetypeCatalog[] getCatalog()
            throws MavenManagerException;

    /**
     * Execute maven archetype to create a new project.
     *
     *
     * @param projectId
     * @return
     * @throws ProjectManagerException
     */
    int createProject(Project projectId)
            throws MavenManagerException;

    int renameProject()
            throws MavenManagerException;

    /**
     * Compile a project
     *
     * @param command
     * @param filePath
     * @throws ProjectManagerException
     */
    int execute(String command, String filePath)
            throws MavenManagerException;
}
