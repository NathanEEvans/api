package com.stormcloud.ide.api.derby;

import com.stormcloud.ide.api.derby.exception.DerbyManagerException;
import com.stormcloud.ide.model.derby.Databases;

/**
 *
 * @author martijn
 */
public interface IDerbyManager {

    Databases getDatabases() throws DerbyManagerException;
}
