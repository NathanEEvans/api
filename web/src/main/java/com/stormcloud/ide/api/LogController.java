package com.stormcloud.ide.api;

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

import com.stormcloud.ide.api.core.dao.IStormCloudDao;
import com.stormcloud.ide.api.core.entity.User;
import com.stormcloud.ide.api.core.remote.RemoteUser;
import com.stormcloud.ide.api.maven.exception.MavenManagerException;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
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
@RequestMapping(value = "/log")
public class LogController {

    private Logger LOG = Logger.getLogger(getClass());
    @Autowired
    private IStormCloudDao dao;

    @RequestMapping(value = "/maven",
    method = RequestMethod.GET)
    @ResponseBody
    public String getMavenLog()
            throws MavenManagerException {

        String contents;

        User user = dao.getUser(RemoteUser.get().getUserName());

        try {

            contents = FileUtils.readFileToString(new File(user.getHomeFolder() + "/.log/maven.log"));

        } catch (IOException e) {
            contents = e.getMessage();
        }

        return contents;
    }

    @RequestMapping(value = "/tomcat",
    method = RequestMethod.GET)
    @ResponseBody
    public String getTomcatLog()
            throws MavenManagerException {

        String contents;

        try {

            contents = FileUtils.readFileToString(new File("tomcat.log"));

        } catch (IOException e) {
            contents = e.getMessage();
        }

        return contents;
    }
}
