package com.stormcloud.ide.api;

/*
 * #%L Stormcloud IDE - API - Web %% Copyright (C) 2012 - 2013 Stormcloud IDE %%
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
import com.stormcloud.ide.api.core.remote.RemoteUser;
import com.stormcloud.ide.api.tomcat.ITomcatManager;
import com.stormcloud.ide.api.tomcat.exception.TomcatManagerException;
import com.stormcloud.ide.model.filesystem.Item;
import com.stormcloud.ide.model.services.Services;
import com.stormcloud.ide.model.user.UserSettings;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author martijn
 */
@Controller
@RequestMapping(value = "/services")
public class ServicesController extends BaseController {

    private Logger LOG = Logger.getLogger(getClass());
    @Autowired
    private ITomcatManager tomcatManager;

    @RequestMapping(method = RequestMethod.GET,
    produces = "application/json")
    @ResponseBody
    public Services[] getServices()
            throws TomcatManagerException {

        Services[] response = new Services[1];

        // create root node
        Services services = new Services();

        // create databases
        Item databases = new Item();
        databases.setLabel("Databases");
        databases.setType("rdbms");

        /**
         * @todo change this into rdbmsManager who will fetch database info like
         * tomcatManager does
         */
        Item javaDb = new Item();
        javaDb.setLabel("Java DB");
        javaDb.setType("javadb");
        databases.getChildren().add(javaDb);

        Item mysql = new Item();
        mysql.setLabel("MySQL");
        mysql.setType("mysql");
        databases.getChildren().add(mysql);

        Item oracle = new Item();
        oracle.setLabel("Oracle");
        oracle.setType("oracle");
        databases.getChildren().add(oracle);

        // add databases to services
        services.getChildren().add(databases);


        // Web Services
        Item webservices = new Item();
        webservices.setLabel("Web Services");
        webservices.setType("webServices");

        /**
         * @todo add some example webservices
         */
        // add webservices to services
        services.getChildren().add(webservices);

        // create servers group
        Item servers = new Item();
        servers.setLabel("Servers");
        servers.setType("servers");

        /**
         * @todo add glassfish, jboss, weblogic
         */
        // add tomcat
        servers.getChildren().add(tomcatManager.getTomcat());
        // add servers to services
        services.getChildren().add(servers);

        // Maven Repositories
        Item mavenRepos = new Item();
        mavenRepos.setLabel("Maven Repositories");
        mavenRepos.setType("mavenRepositories");

        // add local repo
        Item local = new Item();
        local.setLabel("Local");
        local.setType("localMavenRepository");

        File m2 = new File(RemoteUser.get().getSetting(UserSettings.LOCAL_MAVEN_REPOSITORY));

        FilenameFilter filter = new FilenameFilter() {

            @Override
            public boolean accept(File file, String string) {

                if (string.startsWith("_")) {
                    return false;
                }

                if (string.endsWith(".lastUpdated")) {
                    return false;
                }

                return true;
            }
        };

        walk(local, m2, filter);

        mavenRepos.getChildren().add(local);

        // add maven repositories
        services.getChildren().add(mavenRepos);

        // Continuous Integration
        Item ci = new Item();
        ci.setLabel("Continuous Integration");
        ci.setType("continuousIntegration");

        // add ci to services
        services.getChildren().add(ci);

        // Issue Trackers
        Item issueTrackers = new Item();
        issueTrackers.setLabel("Issue Trackers");
        issueTrackers.setType("issueTrackers");

        // add issue trackers to services
        services.getChildren().add(issueTrackers);

        response[0] = services;

        return response;
    }

    private void walk(
            Item current,
            File dir,
            FilenameFilter filter) {

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

                if (file.getName().endsWith(".java")) {

                    item.setType("javaFile");
                    item.setLabel(file.getName());

                } else if (file.getName().endsWith(".jar")) {

                    item.setType("jarFile");
                    item.setLabel(file.getName());

                } else if (file.getName().endsWith(".pom")) {

                    item.setType("xmlFile");
                    item.setLabel(file.getName());

                } else if (file.getName().endsWith(".sha1")) {

                    item.setType("sha1File");
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

                    walk(item, file, filter);
                }

                if (current != null) {
                    current.getChildren().add(item);
                }
            }
        }
    }
}
