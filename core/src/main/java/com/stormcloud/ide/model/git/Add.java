package com.stormcloud.ide.model.git;

/**
 *
 * @author martijn
 */
public class Add {

    private String repository;
    private String pattern;

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}
