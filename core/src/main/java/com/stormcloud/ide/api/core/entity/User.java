package com.stormcloud.ide.api.core.entity;

import com.stormcloud.ide.model.user.UserInfo;
import com.stormcloud.ide.model.user.UserSettings;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 *
 * @author martijn
 */
@Entity
@Table(name = "`user`")
@XmlRootElement
@SuppressWarnings("serial")
public class User implements Serializable {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;
    @JsonIgnore
    @Column(name = "active")
    private boolean active = false;
    @Column(name = "user_name", nullable = false, unique = true)
    private String userName;
    @JsonIgnore
    @Column(name = "password", nullable = false)
    private String password;
    @JsonIgnore
    @Column(name = "authorization_code")
    private String authorizationCode;
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private Set<Setting> settings;
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private Set<Preference> preferences;
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private Set<Info> info;
    @Column(name = "last_login")
    @Temporal(TemporalType.DATE)
    private Date lastLogin;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public Set<Setting> getSettings() {
        return settings;
    }

    public void setSettings(Set<Setting> settings) {
        this.settings = settings;
    }

    public Set<Preference> getPreferences() {
        return preferences;
    }

    public void setPreferences(Set<Preference> preferences) {
        this.preferences = preferences;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Set<Info> getInfo() {
        return info;
    }

    public void setInfo(Set<Info> info) {
        this.info = info;
    }

    /**
     * Convenience method to retrieve a specific setting from the suer settings.
     *
     * @param settingsKey
     * @return
     */
    public String getSetting(UserSettings settingsKey) {

        for (Setting setting : getSettings()) {

            if (setting.getKey().equals(settingsKey.name())) {

                return setting.getValue();
            }
        }

        return null;
    }

    public void setInfo(UserInfo infoKey, String value) {

        for (Info userInfo : getInfo()) {

            if (userInfo.getKey().equals(infoKey.name())) {

                userInfo.setValue(value);
            }
        }
    }

    public String getInfo(UserInfo infoKey) {

        for (Info userInfo : getInfo()) {

            if (userInfo.getKey().equals(infoKey.name())) {

                return userInfo.getValue();
            }
        }

        return null;
    }
}
