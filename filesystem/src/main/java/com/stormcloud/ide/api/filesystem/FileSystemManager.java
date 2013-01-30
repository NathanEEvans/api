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
import com.stormcloud.ide.api.core.dao.IStormCloudDao;
import com.stormcloud.ide.api.core.entity.FileType;
import com.stormcloud.ide.api.core.entity.FileTypes;
import com.stormcloud.ide.api.core.entity.User;
import com.stormcloud.ide.api.core.remote.RemoteUser;
import com.stormcloud.ide.api.filesystem.exception.FilesystemManagerException;
import com.stormcloud.ide.api.git.IGitManager;
import com.stormcloud.ide.api.git.exception.GitManagerException;
import com.stormcloud.ide.model.factory.MavenModelFactory;
import com.stormcloud.ide.model.factory.exception.MavenModelFactoryException;
import com.stormcloud.ide.model.filesystem.Filesystem;
import com.stormcloud.ide.model.filesystem.Item;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.maven.pom._4_0.Model;
import org.eclipse.jgit.util.Base64;

/**
 *
 * @author martijn
 */
public class FileSystemManager implements IFilesystemManager {

    private Logger LOG = Logger.getLogger(getClass());
    private IGitManager gitManager;
    private IStormCloudDao dao;
    private static final String TRASH = "/.trash";
    private static final String CLOSED = "/.closed";
    private static final String POM = "/pom.xml";
    private static final String SETTINGS_XML = "/settings.xml";
    private static final String MULE_CONFIG = "/mule-config.xml";
    private static final String SOURCE_DIR = "/src/main/java";
    private static final String RESOURCE_DIR = "/src/main/resources";
    private static final String MULE_CONFIG_DIR = "/src/main/app";
    private static final String WEB_DIR = "/src/main/webapp";
    private static final String TEST_SOURCE_DIR = "/src/test/java";
    private static final String TEST_RESOURCE_DIR = "/src/test/resources";

    @Override
    public Filesystem getFileTemplates()
            throws FilesystemManagerException {


        LOG.info(
                "List File Templates for user["
                + RemoteUser.get().getUserName()
                + "]");

        Filesystem filesystem = new Filesystem();

        User user = getUser();

        // list all files under the user home folder, 
        // these are the project dirs
        File[] files = new File(
                user.getHomeFolder() + "/templates").listFiles(
                Filters.getProjectFilter());

        for (File file : files) {

            Item item = new Item();
            item.setId(file.getAbsolutePath());
            item.setLabel(file.getName());
            item.setType("folder");

            filesystem.getChildren().add(item);

            walk(item, file, Filters.getProjectFilter(), user, false);
        }

        return filesystem;
    }

    @Override
    public Filesystem bare()
            throws FilesystemManagerException {


        LOG.info(
                "list Filesystem for user["
                + RemoteUser.get().getUserName()
                + "]");

        Filesystem filesystem = new Filesystem();

        User user = getUser();

        // list all files under the user home folder, 
        // these are the project dirs
        File[] files = new File(
                user.getProjectFolder()).listFiles(
                Filters.getProjectFilter());

        // walk all project roots
        for (File file : files) {

            Item item = new Item();
            item.setId(file.getAbsolutePath());
            item.setLabel(file.getName());
            item.setType("folder");

            filesystem.getChildren().add(item);

            walk(item, file, Filters.getProjectFilter(), user, false);
        }

        return filesystem;
    }

    @Override
    public Filesystem folderPicker(String root)
            throws FilesystemManagerException {

        LOG.info("Browse from " + root);

        Filesystem filesystem = new Filesystem();

        // list all files under the user home folder, 
        // these are the project dirs
        File[] files = new File(root).listFiles(
                Filters.getProjectFilter());

        // walk all project roots
        for (File file : files) {

            Item item = new Item();
            item.setId(file.getAbsolutePath());
            item.setLabel(file.getName());
            item.setType("folder");

            filesystem.getChildren().add(item);

            walkDirs(item, file, Filters.getProjectFilter());
        }

        return filesystem;
    }

