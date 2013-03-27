package com.stormcloud.ide.model.derby;

import com.stormcloud.ide.model.filesystem.Item;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author martijn
 */
public class Databases {

    private String id = "databases";
    private String label = "databases";
    private String type = "root";
    private String status;
    private Set<Item> children = new LinkedHashSet<Item>(0);

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Set<Item> getChildren() {
        return children;
    }

    public void setChildren(Set<Item> children) {
        this.children = children;
    }
}
