/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/

package org.eclipse.che.plugin.openocdexample.ide;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationsManager;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerManager;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;

public class MyAction extends Action {

    private final NotificationManager notificationManager;
    private final MyServiceClient serviceClient;
    private final DebuggerManager debuggerManager;
    private final DebugConfigurationsManager configurationsManager;

    @Inject
    public MyAction(MyResources resources, NotificationManager notificationManager, MyServiceClient serviceClient, 
                    DebuggerManager debuggerManager , DebugConfigurationsManager  configurationsManager) {
        super("GDB OpenOCD Action Reset", "Desc: Target Reset command", null, resources.MyProjectTypeIcon());
        this.notificationManager = notificationManager;
        this.serviceClient = serviceClient;
        this.debuggerManager = debuggerManager;
        this.configurationsManager = configurationsManager;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Debugger debugger = debuggerManager.getActiveDebugger();

        /** Suspend active debugger **/
        if (debugger != null) {
            debugger.suspend();
            notificationManager.notify("MyAction step 1: Debugger suspended", StatusNotification.Status.PROGRESS, NOT_EMERGE_MODE);
        }
        else {
            notificationManager.notify("MyAction step 1: Fail", StatusNotification.Status.FAIL, EMERGE_MODE);
            return;
        }
        
        /** Disconnect active debugger using MyService Server action **/
        // This calls the service in the workspace.
        // This method is in our MyServiceClient class
        // This is a Promise, so the .then() method is invoked after the response is made
        serviceClient.getHello("Target Reset action: ", "DISCONNECT")
                .then(new Operation<String>() {
                    @Override
                    public void apply(String arg) throws OperationException {
                        /** Restart debugger **/
                        Debugger debugger1 = debuggerManager.getActiveDebugger();
                        if(debugger1 == null) {
                            notificationManager.notify("MyAction step 2: Debugger disconnected", StatusNotification.Status.PROGRESS, NOT_EMERGE_MODE);

                            /** Restart debugger **/
                            Optional<DebugConfiguration> configurationOptional = configurationsManager.getCurrentDebugConfiguration();
                            if (configurationOptional.isPresent()) {
                                configurationsManager.apply(configurationOptional.get());
                            }
                            notificationManager.notify("MyAction step 3: Debugger restart launched", StatusNotification.Status.PROGRESS, NOT_EMERGE_MODE);
                        }
                        else {
                            notificationManager.notify("MyAction step 2: Fail", StatusNotification.Status.PROGRESS, NOT_EMERGE_MODE);
                        }
                        
                        // This passes the response String to the notification manager.
                        notificationManager.notify(arg, StatusNotification.Status.SUCCESS, EMERGE_MODE);
                    }
                })
                .catchError(new Operation<PromiseError>() {
                    @Override
                    public void apply(PromiseError arg) throws OperationException {
                        notificationManager.notify("Fail", StatusNotification.Status.FAIL, EMERGE_MODE);
                    }
                });
    }
    
    @Override
    public void update(ActionEvent event) {
        Debugger debugger = debuggerManager.getActiveDebugger();
        event.getPresentation().setEnabled(debugger != null && debugger.isConnected());
    }
}