    @Override
    public Item[] availableProjects()
            throws FilesystemManagerException {

        LOG.info(
                "list Available Projects for user["
                + RemoteUser.get().getUserName()
                + "]");

        User user = getUser();

        // list all files under the user home folder, 
        // these are the project dirs
        File[] files = new File(
                user.getProjectFolder()).listFiles(
                Filters.getProjectFilter());

        List<Item> items = new ArrayList<Item>();

        for (File file : files) {

            File closed = new File(file.getAbsolutePath() + CLOSED);

            if (!closed.exists()) {

                Item item = new Item();
                item.setId(file.getAbsolutePath());
                item.setLabel(file.getName());
                item.setType("project");

                items.add(item);
            }
        }

        return items.toArray(new Item[items.size()]);
    }

    /**
     * @param opened
     * @return
     * @throws FilesystemManagerException
     */
    @Override
    public Filesystem list(boolean opened)
            throws FilesystemManagerException {

        LOG.info(
                "list Filesystem for user["
                + RemoteUser.get().getUserName()
                + "]");

        Filesystem filesystem = new Filesystem();

        User user = getUser();

        // list all files under the user home folder, 
        // these are the project dirs
        File[] files = new File(
                user.getProjectFolder()).listFiles(
                Filters.getProjectFilter());

        // loop trough the project files
        for (File file : files) {

            // check if this project is open and match with requested
            // listing type (open true / false)
            File closed = new File(file.getAbsolutePath() + CLOSED);

            if (closed.exists() && opened) {

                // project is closed and open projects are requested
                // continue to the next project
                continue;

            } else if (closed.exists() && !opened) {

                // project is closed and closed requested
                // only add project folder
                Item item = new Item();
                item.setId(file.getAbsolutePath());
                item.setLabel(file.getName());
                item.setType("closedProject");

                filesystem.getChildren().add(item);

            } else if (!closed.exists() && opened) {

                // project is openend and open projects are requested
                // process the project

                // check for pom file to validate if the project is 
                // well-formed, when pom not found return malformed project
                if (new File(file.getAbsolutePath() + POM).exists()) {

                    // read the pom to see what type of project it is
                    Model pom;

                    try {

                        pom = MavenModelFactory.getProjectModel(
                                new File(file.getAbsolutePath() + POM));

                        String packaging = pom.getPackaging();

                        // check packaging to determine what type of
                        // processing we need (single or multi)
                        if (packaging.equals("pom")) {

                            Item project = processModule(file, user, true);

                            // a parent pom, nested modules

                            // get the defined modules
                            List<String> modules = pom.getModules().getModule();

                            for (String module : modules) {

                                // process each module
                                File moduleDir =
                                        new File(
                                        file.getAbsolutePath() + "/" + module);

                                if (moduleDir.exists()) {

                                    Item projectModule =
                                            processModule(moduleDir, user, false);

                                    project.getChildren().add(projectModule);
                                }
                            }

                            // done with the modules add the root pom
                            // so it ends up last
                            Item pomFile = new Item();
                            pomFile.setId(file.getAbsolutePath() + POM);
                            pomFile.setType("projectSettings");
                            pomFile.setLabel("Project Settings");
                            String status = gitManager.getStatus(pomFile.getId(), user.getHomeFolder());
                            pomFile.setStatus(status);

                            project.getChildren().add(pomFile);

                            Item settings = new Item();
                            settings.setId(user.getM2Folder() + SETTINGS_XML);
                            settings.setLabel("Maven Settings");
                            settings.setType("mavenSettings");

                            project.getChildren().add(settings);

                            filesystem.getChildren().add(project);

                        } else {

                            // single project, no nested modules
                            Item project =
                                    processModule(file, user, false);


                            Item settings = new Item();
                            settings.setId(user.getM2Folder() + SETTINGS_XML);
                            settings.setLabel("Maven Settings");
                            settings.setType("mavenSettings");

                            project.getChildren().add(settings);

                            filesystem.getChildren().add(project);
                        }

                    } catch (MavenModelFactoryException e) {
                        LOG.error(e);
                        throw new FilesystemManagerException(e);
                    } catch (GitManagerException e) {
                        LOG.error(e);
                        throw new FilesystemManagerException(e);
                    }

                } else {

                    // No pom.xml found where expected
                    // mark as malformed
                    Item item = new Item();
                    item.setId("none");
                    item.setLabel(file.getName() + "[Malformed Project!]");
                    item.setType("malformedProject");

                    filesystem.getChildren().add(item);

                }
            }
        }

        // if we did not get any results
        // mark it as no results
        if (filesystem.getChildren().isEmpty()) {

            Item item = new Item();
            item.setId("none");
            item.setLabel("No Projects Available");
            item.setType("noAvailableProjects");

            filesystem.getChildren().add(item);
        }

        return filesystem;
    }

