package com.stormcloud.ide.api.maven;

/*
 * #%L Stormcloud IDE - API - Maven %% Copyright (C) 2012 - 2013 Stormcloud IDE
 * %% This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
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
import com.stormcloud.ide.api.core.entity.Archetype;
import com.stormcloud.ide.api.core.entity.ArchetypeCatalog;
import com.stormcloud.ide.api.core.remote.RemoteUser;
import com.stormcloud.ide.api.maven.exception.MavenManagerException;
import com.stormcloud.ide.api.core.thread.StreamGobbler;
import com.stormcloud.ide.model.maven.Project;
import com.stormcloud.ide.model.user.UserSettings;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;

public class MavenManager implements IMavenManager {

    private Logger LOG = Logger.getLogger(getClass());
    private static final String BASH = "/bin/sh";
    private static final String COMMAND = "-c";
    private static final String MAVEN_EXECUTABLE = "/usr/local/maven/bin/mvn";
    private static final String MAVEN_POM = "pom.xml";
    private IStormCloudDao dao;

    @Override
    public ArchetypeCatalog[] getCatalog() {

        LOG.info("Get Catalog");

        List<Archetype> archetypes = dao.getCatalog();

        LOG.debug("Retrieved " + archetypes.size() + " Archetypes");

        ArchetypeCatalog[] catalog = new ArchetypeCatalog[1];

        Set<Archetype> arch = new LinkedHashSet<Archetype>(archetypes);

        ArchetypeCatalog ac = new ArchetypeCatalog();
        ac.getChildren().addAll(arch);

        catalog[0] = ac;

        return catalog;
    }

    @Override
    public int createProject(Project project) throws MavenManagerException {

        LOG.info("Create Project " + project.getProjectName());

        String logHome = RemoteUser.get().getSetting(UserSettings.LOG_FOLDER);
        String projectHome = RemoteUser.get().getSetting(UserSettings.PROJECT_FOLDER);

        int exitVal = 1;

        try {

            /**
             * First clear any previous log file
             */
            String[] clear = {
                BASH,
                COMMAND,
                "echo ... > " + logHome + "/maven.log"};

            Process proc = Runtime.getRuntime().exec(clear);

            // any error message?
            StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");

            // any output?
            StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");

            // kick them off
            errorGobbler.start();
            outputGobbler.start();

            // any error???
            exitVal = proc.waitFor();

            LOG.info("Clear file " + logHome + "/maven.log, status " + exitVal);

            if (exitVal != 0) {
                // ran into crap, return immediatly
                return exitVal;
            }

            // parameterize archetype values (version, id, groupId)
            // parameterize project root dir (now /data/maven)
            String command =
                    " cd " + projectHome + " ; "
                    + MAVEN_EXECUTABLE
                    + " "
                    + " archetype:generate -DarchetypeGroupId="
                    + project.getArchetypeGroupId()
                    + " -DarchetypeArtifactId="
                    + project.getArchetypeArtifactId()
                    + " -DarchetypeVersion="
                    + project.getArchetypeVersion()
                    + " -DinteractiveMode=false"
                    + " -DgroupId=\""
                    + project.getGroupId()
                    + "\" -DartifactId=\""
                    + project.getArtifactId()
                    + "\" -Dpackage=\""
                    + project.getJavaPackage()
                    + "\" -DprojectName=\""
                    + project.getProjectName()
                    + "\" -Dversion=\""
                    + project.getVersion()
                    + "\" -DprojectDescription=\""
                    + project.getDescription()
                    + "\" -DmuleVersion=3.2.1"
                    + " > " + logHome + "/maven.log ; ";

            if (!project.getArtifactId().equals(project.getProjectName())) {

                command += " mv \"" + projectHome + "/" + project.getArtifactId() + "\" \"" + projectHome + "/" + project.getProjectName() + "\"";
            }

            /**
             * Now run the command
             */
            String[] run = {
                BASH,
                COMMAND,
                command};

            LOG.info("Execute " + BASH + " " + COMMAND + " " + command);

            proc = Runtime.getRuntime().exec(run);

            // any error message?
            errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");

            // any output?
            outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");

            // kick them off
            errorGobbler.start();
            outputGobbler.start();

            // any error???
            exitVal = proc.waitFor();

            LOG.info("Create Maven project " + project.getProjectName() + ", status " + proc.exitValue());

            if (exitVal != 0) {
                // ran into crap, return immediatly
                return exitVal;
            }

            command = "cd \"" + projectHome + "/" + project.getProjectName() + "\" ; git init";

            String[] git = {
                BASH,
                COMMAND,
                command};

            LOG.info("Execute " + BASH + " " + COMMAND + " " + command);

            proc = Runtime.getRuntime().exec(git);

            // any error message?
            errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");

            // any output?
            outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");

            // kick them off
            errorGobbler.start();
            outputGobbler.start();

            // any error???
            exitVal = proc.waitFor();

            LOG.info("Init Git Repository " + project.getProjectName() + ", status " + proc.exitValue());


            return exitVal;

        } catch (IOException e) {
            throw new MavenManagerException(e);
        } catch (InterruptedException e) {
            throw new MavenManagerException(e);
        }
    }

    @Override
    public int renameProject() throws MavenManagerException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int execute(String command, String filePath)
            throws MavenManagerException {


        LOG.debug(
                "Executing on filePath[" + filePath + "] for user[" + RemoteUser.get().getUserName() + "]");

        String logfile = RemoteUser.get().getSetting(UserSettings.LOG_FOLDER) + "/maven.log";

        try {

            /**
             * First clear any previous log files
             */
            String[] clear = {
                BASH,
                COMMAND,
                "echo ... > \"" + logfile + "\""};

            Process proc = Runtime.getRuntime().exec(clear);

            // any error message?
            StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");

            // any output?
            StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");

            // kick them off
            errorGobbler.start();
            outputGobbler.start();

            // any error???
            int exitVal = proc.waitFor();

            LOG.info("Cleared file " + logfile + ", status " + exitVal);

            /**
             * Now run the command
             */
            String[] run = {
                BASH,
                COMMAND,
                command(command, filePath, logfile)};

            LOG.info("Execute " + BASH + " " + COMMAND + " " + command(command, filePath, logfile));

            proc = Runtime.getRuntime().exec(run);

            // any error message?
            errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");

            // any output?
            outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");

            // kick them off
            errorGobbler.start();
            outputGobbler.start();

            // any error???
            exitVal = proc.waitFor();

            LOG.info("Maven project " + filePath + ", status " + proc.exitValue());

            return exitVal;

        } catch (IOException e) {
            throw new MavenManagerException(e);
        } catch (InterruptedException e) {
            throw new MavenManagerException(e);
        }
    }

    private String command(String argument, String filePath, String logFIle) {

        return MAVEN_EXECUTABLE
                + " -fae "
                + argument
                + " -f \""
                + filePath
                + "/"
                + MAVEN_POM
                + "\" > \""
                + logFIle
                + "\"";
    }

    public IStormCloudDao getDao() {
        return dao;
    }

    public void setDao(IStormCloudDao dao) {
        this.dao = dao;
    }
}
