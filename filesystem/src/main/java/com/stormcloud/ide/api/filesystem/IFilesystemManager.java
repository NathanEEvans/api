package com.stormcloud.ide.api.filesystem;

import com.stormcloud.ide.api.filesystem.exception.FilesystemManagerException;
import com.stormcloud.ide.model.filesystem.Filesystem;
import com.stormcloud.ide.model.filesystem.Find;
import com.stormcloud.ide.model.filesystem.FindResult;
import com.stormcloud.ide.model.filesystem.Item;
import com.stormcloud.ide.model.filesystem.Save;

/**
 * Provides the various methods to retrieve Filesystem items.
 *
 * @author martijn
 */
public interface IFilesystemManager {

    /**
     * Get the Templates used for creating new files or folder.
     *
     * @return Filesystem with appropriate Item children.
     * @throws FilesystemManagerException
     */
    Filesystem getFileTemplates()
            throws FilesystemManagerException;

    /**
     * Get the opened projects from the FileSystem 'as is'.
     *
     * Represents the opened projects without any project type specific
     * presentation, you get the bare FileSystem layout as it is.
     *
     * @return Filesystem with appropriate Item children.
     * @throws FilesystemManagerException
     */
    Filesystem getFilesystem()
            throws FilesystemManagerException;

    /**
     * Represents the folder structure starting at the given root.
     *
     * This method will only return folders, no files.
     *
     * @param root
     * @return Filesystem with appropriate Item children.
     * @throws FilesystemManagerException
     */
    Filesystem folderPicker(String root)
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
