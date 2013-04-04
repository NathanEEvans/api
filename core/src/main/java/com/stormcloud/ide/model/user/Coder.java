package com.stormcloud.ide.model.user;

import java.util.Date;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author martijn
 */
@XmlRootElement
public class Coder {

    private String fullName;
    private String userName;
    private String emailAddress;
    private String homeTown;
    private String country;
    private String gravatar;
    private String joined;
    private Date lastSeen;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getGravatar() {
        return gravatar;
    }

    public void setGravatar(String gravatar) {
        this.gravatar = gravatar;
    }

    public String getHomeTown() {
        return homeTown;
    }

    public void setHomeTown(String homeTown) {
        this.homeTown = homeTown;
    }

    public String getJoined() {
        return joined;
    }

    public void setJoined(String joined) {
        this.joined = joined;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Date getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Date lastSeen) {
        this.lastSeen = lastSeen;
    }
}
