package com.stormcloud.ide.api.derby;

import com.stormcloud.ide.api.core.remote.RemoteUser;
import com.stormcloud.ide.api.derby.exception.DerbyManagerException;
import com.stormcloud.ide.model.derby.Databases;
import com.stormcloud.ide.model.user.UserSettings;
import java.io.File;
import java.sql.*;
import java.util.Properties;

/**
 *
 * @author martijn
 */
public class DerbyManager implements IDerbyManager {

    private static final String[] EXCLUDED = {"SYS", "SYSCAT", "SYSCS_DIAG", "SYSCS_UTIL", "SYSFUN", "SYSIBM", "SYSPROC", "SYSSTAT"};
    private static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";

    @Override
    public Databases getDatabases() throws DerbyManagerException {

        Connection connection;

        try {

            Class.forName(DRIVER).newInstance();

            File[] dbFolders =
                    new File(RemoteUser.get().getSetting(
                    UserSettings.DERBY_HOME)).listFiles();

            for (File dbFolder : dbFolders) {

                if (dbFolder.isDirectory()) {

                    String dbName = dbFolder.getAbsolutePath();

                    Properties props = new Properties();
                    props.put("user", "app");
                    props.put("password", "app");

                    // connect
                    connection = DriverManager.getConnection("jdbc:derby:" + dbName + ";create=true", props);

                    DatabaseMetaData metaData = connection.getMetaData();

                    // list schemas
                    ResultSet schemas = metaData.getSchemas();

                    while (schemas.next()) {

                        System.out.println(schemas.getString(1));

                        // all other schemas get tables, views and procedures

                        ResultSet tables = metaData.getTables("APP", "APP", null, new String[]{"TABLE"});

                        ResultSetMetaData tableMeta = tables.getMetaData();

                        while (tables.next()) {

                            for (int i = 1; i <= tableMeta.getColumnCount(); i++) {

                                System.out.println(tableMeta.getColumnLabel(i) + " " + tables.getString(i) + " ");

                            }
                        }
                    }
                }
            }

        } catch (SQLException e) {
            throw new DerbyManagerException(e);

        } catch (ClassNotFoundException e) {
            throw new DerbyManagerException(e);

        } catch (InstantiationException e) {
            throw new DerbyManagerException(e);

        } catch (IllegalAccessException e) {
            throw new DerbyManagerException(e);
        }

        return null;
    }
}
