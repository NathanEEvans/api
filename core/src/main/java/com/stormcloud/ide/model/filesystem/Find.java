package com.stormcloud.ide.model.filesystem;

/**
 *
 * @author martijn
 */
public class Find {

    private String containingText;
    private String fileNamePatterns;
    private String scope;
    private boolean matchCase;
    private boolean wholeWords;
    private boolean regex;

    public String getContainingText() {
        return containingText;
    }

    public void setContainingText(String containingText) {
        this.containingText = containingText;
    }

    public String getFileNamePatterns() {
        return fileNamePatterns;
    }

    public void setFileNamePatterns(String fileNamePatterns) {
        this.fileNamePatterns = fileNamePatterns;
    }

    public boolean isMatchCase() {
        return matchCase;
    }

    public void setMatchCase(boolean matchCase) {
        this.matchCase = matchCase;
    }

    public boolean isRegex() {
        return regex;
    }

    public void setRegex(boolean regex) {
        this.regex = regex;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public boolean isWholeWords() {
        return wholeWords;
    }

    public void setWholeWords(boolean wholeWords) {
        this.wholeWords = wholeWords;
    }
}
