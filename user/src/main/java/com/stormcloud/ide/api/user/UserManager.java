package com.stormcloud.ide.api.user;

import com.stormcloud.ide.api.core.dao.IStormCloudDao;
import com.stormcloud.ide.api.core.entity.User;
import com.stormcloud.ide.api.core.mail.IMailManager;
import com.stormcloud.ide.api.core.mail.exception.MailManagerException;
import com.stormcloud.ide.api.user.exception.UserManagerException;

/**
 *
 * @author martijn
 */
public class UserManager implements IUserManager {

    private IStormCloudDao dao;
    private IMailManager mailManager;

    @Override
    public String createAccount(
            String userName,
            String password,
            String emailAddress)
            throws UserManagerException {

        // check if username is available
        User user = dao.getUser(userName);


        // check if email address already exists


        // add user in database


        // generate authorization code

        try {

            String subject = "Stormcloud IDE - New Account Confirmation";
            String body =
                    "Hi " + userName + "! \n\n "
                    + "Welcome as a Community Coder on Cloud Coders' Stormcloud IDE!";


            // send email with verify url
            mailManager.send(emailAddress, subject, body);


        } catch (MailManagerException e) {
            throw new UserManagerException(e);
        }

        return "0";
    }

    @Override
    public String confirmAccount(
            String userName,
            String authorizationCode)
            throws UserManagerException {


        return "0";
    }

    public IStormCloudDao getDao() {
        return dao;
    }

    public void setDao(IStormCloudDao dao) {
        this.dao = dao;
    }

    public IMailManager getMailManager() {
        return mailManager;
    }

    public void setMailManager(IMailManager mailManager) {
        this.mailManager = mailManager;
    }

    @Override
    public String changePassword(String currentPassword, String newPassword) throws UserManagerException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
