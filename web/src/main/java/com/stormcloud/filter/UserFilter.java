package com.stormcloud.filter;

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

import com.stormcloud.ide.api.core.remote.RemoteUser;
import com.stormcloud.ide.api.core.remote.User;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;

/**
 *
 * @author martijn
 */
public class UserFilter implements Filter {

    private Logger LOG = Logger.getLogger(getClass());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // nothing to do
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        LOG.info("Filter Request [" + request.getRemoteAddr() + ", " + request.getRemoteHost() + ", " + httpRequest.getRemoteUser() + "]");

        User user = new User();
        user.setAddress(httpRequest.getRemoteAddr());
        user.setHost(httpRequest.getRemoteHost());
        user.setUserName(httpRequest.getRemoteUser());

        RemoteUser.set(user);

        try {

            chain.doFilter(request, response);

        } finally {

            RemoteUser.destroy();
        }
    }

    @Override
    public void destroy() {
        // nothing to do
    }
}
