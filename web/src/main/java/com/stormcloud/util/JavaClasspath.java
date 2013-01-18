package com.stormcloud.util;

/*
 * #%L
 * Stormcloud IDE - API - Web
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 *
 * @author martijn
 */
public class JavaClasspath {

    public static void main(String[] args) {


        try {

            List<String> content = getJarContent("/filesystem/martijn/java/classes.jar");

            for (String file : content) {

                if (file.endsWith(".class")) {

                    String name = file.replaceAll("/", ".").substring(0, file.lastIndexOf("."));
                    String clazz = file.substring(file.lastIndexOf("/") + 1, file.lastIndexOf("."));
                    String pkg = name.substring(0, name.lastIndexOf(clazz) - 1);

                    System.out.println(" INSERT INTO `classpath` (name,label,java_class,java_package) VALUES ('" + name + "','" + clazz + " (" + pkg + ")','" + clazz + "','" + pkg + "');");
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getJarContent(String jarPath) throws IOException {

        List<String> content = new ArrayList<String>();

        JarFile jarFile = new JarFile(jarPath);

        Enumeration<JarEntry> e = jarFile.entries();

        while (e.hasMoreElements()) {

            JarEntry entry = (JarEntry) e.nextElement();
            String name = entry.getName();
            if (!name.contains("$") && !name.startsWith("META-INF")) {
                content.add(name);
            }
        }

        return content;
    }
}
