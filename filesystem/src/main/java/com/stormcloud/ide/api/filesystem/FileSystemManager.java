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
import com.stormcloud.ide.api.core.entity.User;
import com.stormcloud.ide.api.core.remote.RemoteUser;
import com.stormcloud.ide.api.core.thread.StreamGobbler;
import com.stormcloud.ide.api.filesystem.exception.FilesystemManagerException;
import com.stormcloud.ide.api.git.IGitManager;
import com.stormcloud.ide.api.git.exception.GitManagerException;
import com.stormcloud.ide.model.factory.MavenModelFactory;
import com.stormcloud.ide.model.factory.exception.MavenModelFactoryException;
import com.stormcloud.ide.model.filesystem.Filesystem;
import com.stormcloud.ide.model.filesystem.Find;
import com.stormcloud.ide.model.filesystem.FindResult;
import com.stormcloud.ide.model.filesystem.Item;
import com.stormcloud.ide.model.filesystem.Save;
import com.stormcloud.ide.model.user.UserSettings;
import java.io.*;
import java.text.SimpleDateFormat;
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
    private static final String BASH = "/bin/sh";
    private static final String COMMAND = "-c";
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

        Filesystem filesystem = new Filesystem();

        File[] files = new File(
                RemoteUser.get().getSetting(UserSettings.FILE_TEMPLATE_FOLDER)).listFiles(
                Filters.getProjectFilter());

        for (File file : files) {

            Item item = new Item();
            item.setId(file.getAbsolutePath());
            item.setLabel(file.getName());
            item.setType("folder");

            filesystem.getChildren().add(item);

            walk(item, file, Filters.getProjectFilter(), false);
        }

        return filesystem;
    }

    @Override
    public Filesystem bare()
            throws FilesystemManagerException {

        Filesystem filesystem = new Filesystem();

        // list all files under the user home folder, 
        // these are the project dirs
        File[] files = new File(
                RemoteUser.get().getSetting(UserSettings.PROJECT_FOLDER)).listFiles(
                Filters.getProjectFilter());

        // walk all project roots
        for (File file : files) {

            Item item = new Item();
            item.setId(file.getAbsolutePath());
            item.setLabel(file.getName());
            item.setType("folder");

            filesystem.getChildren().add(item);

            walk(item, file, Filters.getProjectFilter(), false);
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

            if (file.isDirectory()) {

                Item item = new Item();
                item.setId(file.getAbsolutePath());
                item.setLabel(file.getName());
                item.setType("folder");

                filesystem.getChildren().add(item);

                walkDirs(item, file, Filters.getProjectFilter());
            }
        }

        return filesystem;
    }

    @Override
    public Item[] availableProjects()
            throws FilesystemManagerException {


        // list all files under the user home folder, 
        // these are the project dirs
        File[] files = new File(
                RemoteUser.get().getSetting(UserSettings.PROJECT_FOLDER)).listFiles(
                Filters.getProjectFilter());

        List<Item> items = new ArrayList<Item>(0);

        for (File file : files) {

            Item item = new Item();
            item.setId(file.getAbsolutePath());
            item.setLabel(file.getName());
            item.setType("project");

            items.add(item);
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

        Filesystem filesystem = new Filesystem();

        File[] files = null;

        if (opened) {

            files = new File(
                    RemoteUser.get().getSetting(UserSettings.PROJECT_FOLDER)).listFiles(
                    Filters.getProjectFilter());
        } else {

            files = new File(
                    RemoteUser.get().getSetting(UserSettings.CLOSED_PROJECTS_FOLDER)).listFiles(
                    Filters.getProjectFilter());

        }

        // loop trough the project files
        for (File file : files) {

            if (!opened) {

                // project is closed and closed requested
                // only add project folder
                Item item = new Item();
                item.setId(file.getAbsolutePath());
                item.setType("closedProject");

                if (new File(file.getAbsolutePath() + POM).exists()) {

                    try {

                        Model pom = MavenModelFactory.getProjectModel(
                                new File(file.getAbsolutePath() + POM));

                        if (pom.getName() == null || pom.getName().isEmpty()) {

                            item.setLabel(file.getName());
                        } else {

                            item.setLabel(pom.getName());
                        }

                    } catch (MavenModelFactoryException e) {
                        LOG.error(e);
                        throw new FilesystemManagerException(e);
                    }
                } else {

                    // No pom.xml found where expected
                    // mark as malformed
                    item.setId("none");
                    item.setLabel(file.getName() + " [Malformed Project!]");
                    item.setType("malformedProject");
                }

                filesystem.getChildren().add(item);

            } else {

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

                            Item project = processModule(file, true);

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
                                            processModule(moduleDir, false);

                                    project.getChildren().add(projectModule);
                                }
                            }

                            // done with the modules add the root pom
                            // so it ends up last
                            Item pomFile = new Item();
                            pomFile.setId(file.getAbsolutePath() + POM);
                            pomFile.setType("projectSettings");
                            pomFile.setLabel("Project Settings");
                            String status = gitManager.getStatus(pomFile.getId(), RemoteUser.get().getSetting(UserSettings.USER_HOME));
                            pomFile.setStatus(status);

                            project.getChildren().add(pomFile);

                            Item settings = new Item();
                            settings.setId(RemoteUser.get().getSetting(UserSettings.LOCAL_MAVEN_REPOSITORY) + SETTINGS_XML);
                            settings.setLabel("Maven Settings");
                            settings.setType("mavenSettings");

                            project.getChildren().add(settings);

                            filesystem.getChildren().add(project);

                        } else {

                            // single project, no nested modules
                            Item project =
                                    processModule(file, false);


                            Item settings = new Item();
                            settings.setId(RemoteUser.get().getSetting(UserSettings.LOCAL_MAVEN_REPOSITORY) + SETTINGS_XML);
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


        String userHome = RemoteUser.get().getSetting(UserSettings.USER_HOME);

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
            webapp.setId(dir.getAbsolutePath() + WEB_DIR);
            webapp.setLabel("Web Pages");
            webapp.setType("webapp");

            String status = gitManager.getStatus(dir.getAbsolutePath() + WEB_DIR, userHome);
            webapp.setStatus(status);

            project.getChildren().add(webapp);

            walk(webapp, new File(dir.getAbsolutePath() + WEB_DIR), Filters.getProjectFilter(), true);
        }

        // if there is a source dir process it
        if (new File(dir.getAbsolutePath() + SOURCE_DIR).exists()) {

            Item sources = new Item();
            sources.setId(dir.getAbsolutePath() + SOURCE_DIR);
            sources.setLabel("Source Packages");
            sources.setType("sources");

            String status = gitManager.getStatus(dir.getAbsolutePath() + SOURCE_DIR, userHome);
            sources.setStatus(status);

            project.getChildren().add(sources);

            walk(sources, new File(dir.getAbsolutePath() + SOURCE_DIR), Filters.getProjectFilter(), true);
        }

        // if there is a resources dir process it
        if (new File(dir.getAbsolutePath() + RESOURCE_DIR).exists()) {

            Item resources = new Item();
            resources.setId(dir.getAbsolutePath() + RESOURCE_DIR);
            resources.setLabel("Resources");
            resources.setType("resources");

            String status = gitManager.getStatus(dir.getAbsolutePath() + RESOURCE_DIR, userHome);
            resources.setStatus(status);

            project.getChildren().add(resources);

            walk(resources, new File(dir.getAbsolutePath() + RESOURCE_DIR), Filters.getProjectFilter(), true);
        }

        // if there is a test source dir, process it
        if (new File(dir.getAbsolutePath() + TEST_SOURCE_DIR).exists()) {

            Item sources = new Item();
            sources.setId(dir.getAbsolutePath() + TEST_SOURCE_DIR);
            sources.setLabel("Test Source Packages");
            sources.setType("sources");

            String status = gitManager.getStatus(dir.getAbsolutePath() + TEST_SOURCE_DIR, userHome);
            sources.setStatus(status);

            project.getChildren().add(sources);

            walk(sources, new File(dir.getAbsolutePath() + TEST_SOURCE_DIR), Filters.getProjectFilter(), true);
        }

        // if there is test resources dir dir, process it
        if (new File(dir.getAbsolutePath() + TEST_RESOURCE_DIR).exists()) {

            Item resources = new Item();
            resources.setId(dir.getAbsolutePath() + TEST_RESOURCE_DIR);
            resources.setLabel("Test Resources");
            resources.setType("resources");

            String status = gitManager.getStatus(dir.getAbsolutePath() + TEST_RESOURCE_DIR, userHome);
            resources.setStatus(status);

            project.getChildren().add(resources);

            walk(resources, new File(dir.getAbsolutePath() + TEST_RESOURCE_DIR), Filters.getProjectFilter(), true);
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

            String status = gitManager.getStatus(pomFile.getId(), RemoteUser.get().getSetting(UserSettings.USER_HOME));

            pomFile.setStatus(status);

            project.getChildren().add(pomFile);
        }

        return project;
    }

    private void walk(
            Item current,
            File dir,
            FilenameFilter filter,
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

                        String status = gitManager.getStatus(item.getId(), RemoteUser.get().getSetting(UserSettings.USER_HOME));
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

                    walk(item, file, filter, versioning);
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
    public int open(String filePath) throws FilesystemManagerException {

        try {

            String project = filePath.substring(filePath.lastIndexOf('/') + 1, filePath.length());

            FileUtils.moveDirectory(
                    new File(RemoteUser.get().getSetting(UserSettings.CLOSED_PROJECTS_FOLDER) + "/" + project),
                    new File(RemoteUser.get().getSetting(UserSettings.PROJECT_FOLDER) + "/" + project));

        } catch (IOException e) {
            LOG.error(e);
            return -1;
        }

        return 0;
    }

    @Override
    public int close(String filePath) throws FilesystemManagerException {

        try {

            String project = filePath.substring(filePath.lastIndexOf('/') + 1, filePath.length());

            FileUtils.moveDirectory(
                    new File(RemoteUser.get().getSetting(UserSettings.PROJECT_FOLDER) + "/" + project),
                    new File(RemoteUser.get().getSetting(UserSettings.CLOSED_PROJECTS_FOLDER) + "/" + project));

        } catch (IOException e) {
            LOG.error(e);
            return -1;
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

        User user = RemoteUser.get();

        try {

            if (file.isDirectory()) {

                try {

                    FileUtils.moveDirectory(file, new File(RemoteUser.get().getSetting(UserSettings.TRASH_FOLDER) + "/" + file.getName()));

                } catch (FileExistsException e) {

                    LOG.info("Destination already exists, appending Date.");
                    // when a project with the same name is in there, add the date to make it unique
                    FileUtils.moveDirectory(file, new File(RemoteUser.get().getSetting(UserSettings.TRASH_FOLDER) + "/" + file.getName() + "-" + new Date()));

                }

            } else {

                try {

                    FileUtils.moveFile(file, new File(RemoteUser.get().getSetting(UserSettings.TRASH_FOLDER) + "/" + file.getName()));

                } catch (FileExistsException e) {

                    LOG.info("Destination already exists, appending Date.");
                    FileUtils.moveFile(file, new File(RemoteUser.get().getSetting(UserSettings.TRASH_FOLDER) + "/" + file.getName() + "-" + new Date()));
                }
            }

        } catch (IOException e) {
            LOG.error(e);
            throw new FilesystemManagerException(e);
        }

        return 0;
    }

    @Override
    public Filesystem viewTrash()
            throws FilesystemManagerException {

        Filesystem filesystem = new Filesystem();

        // list all files under the user trash folder
        File[] files = new File(
                RemoteUser.get().getSetting(UserSettings.TRASH_FOLDER)).listFiles();

        // walk all roots in the trash
        for (File file : files) {

            Item item = new Item();
            item.setId(file.getAbsolutePath());

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

            filesystem.getChildren().add(item);
        }

        if (filesystem.getChildren().isEmpty()) {

            Item item = new Item();
            item.setId("none");
            item.setLabel("Your Trash is Empty!");
            item.setType("noTrash");

            filesystem.getChildren().add(item);
        }

        return filesystem;
    }

    @Override
    public int emptyTrash() throws FilesystemManagerException {

        try {

            File[] contents = new File(RemoteUser.get().getSetting(UserSettings.TRASH_FOLDER)).listFiles();

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

        File[] contents = new File(RemoteUser.get().getSetting(UserSettings.TRASH_FOLDER)).listFiles();

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
     * @param save
     */
    @Override
    public String save(
            Save save)
            throws FilesystemManagerException {

        File file = new File(save.getFilePath());
        String status = null;

        String userHome = RemoteUser.get().getSetting(UserSettings.USER_HOME);
        String projectFolder = RemoteUser.get().getSetting(UserSettings.PROJECT_FOLDER);

        try {

            FileUtils.writeStringToFile(file, save.getContents());


            // test if this is saved to the projects dir
            // if so it should be versioned
            // if not we don't bother
            if (save.getFilePath().startsWith(projectFolder)) {

                String relativePath = file.getAbsolutePath().replaceFirst(userHome, "").replaceFirst("/", "");

                String project = relativePath.substring(0, relativePath.indexOf('/'));

                LOG.info("project " + project);

                String repository = userHome + "/" + project;

                LOG.info("repository " + repository);

                relativePath = relativePath.replaceFirst(project, "").replaceFirst("/", "");

                LOG.info("relativePath " + relativePath);

                status = gitManager.getStatus(file, userHome);

            }

        } catch (IOException e) {
            LOG.error(e);
            throw new FilesystemManagerException(e);
        } catch (GitManagerException e) {
            LOG.error(e);
            throw new FilesystemManagerException(e);
        }

        return status;
    }

    @Override
    public FindResult find(Find find)
            throws FilesystemManagerException {

        String scope;
        int exitVal;

        // first check from where we are going to search
        if (find.getScope() == null || find.getScope().isEmpty()) {

            scope = RemoteUser.get().getSetting(UserSettings.PROJECT_FOLDER);
            LOG.info("Scope is All Opened Projects " + scope);

        } else {

            scope = find.getScope();
            LOG.info("Scope is Project " + scope);
        }

        // check for text, filename patterns or both
        String fileNamePatterns = find.getFileNamePatterns();
        String text = find.getContainingText();

        String findCommand;
        String filePatternChain = "";
        String[] patterns = null;

        if (fileNamePatterns != null && !fileNamePatterns.isEmpty()) {

            patterns = fileNamePatterns.split(",");

            for (int i = 0; i < patterns.length; i++) {

                filePatternChain += " --include=" + patterns[i].trim();
            }
        }

        // construct text search
        if (text != null && !text.isEmpty()) {

            findCommand = "grep -rn -I "
                    + (find.isMatchCase() ? "" : "-i")
                    + " "
                    + (find.isWholeWords() ? "-w" : "")
                    + " "
                    + filePatternChain
                    + " "
                    + "\"" + text + "\""
                    + " " + scope
                    + " | grep -v /target/ ";

        } else {

            // only find files with specified pattern
            findCommand = "find " + scope;

            filePatternChain = "";

            if (patterns != null) {

                for (int i = 0; i < patterns.length; i++) {

                    filePatternChain += " -name \"" + patterns[i] + "\"";
                }

                findCommand += filePatternChain;
            }
        }

        File output = null;

        try {

            output = File.createTempFile("stormcloud", "out");

        } catch (IOException e) {
            LOG.error(e);
            throw new FilesystemManagerException(e);
        }

        String[] run = {
            BASH,
            COMMAND,
            findCommand + " > " + output.getAbsolutePath()};

        LOG.info("Writing to : " + output.getAbsolutePath());

        Process proc;

        try {

            LOG.info("Executing command : " + findCommand);

            proc = Runtime.getRuntime().exec(run);

            // any output?
            StreamGobbler outputGobbler =
                    new StreamGobbler(proc.getInputStream());

            outputGobbler.start();

            exitVal = proc.waitFor();

            LOG.info("Find exit value " + exitVal + ", file size " + output.length());

            FindResult result = new FindResult();


            FileInputStream fstream = new FileInputStream(output);

            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;

            // process result
            while ((line = br.readLine()) != null) {

                String[] fields = line.split(":");
                Item item = new Item();

                String id = fields[0];

                String fileName = id.substring(id.lastIndexOf('/') + 1, id.length());

                // set filepath as id
                item.setId(id);
                // set filename as label
                item.setLabel(fileName);
                item.setType(getFileType(fileName));

                if (fields.length > 1) {

                    item.setStatus("[" + fields[1] + "] " + fields[2].trim());
                }

                boolean added = false;

                if (result.getResult().size() > 0) {

                    // check if this file is already in there
                    for (Item stashedItem : result.getResult()) {

                        if (stashedItem.getId().equals(id)) {
                            stashedItem.getChildren().add(item);
                            added = true;
                        }
                    }

                    if (!added) {
                        result.getResult().add(item);
                    }

                } else {

                    // for adding the first one
                    result.getResult().add(item);
                }
            }

            return result;


        } catch (IOException e) {
            LOG.error(e);
            throw new FilesystemManagerException(e);
        } catch (InterruptedException e) {
            LOG.error(e);
            throw new FilesystemManagerException(e);
        } finally {

            if (output != null) {
                output.delete();
            }

        }
    }

    private String getFileType(String fileName) {

        if (fileName.endsWith(".java")) {

            return "javaFile";

        } else if (fileName.endsWith(".jsp")) {

            return "jspFile";

        } else if (fileName.endsWith(".xml")) {

            return "xmlFile";

        } else if (fileName.endsWith(".wsdl")) {

            return "wsdlFile";

        } else if (fileName.endsWith(".xsd")) {

            return "xsdFile";

        } else if (fileName.endsWith(".html")) {

            return "htmlFile";

        } else if (fileName.endsWith(".xhtml")) {

            return "xhtmlFile";

        } else if (fileName.endsWith(".txt")) {

            return "textFile";

        } else if (fileName.endsWith(".tld")) {

            return "tldFile";

        } else if (fileName.endsWith(".png")
                || fileName.endsWith(".gif")
                || fileName.endsWith(".jpg")
                || fileName.endsWith(".jpeg")
                || fileName.endsWith(".tiff")
                || fileName.endsWith(".bmp")) {

            return "imageFile";

        } else if (fileName.endsWith(".js")) {

            return "jsFile";

        } else if (fileName.endsWith(".css")) {

            return "cssFile";

        } else if (fileName.endsWith(".sql")) {

            return "sqlFile";

        } else if (fileName.endsWith(".properties")) {

            return "propertiesFile";

        } else {

            return "folder";

        }


    }

    @Override
    public String create(
            String filePath,
            String fileType)
            throws FilesystemManagerException {

        boolean isFile = fileType.contains(".");

        File file = new File(filePath);

        try {

            if (isFile) {
                // get the template contents
                String contents = FileUtils.readFileToString(new File(fileType));

                // set the author name
                String author = RemoteUser.get().getUserName();
                contents = contents.replaceAll("(\\{author\\})", author);

                // set date & year
                Date now = new Date();

                String year = new SimpleDateFormat("yyyy").format(now);
                String date = new SimpleDateFormat("dd-MM-yyyy").format(now);
                String time = new SimpleDateFormat("HH:mm:ss").format(now);

                contents = contents.replaceAll("(\\{year\\})", year);
                contents = contents.replaceAll("(\\{date\\})", date);
                contents = contents.replaceAll("(\\{time\\})", time);

                // set fileName
                String fileName = filePath.substring(filePath.lastIndexOf('/') + 1, filePath.length());
                contents = contents.replaceAll("(\\{fileName\\})", fileName);

                // set the classname
                String className = filePath.substring(filePath.lastIndexOf('/') + 1, filePath.lastIndexOf('.'));
                contents = contents.replaceAll("(\\{className\\})", className);

                // set the package name if it's a java file
                if (fileType.endsWith(".java")) {

                    // first we chop of the filename
                    String packageName = filePath.substring(0, filePath.lastIndexOf('/'));
                    LOG.info(packageName);
                    // then we chop of the projects folder
                    packageName = packageName.replace(RemoteUser.get().getSetting(UserSettings.PROJECT_FOLDER), "");
                    LOG.info(packageName);
                    // then we chop of the leading slash
                    packageName = packageName.substring(1, packageName.length());
                    // the projectname needs to go
                    packageName = packageName.substring(packageName.indexOf('/'), packageName.length());
                    LOG.info(packageName);

                    // replace any src/main
                    packageName = packageName.replace("/src/main/java/", "");

                    // replace src/test
                    packageName = packageName.replace("/src/test/java/", "");

                    LOG.info(packageName);

                    // what we are left with should be the source package
                    // so replace the slashes for dots
                    packageName = packageName.replaceAll("(/)", ".");

                    if (!packageName.isEmpty()) {
                        contents = contents.replaceAll("(\\{packageName\\})", packageName);
                    }
                }

                // write it
                FileUtils.writeStringToFile(file, contents);

            } else {

                // just create the directory
                file.mkdirs();

            }

        } catch (IOException e) {
            LOG.error(e);
            throw new FilesystemManagerException(e);
        }

        return "untracked";
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
