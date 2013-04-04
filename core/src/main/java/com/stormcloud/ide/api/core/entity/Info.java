package com.stormcloud.ide.api.core.entity;

import java.io.Serializable;
import javax.persistence.*;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 *
 * @author martijn
 */
@Entity
@Table(name = "`info`")
@SuppressWarnings("serial")
public class Info implements Serializable {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    @JsonIgnore
    @Column(name = "editable")
    private boolean editable;
    @Column(name = "`group`")
    private String group;
    @Column(name = "`key`")
    private String key;
    @Column(name = "`value`")
    private String value;

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
