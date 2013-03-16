package openadk.library.impl;

import openadk.library.*;
import openadk.library.infra.SIF_Ack;
import openadk.library.infra.SIF_Header;
import openadk.library.tools.HTTPUtil;
import openadk.util.GUIDGenerator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.Calendar;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: UBAINDA
 * Date: 9/13/11
 * Time: 9:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class HttpPushProtocolContextHandler extends AbstractHandler {

    private ZoneImpl fZone;
    private static final int SLASH_ZONES_SLASH_OFFSET = "/zone/".length();
    private static final String TRAILING_SLASH = "/";
    Server fServer;

    public HttpPushProtocolContextHandler(ZoneImpl zone) {
        fZone = zone;
    }

    /**
     *  Process an http request from Jetty.
     */

    public void handle( String target,
                        org.eclipse.jetty.server.Request baseRequest,
                        HttpServletRequest request,
                        HttpServletResponse response ) throws IOException, ServletException
    {

        if( ( ADK.debug & ADK.DBG_MESSAGING ) != 0 )
            fZone.log.debug("Received push message from "+request.getRemoteAddr()+" ("+request.getScheme()+")" );

        SIF_Ack ack = null;
        SIFMessagePayload parsed = null;

        //  Check request length and type
        if( request.getContentLength() < 1 ) {
            try {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
            catch (IOException e) {
                fZone.log.error("Problem sending Bad request error");
            }
        }
/*			if( !request.getContentType().equalsIgnoreCase( SIFIOFormatter.CONTENT_TYPE ) )
				throw new HttpException( HttpResponse.__415_Unsupported_Media_Type );
*/
        String contentTest = "";
        if(request.getContentType() != null) {
        	contentTest = request.getContentType().toLowerCase();
    	}
        if ( (!contentTest.contains(SIFIOFormatter.CONTENT_TYPE_BASE)) ||
                (!contentTest.contains(SIFIOFormatter.CONTENT_TYPE_UTF8)) ) {
            try {
                response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            }
            catch (IOException e) {
                fZone.log.error("Problem sending UNSUPPORTED_MEDIA_TYPE error");
            }
        }

        //  Read raw content
        StringBuffer xml = readPush(request,response);
        if( xml == null ){
            // Shouldn't happen
            xml = new StringBuffer();
        }

        if( ( ADK.debug & ADK.DBG_MESSAGE_CONTENT ) != 0 ){
            fZone.log.debug("Received "+xml.length()+" bytes:\r\n"+xml );
        }

        Throwable parseEx = null;
        boolean reparse = false;
        boolean cancelled = false;
        SIFParser parser;
        try {
            parser = SIFParser.newInstance();
        } catch( ADKException adke ) {
            throw new openadk.util.InternalError( adke.toString() );
        }
        int reparsed = 0;

        do
        {
            try
            {
                parseEx = null;

                //  Parse content
                parsed = (SIFMessagePayload)parser.parse(xml.toString(),fZone);
                parsed.LogRecv(fZone.log);
            }
            catch( ADKParsingException adke )
            {
                parseEx = adke;
            }
            catch( Throwable ex )
            {
                parseEx = ex;
            }

            //
            //	Notify listeners...
            //
            //	If we're asked to reparse the message, do so but do not notify
            //	listeners the second time around.
            //
            if( reparsed == 0 )
            {
                List<MessagingListener> msgList = MessageDispatcher.getMessagingListeners( fZone );
                for( MessagingListener ml : msgList  )
                {
                    try
                    {
                        byte pload = ADK.DTD().getElementType( parsed.getElementDef().name() );
                        byte code = ml.onMessageReceived( pload, xml );
                        switch( code )
                        {
                            case MessagingListener.RX_DISCARD:
                                cancelled = true;
                                break;

                            case MessagingListener.RX_REPARSE:
                                reparse = true;
                                break;
                        }
                    }
                    catch( ADKException adke )
                    {
                        parseEx = adke;
                    }
                    catch(Throwable e) {
                    	if(parseEx == null) {
                    		parseEx = e;
                    	}
                    }
                }
            }

            if( cancelled )
                return;

            reparsed++;
        }
        while( reparse );

        if( parseEx != null )
        {
            //  TODO: Handle the case where SIF_OriginalSourceId and SIF_OriginalMsgId
            //  are not available because parsing failed. See SIFInfra
            //  Resolution #157.
            if( parseEx instanceof SIFException && parsed != null )
            {
                //  Specific SIF error already provided to us by SIFParser
                ack = parsed.ackError( (SIFException)parseEx );
            }
            else{
                String errorMessage = null;
                if( parseEx instanceof ADKException )
                {
                    errorMessage = parseEx.getMessage();
                } else {
                    // Unchecked Throwable
                    errorMessage = "Could not parse message";
                }

                if( parsed == null )
                {
                    SIFException sifError = null;
                    if( parseEx instanceof SIFException ){
                        sifError = (SIFException) parseEx;

                    }else {
                        sifError = new SIFException(SIFErrorCategory.XML_VALIDATION,
                                SIFErrorCodes.XML_GENERIC_ERROR_1,
                                "Could not parse message" , parseEx.toString(), fZone );
                    }

                    ack = SIFPrimitives.ackError(
                            xml.toString(),
                            sifError,
                            fZone );
                }
                else
                {
                    ack = parsed.ackError(
                        SIFErrorCategory.GENERIC,
                        SIFErrorCodes.GENERIC_GENERIC_ERROR_1,
                        errorMessage,
                        parseEx.toString() );
                }

            }



            if( ( ADK.debug & ADK.DBG_MESSAGING ) != 0 )
                fZone.log.warn("Failed to parse push message from zone \"" + fZone + "\": " + parseEx );

            if( ack != null )
            {
                //  Ack messages in the same version of SIF as the original message
                if( parsed != null ){
                    ack.setSIFVersion( parsed.getSIFVersion() );
                }
                ackPush(ack,request,response);
            }
            else
            {
                //  If we couldn't build a SIF_Ack, returning an HTTP 500 is
                //  probably the best we can do to let the server know that
                //  we didn't get the message. Note this should cause the ZIS
                //  to resend the message, which could result in a deadlock
                //  condition. The administrator would need to manually remove
                //  the offending message from the agent's queue.

                if( ( ADK.debug & ADK.DBG_MESSAGING ) != 0 )
                    fZone.log.debug("Could not generate SIF_Ack for failed push message (returning HTTP/1.1 500)");
                try {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
                catch (IOException e) {
                    fZone.log.error("Problem sending INTERNAL_SERVER_ERROR error");
                }
            }

            return;
        }

        //  Check SourceId to see if it matches this agent's SourceId
        String destId = parsed.getDestinationId();
        if( destId != null && !destId.equals( fZone.getAgent().getId() ) )
        {
            fZone.log.warn("Received push message for DestinationId \""+destId+"\", but agent is registered as \""+fZone.getAgent().getId()+"\"" );

            ack = parsed.ackError(
                SIFErrorCategory.TRANSPORT,
                SIFErrorCodes.WIRE_GENERIC_ERROR_1,
                "Message not intended for this agent (SourceId of agent does not match DestinationId of message)",
                "Message intended for \"" + destId + "\" but this agent is registered as \"" + fZone.getAgent().getId() + "\"" );

            ackPush(ack,request,response);

            return;
        }

        //
        //  Check Zone ID.
        //  Extract the zone ID from the path. The path will be the context
        //  string "/zone/{zondId}/" and the pathInContext should be "/" (the
        //  trailing slash) unless the ZIS specified additional information
        //  on URI path for some reason. Extract the zone name
        //  from that.
        //
        String zone = request.getContextPath();
        zone = zone.substring(SLASH_ZONES_SLASH_OFFSET, zone.length());
        if( !zone.equals( fZone.getZoneId() ) )
        {
            fZone.log.warn("Received push message from zone \""+zone+"\", but agent is expecting messages from zone \""+fZone.getZoneId() );

            ack = parsed.ackError(
                SIFErrorCategory.SYSTEM,
                SIFErrorCodes.SYS_GENERIC_ERROR_1,
                "Unexpected Zone",
                "Agent not expecting messages from zone: "+zone);

            ackPush(ack,request,response);

            return;
        }

        //  Convert content to SIF message object and dispatch it
        ack = processPush(parsed);

        //  Send SIF_Ack reply
        ackPush(ack,request,response);
    }


    private SIF_Ack processPush( SIFMessagePayload parsed )
		{
			try
			{
				//  Dispatch. When the result is an Integer it is an ack code to
				//  return; otherwise it is ack data to return and the code is assumed
				//  to be 1 for an immediate acknowledgement.
				int ackStatus = fZone.getFDispatcher().dispatch(parsed);

				//  Ask the original message to generate a SIF_Ack for itself
		    	return parsed.ackStatus(ackStatus);
			}
			catch( SIFException se )
			{
				return parsed.ackError( se );
			}
			catch( ADKException adke )
			{
				return parsed.ackError(
					SIFErrorCategory.GENERIC,
					SIFErrorCodes.GENERIC_GENERIC_ERROR_1,
					adke.getMessage() );
			}
			catch( Throwable thr )
			{
				if( ( ADK.debug & ADK.DBG_MESSAGING ) != 0 )
					fZone.log.debug("Uncaught exception dispatching push message: "+thr);

				return parsed.ackError(
						SIFErrorCategory.GENERIC,
					SIFErrorCodes.GENERIC_GENERIC_ERROR_1,
					"An unexpected error has occurred",
					thr.toString() );
			}
		}

		private StringBuffer readPush( HttpServletRequest request, HttpServletResponse response )
		{
			Reader in = null;
			int expected = request.getContentLength(),
				totalRead = 0;

			try
			{
				// NOTE: Keep this a StringBuffer for now because it is used externally
				StringBuffer strbuf = new StringBuffer(expected > 64 ? expected : 64);
				char buf[] = new char[expected < 1024 ? expected : 1024];

				String contentEncoding = request.getHeader("Content-Encoding");
				if (contentEncoding == null) {
					in = SIFIOFormatter.createInputReader( request.getInputStream() );
				} else {
					String encoding = contentEncoding.toLowerCase();
					if (encoding.contains("gzip")) { // gzip or x-gzip
					in = SIFIOFormatter.createInputReader(new GZIPInputStream(request.getInputStream()));
					} 
					else if (encoding.contains("compress")) {	// compress or x-compress
						in = SIFIOFormatter.createInputReader(new ZipInputStream(request.getInputStream()));
					}
					else if (encoding.contains("deflate")) {	// deflate or x-deflate
						in = SIFIOFormatter.createInputReader(new InflaterInputStream(request.getInputStream()));
				} else {
						fZone.log.error("HttpProtocolHandler push message failure : Unsuported Content-Encoding of '" + contentEncoding + "'" );
		                try {
				            response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
		                }
		                catch (IOException e) {
		                    fZone.log.error("Problem sending UNSUPPORTED_MEDIA_TYPE error");
		                }
			            return null;
					}
				}

				int charsRead = 0;
				while ((charsRead = in.read(buf)) > -1) {
					if (charsRead > 0) {
						strbuf.append(buf, 0, charsRead);
						totalRead += charsRead;
					}
				}

				return strbuf;
			}
			catch( Throwable thr )
			{
				fZone.log.error("HttpProtocolHandler failed to read push message (approximately "+
					totalRead+" of "+expected+" bytes read; zone="+fZone.getZoneId()+"): "+thr);
                try {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
                catch (IOException e) {
                    fZone.log.error("Problem sending INTERNAL_SERVER_ERROR error");
                }
                return null;
			}
			finally
			{
				if( in != null ) {
					try {
						in.close();
					} catch( IOException ignored )
					{
						fZone.log.warn( ignored.getMessage(), ignored );
					}
				}
			}
		}

		private void ackPush( SIF_Ack ack, HttpServletRequest request, HttpServletResponse response )
		{
			try
			{
				//  Set SIF_Ack / SIF_Header fields
				SIF_Header hdr = ack.getHeader();
				hdr.setSIF_Timestamp( Calendar.getInstance() );
				hdr.setSIF_MsgId(GUIDGenerator.makeGUID());
				hdr.setSIF_SourceId(fZone.getAgent().getId());

				ack.LogSend(fZone.log);

			    //  Convert message to a string
				ByteArrayOutputStream raw = new ByteArrayOutputStream();
				SIFWriter out = new SIFWriter( raw, fZone );
				out.write(ack);
				out.close();
				raw.close();

				byte[] realData = raw.toByteArray();
				boolean compressed = false;
				raw.reset();

				if (fZone.getProperties().getCompressionThreshold() > -1 && realData.length > fZone.getProperties().getCompressionThreshold()) {
					String acceptEncoding = request.getHeader("Accept-Encoding");
					if (acceptEncoding != null) {
						List<String> tokens = HTTPUtil.derivePreferredCodingFrom(acceptEncoding);
						if (tokens.contains("gzip")) {
							GZIPOutputStream gzos = new GZIPOutputStream(raw);
							gzos.write(realData);
							gzos.flush();
							gzos.finish();
							realData = raw.toByteArray();
							compressed = true;
						}
					}
				}

			    //  Send reply
				response.setContentType( SIFIOFormatter.CONTENT_TYPE );
				response.setContentLength(realData.length);
				if (compressed) {
					response.setHeader("Content-Encoding", "gzip");
				}
				response.getOutputStream().write(realData);
				response.getOutputStream().flush();
		//		reply.close();
			}
			catch( Throwable thr )
			{
				fZone.log.error("HttpProtocolHandler failed to send SIF_Ack for pushed message (zone="+
					fZone.getZoneId()+"): "+thr);
                try {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
                catch (IOException e) {
                    fZone.log.error("Problem sending INTERNAL_SERVER_ERROR error");
                }
			}
		}
}
