//
// Copyright (c)1998-2011 Pearson Education, Inc. or its affiliate(s). 
// All rights reserved.
//

package openadk.library.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import openadk.library.ADK;
import openadk.library.ADKException;
import openadk.library.ADKParsingException;
import openadk.library.ADKTransportException;
import openadk.library.MessagingListener;
import openadk.library.SIFErrorCategory;
import openadk.library.SIFErrorCodes;
import openadk.library.SIFException;
import openadk.library.SIFMessagePayload;
import openadk.library.SIFParser;
import openadk.library.SIFVersion;
import openadk.library.SIFWriter;
import openadk.library.Zone;
import openadk.library.infra.SIF_Ack;
import openadk.library.infra.SIF_Data;
import openadk.library.infra.SIF_Event;
import openadk.library.infra.SIF_Header;
import openadk.library.infra.SIF_Protocol;
import openadk.library.infra.SIF_Status;
import openadk.library.tools.HTTPUtil;
import openadk.util.GUIDGenerator;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerCollection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.Calendar;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Andy Elmhorst
 * @version ADK 2.1
 *
 */
public class HttpPushProtocolHandler extends BaseHttpProtocolHandler
{
	/**
	 *
	 */
	private static final long serialVersionUID = 96862608535041043L;
	private ContextHandler fHttpCtx;
	private Server fServer;
    private boolean failed;
	public static AnonymousHttpHandler sAnonymousHandler;

	HttpPushProtocolHandler( HttpTransport transport, Server server )
	{
		super( transport );
		fServer = server;
	}
    
	@Override
	public synchronized void start() throws ADKException
	{
		try
		{
			//
			//  For Push mode, establish an HttpContext for this zone.
			//  Messages received by the ZIS server will be routed to us.
			//
			String ctx = "/zone/" + fZone.getZoneId();

            Handler handler = fServer.getHandler();
            boolean alreadyHandled = false;
            if(handler != null && handler instanceof ContextHandlerCollection){
                ContextHandlerCollection handlerCollection = (ContextHandlerCollection) handler;
                Handler[] handlers = handlerCollection.getHandlers();
                if( handlers != null ) {
                    for( int i = 0; i < handlers.length; i++ ) {
                        if(handlers[i] instanceof ContextHandler) {
                            ContextHandler ch = (ContextHandler)handlers[i];
                            if( ctx.equals(ch.getContextPath()) ) {
                                alreadyHandled = true;
                                fHttpCtx = ch;
                                break;
                            }
                        }
                    }
                }
                if( !alreadyHandled ){
                    fHttpCtx = new ContextHandler(ctx);
                    fHttpCtx.setResourceBase(".");
                    fHttpCtx.setClassLoader(this.getClass().getClassLoader());
                    fHttpCtx.setServer(fServer);
                    fHttpCtx.setHandler(new HttpPushProtocolContextHandler(fZone));
                    handlerCollection.addHandler(fHttpCtx);
                    if(fServer.isRunning()) {
                    	fHttpCtx.start();
                    }
                }
            }
            else {
                throw new Exception("handler not set as was expected handler:" +handler.toString());
            }
            if (!fServer.isRunning()) {
                fServer.start();
            }
		}
		catch( Exception e )
		{
            failed = true;
			throw new ADKException("HttpProtocolHandler could not establish HttpContext: "+e,fZone);
		}

	}


    @Override
    public void shutdown() {
        if( fServer != null && fHttpCtx != null ) {
            try {
                fHttpCtx.stop();
            } catch( Exception ignored ) {
                fZone.log.warn( "Error shutting down context: " + ignored, ignored );
                failed = true;
            }
            Handler handler = fServer.getHandler();
            if(handler instanceof ContextHandlerCollection){
                ContextHandlerCollection handlerCollection = (ContextHandlerCollection) handler;
                handlerCollection.removeHandler(fHttpCtx);
            }
        }
    }


    public void stop() throws InterruptedException {
        shutdown();
    }


    /* (non-Javadoc)
          * @see com.edustructures.sifworks.impl.IProtocolHandler#isActive(com.edustructures.sifworks.impl.ZoneImpl)
          */
    public boolean isActive(ZoneImpl zone)
        throws ADKTransportException
    {
        return fTransport.isActive( zone ) && fHttpCtx != null && fHttpCtx.isStarted();
    }


    /**
     * Creates a SIF_Protocol object for a SIF_Register message.
     * <p>
     *
     * @param zone
     *            The zone the SIF_Register message will be sent to
     * @return A SIF_Protocol object to be included in the SIF_Register message,
     *         or null if the zone is not operating in Push mode
     */
    public SIF_Protocol makeSIF_Protocol(Zone zone, SIFVersion version)
        throws ADKTransportException
    {
        SIF_Protocol proto = new SIF_Protocol();
        fTransport.configureSIF_Protocol( proto, zone, version );
        return proto;
    }
}

