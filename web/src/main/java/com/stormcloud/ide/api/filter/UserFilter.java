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
import org.springframework.beans.factory.annotation.Autowired;

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


        /**
         * Either we received a login request and the Basic Auth succeeded or we
         * need to validate the key we received in the cookie
         *
         *
         *
         */
        try {

            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;


            LOG.info("Filter Request [" + request.getRemoteAddr() + ", " + request.getRemoteHost() + ", " + httpRequest.getRemoteUser() + "]");


            /**
             * Check if the request came trough the api/login service
             */
            LOG.debug("Request URL : " + httpRequest.getRequestURI());

            if (httpRequest.getRequestURI().endsWith("/api/login")) {

                // it's a login request which succeeded (Basic Auth)
                // so we now need to genereate an authentication token
                // and store it in a cookie we sent back
                // create the cookie with key for consecutive Rest API Calls
                String keyInput;
                String key = null;

                // include username 
                String userName = httpRequest.getRemoteUser();



                //String password =
                // not sure here if we can use remoteHost or remoteAddress
                // because of possible user being behind a proxy
                // i'm using the ip address for now
                // String remoteHost = request.getRemoteHost();
                String remoteAddress = request.getRemoteAddr();

                try {


                    keyInput = userName + remoteAddress;

                    key = md5(keyInput);

                    Cookie scuCookie = new Cookie("sc", key);
                    scuCookie.setMaxAge(60 * 60 * 24); // 1 day

                    httpResponse.addCookie(scuCookie);


                } catch (NoSuchAlgorithmException e) {
                    LOG.error(e);

                    try {

                        // no go
                        httpResponse.sendError(500);

                    } catch (IOException ioe) {
                        LOG.error(ioe);
                    }
                }


            } else {



                // any other request than a login

                /**
                 * Cookie cookie = new Cookie(name,value);
                 * response.addCookie(cookie);
                 */
                Cookie[] cookies = httpRequest.getCookies();

                if (cookies != null) {
                    LOG.info("Found " + cookies.length + " Cookies");

                    for (int i = 0; i < cookies.length; i++) {
                        LOG.debug("name = " + cookies[i].getName());
                        LOG.debug("value = " + cookies[i].getValue());
                    }

                }


                // when all is well, get user from db and add to the localthread
                // get the username from the cookie
                User user = dao.getUser("martijn");
                RemoteUser.set(user);
            }

            chain.doFilter(request, response);


        } catch (IOException e) {
            LOG.error(e);
        } catch (ServletException e) {
            LOG.error(e);
        } finally {

            RemoteUser.destroy();
        }
    }

    private String md5(String key) throws NoSuchAlgorithmException {

        MessageDigest digest = MessageDigest.getInstance("MD5");

        digest.update(key.getBytes(), 0, key.length());

        return new BigInteger(1, digest.digest()).toString(16);

    }

    @Override
    public void destroy() {
        // nothing to do
    }
}
