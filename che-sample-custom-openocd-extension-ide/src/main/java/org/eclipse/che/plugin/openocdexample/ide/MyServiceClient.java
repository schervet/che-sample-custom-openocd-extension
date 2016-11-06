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

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;

import javax.inject.Inject;


public class MyServiceClient {
    private final AsyncRequestFactory asyncRequestFactory;
    private final LoaderFactory loaderFactory;
    private final AppContext appContext;

    @Inject
    public MyServiceClient(AsyncRequestFactory asyncRequestFactory,
                           AppContext appContext,
                           LoaderFactory loaderFactory) {
        this.asyncRequestFactory = asyncRequestFactory;
        this.loaderFactory = loaderFactory;
        this.appContext = appContext;

        // appContext is a Che class that provides access to workspace
    }

    // Invoked by our MyAction class
    // Invokes the request to the server
    public Promise<String> getHello(String name, String actionParam) {
      String helloPath = appContext.getDevMachine().getWsAgentBaseUrl() + "/hello/" + name;
      final String params = "?actionParam=" + actionParam;
      
      
        return asyncRequestFactory.createGetRequest(helloPath + params)
                .loader(loaderFactory.newLoader("Loading your response..."))
                .send(new StringUnmarshaller());
    }
}