    private Item processModule(
            File dir,
            User user,
            boolean root)
            throws FilesystemManagerException, MavenModelFactoryException, GitManagerException {

        // create new project object
        Item project = new Item();
        project.setId(dir.getAbsolutePath());
        project.setType("project");

        if (!new File(dir.getAbsolutePath() + POM).exists()) {

            // No pom.xml found where expected
            // mark as malformed
            Item item = new Item();
            item.setId("none");
            item.setLabel(dir.getName() + "[Malformed Project!]");
            item.setType("malformedProject");
            return item;
        }

        Model pom = MavenModelFactory.getProjectModel(
                new File(dir.getAbsolutePath() + POM));

        // use name from the pom when available
        // otherwise the folder name
        if (pom.getName() == null || pom.getName().isEmpty()) {

            project.setLabel(dir.getName());
        } else {

            project.setLabel(pom.getName());
        }


        if (pom.getBuild() == null || pom.getBuild().getFinalName() == null || pom.getBuild().getFinalName().isEmpty()) {

            project.setBuildName(dir.getName() + "-" + pom.getVersion());

        } else {

            project.setBuildName(pom.getBuild().getFinalName());
        }

        //String status = gitManager.getStatus(project.getId());
        //project.setStatus(status);

        if (new File(dir.getAbsolutePath() + MULE_CONFIG_DIR).exists()) {

            Item muleConfig = new Item();
            muleConfig.setLabel("Flow Design");
            muleConfig.setType("flowDesign");
            muleConfig.setId(dir.getAbsolutePath() + MULE_CONFIG_DIR + MULE_CONFIG);

            project.getChildren().add(muleConfig);
        }

        // if there is a webapp dir, process it
        if (new File(dir.getAbsolutePath() + WEB_DIR).exists()) {

            Item webapp = new Item();
            webapp.setLabel("Web Pages");
            webapp.setType("webapp");

            String status = gitManager.getStatus(dir.getAbsolutePath() + WEB_DIR, user.getHomeFolder());
            webapp.setStatus(status);

            project.getChildren().add(webapp);

            walk(webapp, new File(dir.getAbsolutePath() + WEB_DIR), Filters.getProjectFilter(), user, true);
        }

        // if there is a source dir process it
        if (new File(dir.getAbsolutePath() + SOURCE_DIR).exists()) {

            Item sources = new Item();
            sources.setLabel("Source Packages");
            sources.setType("sources");

            String status = gitManager.getStatus(dir.getAbsolutePath() + SOURCE_DIR, user.getHomeFolder());
            sources.setStatus(status);

            project.getChildren().add(sources);

            walk(sources, new File(dir.getAbsolutePath() + SOURCE_DIR), Filters.getProjectFilter(), user, true);
        }

        // if there is a resources dir process it
        if (new File(dir.getAbsolutePath() + RESOURCE_DIR).exists()) {

            Item resources = new Item();
            resources.setLabel("Resources");
            resources.setType("resources");

            String status = gitManager.getStatus(dir.getAbsolutePath() + RESOURCE_DIR, user.getHomeFolder());
            resources.setStatus(status);

            project.getChildren().add(resources);

            walk(resources, new File(dir.getAbsolutePath() + RESOURCE_DIR), Filters.getProjectFilter(), user, true);
        }

        // if there is a test source dir, process it
        if (new File(dir.getAbsolutePath() + TEST_SOURCE_DIR).exists()) {

            Item sources = new Item();
            sources.setLabel("Test Source Packages");
            sources.setType("sources");

            String status = gitManager.getStatus(dir.getAbsolutePath() + TEST_SOURCE_DIR, user.getHomeFolder());
            sources.setStatus(status);

            project.getChildren().add(sources);

            walk(sources, new File(dir.getAbsolutePath() + TEST_SOURCE_DIR), Filters.getProjectFilter(), user, true);
        }

        // if there is test resources dir dir, process it
        if (new File(dir.getAbsolutePath() + TEST_RESOURCE_DIR).exists()) {

            Item resources = new Item();
            resources.setLabel("Test Resources");
            resources.setType("resources");

            String status = gitManager.getStatus(dir.getAbsolutePath() + TEST_RESOURCE_DIR, user.getHomeFolder());
            resources.setStatus(status);

            project.getChildren().add(resources);

            walk(resources, new File(dir.getAbsolutePath() + TEST_RESOURCE_DIR), Filters.getProjectFilter(), user, true);
        }

        /**
         * @todo fix git status for pom's
         */
        // add the pom, at the bottom
        if (!root) {
            Item pomFile = new Item();
            pomFile.setId(dir.getAbsolutePath() + POM);
            pomFile.setType("projectSettings");
            pomFile.setLabel("Project Settings");

            //status = gitManager.getStatus(repository, relativePath);
            //pomFile.setStatus(status);

            project.getChildren().add(pomFile);
        }

        return project;
    }

