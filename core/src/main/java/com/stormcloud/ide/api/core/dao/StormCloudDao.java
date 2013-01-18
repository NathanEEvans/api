package com.stormcloud.ide.api.core.dao;

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

import com.stormcloud.ide.api.core.entity.Archetype;
import com.stormcloud.ide.api.core.entity.Classpath;
import com.stormcloud.ide.api.core.entity.FileType;
import com.stormcloud.ide.api.core.entity.User;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author martijn
 */
@Transactional
public class StormCloudDao implements IStormCloudDao {

    private Logger LOG = Logger.getLogger(getClass());
    @PersistenceContext(unitName = "StormCloudPU")
    private EntityManager manager;

    @Override
    public List<FileType> getFileTypes() {

        Query query = manager.createQuery("Select f From FileType f Order By f.label");

        List<FileType> types = (List<FileType>) query.getResultList();

        LOG.debug("Found " + types.size() + " FileTypes");

        return types;
    }

    @Override
    public List<Archetype> getCatalog() {

        Query query = manager.createQuery("Select a From Archetype a");

        List<Archetype> archetypes = (List<Archetype>) query.getResultList();

        LOG.debug("Found " + archetypes.size() + " Archetypes");

        return archetypes;
    }

    @Override
    public User getUser(String userName) {

        LOG.info("Get User for [" + userName + "]");

        User result;

        Query query = manager.createQuery("SELECT u FROM User u WHERE u.userName = :userName");

        query.setParameter("userName", userName);

        result = (User) query.getSingleResult();

        return result;
    }

    @Override
    public void save(User user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void delete(User user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Classpath> searchJdkClasspath(String searchKey, int start, int count) {

        Query query = manager.createQuery("Select c From Classpath c Where c.javaClass Like :key Order by c.javaClass");

        String key = searchKey.replaceAll("\\*", "%");

        query.setParameter("key", key);
        query.setFirstResult(start);
        query.setMaxResults(count);

        List<Classpath> result = (List<Classpath>) query.getResultList();

        return result;
    }
}
