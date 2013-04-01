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
import com.stormcloud.ide.api.core.entity.User;
import com.stormcloud.ide.api.core.remote.RemoteUser;
import org.apache.log4j.Logger;
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
@RequestMapping(value = "/user")
public class UserController extends BaseController {

    private Logger LOG = Logger.getLogger(getClass());

    /**
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.GET,
    produces = "application/json")
    @ResponseBody
    public User getUser() {

        LOG.info("Get User.");

        // get user which was already set in the filter and return
        return RemoteUser.get();
    }

    @RequestMapping(method = RequestMethod.POST,
    produces = "application/json",
    consumes = "application/json")
    @ResponseBody
    public String savePreference(
            @RequestParam String key,
            @RequestParam String value) {



        return "0";
    }
}
