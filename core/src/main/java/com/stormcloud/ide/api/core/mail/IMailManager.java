package com.stormcloud.ide.api.core.mail;

import com.stormcloud.ide.api.core.mail.exception.MailManagerException;

/**
 *
 * @author martijn
 */
public interface IMailManager {

    void send(
            String recipient,
            String subject,
            String body)
            throws MailManagerException;
}
