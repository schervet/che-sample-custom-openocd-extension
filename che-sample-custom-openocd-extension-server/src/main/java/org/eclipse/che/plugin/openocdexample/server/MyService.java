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
package org.eclipse.che.plugin.openocdexample.server;

import org.eclipse.che.api.debugger.server.DebuggerManager;
import org.eclipse.che.api.debug.shared.model.action.Action;
import org.eclipse.che.api.debug.shared.model.action.SuspendAction;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.api.debug.shared.dto.action.SuspendActionDto;
import org.eclipse.che.api.debugger.server.Debugger;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerNotFoundException;

import com.google.inject.Inject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

@Path("hello")
public class MyService {
    private final DebuggerManager debuggerManager;
    
    @Inject
    public MyService(DebuggerManager debuggerManager) {
        this.debuggerManager = debuggerManager;
    }
    
    @GET
    @Path("{name}")
    public String sayHello(@PathParam("name") String name,
                            @QueryParam("actionParam") String actionParam) {
        Debugger debugger = null;
        String sessionIdFound = null;
        
        if(actionParam.equals("DISCONNECT")) {
            int tempSessionId = 0;
            String tempSessionIdS = "0";
            debugger = null;
            
            /** Search for active Debugger sessionId **/
            while((debugger == null) && (tempSessionId < 255)){
                tempSessionIdS = Integer.toString(tempSessionId);
                try {
                    if(debuggerManager.getDebuggerType(tempSessionIdS).equals("gdbOpenocd")) {
                        debugger = debuggerManager.getDebugger(tempSessionIdS);
                        sessionIdFound = tempSessionIdS;
                    } else {
                        tempSessionId++;
                    }
                    
                } catch (DebuggerNotFoundException e) {
                    /* continue to search sessionId */
                    tempSessionId++;
                }
            }
        }
        
        if(debugger == null){
            return name + " Failed. Debugger Problem (Not gdbOpenocd or disconnected).";
        }
        else{
            String debuggerName = "";
            int tempLoop = 0;
            try {
                /** Suspend Active Debugger **/
                SuspendActionDto actionSusp = DtoFactory.newDto(SuspendActionDto.class);
                
                actionSusp.setType(Action.TYPE.SUSPEND);
                debugger.suspend((SuspendAction)actionSusp);
                
                debuggerName = debuggerManager.getDebuggerType(sessionIdFound);
                debuggerName = debuggerName + ": " + debugger.getInfo().getName();
                
                /** Disconnect Debugger **/
                debugger.disconnect();
                
                do {
                    try {
                        Thread.sleep(2000);                 //1000 milliseconds is one second.
                        
                    } catch(InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    tempLoop++;
                    try {
                        debugger = debuggerManager.getDebugger(sessionIdFound);
                    } catch (DebuggerNotFoundException e) {
                        debugger = null;
                    }
                } while ((debugger != null) && (tempLoop < 5));
                /** debugger is now null, ready for restart **/
            } catch(DebuggerException ex) {
                debuggerName = "";
            }
            if(actionParam.equals("DISCONNECT")) {
                if(debuggerName.equals("")) {
                    return name + " Debugger Disconnect failed";
                } else {
                    return name + " Debugger Ready for restart! " + debuggerName;
                }
            } else {
                return name + " :-)";
            }
        }
    }
}
