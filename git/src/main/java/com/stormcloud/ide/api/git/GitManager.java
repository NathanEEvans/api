package com.stormcloud.ide.api.git;

/*
 * #%L Stormcloud IDE - API - Git %% Copyright (C) 2012 - 2013 Stormcloud IDE %%
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/gpl-3.0.html>. #L%
 */
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.stormcloud.ide.api.core.dao.IStormCloudDao;
import com.stormcloud.ide.api.core.entity.User;
import com.stormcloud.ide.api.core.remote.RemoteUser;
import com.stormcloud.ide.api.git.exception.GitManagerException;
import com.stormcloud.ide.api.git.model.IndexState;
import com.stormcloud.ide.model.user.UserSettings;
import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.IndexDiff;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.WorkingTreeIterator;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.eclipse.jgit.util.FS;

/**
 *
 * @author martijn
 */
public class GitManager implements IGitManager {

    private Logger LOG = Logger.getLogger(getClass());
    private IStormCloudDao dao;

    @Override
    public String cloneRemoteRepository(String uri)
            throws GitManagerException {


        SshSessionFactory.setInstance(new JschConfigSessionFactory() {

            @Override
            protected void configure(Host hc, Session session) {

                try {

                    session.setConfig("StrictHostKeyChecking", "no");

                    String keyLocation = RemoteUser.get().getSetting(UserSettings.SSH_HOME) + "/" + RemoteUser.get().getUserName();
                    String knownHosts = RemoteUser.get().getSetting(UserSettings.SSH_HOME) + "/known_hosts";

                    LOG.debug("KeyLoacation " + keyLocation);
                    LOG.debug("KnownHosts " + knownHosts);


                    JSch jsch = getJSch(hc, FS.DETECTED);
                    jsch.addIdentity(keyLocation);
                    jsch.setKnownHosts(knownHosts);

                } catch (JSchException e) {
                    LOG.error(e);
                }
            }
        });


        CloneCommand cloneCommand = Git.cloneRepository();

        int start = uri.lastIndexOf("/");

        if (start == -1) {
            start = uri.lastIndexOf(":");
        }

        String folder = uri.substring(start + 1, uri.length());

        LOG.info("Cloning : " + uri + " into " + folder);

        cloneCommand.setDirectory(new File(RemoteUser.get().getSetting(UserSettings.PROJECT_FOLDER) + "/" + folder));
        cloneCommand.setURI(uri);

        try {

            cloneCommand.call();

        } catch (GitAPIException e) {
            LOG.error(e);
            throw new GitManagerException(e);
        }

        return "0";
    }

    @Override
    public IndexState getIndexState(String repository) throws GitManagerException {

        IndexState indexState = new IndexState();

        try {

            Git git = Git.open(new File(repository));

            IndexDiff diff =
                    new IndexDiff(
                    git.getRepository(),
                    "HEAD",
                    new FileTreeIterator(git.getRepository()));

            diff.diff();

            indexState.setAdded(diff.getAdded());
            indexState.setAssumeUnchanged(diff.getAssumeUnchanged());
            indexState.setChanged(diff.getChanged());
            indexState.setConflicting(diff.getConflicting());
            indexState.setIgnoredNotInIndex(diff.getIgnoredNotInIndex());
            indexState.setMissing(diff.getMissing());
            indexState.setModified(diff.getModified());
            indexState.setRemoved(diff.getRemoved());
            indexState.setUntracked(diff.getUntracked());
            indexState.setUntrackedFolders(diff.getUntrackedFolders());

        } catch (IOException e) {
            LOG.error(e);
            throw new GitManagerException(e);
        }

        return indexState;
    }

    @Override
    public boolean isModified(String repository)
            throws GitManagerException {

        try {

            Git git = Git.open(new File(repository));

            IndexDiff diff =
                    new IndexDiff(
                    git.getRepository(),
                    "HEAD",
                    new FileTreeIterator(git.getRepository()));

            diff.diff();

            if (!diff.getAdded().isEmpty()) {
                return true;
            }

            if (!diff.getChanged().isEmpty()) {
                return true;
            }

            if (!diff.getMissing().isEmpty()) {
                return true;
            }

            if (!diff.getModified().isEmpty()) {
                return true;
            }

            if (!diff.getRemoved().isEmpty()) {
                return true;
            }

            if (!diff.getUntracked().isEmpty()) {
                return true;
            }

            if (!diff.getUntrackedFolders().isEmpty()) {
                return true;
            }

        } catch (IOException e) {
            LOG.error(e);
            throw new GitManagerException(e);
        }

        return false;
    }