    private void walk(
            Item current,
            File dir,
            FilenameFilter filter,
            User user,
            boolean versioning)
            throws FilesystemManagerException {

        /**
         * @todo read pom for item label etc.
         *
         */
        File[] files = dir.listFiles(filter);

        if (files != null) {

            Comparator comp = new Comparator() {

                @Override
                public int compare(Object o1, Object o2) {
                    File f1 = (File) o1;
                    File f2 = (File) o2;
                    if (f1.isDirectory() && !f2.isDirectory()) {
                        // Directory before non-directory
                        return -1;
                    } else if (!f1.isDirectory() && f2.isDirectory()) {
                        // Non-directory after directory
                        return 1;
                    } else {
                        // Alphabetic order otherwise
                        return f1.compareTo(f2);
                    }
                }
            };

            Arrays.sort(files, comp);

            for (File file : files) {

                // create new item
                Item item = new Item();
                item.setId(file.getAbsolutePath());

                if (versioning) {

                    try {

                        String status = gitManager.getStatus(item.getId(), user.getHomeFolder());
                        item.setStatus(status);

                    } catch (GitManagerException e) {
                        throw new FilesystemManagerException(e);
                    }
                }

                if (file.getName().endsWith(".java")) {

                    item.setType("javaFile");
                    item.setLabel(file.getName());

                } else if (file.getName().endsWith(".jsp")) {

                    item.setType("jspFile");
                    item.setLabel(file.getName());

                } else if (file.getName().endsWith(".xml")) {

                    item.setType("xmlFile");
                    item.setLabel(file.getName());

                } else if (file.getName().endsWith(".wsdl")) {

                    item.setType("wsdlFile");
                    item.setLabel(file.getName());

                } else if (file.getName().endsWith(".xsd")) {

                    item.setType("xsdFile");
                    item.setLabel(file.getName());

                } else if (file.getName().endsWith(".html")) {

                    item.setType("htmlFile");
                    item.setLabel(file.getName());

                } else if (file.getName().endsWith(".xhtml")) {

                    item.setType("xhtmlFile");
                    item.setLabel(file.getName());

                } else if (file.getName().endsWith(".txt")) {

                    item.setType("textFile");
                    item.setLabel(file.getName());

                } else if (file.getName().endsWith(".tld")) {

                    item.setType("tldFile");
                    item.setLabel(file.getName());


                } else if (file.getName().endsWith(".png")
                        || file.getName().endsWith(".gif")
                        || file.getName().endsWith(".jpg")
                        || file.getName().endsWith(".jpeg")
                        || file.getName().endsWith(".tiff")
                        || file.getName().endsWith(".bmp")) {

                    item.setType("imageFile");
                    item.setLabel(file.getName());

                } else if (file.getName().endsWith(".js")) {

                    item.setType("jsFile");
                    item.setLabel(file.getName());

                } else if (file.getName().endsWith(".css")) {

                    item.setType("cssFile");
                    item.setLabel(file.getName());

                } else if (file.getName().endsWith(".sql")) {

                    item.setType("sqlFile");
                    item.setLabel(file.getName());


                } else if (file.getName().endsWith(".properties")) {

                    item.setType("propertiesFile");
                    item.setLabel(file.getName());

                } else {

                    item.setType("folder");
                    item.setLabel(file.getName());

                }

                if (file.isDirectory()) {

                    walk(item, file, filter, user, versioning);
                }

                if (current != null) {
                    current.getChildren().add(item);
                }
            }
        }
    }

