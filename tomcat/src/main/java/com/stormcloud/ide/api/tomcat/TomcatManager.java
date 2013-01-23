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
import com.stormcloud.ide.api.core.dao.IStormCloudDao;
import com.stormcloud.ide.api.core.entity.User;
import com.stormcloud.ide.api.core.remote.RemoteUser;
import com.stormcloud.ide.model.filesystem.Item;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;

/**
 *
 * @author martijn
 */
public class TomcatManager implements ITomcatManager {

    private IStormCloudDao dao;

    @Override
    public Item getTomcat() {

        User user = dao.getUser(RemoteUser.get().getUserName());

        Item tomcat = new Item();
        tomcat.setLabel("Tomcat");
        tomcat.setType("tomcat");

        Item webapps = new Item();
        webapps.setId(user.getHomeFolder() + "/tomcat/latest/webapps");
        webapps.setLabel("Web Applications");
        webapps.setType("tomcatWebApps");

        tomcat.getChildren().add(webapps);

        File webappsDir = new File(webapps.getId());

        for (File file : webappsDir.listFiles()) {

            Item app = new Item();
            app.setId(file.getAbsolutePath());
            app.setLabel(file.getName());
            app.setType("tomcatApp");

            webapps.getChildren().add(app);

            walk(app, file, null);

        }

        Item lib = new Item();
        lib.setId(user.getHomeFolder() + "/tomcat/latest/lib");
        lib.setLabel("lib");
        lib.setType("tomcatLib");

        tomcat.getChildren().add(lib);

        walk(lib, new File(lib.getId()), null);

        return tomcat;
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

    public IStormCloudDao getDao() {
        return dao;
    }

    public void setDao(IStormCloudDao dao) {
        this.dao = dao;
    }
}