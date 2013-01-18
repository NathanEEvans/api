package com.stormcloud.ide.api.core.entity;

/*
 * #%L
 * Stormcloud IDE - API - Core
 * %%
 * Copyright (C) 2012 - 2013 Stormcloud IDE
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.stormcloud.ide.api.core.entity.FileType;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author martijn
 */
public class FileTypes {

    private String id = "filetypes";
    private String label = "filetypes";
    private String type = "root";
    private Set<FileType> children = new LinkedHashSet<FileType>();

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

    public Set<FileType> getChildren() {
        return children;
    }

    public void setChildren(Set<FileType> children) {
        this.children = children;
    }
}