    private void walkDirs(
            Item current,
            File dir,
            FilenameFilter filter)
            throws FilesystemManagerException {

        File[] files = dir.listFiles(filter);

        if (files != null) {

            for (File file : files) {

                if (file.isDirectory()) {
                    // create new item
                    Item item = new Item();
                    item.setId(file.getAbsolutePath());
                    item.setType("folder");
                    item.setLabel(file.getName());


                    walkDirs(item, file, filter);


                    if (current != null) {
                        current.getChildren().add(item);
                    }
                }
            }
        }
    }

    @Override
    public FileTypes[] getFileTypes()
            throws FilesystemManagerException {

        LOG.info("Get FileTypes");

        List<FileType> fileTypes = dao.getFileTypes();

        FileTypes[] types = new FileTypes[1];

        Set<FileType> arch = new LinkedHashSet<FileType>(fileTypes);

        FileTypes ft = new FileTypes();
        ft.getChildren().addAll(arch);

        types[0] = ft;

        return types;
    }

    @Override
    public int open(String filePath) throws FilesystemManagerException {

        File file = new File(filePath + CLOSED);

        boolean succes = file.delete();

        if (!succes) {
            throw new FilesystemManagerException("Failed to close Project");
        }

        return 0;
    }

    @Override
    public int close(String filePath) throws FilesystemManagerException {

        File file = new File(filePath + CLOSED);

        try {

            FileUtils.writeStringToFile(file, "");

        } catch (IOException e) {
            LOG.error(e);
            throw new FilesystemManagerException(e);
        }

        return 0;
    }

    /**
     * <p>Delete a file from file system.</p> If it is a directory it will be
     * traversed and all children deleted.
     *
     * @param filePath
     */
    @Override
    public int delete(
            final String filePath)
            throws FilesystemManagerException {

        File file = new File(filePath);

        User user = getUser();

        try {

            if (file.isDirectory()) {

                try {

                    FileUtils.moveDirectory(file, new File(user.getHomeFolder() + TRASH + "/" + file.getName()));

                } catch (FileExistsException e) {

                    LOG.info("Destination already exists, appending Date.");
                    // when a project with the same name is in there, add the date to make it unique
                    FileUtils.moveDirectory(file, new File(user.getHomeFolder() + TRASH + "/" + file.getName() + "-" + new Date()));

                }

            } else {

                try {

                    FileUtils.moveFile(file, new File(user.getHomeFolder() + TRASH + "/" + file.getName()));

                } catch (FileExistsException e) {

                    LOG.info("Destination already exists, appending Date.");
                    FileUtils.moveFile(file, new File(user.getHomeFolder() + TRASH + "/" + file.getName() + "-" + new Date()));
                }
            }

        } catch (IOException e) {
            LOG.error(e);
            throw new FilesystemManagerException(e);
        }

        return 0;
    }

