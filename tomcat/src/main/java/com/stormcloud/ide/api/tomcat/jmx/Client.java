package com.stormcloud.ide.api.tomcat.jmx;

import java.io.IOException;
import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import org.apache.log4j.Logger;

public class Client {

    private Logger LOG = Logger.getLogger(getClass());

    /**
     * Inner class that will handle the notifications.
     */
    public class ClientListener implements NotificationListener {

        @Override
        public void handleNotification(Notification notification,
                Object handback) {

            LOG.info("\nReceived notification:");
            LOG.info("\tClassName: " + notification.getClass().getName());
            LOG.info("\tSource: " + notification.getSource());
            LOG.info("\tType: " + notification.getType());
            LOG.info("\tMessage: " + notification.getMessage());

            if (notification instanceof AttributeChangeNotification) {

                AttributeChangeNotification acn =
                        (AttributeChangeNotification) notification;

                LOG.info("\tAttributeName: " + acn.getAttributeName());
                LOG.info("\tAttributeType: " + acn.getAttributeType());
                LOG.info("\tNewValue: " + acn.getNewValue());
                LOG.info("\tOldValue: " + acn.getOldValue());
            }
        }
    }

    /*
     * For simplicity, we declare "throws Exception". Real programs will usually
     * want finer-grained exception handling.
     */
    public static void main(String[] args) {

        // Create an RMI connector client and
        // connect it to the RMI connector server
        //
        new Client().run();


    }

    public void run() {

        LOG.info("\nCreate an RMI connector client and "
                + "connect it to the RMI connector server");

        try {


            JMXServiceURL url =
                    new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi");

            JMXConnector jmxc = JMXConnectorFactory.connect(url, null);


            MBeanServerConnection connection = jmxc.getMBeanServerConnection();


            LOG.info(connection.getDefaultDomain());



        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
