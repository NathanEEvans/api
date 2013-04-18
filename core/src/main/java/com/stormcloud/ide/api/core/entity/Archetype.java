package com.stormcloud.ide.api.core.entity;

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
@Table(name = "maven_archetype")
public class Archetype implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "archetype_id", unique = true, nullable = false)
    private Long archetypeId;
    @Column(name = "label", nullable = false)
    private String label;
    @Column(name = "style", nullable = false)
    private String style;
    @Column(name = "groupd_id", nullable = false)
    private String groupId;
    @Column(name = "artifact_id", nullable = false)
    private String artifactId;
    @Column(name = "version", nullable = false)
    private String version;
    @Column(name = "description", nullable = false)
    private String description;

    public Long getArchetypeId() {
        return archetypeId;
    }

    public void setArchetypeId(Long archetypeId) {
        this.archetypeId = archetypeId;
    }

    public String getId() {
        return label;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
