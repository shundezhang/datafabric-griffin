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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import au.org.arcs.griffin.cmd.AbstractFtpCmd;
import au.org.arcs.griffin.exception.FtpCmdException;
import au.org.arcs.griffin.filesystem.FileObject;


/**
 * <b>MDTM</b>
 * <p>
 * Modifies the timestamp of a file or path.
 * 
 * @author Lars Behnke
 * @author Shunde Zhang
 */
public class FtpCmdMdtm extends AbstractFtpCmd {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    /**
     * {@inheritDoc}
     */
    public void execute() throws FtpCmdException {
        String[] argParts = getArguments().split(" ");
        Date date = null;
        String pathName = null;

        /* Check arguments */
        if (argParts.length == 2) {
            try {
                date = dateFormat.parse(argParts[0]);
            } catch (ParseException e) {
                date = null;
            }
            pathName = argParts[1].trim();
        } else if (argParts.length == 1) {
            pathName = argParts[0];
        }

        if (pathName == null) {
            msgOut(MSG501);
            return;
        }

        /* Proceed with valid arguments */
        pathName = getAbsPath(pathName);
        FileObject path = getCtx().getFileSystemConnection().getFileObject(pathName);
        if (path.exists()) {
            if (date != null) {
                if ((path.getPermission() & PRIV_WRITE) == 0) {
                    msgOut(MSG550_PERM);
                    return;
                } else {
                    path.setLastModified(date.getTime());
                }

            }
            if ((path.getPermission() & PRIV_READ) == 0) {
                msgOut(MSG550_PERM);
            } else {
                date = new Date(path.lastModified());
                String formattedDate = dateFormat.format(date);
                msgOut(MSG213_TIME, formattedDate);
            }
        } else {
            msgOut(MSG501_PATH);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getHelp() {
        return "Sets the modification date of a file/path";
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAuthenticationRequired() {
        return true;
    }

	public boolean isExtension() {
		// TODO Auto-generated method stub
		return false;
	}

}
