package com.stormcloud.ide.api.filesystem;

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
import com.stormcloud.ide.model.project.ProjectType;
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
            item.setType("folder");

            filesystem.getChildren().add(item);

            walk(item, file, Filters.getProjectFilter(), false);
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
            item.setType("folder");

            filesystem.getChildren().add(item);

            walk(item, file, Filters.getProjectFilter(), false);
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

        // loop trough the project folders
        for (File file : files) {

            if (!opened) {

                // project is closed and closed requested
                // only add project folder
                Item item = new Item();
                item.setId(file.getAbsolutePath());
                item.setType("closedProject");
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
                            pomFile.setId(file.getAbsolutePath() + POM_FILE);
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
        project.setType("project");
        project.setDirectory(true);

        if (!new File(dir.getAbsolutePath() + POM_FILE).exists()) {

            // No pom.xml found where expected
            // mark as malformed
            Item item = new Item();
            item.setId("none");
            item.setLabel(dir.getName() + "[Malformed Project!]");
            item.setType("malformedProject");
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

        String userHome = RemoteUser.get().getSetting(UserSettings.USER_HOME);

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
            sources.setDirectory(true);

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
            resources.setDirectory(true);

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
            sources.setDirectory(true);

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
            resources.setDirectory(true);

            String status = gitManager.getStatus(dir.getAbsolutePath() + TEST_RESOURCE_DIR, userHome);
            resources.setStatus(status);

            project.getChildren().add(resources);

            walk(resources, new File(dir.getAbsolutePath() + TEST_RESOURCE_DIR), Filters.getProjectFilter(), true);
        }

        // add the pom, at the bottom
        if (!root) {
            Item pomFile = new Item();
            pomFile.setId(dir.getAbsolutePath() + POM_FILE);
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
                item.setType(getFileType(file));

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
            item.setLabel(file.getName());
            item.setType(getFileType(file));

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
                item.setType(getFileType(new File(fileName)));

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

    private String getFileType(File file) {


        if (file.isDirectory()) {

            return "folder";

        } else if (file.getName().endsWith(".java")) {

            return "javaFile";

        } else if (file.getName().endsWith(".jsp")) {

            return "jspFile";

        } else if (file.getName().endsWith(".xml")) {

            return "xmlFile";

        } else if (file.getName().endsWith(".wsdl")) {

            return "wsdlFile";

        } else if (file.getName().endsWith(".xsd")) {

            return "xsdFile";

        } else if (file.getName().endsWith(".html")) {

            return "htmlFile";

        } else if (file.getName().endsWith(".xhtml")) {

            return "xhtmlFile";

        } else if (file.getName().endsWith(".txt")) {

            return "textFile";

        } else if (file.getName().endsWith(".tld")) {

            return "tldFile";

        } else if (file.getName().endsWith(".png")
                || file.getName().endsWith(".gif")
                || file.getName().endsWith(".jpg")
                || file.getName().endsWith(".jpeg")
                || file.getName().endsWith(".tiff")
                || file.getName().endsWith(".bmp")) {

            return "imageFile";

        } else if (file.getName().endsWith(".js")) {

            return "jsFile";

        } else if (file.getName().endsWith(".css")) {

            return "cssFile";

        } else if (file.getName().endsWith(".sql")) {

            return "sqlFile";

        } else if (file.getName().endsWith(".properties")) {

            return "propertiesFile";

        } else if (file.getName().endsWith(".MF")) {

            return "manifestFile";

        } else if (file.getName().endsWith(".yaml")) {

            return "yamlFile";

        } else if (file.getName().endsWith(".sh")) {

            return "bashFile";


        } else {

            return "textFile";
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