    @Override
    public void log(String repo) throws GitManagerException {

        try {
            File gitDir = new File(repo + "/.git");
            LOG.info("gitDir:" + gitDir);
            Repository repository = new FileRepository(gitDir);

            WorkingTreeIterator fileTreeIterator =
                    new FileTreeIterator(repository);

            IndexDiff indexDiff = new IndexDiff(repository, Constants.HEAD, fileTreeIterator);

            boolean hasDiff = indexDiff.diff();

            LOG.info("hasDiff " + hasDiff);

            if (hasDiff) {

                for (String file : indexDiff.getAdded()) {
                    LOG.info("Added " + file);
                }

                for (String file : indexDiff.getAssumeUnchanged()) {
                    LOG.info("Assume Unchanged " + file);
                }

                for (String file : indexDiff.getChanged()) {
                    LOG.info("Changed " + file);
                }

                for (String file : indexDiff.getConflicting()) {
                    LOG.info("Conflicting " + file);
                }

                for (String file : indexDiff.getIgnoredNotInIndex()) {
                    LOG.info("Ignored not in index " + file);
                }

                for (String file : indexDiff.getMissing()) {
                    LOG.info("Missing " + file);
                }

                for (String file : indexDiff.getModified()) {
                    LOG.info("Modified " + file);
                }

                for (String file : indexDiff.getRemoved()) {
                    LOG.info("Removed " + file);
                }

                for (String file : indexDiff.getUntracked()) {
                    LOG.info("Untracked " + file);
                }

                for (String file : indexDiff.getUntrackedFolders()) {
                    LOG.info("Untracked Folders " + file);
                }
            }



        } catch (IOException e) {
            LOG.error(e);
            throw new GitManagerException(e);
        }

    }

    @Override
    public DirCache add(String repository, String pattern) throws GitManagerException {

        try {

            Git git = Git.open(new File(repository));

            AddCommand add = git.add();

            add.addFilepattern(pattern);

            DirCache result = add.call();

            return result;

        } catch (IOException e) {
            LOG.error(e);
            throw new GitManagerException(e);
        } catch (GitAPIException e) {
            LOG.error(e);
            throw new GitManagerException(e);
        }
    }

    @Override
    public void commit(
            String repository,
            String message,
            String[] files,
            boolean all) throws GitManagerException {

        try {

            Git git = Git.open(new File(repository));

            CommitCommand commit = git.commit();

            commit.setMessage(message);

            if (all) {

                commit.setAll(true);

            } else {

                for (String file : files) {

                    commit.setOnly(file);
                }

            }

            RevCommit result = commit.call();

            // result....

        } catch (IOException e) {
            LOG.error(e);
            throw new GitManagerException(e);
        } catch (GitAPIException e) {
            LOG.error(e);
            throw new GitManagerException(e);
        }
    }

    @Override
    public String getStatus(File file, String userHome) throws GitManagerException {
        return getStatus(file.getAbsolutePath(), userHome);
    }

    @Override
    public String getStatus(String file, String userHome) throws GitManagerException {

        String tmpRelativePath = new File(file).getAbsolutePath().replaceFirst(userHome + "/projects", "").replaceFirst("/", "");

        String project;

        if (tmpRelativePath.contains("/")) {

            project = tmpRelativePath.substring(0, tmpRelativePath.indexOf('/'));

        } else {

            project = tmpRelativePath;
        }

        String repository = userHome + "/projects/" + project;
        String relativePath = tmpRelativePath.replaceFirst(project, "").replaceFirst("/", "");

        String status = "unmodified";

        try {

            Git git = Git.open(new File(repository));

            IndexDiff diff = new IndexDiff(git.getRepository(), "HEAD",
                    new FileTreeIterator(git.getRepository()));

            diff.setFilter(PathFilterGroup.createFromStrings(relativePath));

            diff.diff();


            if (!diff.getModified().isEmpty()) {
                return "modified";
            }

            if (!diff.getUntracked().isEmpty()) {
                return "untracked";
            }

            if (!diff.getAdded().isEmpty()) {
                return "added";
            }

            if (!diff.getAssumeUnchanged().isEmpty()) {
                return "assumeUnchanged";
            }

            if (!diff.getChanged().isEmpty()) {
                return "changed";
            }

            if (!diff.getConflicting().isEmpty()) {
                return "conflict";
            }

            if (!diff.getIgnoredNotInIndex().isEmpty()) {
                return "ignoredNotInIndex";
            }

            if (!diff.getMissing().isEmpty()) {
                return "missing";
            }

            if (!diff.getRemoved().isEmpty()) {
                return "removed";
            }

            if (!diff.getUntrackedFolders().isEmpty()) {
                return "untracked";
            }

            return status;

        } catch (IOException e) {
            LOG.error(e);
            throw new GitManagerException(e);
        }
    }

    public void initRepository() {


        InitCommand initCommand = Git.init();



    }

    public IStormCloudDao getDao() {
        return dao;
    }

    public void setDao(IStormCloudDao dao) {
        this.dao = dao;
    }
}
