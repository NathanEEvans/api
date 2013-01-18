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

import com.sun.jdi.Field;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.ModificationWatchpointEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ModificationWatchpointRequest;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author martijn
 */
public class FieldMonitor {

    public static final String CLASS_NAME = "Test";
    public static final String FIELD_NAME = "foo";

    public static void main(String[] args) throws IOException, InterruptedException {

        // connect
        VirtualMachine vm = new VMAcquirer().connect(8000);

        // set watch field on already loaded classes
        List<ReferenceType> referenceTypes = vm.classesByName(CLASS_NAME);

        for (ReferenceType refType : referenceTypes) {
            addFieldWatch(vm, refType);
        }

        // watch for loaded classes
        addClassWatch(vm);

        // resume the vm
        vm.resume();

        // process events
        EventQueue eventQueue = vm.eventQueue();

        while (true) {

            EventSet eventSet = eventQueue.remove();

            for (Event event : eventSet) {

                if (event instanceof VMDeathEvent || event instanceof VMDisconnectEvent) {

                    // exit
                    return;

                } else if (event instanceof ClassPrepareEvent) {

                    // watch field on loaded class
                    ClassPrepareEvent classPrepEvent = (ClassPrepareEvent) event;

                    ReferenceType refType = classPrepEvent.referenceType();

                    addFieldWatch(vm, refType);

                } else if (event instanceof ModificationWatchpointEvent) {

                    // a Test.foo has changed
                    ModificationWatchpointEvent modEvent = (ModificationWatchpointEvent) event;

                    System.out.println("old=" + modEvent.valueCurrent());
                    System.out.println("new=" + modEvent.valueToBe());
                    System.out.println();
                }
            }
            eventSet.resume();
        }
    }

    /**
     * Watch all classes of name "Test"
     */
    private static void addClassWatch(VirtualMachine vm) {

        EventRequestManager erm = vm.eventRequestManager();
        ClassPrepareRequest classPrepareRequest = erm.createClassPrepareRequest();
        classPrepareRequest.addClassFilter(CLASS_NAME);
        classPrepareRequest.setEnabled(true);
    }

    /**
     * Watch field of name "foo"
     */
    private static void addFieldWatch(VirtualMachine vm, ReferenceType refType) {

        EventRequestManager erm = vm.eventRequestManager();

        Field field = refType.fieldByName(FIELD_NAME);
        ModificationWatchpointRequest modificationWatchpointRequest = erm.createModificationWatchpointRequest(field);

        modificationWatchpointRequest.setEnabled(true);
    }
}