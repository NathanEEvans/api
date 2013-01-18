package com.stormcloud.ide.api.tomcat;

/*
 * #%L
 * Stormcloud IDE - API - Tomcat
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

import com.stormcloud.ide.api.tomcat.IMuleManager;
import com.stormcloud.ide.api.tomcat.exception.MuleManagerException;
import com.stormcloud.ide.api.tomcat.thread.StreamGobbler;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

public class MuleManager implements IMuleManager {

    private Logger LOG = Logger.getLogger(getClass());
    public static final String BUILD_PATH = "/target/";
    public static final String MULE_DEPLOY_DIR = "/data/mule/apps";
    private static final String BASH = "/bin/sh";
    private static final String COMMAND = "-c";

    @Override
    public void stop() throws MuleManagerException {

        LOG.debug("Stopping mule...");

        List<String> arguments = new LinkedList<String>();
        arguments.add("stop");

//        MuleThread mule =
//                new MuleThread(
//                "/data/mule",
//                "/data/mule/bin/mule",
//                arguments,
//                1);
//
//        mule.start();
    }

    @Override
    public void start() throws MuleManagerException {

        LOG.debug("Starting mule...");

        List<String> arguments = new LinkedList<String>();
        arguments.add("start");

//        MuleThread mule =
//                new MuleThread(
//                "/data/mule",
//                "/data/mule/bin/mule",
//                arguments,
//                1);
//
//        mule.start();
    }

    @Override
    public String deploy(String filePath) throws MuleManagerException {

        int exitVal = 1;
        String deployedArtifact = null;

        LOG.debug("Deploy filePath[" + filePath + "]");

        File buildPath = new File(filePath + BUILD_PATH);

        LOG.debug("Build Path [" + buildPath.getAbsolutePath() + "]");

        File[] buildProducts = buildPath.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String string) {

                return string.endsWith(".zip");
            }
        });

        LOG.debug("Found " + buildProducts.length + " deployable products.");

        // check if the zip file exists
        if (buildProducts.length != 0) {

            File file = buildProducts[0];

            LOG.debug("Deploying : " + file.getName());

            List<String> arguments = new LinkedList<String>();
            arguments.add(file.getAbsolutePath());
            arguments.add(new File(MULE_DEPLOY_DIR).getAbsolutePath());


            String[] deploy = {
                BASH,
                COMMAND,
                "mv \"" + file.getAbsolutePath() + "\" " + MULE_DEPLOY_DIR};

            try {

                Process proc = Runtime.getRuntime().exec(deploy);

                // any error message?
                StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");

                // any output?
                StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");

                // kick them off
                errorGobbler.start();
                outputGobbler.start();

                // any error???
                exitVal = proc.waitFor();

                LOG.info("Deployed file " + file.getAbsolutePath() + ", status " + exitVal);

                deployedArtifact = file.getName().substring(0, file.getName().lastIndexOf('.'));

            } catch (IOException e) {
                throw new MuleManagerException(e);
            } catch (InterruptedException e) {
                throw new MuleManagerException(e);
            }
        }

        return deployedArtifact;
    }

    @Override
    public void undeploy(String filePath) throws MuleManagerException {

        LOG.debug("Undeploying filePath[" + filePath + "]");

        List<String> arguments = new LinkedList<String>();
        arguments.add("-f");
        arguments.add(filePath + "-anchor.txt");

//        MuleThread mule =
//                new MuleThread(
//                filePath,
//                "rm",
//                arguments,
//                1);
//
//        mule.start();
    }
}
