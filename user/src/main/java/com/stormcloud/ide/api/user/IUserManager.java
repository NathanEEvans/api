package com.stormcloud.ide.api.user;

import com.stormcloud.ide.api.core.entity.User;

/**
 *
 * @author martijn
 */
public interface IUserManager {

    String createAccount();

    String confirmAccount();

    String changeEmail();

    String changePassword();
}
