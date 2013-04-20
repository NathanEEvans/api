package com.stormcloud.ide.api.filesystem;

import com.stormcloud.ide.api.core.dao.IStormCloudDao;
import com.stormcloud.ide.api.core.remote.RemoteUser;
import com.stormcloud.ide.api.core.thread.StreamGobbler;
import com.stormcloud.ide.api.filesystem.exception.FilesystemManagerException;
import com.stormcloud.ide.api.git.IGitManager;
import com.stormcloud.ide.api.git.exception.GitManagerException;
import com.stormcloud.ide.model.factory.MavenModelFactory;
import com.stormcloud.ide.model.factory.exception.MavenModelFactoryException;
import com.stormcloud.ide.model.filesystem.*;
import com.stormcloud.ide.model.project.ProjectType;
import com.stormcloud.ide.model.user.UserSettings;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
    private static final String POM_FILE = "/pom.xml";
    private static final String ANT_BUILD_FILE = "/pom.xml";
    private static final String SETTINGS_XML = "/settings.xml";
    private static final String SOURCE_DIR = "/src/main/java";
    private static final String RESOURCE_DIR = "/src/main/resources";
    private static final String WEB_DIR = "/src/main/webapp";
    private static final String TEST_SOURCE_DIR = "/src/test/java";
    private static final String TEST_RESOURCE_DIR = "/src/test/resources";

    @Override
    public Filesystem getFileTemplates()
            throws FilesystemManagerException {

        Filesystem filesystem = new Filesystem();

        File[] files = listTemplates();

        for (File file : files) {

            Item item = new Item();
            item.setId(file.getAbsolutePath());
            item.setLabel(file.getName());
            item.setType(ItemType.FOLDER);
            item.setStyle("folder");

            filesystem.getChildren().add(item);

            walk(item, file, Filters.getProjectFilter(), false, null);
        }

        return filesystem;
    }

    @Override
    public Filesystem getFilesystem()
            throws FilesystemManagerException {

        Filesystem filesystem = new Filesystem();

        File[] files = listOpenedProjects();

        for (File file : files) {

            Item item = new Item();
            item.setId(file.getAbsolutePath());
            item.setLabel(file.getName());
            item.setType(ItemType.FOLDER);
            item.setStyle("folder");

            filesystem.getChildren().add(item);

            walk(item, file, Filters.getProjectFilter(), false, null);
        }

        return filesystem;
    }

    @Override
    public Filesystem folderPicker(String root)
            throws FilesystemManagerException {

        Filesystem filesystem = new Filesystem();

        File[] files = new File(root).listFiles(
                Filters.getProjectFilter());

        for (File file : files) {

            if (file.isDirectory()) {

                Item item = new Item();
                item.setId(file.getAbsolutePath());
                item.setLabel(file.getName());
                item.setType(ItemType.FOLDER);
                item.setStyle("folder");

                filesystem.getChildren().add(item);

                walkDirs(item, file, Filters.getProjectFilter());
            }
        }

        return filesystem;
    }

    /**
     *
     * @return @throws FilesystemManagerException
     */
    @Override
    public Set<Item> getProjects()
            throws FilesystemManagerException {

        // create return set
        Set<Item> items = new LinkedHashSet<Item>(0);

        // add filesystem root
        Item root = new Item();
        root.setDirectory(true);
        root.setId("root");
        root.setLabel("root");
        root.setParent(null);
        root.setStatus("root");
        root.setStyle("root");
        root.setType(ItemType.NONE);

        items.add(root);

        // get project folders
        File[] files = new File(
                RemoteUser.get().getSetting(
                UserSettings.PROJECT_FOLDER)).listFiles(
                Filters.getProjectFilter());

        // loop trough the projects
        for (File file : files) {

            // create and add project
            Item project = new Item();
            project.setDirectory(true);
            project.setId(file.getAbsolutePath());
            project.setParent("root");
            project.setStatus("");
            project.setType(ItemType.OPENED_PROJECT);

            items.add(project);

            // get some data from the pom for better presentation
            if (new File(file.getAbsolutePath() + POM_FILE).exists()) {

                try {

                    // parse the pom
                    Model pom = MavenModelFactory.getProjectModel(
                            new File(file.getAbsolutePath() + POM_FILE));

                    // get the packaging type
                    String packaging = pom.getPackaging();

                    // set the packagin type if available
                    // jar is the default when not available
                    if (packaging != null && !packaging.isEmpty()) {

                        project.setStyle(packaging + "Project");

                    } else {

                        project.setStyle("jarProject");
                    }

                    // add the project name from the pom if it exists
                    // otherwise use the folder name.
                    if (pom.getName() == null || pom.getName().isEmpty()) {

                        project.setLabel(file.getName());

                    } else {

                        project.setLabel(pom.getName());
                    }


                    // if it's a pom we have possible modules
                    if (packaging.equals("pom")) {

                        // create a folder as modules placeholder
                        Item modulesFolder = new Item();
                        modulesFolder.setId(project.getId() + "/modules");
                        modulesFolder.setParent(project.getId());
                        modulesFolder.setDirectory(true);
                        modulesFolder.setLabel("Modules");
                        modulesFolder.setType(ItemType.FOLDER);
                        modulesFolder.setStyle("modules");

                        items.add(modulesFolder);

                        // loop trough the modules
                        if (pom.getModules() != null) {

                            List<String> modules = pom.getModules().getModule();

                            for (String moduleName : modules) {

                                // process each module
                                File moduleDir =
                                        new File(
                                        file.getAbsolutePath() + "/" + moduleName);

                                if (moduleDir.exists()) {

                                    // add the module
                                    Item module = new Item();
                                    module.setId(moduleDir.getAbsolutePath());
                                    module.setParent(modulesFolder.getId());
                                    module.setType(ItemType.FOLDER);

                                    // parse the pom
                                    Model modulePom = MavenModelFactory.getProjectModel(
                                            new File(moduleDir.getAbsolutePath() + POM_FILE));

                                    // get the packaging type
                                    String modulePackaging = modulePom.getPackaging();

                                    // set the packagin type if available
                                    // jar is the default when not available
                                    if (modulePackaging != null && !modulePackaging.isEmpty()) {

                                        module.setStyle(modulePackaging + "Project");

                                    } else {

                                        module.setStyle("jarProject");
                                    }

                                    // add the project name from the pom if it exists
                                    // otherwise use the folder name.
                                    if (modulePom.getName() == null || modulePom.getName().isEmpty()) {

                                        module.setLabel(moduleDir.getName());

                                    } else {

                                        module.setLabel(modulePom.getName());
                                    }

                                    items.add(module);

                                    processProject(items, moduleDir);
                                }
                            }
                        }

                        // add the project files
                        Item projectFiles = new Item();
                        projectFiles.setId(project.getId() + "/projectFiles");
                        projectFiles.setParent(project.getId());
                        projectFiles.setLabel("Project Files");
                        projectFiles.setType(ItemType.FOLDER);
                        projectFiles.setStyle("projectFiles");
                        projectFiles.setDirectory(true);

                        items.add(projectFiles);

                        Item pomFile = new Item();
                        pomFile.setId(project.getId() + POM_FILE);
                        pomFile.setParent(projectFiles.getId());
                        pomFile.setType(ItemType.FILE);
                        pomFile.setStyle("xml");
                        pomFile.setLabel("pom.xml");
                        pomFile.setDirectory(false);

                        items.add(pomFile);

                        Item settings = new Item();
                        settings.setId(RemoteUser.get().getSetting(UserSettings.LOCAL_MAVEN_REPOSITORY) + SETTINGS_XML);
                        settings.setParent(projectFiles.getId());
                        settings.setLabel("settings.xml");
                        settings.setType(ItemType.FILE);
                        settings.setStyle("xml");
                        settings.setDirectory(false);

                        items.add(settings);

                    } else {

                        // otherwise it's a single project
                        processProject(items, file);
                    }


                } catch (MavenModelFactoryException e) {
                    LOG.error(e);
                    throw new FilesystemManagerException(e);
                }
            }
        }

        // no projects available
        if (files.length == 0) {

            Item item = new Item();
            item.setId(null);
            item.setParent(root.getId());
            item.setLabel("No Projects Available");
            item.setType(ItemType.NONE);
            item.setStyle("noAvailableProjects");

            items.add(item);
        }

        return items;
    }

    private void processProject(
            Set<Item> root,
            File project)
            throws FilesystemManagerException {

        if (new File(project.getAbsolutePath() + WEB_DIR).exists()) {

            Item webapp = new Item();
            webapp.setId(project.getAbsolutePath() + WEB_DIR);
            webapp.setParent(project.getAbsolutePath());
            webapp.setLabel("Web Pages");
            webapp.setType(ItemType.FOLDER);
            webapp.setStyle("webapp");

            root.add(webapp);

            walk(root, new File(project.getAbsolutePath() + WEB_DIR), Filters.getProjectFilter());
        }

        // if there is a source dir process it
        if (new File(project.getAbsolutePath() + SOURCE_DIR).exists()) {

            Item sources = new Item();
            sources.setId(project.getAbsolutePath() + SOURCE_DIR);
            sources.setParent(project.getAbsolutePath());
            sources.setLabel("Source Packages");
            sources.setType(ItemType.FOLDER);
            sources.setStyle("sources");
            sources.setDirectory(true);

            root.add(sources);

            walk(root, new File(project.getAbsolutePath() + SOURCE_DIR), Filters.getProjectFilter());
        }

        // if there is a test source dir, process it
        if (new File(project.getAbsolutePath() + TEST_SOURCE_DIR).exists()) {

            Item sources = new Item();
            sources.setId(project.getAbsolutePath() + TEST_SOURCE_DIR);
            sources.setParent(project.getAbsolutePath());
            sources.setLabel("Test Packages");
            sources.setType(ItemType.FOLDER);
            sources.setStyle("sources");
            sources.setDirectory(true);

            root.add(sources);

            walk(root, new File(project.getAbsolutePath() + TEST_SOURCE_DIR), Filters.getProjectFilter());
        }

        // if there is a resources dir process it
        if (new File(project.getAbsolutePath() + RESOURCE_DIR).exists()) {

            Item resources = new Item();
            resources.setId(project.getAbsolutePath() + RESOURCE_DIR);
            resources.setParent(project.getAbsolutePath());
            resources.setLabel("Other Sources");
            resources.setType(ItemType.FOLDER);
            resources.setStyle("resources");
            resources.setDirectory(true);

            root.add(resources);

            walk(root, new File(project.getAbsolutePath() + RESOURCE_DIR), Filters.getProjectFilter());
        }

        // if there is test resources dir dir, process it
        if (new File(project.getAbsolutePath() + TEST_RESOURCE_DIR).exists()) {

            Item resources = new Item();
            resources.setId(project.getAbsolutePath() + TEST_RESOURCE_DIR);
            resources.setParent(project.getAbsolutePath());
            resources.setLabel("Other Test Sources");
            resources.setType(ItemType.FOLDER);
            resources.setStyle("resources");
            resources.setDirectory(true);

            root.add(resources);

            walk(root, new File(project.getAbsolutePath() + TEST_RESOURCE_DIR), Filters.getProjectFilter());
        }

        // add the project files
        Item projectFiles = new Item();
        projectFiles.setId(project.getAbsolutePath() + "/projectFiles");
        projectFiles.setParent(project.getAbsolutePath());
        projectFiles.setLabel("Project Files");
        projectFiles.setType(ItemType.FOLDER);
        projectFiles.setStyle("projectFiles");
        projectFiles.setDirectory(true);

        root.add(projectFiles);

        Item pomFile = new Item();
        pomFile.setId(project.getAbsolutePath() + POM_FILE);
        pomFile.setParent(projectFiles.getId());
        pomFile.setType(ItemType.FILE);
        pomFile.setStyle("xml");
        pomFile.setLabel("pom.xml");
        pomFile.setDirectory(false);

        root.add(pomFile);

        Item settings = new Item();
        settings.setId(RemoteUser.get().getSetting(UserSettings.LOCAL_MAVEN_REPOSITORY) + SETTINGS_XML);
        settings.setParent(projectFiles.getId());
        settings.setLabel("settings.xml");
        settings.setType(ItemType.FILE);
        settings.setStyle("xml");
        settings.setDirectory(false);

        root.add(settings);
    }

    private void walk(
            Set<Item> root,
            File dir,
            FilenameFilter filter)
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
                item.setParent(file.getParent());
                item.setDirectory(file.isDirectory());
                item.setLabel(file.getName());
                item.setStyle(getStyle(file));

                if (file.isDirectory()) {

                    item.setType(ItemType.FOLDER);

                } else {

                    item.setType(ItemType.FILE);
                }

                root.add(item);

                if (file.isDirectory()) {

                    walk(root, file, filter);
                }
            }
        }
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

        // loop trough the project folders
        for (File file : files) {

            if (!opened) {

                // project is closed and closed requested
                // only add project folder
                Item item = new Item();
                item.setId(file.getAbsolutePath());
                item.setType(ItemType.CLOSED_PROJECT);
                item.setDirectory(true);

                if (new File(file.getAbsolutePath() + POM_FILE).exists()) {

                    try {

                        Model pom = MavenModelFactory.getProjectModel(
                                new File(file.getAbsolutePath() + POM_FILE));

                        if (pom.getName() == null || pom.getName().isEmpty()) {

                            item.setLabel(file.getName());

                        } else {

                            item.setLabel(pom.getName());
                        }

                        if (pom.getPackaging() != null && !pom.getPackaging().isEmpty()) {

                            item.setStyle(pom.getPackaging());

                        } else {

                            item.setStyle("jar");
                        }

                    } catch (MavenModelFactoryException e) {
                        LOG.error(e);
                        throw new FilesystemManagerException(e);
                    }
                } else {


                    // @todo test if it's an Ant project

                    // else @todo test if it's a PHP project

                    // else @todo open as plain 'free' project
                    //            which will also include html/javascript
                    //            projects and gives the advantage that a
                    //            project is always opened so you might be
                    //            able to fix your 'mis-understood' project.


                    // No pom.xml found where expected
                    // mark as malformed
                    item.setId("");
                    item.setLabel(file.getName() + " [Malformed Project!]");
                    item.setType(ItemType.MALFORMED_PROJECT);
                    item.setStyle("brokenProject");
                }

                filesystem.getChildren().add(item);

            } else {

                // project is openend and open projects are requested
                // process the project

                // check for pom file to validate if the project is
                // well-formed, when pom not found return malformed project
                if (new File(file.getAbsolutePath() + POM_FILE).exists()) {

                    // read the pom to see what type of project it is
                    Model pom;

                    try {

                        pom = MavenModelFactory.getProjectModel(
                                new File(file.getAbsolutePath() + POM_FILE));

                        String packaging = pom.getPackaging();

                        // check packaging to determine what type of
                        // processing we need (single or multi)
                        if (packaging.equals("pom")) {

                            Item project = processModule(file, true);

                            // a parent pom, nested modules
                            Item modulesFolder = new Item();
                            modulesFolder.setId("");
                            modulesFolder.setDirectory(true);
                            modulesFolder.setLabel("Modules");
                            modulesFolder.setType(ItemType.FOLDER);
                            modulesFolder.setStyle("modules");

                            if (gitManager.isModified(file.getAbsolutePath())) {
                                project.setStatus("modified");
                                modulesFolder.setStatus("modified");
                            }


                            // get the defined modules
                            if (pom.getModules() != null) {

                                List<String> modules = pom.getModules().getModule();

                                for (String module : modules) {

                                    // process each module
                                    File moduleDir =
                                            new File(
                                            file.getAbsolutePath() + "/" + module);

                                    if (moduleDir.exists()) {

                                        Item projectModule =
                                                processModule(moduleDir, false);

                                        String status = gitManager.getStatus(moduleDir, RemoteUser.get().getSetting(UserSettings.USER_HOME));
                                        projectModule.setStatus(status);

                                        modulesFolder.getChildren().add(projectModule);
                                    }
                                }
                            }

                            project.getChildren().add(modulesFolder);

                            // done with the modules add the root pom
                            // so it ends up last

                            Item projectFiles = new Item();
                            projectFiles.setId("");
                            projectFiles.setLabel("Project Files");
                            projectFiles.setType(ItemType.FOLDER);
                            projectFiles.setStyle("projectFiles");
                            projectFiles.setDirectory(true);


                            Item pomFile = new Item();
                            pomFile.setId(file.getAbsolutePath() + POM_FILE);
                            pomFile.setType(ItemType.FILE);
                            pomFile.setStyle("xml");
                            pomFile.setLabel("pom.xml");
                            String status = gitManager.getStatus(pomFile.getId(), RemoteUser.get().getSetting(UserSettings.USER_HOME));
                            pomFile.setStatus(status);
                            projectFiles.setStatus(status);
                            projectFiles.getChildren().add(pomFile);

                            Item settings = new Item();
                            settings.setId(RemoteUser.get().getSetting(UserSettings.LOCAL_MAVEN_REPOSITORY) + SETTINGS_XML);
                            settings.setLabel("settings.xml");
                            settings.setType(ItemType.FILE);
                            settings.setStyle("xml");

                            projectFiles.getChildren().add(settings);

                            project.getChildren().add(projectFiles);

                            filesystem.getChildren().add(project);

                        } else {

                            // single project, no nested modules
                            Item project =
                                    processModule(file, false);

                            if (gitManager.isModified(file.getAbsolutePath())) {
                                project.setStatus("modified");
                            }

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
                    item.setId("");
                    item.setLabel(file.getName() + " [Malformed Project!]");
                    item.setType(ItemType.MALFORMED_PROJECT);
                    item.setStyle("brokenProject");

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
            item.setType(ItemType.NONE);
            item.setStyle("noAvailableProjects");

            filesystem.getChildren().add(item);
        }

        return filesystem;
    }

    /**
     * Retrieve the project File array from the PROJECT_FOLDER.
     *
     * @return File Array of project 'roots'
     */
    public File[] listOpenedProjects() {

        return new File(
                RemoteUser.get().getSetting(
                UserSettings.PROJECT_FOLDER)).listFiles(
                Filters.getProjectFilter());
    }

    /**
     * Retrieve the project FIle array from the CLOSED_PROJECT_FOLDER.
     *
     * @return File array of project 'roots'
     */
    public File[] listClosedProjects() {

        return new File(
                RemoteUser.get().getSetting(
                UserSettings.CLOSED_PROJECTS_FOLDER)).listFiles(
                Filters.getProjectFilter());
    }

    /**
     * Retrieve the available File Template folders.
     *
     * @return File array of template 'roots'
     */
    public File[] listTemplates() {

        return new File(
                RemoteUser.get().getSetting(
                UserSettings.FILE_TEMPLATE_FOLDER)).listFiles(
                Filters.getProjectFilter());
    }

    /**
     * Determines what type of project the file represents.
     *
     * Either a MAVEN, ant or 'generic' project. Generic can be any
     * HTML/PHP/JavaScript project. It's a 'free form project, no compiling or
     * 'building' is needed, the project type is specific to how it's run not
     * how we should present the layout.
     *
     * @param file
     * @return ProjectType
     */
    public ProjectType getProjectType(File file) {

        // it has to be a directory
        if (!file.isDirectory()) {
            return ProjectType.INVALID;
        }

        // check if it's a maven project
        if (new File(file.getAbsolutePath() + POM_FILE).exists()) {
            return ProjectType.MAVEN;
        }

        // check if it's an ant project
        if (new File(file.getAbsolutePath() + ANT_BUILD_FILE).exists()) {
            return ProjectType.ANT;
        }

        return ProjectType.GENERIC;
    }

    private Item processModule(
            File dir,
            boolean root)
            throws FilesystemManagerException, MavenModelFactoryException, GitManagerException {

        // create new project object
        Item project = new Item();
        project.setId(dir.getAbsolutePath());
        project.setDirectory(true);
        project.setType(ItemType.OPENED_PROJECT);

        if (!new File(dir.getAbsolutePath() + POM_FILE).exists()) {

            // No pom.xml found where expected
            // mark as malformed
            Item item = new Item();
            item.setId("");
            item.setLabel(dir.getName() + " [Malformed Project!]");
            item.setType(ItemType.MALFORMED_PROJECT);
            item.setStyle("brokenProject");
            return item;
        }

        Model pom = MavenModelFactory.getProjectModel(new File(dir.getAbsolutePath() + POM_FILE));


        // use name from the pom when available
        // otherwise the folder name
        if (pom.getName() == null || pom.getName().isEmpty()) {

            project.setLabel(dir.getName());

        } else {

            project.setLabel(pom.getName());
        }


        // set the project type based on the packaging

        if (pom.getPackaging() != null && !pom.getPackaging().isEmpty()) {

            project.setStyle(pom.getPackaging() + "Project");

        } else {

            project.setStyle("jarProject");
        }

        String userHome = RemoteUser.get().getSetting(UserSettings.USER_HOME);

        // if there is a webapp dir, process it
        if (new File(dir.getAbsolutePath() + WEB_DIR).exists()) {

            Item webapp = new Item();
            webapp.setId(dir.getAbsolutePath() + WEB_DIR);
            webapp.setLabel("Web Pages");
            webapp.setType(ItemType.FOLDER);
            webapp.setStyle("webapp");

            String status = gitManager.getStatus(dir.getAbsolutePath() + WEB_DIR, userHome);
            webapp.setStatus(status);

            project.getChildren().add(webapp);

            walk(webapp, new File(dir.getAbsolutePath() + WEB_DIR), Filters.getProjectFilter(), true, null);
        }

        // if there is a source dir process it
        if (new File(dir.getAbsolutePath() + SOURCE_DIR).exists()) {

            Item sources = new Item();
            sources.setId(dir.getAbsolutePath() + SOURCE_DIR);
            sources.setLabel("Source Packages");
            sources.setType(ItemType.FOLDER);
            sources.setStyle("sources");
            sources.setDirectory(true);

            String status = gitManager.getStatus(dir.getAbsolutePath() + SOURCE_DIR, userHome);
            sources.setStatus(status);

            project.getChildren().add(sources);

            walk(sources, new File(dir.getAbsolutePath() + SOURCE_DIR), Filters.getProjectFilter(), true, "package");
        }

        // if there is a test source dir, process it
        if (new File(dir.getAbsolutePath() + TEST_SOURCE_DIR).exists()) {

            Item sources = new Item();
            sources.setId(dir.getAbsolutePath() + TEST_SOURCE_DIR);
            sources.setLabel("Test Packages");
            sources.setType(ItemType.FOLDER);
            sources.setStyle("sources");
            sources.setDirectory(true);

            String status = gitManager.getStatus(dir.getAbsolutePath() + TEST_SOURCE_DIR, userHome);
            sources.setStatus(status);

            project.getChildren().add(sources);

            walk(sources, new File(dir.getAbsolutePath() + TEST_SOURCE_DIR), Filters.getProjectFilter(), true, "package");
        }

        // if there is a resources dir process it
        if (new File(dir.getAbsolutePath() + RESOURCE_DIR).exists()) {

            Item resources = new Item();
            resources.setId(dir.getAbsolutePath() + RESOURCE_DIR);
            resources.setLabel("Other Sources");
            resources.setType(ItemType.FOLDER);
            resources.setStyle("resources");
            resources.setDirectory(true);

            String status = gitManager.getStatus(dir.getAbsolutePath() + RESOURCE_DIR, userHome);
            resources.setStatus(status);

            project.getChildren().add(resources);

            walk(resources, new File(dir.getAbsolutePath() + RESOURCE_DIR), Filters.getProjectFilter(), true, null);
        }

        // if there is test resources dir dir, process it
        if (new File(dir.getAbsolutePath() + TEST_RESOURCE_DIR).exists()) {

            Item resources = new Item();
            resources.setId(dir.getAbsolutePath() + TEST_RESOURCE_DIR);
            resources.setLabel("Other Test Sources");
            resources.setType(ItemType.FOLDER);
            resources.setStyle("resources");
            resources.setDirectory(true);

            String status = gitManager.getStatus(dir.getAbsolutePath() + TEST_RESOURCE_DIR, userHome);
            resources.setStatus(status);

            project.getChildren().add(resources);

            walk(resources, new File(dir.getAbsolutePath() + TEST_RESOURCE_DIR), Filters.getProjectFilter(), true, null);
        }


        // Dependencies
        // Test Dependencies
        // Java Dependencies


        // add the project files at the bottom
        if (!root) {

            Item projectFiles = new Item();
            projectFiles.setId("");
            projectFiles.setLabel("Project Files");
            projectFiles.setType(ItemType.FOLDER);
            projectFiles.setStyle("projectFiles");
            projectFiles.setDirectory(true);

            Item pomFile = new Item();
            pomFile.setId(dir.getAbsolutePath() + POM_FILE);
            pomFile.setType(ItemType.FILE);
            pomFile.setStyle("xml");
            pomFile.setLabel("pom.xml");

            String status = gitManager.getStatus(pomFile.getId(), RemoteUser.get().getSetting(UserSettings.USER_HOME));
            pomFile.setStatus(status);
            projectFiles.setStatus(status);
            projectFiles.getChildren().add(pomFile);

            Item settings = new Item();
            settings.setId(RemoteUser.get().getSetting(UserSettings.LOCAL_MAVEN_REPOSITORY) + SETTINGS_XML);
            settings.setLabel("settings.xml");
            settings.setType(ItemType.FILE);
            settings.setStyle("xml");

            projectFiles.getChildren().add(settings);

            project.getChildren().add(projectFiles);
        }

        return project;
    }

    private void walk(
            Item current,
            File dir,
            FilenameFilter filter,
            boolean versioning,
            String style)
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
                item.setDirectory(file.isDirectory());

                if (versioning) {

                    try {

                        String status = gitManager.getStatus(item.getId(), RemoteUser.get().getSetting(UserSettings.USER_HOME));
                        item.setStatus(status);

                    } catch (GitManagerException e) {
                        throw new FilesystemManagerException(e);
                    }
                }

                item.setLabel(file.getName());
                item.setStyle(getStyle(file));

                if (file.isDirectory()) {

                    item.setType(ItemType.FOLDER);

                    if (style != null) {
                        item.setStyle(style);
                    }

                    walk(item, file, filter, versioning, style);

                } else {

                    item.setType(ItemType.FILE);
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
                    item.setType(ItemType.FOLDER);
                    item.setStyle("folder");
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

        try {

            if (file.isDirectory()) {

                try {

                    FileUtils.moveDirectory(
                            file,
                            new File(RemoteUser.get().getSetting(
                            UserSettings.TRASH_FOLDER)
                            + "/"
                            + file.getName()));

                } catch (FileExistsException e) {

                    LOG.info("Destination already exists, appending Date.");

                    // when a project with the same name is in there,
                    // add the date to make it unique
                    FileUtils.moveDirectory(
                            file,
                            new File(RemoteUser.get().getSetting(
                            UserSettings.TRASH_FOLDER)
                            + "/"
                            + file.getName() + "-" + new Date()));
                }

            } else {

                try {

                    FileUtils.moveFile(
                            file,
                            new File(RemoteUser.get().getSetting(
                            UserSettings.TRASH_FOLDER)
                            + "/"
                            + file.getName()));

                } catch (FileExistsException e) {

                    LOG.info("Destination already exists, appending Date.");
                    FileUtils.moveFile(
                            file,
                            new File(RemoteUser.get().getSetting(
                            UserSettings.TRASH_FOLDER)
                            + "/"
                            + file.getName() + "-" + new Date()));
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
            item.setLabel(file.getName());
            item.setStyle(getStyle(file));

            if (file.isDirectory()) {

                item.setType(ItemType.FOLDER);

            } else {

                item.setType(ItemType.FILE);
            }

            filesystem.getChildren().add(item);
        }

        if (filesystem.getChildren().isEmpty()) {

            Item item = new Item();
            item.setId("none");
            item.setLabel("Your Trash is Empty!");
            item.setType(ItemType.NONE);
            item.setStyle("noTrash");

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
    public int move(String srcFilePath, String destFilePath)
            throws FilesystemManagerException {

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
    public int save(
            Save save)
            throws FilesystemManagerException {

        File file = new File(save.getFilePath());

        try {

            FileUtils.writeStringToFile(file, save.getContents());

        } catch (IOException e) {
            LOG.error(e);
            throw new FilesystemManagerException(e);
        }

        return 0;
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
                item.setType(ItemType.FILE);
                item.setStyle(getStyle(new File(fileName)));

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

    private String getStyle(File file) {

        if (file.isDirectory()) {

            return "folder";

        } else {

            return FilenameUtils.getExtension(file.getName());
        }
    }

    @Override
    public int create(
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

        return 0;
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
