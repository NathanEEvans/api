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
import com.stormcloud.ide.api.core.dao.IStormCloudDao;
import com.stormcloud.ide.api.core.dao.exception.StormcloudDaoException;
import com.stormcloud.ide.api.core.entity.User;
import com.stormcloud.ide.api.core.remote.RemoteUser;
import com.stormcloud.ide.api.user.IUserManager;
import com.stormcloud.ide.model.user.Coder;
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
@RequestMapping(value = "/user")
public class UserController extends BaseController {

    private Logger LOG = Logger.getLogger(getClass());
    // @todo, the dao needs to be removed from the controller
    @Autowired
    private IStormCloudDao dao;
    @Autowired
    private IUserManager userManager;

    @RequestMapping(value = "/coders",
    method = RequestMethod.GET,
    produces = "application/json")
    @ResponseBody
    public Coder[] getCoders() {

        LOG.info("Get Coders.");

        return dao.getCoders();
    }

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

    @RequestMapping(value = "/preference",
    method = RequestMethod.POST)
    @ResponseBody
    public String savePreference(
            @RequestParam(value = "key", required = true) String key,
            @RequestParam(value = "value", required = true) String value) {


        LOG.debug("Save preference key[" + key + "], value[" + value + "]");


        dao.savePreference(key, value);

        return "0";
    }

    @RequestMapping(value = "/info",
    method = RequestMethod.POST)
    @ResponseBody
    public String saveSetting(
            @RequestParam(value = "key", required = true) String key,
            @RequestParam(value = "value", required = true) String value) {


        LOG.debug("Save info key[" + key + "], value[" + value + "]");


        dao.saveInfo(key, value);

        return "0";
    }

    @RequestMapping(value = "/password",
    method = RequestMethod.POST)
    @ResponseBody
    public String changePassword(
            @RequestParam(value = "currentPassword", required = true) String currentPassword,
            @RequestParam(value = "newPassword", required = true) String newPassword)
            throws StormcloudDaoException {

        LOG.debug("Change Password");

        // @todo move to userManager
        return dao.changePassword(currentPassword, newPassword);
    }

    @RequestMapping(value = "/delete",
    method = RequestMethod.POST)
    @ResponseBody
    public String deleteAccount(
            @RequestParam(value = "currentPassword", required = true) String currentPassword,
            @RequestParam(value = "newPassword", required = true) String newPassword) {

        LOG.debug("Delete Account");


        // userManager.saveInfo(key, value);

        return "0";
    }
}
