package com.stormcloud.ide.api.filesystem;

/*
 * #%L Stormcloud IDE - API - Filesystem %% Copyright (C) 2012 - 2013 Stormcloud
 * IDE %% This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/gpl-3.0.html>. #L%
 */
import com.stormcloud.ide.api.filesystem.exception.FilesystemManagerException;
import com.stormcloud.ide.model.filesystem.Filesystem;
import com.stormcloud.ide.model.filesystem.Find;
import com.stormcloud.ide.model.filesystem.FindResult;
import com.stormcloud.ide.model.filesystem.Item;
import com.stormcloud.ide.model.filesystem.Save;

/**
 *
 * @author martijn
 */
public interface IFilesystemManager {

    /**
     *
     * @return @throws FilesystemManagerException
     */
    Filesystem bare()
            throws FilesystemManagerException;

    /**
     *
     * @param root
     * @return
     * @throws FilesystemManagerException
     */
    Filesystem folderPicker(String root)
            throws FilesystemManagerException;

    /**
     *
     * @return @throws FilesystemManagerException
     */
    Filesystem getFileTemplates()
            throws FilesystemManagerException;

    /**
     *
     * @return @throws FilesystemManagerException
     */
    Item[] availableProjects()
            throws FilesystemManagerException;

    /**
     * List the a directory on the server. Returns an xml string containing the
     * directory structure.
     *
     * @param filePath
     * @param filter
     * @return
     * @throws FilesystemManagerException
     */
    Filesystem list(boolean open)
            throws FilesystemManagerException;

    /**
     *
     * @param filePath
     * @return
     * @throws FilesystemManagerException
     */
    int open(String filePath)
            throws FilesystemManagerException;

    /**
     *
     * @param filePath
     * @return
     * @throws FilesystemManagerException
     */
    int close(String filePath)
            throws FilesystemManagerException;

    /**
     * Save a file
     *
     * @param filePath
     * @param contents
     * @return
     * @throws FilesystemManagerException
     */
    String save(Save save)
            throws FilesystemManagerException;

    /**
     *
     * @param filePath
     * @param fileType
     * @return
     * @throws FilesystemManagerException
     */
    String create(
            String filePath,
            String fileType)
            throws FilesystemManagerException;

    /**
     * Delete a file
     *
     * @param filePath
     * @return
     * @throws FilesystemManagerException
     */
    int delete(String filePath)
            throws FilesystemManagerException;

    /**
     *
     * @param find
     * @return
     * @throws FilesystemManagerException
     */
    FindResult find(Find find)
            throws FilesystemManagerException;

    /**
     * Retrieve a file.
     *
     * @param filePath
     * @return
     * @throws FilesystemManagerException
     */
    String get(String filePath)
            throws FilesystemManagerException;

    /**
     *
     * @param filePath
     * @return
     * @throws FilesystemManagerException
     */
    String getBinary(String filePath)
            throws FilesystemManagerException;

    /**
     *
     * @return @throws FilesystemManagerException
     */
    Filesystem viewTrash()
            throws FilesystemManagerException;

    /**
     *
     * @return @throws FilesystemManagerException
     * @throws FilesystemManagerException
     */
    int emptyTrash()
            throws FilesystemManagerException;

    /**
     *
     * @return @throws FilesystemManagerException
     * @throws FilesystemManagerException
     */
    int hasTrash()
            throws FilesystemManagerException;

    /**
     *
     * @param srcFilePath
     * @param destFilePath
     * @return
     * @throws FilesystemManagerException
     */
    int copy(String srcFilePath, String destFilePath)
            throws FilesystemManagerException;

    /**
     *
     * @param srcFilePath
     * @param destFilePath
     * @return
     * @throws FilesystemManagerException
     */
    int move(String srcFilePath, String destFilePath)
            throws FilesystemManagerException;
}
