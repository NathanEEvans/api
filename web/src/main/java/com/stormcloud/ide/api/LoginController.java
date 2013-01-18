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
import java.io.IOException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
@RequestMapping(value = "/login")
public class LoginController extends BaseController {

    private Logger LOG = Logger.getLogger(getClass());
    @Autowired
    private IStormCloudDao dao;

    @RequestMapping(
    method = RequestMethod.POST,
    produces = "application/json")
    @ResponseBody
    public void login(HttpServletRequest request, HttpServletResponse response) {

        // if the request came here we know it was succesfull
        LOG.info("Login succeeded for " + request.getRemoteUser());

        User user = dao.getUser(request.getRemoteUser());

        if (user == null) {

            // this would be really really strange, but still
            try {

                response.sendError(401);

            } catch (IOException e) {
                // euh...
            }
        }

        // create cookie
        Cookie scuCookie = new Cookie("scu", user.getUserName());
        scuCookie.setMaxAge(60 * 60 * 24); // 1 day

        response.addCookie(scuCookie);

        Cookie scpCookie = new Cookie("scp", user.getPassword());
        scpCookie.setMaxAge(60 * 60 * 24); // 1 day

        response.addCookie(scpCookie);
    }
}
