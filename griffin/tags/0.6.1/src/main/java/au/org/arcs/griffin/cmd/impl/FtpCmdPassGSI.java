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

import au.org.arcs.griffin.cmd.AbstractFtpCmd;
import au.org.arcs.griffin.exception.FtpCmdException;

/**
 * <b>PASSWORD (PASS)</b>
 * <p>
 * Pass for GSI, it does nothing.
 * </p>
 * 
 * @author Lars Behnke
 * @author Shunde Zhang
 */
public class FtpCmdPassGSI extends AbstractFtpCmd {

    /**
     * {@inheritDoc}
     */
    public void execute() throws FtpCmdException {
//        debug("GssFtpDoorV1::ac_pass: PASS is a no-op with " +
//        	"GSSAPI authentication.");
		if ( getCtx().getGSSIdentity() != null ) {
		    msgOut(MSG200);
		    return;
		}
		else {
		    msgOut(MSG500_ERROR, new String[]{"Send USER first"});
		    return;
		}

//        String response;
//        if (getCtx().getUser() == null || getCtx().getUser().length() == 0) {
//            response = msg(MSG503_USR);
//        } else {
//            getCtx().setPassword(getArguments());
//            if (getCtx().authenticate()) {
//                response = msg(MSG230);
//            } else {
//                response = msg(MSG530);
//            }
//        }
//        out(response);
//        String clientHost = getCtx().getClientSocket().getInetAddress().getHostAddress();
//        getCtx().getEventListener().loginPerformed(clientHost, getCtx().isAuthenticated());
    }

    /**
     * {@inheritDoc}
     */
    public String getHelp() {
        return "Sets the user's password";
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAuthenticationRequired() {
        return false;
    }

	public boolean isExtension() {
		// TODO Auto-generated method stub
		return false;
	}

}
