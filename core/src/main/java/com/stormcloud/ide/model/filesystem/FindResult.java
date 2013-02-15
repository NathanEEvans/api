package com.stormcloud.ide.model.filesystem;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author martijn
 */
@XmlRootElement
public class FindResult {

    private Set<Item> result = new LinkedHashSet<Item>();

    public Set<Item> getResult() {
        return result;
    }

    public void setResult(Set<Item> result) {
        this.result = result;
    }
}
