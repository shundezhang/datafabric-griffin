package au.org.arcs.griffin.cmd.impl;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.org.arcs.griffin.cmd.AbstractFtpCmd;
import au.org.arcs.griffin.cmd.AbstractFtpCmdList;
import au.org.arcs.griffin.common.FtpSessionContext;
import au.org.arcs.griffin.exception.FtpCmdException;
import au.org.arcs.griffin.filesystem.FileObject;
import au.org.arcs.griffin.utils.IOUtils;

/**
 * <b> Listings for Machine Processing </b>
 * <p>
 * The MLST and MLSD commands are intended to standardize the file and
 * directory information returned by the server-FTP process.  These
 * commands differ from the LIST command in that the format of the
 * replies is strictly defined although extensible.
 * 
 * Two commands are defined, MLST and MLSD.  MLST provides data about
 * exactly the object named on its command line, and no others. MLSD,
 * on the other, lists the contents of a directory if a directory is
 * named, otherwise a 501 reply is returned.  In either case, if no
 * object is named, the current directory is assumed.  That will cause
 * MLST to send a one-line response, describing the current directory
 * itself, and MLSD to list the contents of the current directory.
 * 
 * @author Shunde Zhang
 *
 */
public class FtpCmdMlsd extends AbstractFtpCmdList {
	private static Log log = LogFactory.getLog(FtpCmdMlsd.class);
	
    protected void doPrintFileInfo(PrintWriter out, FileObject file, FtpSessionContext ctx) throws IOException {
        out.println(printFact(file));
    }

	private String printFact(FileObject file) {
		StringBuffer buffer=new StringBuffer();
        if (file.isDirectory()){
        	buffer.append("Type=dir;");
        }else{
        	buffer.append("Type=file;");
            buffer.append("Size=").append(file.length()).append(";");
        }
        buffer.append("Modify=").append(file.lastModified()).append(";");
        buffer.append("Perm=");
        int perm=file.getPermission();
        log.debug("file perm:"+perm);
        if (file.isDirectory()){
        	if ((perm&PRIV_WRITE)==PRIV_WRITE) buffer.append("cdm");
        	buffer.append('e');
        	if ((perm&PRIV_READ)==PRIV_READ) buffer.append("l");
        }else{
        	if ((perm&PRIV_READ)==PRIV_READ) buffer.append("r");
        	if ((perm&PRIV_WRITE)==PRIV_WRITE) buffer.append("d");
        }
        buffer.append(";");
        if (file.isDirectory()){
        	buffer.append("UNIX.mode=0755;");
        }else{
        	buffer.append("UNIX.mode=0644;");
        }
        buffer.append(" ");
        buffer.append(file.getName());
		return buffer.toString();
	}

	public String getHelp() {
		// TODO Auto-generated method stub
		return "MLSD";
	}

	public boolean isAuthenticationRequired() {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean isExtension() {
		// TODO Auto-generated method stub
		return true;
	}

}
