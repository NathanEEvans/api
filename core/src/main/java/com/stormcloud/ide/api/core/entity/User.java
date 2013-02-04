package com.stormcloud.ide.api.core.entity;

/*
 * #%L Stormcloud IDE - API - Core %% Copyright (C) 2012 - 2013 Stormcloud IDE
 * %% This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/gpl-3.0.html>. #L%
 */
import com.stormcloud.ide.model.user.UserSettings;
import java.io.Serializable;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
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
    @Column(name = "email_address", nullable = false, unique = true)
    private String emailAddress;
    @JsonIgnore
    @Column(name = "authorization_code")
    private String authorizationCode;
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private Set<Setting> settings;

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

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
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

    /**
     * Convenience method to retrieve a specific setting from the suer settings.
     *
     * @param settingsKey
     * @return
     */
    public String getSetting(UserSettings settingsKey) {

        for (Setting setting : settings) {

            if (setting.getKey().equals(settingsKey.name())) {

                return setting.getValue();
            }
        }

        return null;
    }
}
