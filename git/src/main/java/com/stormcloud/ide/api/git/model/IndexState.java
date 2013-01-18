package com.stormcloud.ide.api.git.model;

/*
 * #%L
 * Stormcloud IDE - API - Git
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

import java.util.Collections;
import java.util.Set;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author martijn
 */
@XmlRootElement
public class IndexState {

    private Set<String> added;
    private Set<String> assumeUnchanged;
    private Set<String> changed;
    private Set<String> conflicting;
    private Set<String> ignoredNotInIndex;
    private Set<String> missing;
    private Set<String> modified;
    private Set<String> removed;
    private Set<String> untracked;
    private Set<String> untrackedFolders;

    @XmlElementWrapper(name = "added")
    @XmlElement(name = "file")
    public Set<String> getAdded() {
        return Collections.unmodifiableSet(added);
    }

    public void setAdded(Set<String> added) {
        this.added = added;
    }

    @XmlElementWrapper(name = "assume-unchanged")
    @XmlElement(name = "file")
    public Set<String> getAssumeUnchanged() {
        return Collections.unmodifiableSet(assumeUnchanged);
    }

    public void setAssumeUnchanged(Set<String> assumeUnchanged) {
        this.assumeUnchanged = assumeUnchanged;
    }

    @XmlElementWrapper(name = "changed")
    @XmlElement(name = "file")
    public Set<String> getChanged() {
        return Collections.unmodifiableSet(changed);
    }

    public void setChanged(Set<String> changed) {
        this.changed = changed;
    }

    @XmlElementWrapper(name = "conflicting")
    @XmlElement(name = "file")
    public Set<String> getConflicting() {
        return Collections.unmodifiableSet(conflicting);
    }

    public void setConflicting(Set<String> conflicting) {
        this.conflicting = conflicting;
    }

    @XmlElementWrapper(name = "ignored-not-in-index")
    @XmlElement(name = "file")
    public Set<String> getIgnoredNotInIndex() {
        return Collections.unmodifiableSet(ignoredNotInIndex);
    }

    public void setIgnoredNotInIndex(Set<String> ignoredNotInIndex) {
        this.ignoredNotInIndex = ignoredNotInIndex;
    }

    @XmlElementWrapper(name = "missing")
    @XmlElement(name = "file")
    public Set<String> getMissing() {
        return Collections.unmodifiableSet(missing);
    }

    public void setMissing(Set<String> missing) {
        this.missing = missing;
    }

    @XmlElementWrapper(name = "modified")
    @XmlElement(name = "file")
    public Set<String> getModified() {
        return Collections.unmodifiableSet(modified);
    }

    public void setModified(Set<String> modified) {
        this.modified = modified;
    }

    @XmlElementWrapper(name = "removed")
    @XmlElement(name = "file")
    public Set<String> getRemoved() {
        return Collections.unmodifiableSet(removed);
    }

    public void setRemoved(Set<String> removed) {
        this.removed = removed;
    }

    @XmlElementWrapper(name = "untracked")
    @XmlElement(name = "file")
    public Set<String> getUntracked() {
        return Collections.unmodifiableSet(untracked);
    }

    public void setUntracked(Set<String> untracked) {
        this.untracked = untracked;
    }

    @XmlElementWrapper(name = "untracked-folders")
    @XmlElement(name = "file")
    public Set<String> getUntrackedFolders() {
        return Collections.unmodifiableSet(untrackedFolders);
    }

    public void setUntrackedFolders(Set<String> untrackedFolders) {
        this.untrackedFolders = untrackedFolders;
    }
}
