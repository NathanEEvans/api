package com.stormcloud.ide.model.maven;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author martijn
 */
@XmlRootElement(name = "project")
public class Project {

    private String archetypeGroupId;
    private String archetypeArtifactId;
    private String archetypeVersion;
    private String projectName;
    private String groupId;
    private String artifactId;
    private String description;
    private String version;
    private String javaPackage;
    private String transports = "";

    public String getArchetypeGroupId() {
        return archetypeGroupId;
    }

    public void setArchetypeGroupId(String archetypeGroupId) {
        this.archetypeGroupId = archetypeGroupId;
    }

    public String getArchetypeArtifactId() {
        return archetypeArtifactId;
    }

    public void setArchetypeArtifactId(String archetypeArtifactId) {
        this.archetypeArtifactId = archetypeArtifactId;
    }

    public String getArchetypeVersion() {
        return archetypeVersion;
    }

    public void setArchetypeVersion(String archetypeVersion) {
        this.archetypeVersion = archetypeVersion;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getDescription() {
        return description;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getJavaPackage() {
        return javaPackage;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getTransports() {
        return transports;
    }

    public String getVersion() {
        return version;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setJavaPackage(String javaPackage) {
        this.javaPackage = javaPackage;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setTransports(String transports) {
        this.transports = transports;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "archetypeGroupId["
                + archetypeGroupId
                + "], archetypeArtifactId["
                + archetypeArtifactId
                + "], archetypeVersion["
                + archetypeVersion
                + "], projectName["
                + projectName
                + "], groupId["
                + groupId
                + "], artifactId["
                + artifactId
                + "], description["
                + description
                + "], version["
                + version
                + "], javaPackage["
                + javaPackage
                + "]";
    }
}
