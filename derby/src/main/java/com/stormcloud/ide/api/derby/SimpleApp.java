package com.stormcloud.ide.api.derby;

import java.sql.*;
import java.util.Arrays;
import java.util.Properties;

public class SimpleApp {

    private static final String[] EXCLUDED = {"NULLID", "SQLJ", "SYS", "SYSCAT", "SYSCS_DIAG", "SYSCS_UTIL", "SYSFUN", "SYSIBM", "SYSPROC", "SYSSTAT"};

    public static void main(String[] args) {
        new SimpleApp().go(args);
    }

    void go(String[] args) {

        Connection connection = null;

        try {

            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();

            String dbName = "/home/stormcloud/derby/sample";

            Properties props = new Properties();
            props.put("user", "app");
            props.put("password", "app");
            
            System.setProperty("derby.system.home", "/home/stormcloud/derby");

            // connect
            connection = DriverManager.getConnection("jdbc:derby:" + dbName + ";create=true", props);

            DatabaseMetaData metaData = connection.getMetaData();

            // list schemas
            ResultSet schemas = metaData.getSchemas();

            while (schemas.next()) {

                if (!Arrays.asList(EXCLUDED).contains(schemas.getString(1))) {

                    System.out.println(schemas.getString(1));

                    // all other schemas get tables, views and procedures

                    ResultSet tables = metaData.getTables(null, schemas.getString(1), null, new String[]{"TABLE"});

                    ResultSetMetaData tableMeta = tables.getMetaData();

                    while (tables.next()) {

                        for (int i = 1; i <= tableMeta.getColumnCount(); i++) {

                            System.out.println(tableMeta.getColumnLabel(i) + " " + tables.getString(i) + " ");

                        }
                    }

                }

            }
            //DriverManager.getConnection("jdbc:derby:;shutdown=true");


        } catch (SQLException e) {
            e.printStackTrace();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();

        } catch (InstantiationException e) {
            e.printStackTrace();

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


    }
}
