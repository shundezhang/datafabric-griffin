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

package au.org.arcs.griffin.cmd;

import java.io.IOException;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.org.arcs.griffin.exception.FtpCmdException;

/**
 * Abstract super class of commands setting up the data channel in passive mode (PASV, EPSV).
 * 
 * @author Behnke
 * @author Shunde Zhang
 */
public abstract class AbstractFtpCmdPort extends AbstractFtpCmd {

    private static Log log = LogFactory.getLog(AbstractFtpCmdPort.class);

    /**
     * {@inheritDoc}
     */
    public void execute() throws FtpCmdException {
        try {
            String args = getArguments();
            if (args.length() == 0) {
                msgOut(MSG501);
                return;
            }

            int protocolIdx = doReadProtocolIdx(args);
            String addr = doReadIPAddr(args);
            int port = doReadPort(args);
            log.debug("Data Channel Protocol: " + protocolIdx + ", IPAddr: " + addr + ", port: " + port);

            setupDataChannel(protocolIdx, addr, port);

            msgOut(MSG200);
        } catch (IOException e) {
            log.error(e.toString());
            out("500 Error: "+e.toString());
        } catch (IllegalArgumentException e) {
            log.error(e.toString());
            msgOut(MSG501);
        }
    }

    /**
     * Sets up the data channel in active transfer mode. IPv4 and IPv6 are supported.
     * 
     * @param protocolIdx Protocol index (IPv4 or IPv6)
     * @param ipAddr IPv4 or IPv6 compliant address.
     * @param port The port.
     * @throws IOException Setting up data channel failed.
     */
    protected void setupDataChannel(int protocolIdx, String ipAddr, int port) throws IOException {
    	getCtx().closeDataChannels();
        DataChannelInfo info = new DataChannelInfo(ipAddr, port);
        DataChannelProvider provider = null;
        if (getCtx().getNetworkStack()==NETWORK_STACK_UDP){
        }else{ //create TCP provider
        	provider=new ActiveModeTCPDataChannelProvider(getCtx(), info);
        }
        provider.init();
        getCtx().setDataChannelProvider(provider);
    	
//    	if (getCtx().getNetworkStack()==NETWORK_STACK_UDP){
//    		DataChannel dc=new UDTDataChannel(getCtx(), ipAddr, port);
////    		DataChannelInfo info=dc.init();
//    		getCtx().setDataChannel(dc);
//    	}else{
//            getCtx().closeSockets();
//            DataChannelInfo info = new DataChannelInfo(ipAddr, port);
//            SocketProvider provider = new ActiveModeSocketProvider(getCtx(), info);
//            provider.init();
//            getCtx().setDataSocketProvider(provider);
//    	}
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAuthenticationRequired() {
        return true;
    }

    /**
     * Reads port from passed arguments.
     * 
     * @param args The arguments.
     * @return The port.
     */
    protected abstract int doReadPort(String args);

    /**
     * Reads the IPv4 or IPv6 compliant address from the passed arguments.
     * 
     * @param args The arguments.
     * @return The IP address.
     */
    protected abstract String doReadIPAddr(String args);

    /**
     * Reads the protocol index (1=IPv4, 2=IPv6) from the passed arguments.
     * 
     * @param args The arguments.
     * @return The protocol index.
     */
    protected abstract int doReadProtocolIdx(String args);

}
