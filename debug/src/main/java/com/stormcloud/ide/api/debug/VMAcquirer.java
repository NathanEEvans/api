package com.stormcloud.ide.api.debug;

/*
 * #%L
 * Stormcloud IDE - API - Debug
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

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author martijn
 */
public class VMAcquirer {

    /**
     * Call this with the localhost port to connect to.
     */
    public VirtualMachine connect(int port) throws IOException {

        String strPort = Integer.toString(port);
        AttachingConnector connector = getConnector();

        try {

            VirtualMachine vm = connect(connector, strPort);

            return vm;

        } catch (IllegalConnectorArgumentsException e) {
            throw new IllegalStateException(e);
        }
    }

    private AttachingConnector getConnector() {

        VirtualMachineManager vmManager = Bootstrap.virtualMachineManager();

        for (Connector connector : vmManager.attachingConnectors()) {

            System.out.println(connector.name());

            if ("com.sun.jdi.SocketAttach".equals(connector.name())) {
                return (AttachingConnector) connector;
            }
        }
        throw new IllegalStateException();
    }

    private VirtualMachine connect(AttachingConnector connector, String port) throws IllegalConnectorArgumentsException, IOException {

        Map<String, Connector.Argument> args = connector.defaultArguments();

        Connector.Argument pidArgument = args.get("port");

        if (pidArgument == null) {
            throw new IllegalStateException();
        }

        pidArgument.setValue(port);

        return connector.attach(args);
    }
}