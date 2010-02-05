/*
 * ------------------------------------------------------------------------------
 * Hermes FTP Server
 * Copyright (c) 2005-2007 Lars Behnke
 * ------------------------------------------------------------------------------
 * 
 * This file is part of Hermes FTP Server.
 * 
 * Hermes FTP Server is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Hermes FTP Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Hermes FTP Server; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * ------------------------------------------------------------------------------
 */

package au.org.arcs.griffin.cmd.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.gsi.GlobusCredential;
import org.ietf.jgss.ChannelBinding;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSName;

import au.org.arcs.griffin.cmd.AbstractFtpCmd;
import au.org.arcs.griffin.exception.FtpCmdException;


/**
 * <b>ABORT (ABOR)</b>
 * <p>
 * This command tells the server to abort the previous FTP service command and any associated
 * transfer of data. The abort command may require "special action", as discussed in the Section on
 * FTP Commands, to force recognition by the server. No action is to be taken if the previous
 * command has been completed (including data transfer). The control connection is not to be closed
 * by the server, but the data connection must be closed.
 * <p>
 * There are two cases for the server upon receipt of this command: (1) the FTP service command was
 * already completed, or (2) the FTP service command is still in progress.
 * <p>
 * In the first case, the server closes the data connection (if it is open) and responds with a 226
 * reply, indicating that the abort command was successfully processed.
 * <p>
 * In the second case, the server aborts the FTP service in progress and closes the data connection,
 * returning a 426 reply to indicate that the service request terminated abnormally. The server then
 * sends a 226 reply, indicating that the abort command was successfully processed.
 * <p>
 * <i>[Excerpt from RFC-959, Postel and Reynolds]</i>
 * </p>
 * 
 * @author Lars Behnke
 */
public class FtpCmdAdat extends AbstractFtpCmd {
	private static Log log = LogFactory.getLog(FtpCmdAdat.class);
    /**
     * {@inheritDoc}
     */
    public void execute() throws FtpCmdException {
        String prot = getArguments().trim();
        if ( prot == null || prot.length() <= 0 ) {
            msgOut(MSG501);
            return;
        }

        if ( getCtx().getServiceContext() == null ) {
        	msgOut(MSG503);
            return;
        }
        log.debug("prot length:"+prot.length());
//        log.debug("prot: "+prot);
//        byte[] b=prot.getBytes();
//        for (int i=0;i<b.length;i++){
//        	log.info(b[i]);
//        }
        byte[] token = Base64.decodeBase64(prot.getBytes());
        log.debug("token length:"+token.length);
//        log.debug(byteArrayToHexString(token));
        ChannelBinding cb;
        try {
            cb = new ChannelBinding(getCtx().getClientSocket().getInetAddress(),InetAddress.getLocalHost(), null);
            log.debug("adat: Local address: " + InetAddress.getLocalHost());
            log.debug("adat: Client address: " + getCtx().getClientSocket().getInetAddress());
        } catch( UnknownHostException e ) {
            msgOut(MSG500_ADAT, new String[]{e.getMessage()});
            return;
        }
        GSSName GSSIdentity=null;
        log.debug("token length:"+token.length);
        try {
            //serviceContext.setChannelBinding(cb);
            //debug("GssFtpDoorV1::ac_adat: CB set");
        	GSSContext context=getCtx().getServiceContext();
        	log.debug("context:"+context.getLifetime());
        	log.debug("context:"+context.getSrcName());
        	log.debug("context:"+context.getTargName());
//        	context.
//        	context.setChannelBinding(cb);
            token = context.acceptSecContext(token, 0, token.length);
            //debug("GssFtpDoorV1::ac_adat: Token created");
            GSSIdentity = context.getSrcName();
            log.debug("GSSIdentity:"+GSSIdentity);
            getCtx().setGSSIdentity(GSSIdentity);
            //debug("GssFtpDoorV1::ac_adat: User principal: " + UserPrincipal);
        } catch( Exception e ) {
        	e.printStackTrace();
            log.error("adat: got service context exception: " +
                  e.getMessage());
            msgOut(MSG535, new String[]{e.getMessage()});
            return;
        }
        if (token != null) {
            if (!getCtx().getServiceContext().isEstablished()) {
                msgOut(MSG335,new String[]{new String(Base64.encodeBase64(token))});
            }
            else {
                msgOut(MSG235,new String[]{new String(Base64.encodeBase64(token))});
            }
        }
        else {
            if (!getCtx().getServiceContext().isEstablished()) {
                msgOut(MSG335,new String[]{""});
            }
            else {
                log.info("adat: security context established " +
                     "with " + GSSIdentity);
                log.debug("delegation cert state:"+getCtx().getServiceContext().getCredDelegState());
                try {
                	GSSCredential cert=getCtx().getServiceContext().getDelegCred();
					log.debug("delegation cert:"+cert.getName().toString());
				} catch (GSSException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                msgOut(MSG235);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getHelp() {
        return "ADAT for GSI auth";
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAuthenticationRequired() {
        return false;
    }

    /**

    * Convert a byte[] array to readable string format. This makes the "hex"
    readable!

    * @return result String buffer in String format 

    * @param in byte[] buffer to convert to string format

    */

    private String byteArrayToHexString(byte in[]) {

        byte ch = 0x00;

        int i = 0; 

        if (in == null || in.length <= 0)

            return null;
            
        String pseudo[] = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

        StringBuffer out = new StringBuffer(in.length * 2);

        

        while (i < in.length) {
            ch = (byte) (in[i] & 0xF0); // Strip off high nibble
            ch = (byte) (ch >>> 4);
         // shift the bits down
            ch = (byte) (ch & 0x0F);    
    // must do this is high order bit is on!
            out.append(pseudo[ (int) ch]); // convert the nibble to a String Character
            ch = (byte) (in[i] & 0x0F); // Strip off low nibble 
            out.append(pseudo[ (int) ch]); // convert the nibble to a String Character
            out.append(" ");
            i++;
        }

        String rslt = new String(out);

        return rslt;

    }

	public boolean isExtension() {
		// TODO Auto-generated method stub
		return false;
	}  
}
