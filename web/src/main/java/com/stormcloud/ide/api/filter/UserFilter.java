package com.stormcloud.ide.api.filter;

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
import com.stormcloud.ide.api.core.dao.IStormCloudDao;
import com.stormcloud.ide.api.core.entity.User;
import com.stormcloud.ide.api.core.remote.RemoteUser;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

/**
 *
 * @author martijn
 */
public class UserFilter implements Filter {

    private Logger LOG = Logger.getLogger(getClass());
    @Autowired
    private IStormCloudDao dao;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {

        try {

            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            LOG.info("Filter Request [" + request.getRemoteAddr() + ", " + request.getRemoteHost() + "]");

            MDC.put("api", httpRequest.getRequestURI());

            if (httpRequest.getRequestURI().endsWith("/api/login")) {


                // configure MDC for the remainging trip
                MDC.put("userName", httpRequest.getRemoteUser());

                LOG.debug("Login Request.");

                // it's a login request which succeeded (Basic Auth)
                // so we now need to genereate an authentication token
                // and store it in a cookie we sent back
                // create the cookie with key for consecutive Rest API Calls

                // Get user from db and add to the localthread
                User user = dao.getUser(httpRequest.getRemoteUser());

                if (user == null) {

                    LOG.error("User not found.");
                    httpResponse.sendError(HttpStatus.FORBIDDEN.value());
                    httpResponse.flushBuffer();
                    return;
                }

                RemoteUser.set(user);

                try {

                    // set the key cookie
                    Cookie keyCookie =
                            new Cookie("stormcloud-key",
                            createKey(user, httpRequest.getRemoteAddr()));

                    keyCookie.setMaxAge(60 * 60 * 24); // 1 day

                    httpResponse.addCookie(keyCookie);

                    // set the username cookie
                    Cookie userCookie =
                            new Cookie("stormcloud-user",
                            user.getUserName());

                    userCookie.setMaxAge(60 * 60 * 24); // 1 day

                    httpResponse.addCookie(userCookie);


                } catch (NoSuchAlgorithmException e) {

                    LOG.error(e);

                    try {

                        // no go
                        httpResponse.sendError(
                                HttpStatus.INTERNAL_SERVER_ERROR.value());

                        httpResponse.flushBuffer();
                        return;

                    } catch (IOException ioe) {
                        LOG.error(ioe);
                    }
                }


            } else {

                LOG.info("API Request.");

                // any other request than a login
                // we need to check the username and received key
                Cookie[] cookies = httpRequest.getCookies();

                String userName = null;
                String key = null;

                if (cookies != null) {

                    LOG.info("Found " + cookies.length + " Cookies");

                    // loop trough the cookies
                    for (int i = 0; i < cookies.length; i++) {

                        if (cookies[i].getName().equals("stormcloud-user")) {

                            LOG.debug("userName = " + cookies[i].getValue());
                            userName = cookies[i].getValue();
                        }

                        if (cookies[i].getName().equals("stormcloud-key")) {

                            LOG.debug("key = " + cookies[i].getValue());
                            key = cookies[i].getValue();
                        }
                    }
                }

                if (userName == null || key == null) {

                    LOG.info("Required credentials not found.");
                    httpResponse.sendError(HttpStatus.FORBIDDEN.value());
                    httpResponse.flushBuffer();
                    return;

                } else {

                    // configure MDC for the remainging trip
                    MDC.put("userName", userName);

                    // get user
                    LOG.debug("Get Persisted User");
                    User user = dao.getUser(userName);

                    if (user == null) {
                        httpResponse.sendError(HttpStatus.FORBIDDEN.value());
                        httpResponse.flushBuffer();
                        return;
                    }

                    RemoteUser.set(user);

                    try {

                        String matchKey = createKey(user, httpRequest.getRemoteAddr());

                        LOG.info("Validating Key.");

                        if (!matchKey.equals(key)) {

                            LOG.warn("Invalid Key!");
                            httpResponse.sendError(HttpStatus.FORBIDDEN.value());
                            httpResponse.flushBuffer();
                            return;

                        } else {

                            LOG.info("Request Authenticated");
                        }


                    } catch (NoSuchAlgorithmException e) {

                        LOG.error(e);

                        try {

                            // no go
                            httpResponse.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value());
                            httpResponse.flushBuffer();
                            return;

                        } catch (IOException ioe) {
                            LOG.error(ioe);
                        }
                    }

                }
            }

            chain.doFilter(request, response);


        } catch (IOException e) {
            LOG.error(e);
        } catch (ServletException e) {
            LOG.error(e);
        } finally {

            // clear the logging diagnostics context
            MDC.clear();

            // Remove the user from memoty
            RemoteUser.destroy();
        }
    }

    private String createKey(User user, String remoteAddress) throws NoSuchAlgorithmException {

        String keyInput;
        String key;

        // include username 
        String userName = user.getUserName();

        String password = user.getPassword();

        // not sure here if we can use remoteHost or remoteAddress
        // because of possible user being behind a proxy
        // i'm using the ip address for now
        // String remoteHost = request.getRemoteHost();

        keyInput = userName + password + remoteAddress;

        MessageDigest digest = MessageDigest.getInstance("MD5");

        digest.update(keyInput.getBytes(), 0, keyInput.length());

        key = new BigInteger(1, digest.digest()).toString(16);

        return key;

    }

    @Override
    public void destroy() {
        // nothing to do
    }
}