    @Override
    public int emptyTrash() throws FilesystemManagerException {

        try {

            User user = getUser();

            File[] contents = new File(user.getHomeFolder() + TRASH).listFiles();

            for (File file : contents) {

                if (file.isDirectory()) {

                    FileUtils.deleteDirectory(file);

                } else {

                    file.delete();
                }
            }

        } catch (IOException e) {
            LOG.error(e);
            throw new FilesystemManagerException(e);
        }

        return 0;
    }

    @Override
    public int hasTrash() throws FilesystemManagerException {

        User user = getUser();

        File[] contents = new File(user.getHomeFolder() + TRASH).listFiles();

        return contents.length;
    }

    @Override
    public int copy(String srcFilePath, String destFilePath) throws FilesystemManagerException {

        File src = new File(srcFilePath);
        File dest = new File(destFilePath);

        try {

            if (src.isDirectory()) {

                FileUtils.copyDirectory(src, new File(dest + "/" + src.getName()));

            } else {

                FileUtils.copyFile(src, new File(dest + "/" + src.getName()));
            }

        } catch (IOException e) {
            LOG.error(e);
            throw new FilesystemManagerException(e);
        }

        return 0;
    }

    @Override
    public int move(String srcFilePath, String destFilePath) throws FilesystemManagerException {

        File src = new File(srcFilePath);
        File dest = new File(destFilePath);


        try {

            if (src.isDirectory()) {

                FileUtils.moveDirectory(src, new File(dest + "/" + src.getName()));

            } else {

                FileUtils.moveFile(src, new File(dest + "/" + src.getName()));
            }

        } catch (IOException e) {
            LOG.error(e);
            throw new FilesystemManagerException(e);
        }

        return 0;
    }

    /**
     * Save a file to disk
     *
     * @param filePath
     * @param contents
     */
    @Override
    public String save(
            String filePath,
            String contents)
            throws FilesystemManagerException {

        File file = new File(filePath);
        String status = null;

        User user = dao.getUser(RemoteUser.get().getUserName());

        try {

            FileUtils.writeStringToFile(file, contents);

            String relativePath = file.getAbsolutePath().replaceFirst(user.getHomeFolder(), "").replaceFirst("/", "");

            String project = relativePath.substring(0, relativePath.indexOf("/"));

            LOG.info("project " + project);

            String repository = user.getHomeFolder() + "/" + project;

            LOG.info("repository " + repository);

            relativePath = relativePath.replaceFirst(project, "").replaceFirst("/", "");

            LOG.info("relativePath " + relativePath);

            status = gitManager.getStatus(file, user.getHomeFolder());

        } catch (IOException e) {
            LOG.error(e);
            throw new FilesystemManagerException(e);
        } catch (GitManagerException e) {
            LOG.error(e);
            throw new FilesystemManagerException(e);
        }

        return status;
    }

    /**
     * Get a file from disk.
     *
     * @param filePath
     * @return
     */
    @Override
    public String get(
            final String filePath)
            throws FilesystemManagerException {

        String contents = null;
        File file = new File(filePath);

        try {

            contents = FileUtils.readFileToString(file);

        } catch (IOException e) {
            LOG.error(e);
            throw new FilesystemManagerException(e);
        }

        return contents;
    }

    @Override
    public String getBinary(
            final String filePath)
            throws FilesystemManagerException {

        String contents;
        File file = new File(filePath);

        try {

            contents = Base64.encodeBytes(FileUtils.readFileToByteArray(file));

        } catch (IOException e) {
            LOG.error(e);
            throw new FilesystemManagerException(e);
        }

        return contents;
    }

    private User getUser() {

        return dao.getUser(RemoteUser.get().getUserName());
    }

    public IGitManager getGitManager() {
        return gitManager;
    }

    public void setGitManager(IGitManager gitManager) {
        this.gitManager = gitManager;
    }

    public IStormCloudDao getDao() {
        return dao;
    }

    public void setDao(IStormCloudDao dao) {
        this.dao = dao;
    }
}
