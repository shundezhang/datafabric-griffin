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
import au.org.arcs.griffin.exception.FtpQuitException;

/**
 * <b>LOGOUT (QUIT)</b> This command terminates a USER and if file transfer is not in progress, the
 * server closes the control connection. If file transfer is in progress, the connection will remain
 * open for result response and the server will then close it. If the user-process is transferring
 * files for several USERs but does not wish to close and then reopen connections for each, then the
 * REIN command should be used instead of QUIT. An unexpected close on the control connection will
 * cause the server to take the effective action of an abort (ABOR) and a logout (QUIT).
 * <p>
 * <i>[Excerpt from RFC-959, Postel and Reynolds]</i>
 * </p>
 * 
 * @author Lars Behnke
 */
public class FtpCmdQuit extends AbstractFtpCmd {

    /**
     * {@inheritDoc}
     */
    public void execute() throws FtpCmdException {
        getCtx().closeSockets();
        String goodbye = getCtx().getOptions().getProperty(OPT_MSG_GOODBYE);
        if (goodbye == null || goodbye.length() == 0) {
            goodbye = getCtx().getRes(MSG_GOODBYE);
        }
        out("221 " + goodbye);
        throw new FtpQuitException();
    }

    /**
     * {@inheritDoc}
     */
    public String getHelp() {
        return "Close FTP session";
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
