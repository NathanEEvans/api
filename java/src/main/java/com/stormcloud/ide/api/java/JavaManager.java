package com.stormcloud.ide.api.java;

/*
 * #%L
 * Stormcloud IDE - API - Java
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
import com.stormcloud.ide.api.core.entity.Classpath;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author martijn
 */
public class JavaManager implements IJavaManager {

    private Logger LOG = Logger.getLogger(getClass());
    private IStormCloudDao dao;

    @Override
    public Classpath[] searchClassPath(String searchKey, int start, int count) {

        LOG.info("Search Classpath for " + searchKey);

        List<Classpath> result = dao.searchJdkClasspath(searchKey, start, count);

        LOG.info("Return " + result.size() + " search Results");

        Classpath[] response = null;

        if (result.isEmpty()) {

            Classpath classpath = new Classpath();
            classpath.setId(0L);
            classpath.setJavaClass("No Suggestions...");
            classpath.setJavaPackage("No Suggestions...");
            classpath.setLabel("No Suggestions...");
            classpath.setName("No Suggestions...");

            response = new Classpath[1];
            response[0] = classpath;

        } else {

            response = result.toArray(new Classpath[result.size()]);

        }

        return response;
    }

    public IStormCloudDao getDao() {
        return dao;
    }

    public void setDao(IStormCloudDao dao) {
        this.dao = dao;
    }
}
