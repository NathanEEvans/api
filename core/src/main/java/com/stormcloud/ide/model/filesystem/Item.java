package com.stormcloud.ide.model.filesystem;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author martijn
 */
@XmlRootElement
public class Item {

    private String id;
    private String label;
    private String type;
    private String status;
    private String buildName;
    private Set<Item> children = new LinkedHashSet<Item>(0);

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBuildName() {
        return buildName;
    }

    public void setBuildName(String buildName) {
        this.buildName = buildName;
    }

    public Set<Item> getChildren() {
        return children;
    }

    public void setChildren(Set<Item> children) {
        this.children = children;
    }
}
