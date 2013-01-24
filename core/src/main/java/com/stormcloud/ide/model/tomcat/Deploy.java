package com.stormcloud.ide.model.tomcat;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author martijn
 */
@XmlRootElement(name = "deploy")
public class Deploy {

    private String filePath;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
