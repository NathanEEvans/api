package com.stormcloud.ide.api.core.dao;

/*
 * #%L Stormcloud IDE - API - Core %% Copyright (C) 2012 - 2013 Stormcloud IDE
 * %% This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/gpl-3.0.html>. #L%
 */
import com.stormcloud.ide.api.core.dao.exception.StormcloudDaoException;
import com.stormcloud.ide.api.core.entity.Archetype;
import com.stormcloud.ide.api.core.entity.Classpath;
import com.stormcloud.ide.api.core.entity.User;
import com.stormcloud.ide.model.user.Coder;
import java.util.List;

/**
 *
 * @author martijn
 */
public interface IStormCloudDao {

    List<Archetype> getCatalog();

    User getUser(String userName);

    void save(User user);

    void delete(User user);

    Coder[] getCoders();

    void savePreference(String key, String value);

    void saveInfo(String key, String value);

    String changePassword(String currentPassword, String newPassword) throws StormcloudDaoException;

    List<Classpath> searchJdkClasspath(String searchKey, int start, int count);
}
