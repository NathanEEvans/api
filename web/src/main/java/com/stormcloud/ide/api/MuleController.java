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

import com.stormcloud.ide.api.tomcat.IMuleManager;
import com.stormcloud.ide.api.tomcat.exception.MuleManagerException;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author martijn
 */
@Controller
@RequestMapping(value = "/mule")
public class MuleController {

    private Logger LOG = Logger.getLogger(getClass());
    private IMuleManager manager;

    @RequestMapping(value = "/deploy",
    method = RequestMethod.POST)
    @ResponseBody
    public String deploy(
            @RequestParam(value = "filePath", required = true) String filePath)
            throws MuleManagerException {

        LOG.debug(
                "Received request to deploy [" + filePath + "]");

        return manager.deploy(filePath);

    }

    @RequestMapping(value = "/undeploy",
    method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void undeploy(
            @RequestParam(value = "filePath", required = true) String filePath)
            throws MuleManagerException {

        LOG.debug(
                "Received request to undeploy [" + filePath + "]");

        manager.undeploy(filePath);
    }

    @RequestMapping(value = "/start",
    method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void start()
            throws MuleManagerException {

        LOG.debug("Received request to start Mule.");

        manager.start();
    }

    @RequestMapping(value = "/stop",
    method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void stop()
            throws MuleManagerException {

        LOG.debug("Received request to stop Mule.");

        manager.stop();
    }
}
