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

import com.stormcloud.ide.api.core.entity.Classpath;
import com.stormcloud.ide.api.java.IJavaManager;
import com.stormcloud.rest.model.SearchResult;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author martijn
 */
@Controller
@RequestMapping(value = "/java")
public class JavaController extends BaseController {

    private Logger LOG = Logger.getLogger(getClass());
    @Autowired
    private IJavaManager javaManager;

    @RequestMapping(
            value = "/classpath",
    method = RequestMethod.GET,
    produces = "application/json")
    @ResponseBody
    public SearchResult searchClasspath(
            @RequestParam String name,
            @RequestParam int start,
            @RequestParam int count) {

        LOG.info("Search for " + name);

        SearchResult result = new SearchResult();

        Classpath[] classpath = javaManager.searchClassPath(name, start, count);

        result.setItems(classpath);

        return result;
    }
}
