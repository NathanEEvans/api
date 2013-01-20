package com.stormcloud.ide.model.services;

import com.stormcloud.ide.model.filesystem.Item;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author martijn
 */
public class Services {

    private String id = "services";
    private String label = "Services";
    private String type = "root";
    private Set<Item> children = new LinkedHashSet<Item>();

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

    public Set<Item> getChildren() {
        return children;
    }

    public void setChildren(Set<Item> children) {
        this.children = children;
    }
}
