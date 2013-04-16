package com.stormcloud.ide.api.user;

import com.stormcloud.ide.api.core.dao.IStormCloudDao;
import com.stormcloud.ide.api.core.dao.exception.StormcloudDaoException;
import com.stormcloud.ide.api.core.entity.Info;
import com.stormcloud.ide.api.core.entity.User;
import com.stormcloud.ide.api.core.mail.IMailManager;
import com.stormcloud.ide.api.core.mail.exception.MailManagerException;
import com.stormcloud.ide.api.user.exception.UserManagerException;
import com.stormcloud.ide.model.user.UserInfo;
import java.util.HashSet;
import java.util.Set;

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

        if (user != null) {
            throw new UserManagerException("Username not available.");
        }

        // check if email address is available
        boolean exists = dao.emailAddressExists(emailAddress);

        if (exists) {
            throw new UserManagerException("Email Address already in use.");
        }


        // generate confirmation code
        String authCode = generateAuthCode();

        try {

            // add user in database
            user = new User();
            user.setUserName(userName);
            user.setPassword(dao.md5Hash(password));
            user.setAuthorizationCode(authCode);

            dao.save(user);

            String subject = "Stormcloud IDE - New Account Confirmation";
            String body =
                    "Hi " + userName + "! \n\n "
                    + "Welcome as a Community Coder on Cloud Coders' Stormcloud IDE!";


            // send email with verify url
            mailManager.send(emailAddress, subject, body);


        } catch (MailManagerException e) {
            throw new UserManagerException(e);
        } catch (StormcloudDaoException e) {
            throw new UserManagerException(e);
        }

        return "0";
    }

    private String generateAuthCode() {

        String ALPHA_NUM = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjklmnpqrstuvwxyz";

        StringBuilder sb = new StringBuilder(6);

        for (int i = 0; i < 6; i++) {
            int ndx = (int) (Math.random() * ALPHA_NUM.length());
            sb.append(ALPHA_NUM.charAt(ndx));
        }

        return sb.toString();
    }

    @Override
    public String confirmAccount(
            String userName,
            String emailAddress,
            String authorizationCode)
            throws UserManagerException {


        User user = dao.getUser(userName);

        Info emailAddressInfo = new Info();
        emailAddressInfo.setEditable(false);
        emailAddressInfo.setKey(UserInfo.EMAIL_ADDRESS.name());
        emailAddressInfo.setValue(emailAddress);

        Set<Info> info = new HashSet<Info>(0);
        info.add(emailAddressInfo);
        user.setInfo(info);






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
