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

import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.plugin.debugger.ide.actions.DisconnectDebuggerAction;
import org.eclipse.che.plugin.debugger.ide.actions.SuspendAction;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerPresenter;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_MAIN_MENU;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_RUN;
import static org.eclipse.che.ide.api.constraints.Constraints.LAST;

@Singleton
@Extension(title = "My Extension", version = "1.0.0")
public class MyExtension {

    @Inject
    public MyExtension(MyResources resources,
                       ActionManager actionManager,
                       DebuggerPresenter debuggerPresenter,
                       MyAction action,
                       SuspendAction suspendAction
                       ) {

        DefaultActionGroup mainMenu = (DefaultActionGroup) actionManager.getAction(GROUP_MAIN_MENU);
        DefaultActionGroup myMenu = new DefaultActionGroup("OpenOCD Menu", true, actionManager);
        mainMenu.add(myMenu, Constraints.LAST);

        actionManager.registerAction("MyMenuID", myMenu);
        actionManager.registerAction("MyActionID", action);
        /* suspend action already registered in DebuggerExtension */
        
        myMenu.add(action);
        myMenu.add(suspendAction, LAST);
        
        /* Add My action to existing Run Menu */
        DefaultActionGroup runMenu = (DefaultActionGroup)actionManager.getAction(GROUP_RUN);
        runMenu.addSeparator();
        runMenu.add(action, LAST);

        /* Add My action to existing debuggerToolbarActionGroup */
        
        DefaultActionGroup myDebuggerToolbarActionGroup = new DefaultActionGroup(actionManager);
        myDebuggerToolbarActionGroup.add(action);
        debuggerPresenter.getDebuggerToolbar().bindCenterGroup(myDebuggerToolbarActionGroup);
    }
}
