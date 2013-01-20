package com.stormcloud.ide.model.filesystem;

/**
 *
 * @author martijn
 */
public class Save {

    private String filePath;
    private String contents;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }
}
