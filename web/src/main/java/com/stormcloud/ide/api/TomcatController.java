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
import com.stormcloud.ide.api.tomcat.ITomcatManager;
import com.stormcloud.ide.api.tomcat.exception.TomcatManagerException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author martijn
 */
@Controller
@RequestMapping(value = "/tomcat")
public class TomcatController {

    private Logger LOG = Logger.getLogger(getClass());
    @Autowired
    private ITomcatManager manager;

    @RequestMapping(value = "/start",
    method = RequestMethod.GET,
    produces = "application/json")
    @ResponseBody
    public String getCatalog()
            throws TomcatManagerException {

        LOG.info("Received Tomcat start request");

        return manager.start();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.METHOD_FAILURE)
    @ResponseBody
    public String handleServerErrors(Exception ex) {
        return ex.getMessage();
    }
}
