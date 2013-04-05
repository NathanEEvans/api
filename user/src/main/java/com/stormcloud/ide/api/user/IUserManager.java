package com.stormcloud.ide.api.user;

import com.stormcloud.ide.api.user.exception.UserManagerException;

/**
 *
 * @author martijn
 */
public interface IUserManager {

    String createAccount(
            String userName,
            String password,
            String emailAddress)
            throws UserManagerException;

    String confirmAccount(
            String userName,
            String authorizationCode)
            throws UserManagerException;

    String changePassword(
            String currentPassword,
            String newPassword)
            throws UserManagerException;
}
