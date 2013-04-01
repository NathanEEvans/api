package com.stormcloud.ide.api.core.mail;

import com.stormcloud.ide.api.core.mail.exception.MailManagerException;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author martijn
 */
public class MailManager implements IMailManager {

    private String username;
    private String password;
    private String from;
    private Properties props;

    public MailManager(
            String mailSmtpAuth,
            String mailSmtpStarttlsEnable,
            String mailSmtpHost,
            String mailSmtpPort) {

        props = new Properties();
        props.put("mail.smtp.auth", mailSmtpAuth);
        props.put("mail.smtp.starttls.enable", mailSmtpStarttlsEnable);
        props.put("mail.smtp.host", mailSmtpHost);
        props.put("mail.smtp.port", mailSmtpPort);
    }

    @Override
    public void send(
            String recipient,
            String subject,
            String body)
            throws MailManagerException {

        Session session = Session.getInstance(getProps(),
                new javax.mail.Authenticator() {

                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {

            Message message = new MimeMessage(session);

            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);

        } catch (MessagingException e) {
            throw new MailManagerException(e);
        }
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Properties getProps() {
        return props;
    }

    public void setProps(Properties props) {
        this.props = props;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
