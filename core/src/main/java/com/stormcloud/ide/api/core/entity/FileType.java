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

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author martijn
 */
@Entity
@Table(name = "file_type")
public class FileType implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "filetype_id", unique = true, nullable = false)
    private Long filetypeId;
    @Column(name = "label", nullable = false)
    private String label;
    @Column(name = "type")
    private String type;
    @Column(name = "initial_content")
    private String initialContent;
    @Column(name = "description")
    private String description;
    @Column(name = "extention")
    private String extention;

    public Long getFiletypeId() {
        return filetypeId;
    }

    public void setFiletypeId(Long filetypeId) {
        this.filetypeId = filetypeId;
    }

    public String getId() {
        return label;
    }

    public String getLabel() {
        return label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInitialContent() {
        return initialContent;
    }

    public void setInitialContent(String initialContent) {
        this.initialContent = initialContent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExtention() {
        return extention;
    }

    public void setExtention(String extention) {
        this.extention = extention;
    }
}
