package com.stormcloud.ide.api.git;

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

    public boolean isModified(String repository) throws GitManagerException;

    String getStatus(String file, String userHome) throws GitManagerException;

    String getStatus(File file, String userHome) throws GitManagerException;

    void commit(String repository, String message, String[] files, boolean all) throws GitManagerException;

    DirCache add(String repository, String pattern) throws GitManagerException;

    void log(String repository) throws GitManagerException;
}
