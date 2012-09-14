//
// Copyright (c)1998-2011 Pearson Education, Inc. or its affiliate(s). 
// All rights reserved.
//

package openadk.library.impl;

import openadk.library.ADK;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.component.LifeCycle;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

public class AnonymousHttpHandler implements Handler
{
    private ContextHandler fHttpCtx;
    private Server fServer;

	public void handle( String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response )
	{
		System.out.println("Warning: Anonymous message received from ZIS");
		ADK.getLog().warn("Warning: Anonymous message received from ZIS" + baseRequest.getUri());
        try {
            response.sendError(403, "Warning: Anonymous message received from ZIS" + baseRequest.getUri() );
        } catch (IOException e) {
            ADK.getLog().error("Problem with 403 Annonymous response.");
        }
    }

    public void setServer(Server server) {
        fServer = server;
    }

    public Server getServer() {
        return fServer;
    }

    public void destroy() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void start() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void stop() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isRunning() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isStarted() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isStarting() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isStopping() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isStopped() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isFailed() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addLifeCycleListener(Listener listener) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeLifeCycleListener(Listener listener) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}