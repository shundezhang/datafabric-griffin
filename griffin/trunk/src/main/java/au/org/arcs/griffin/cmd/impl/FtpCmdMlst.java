package au.org.arcs.griffin.cmd.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.org.arcs.griffin.cmd.AbstractFtpCmd;
import au.org.arcs.griffin.exception.FtpCmdException;
import au.org.arcs.griffin.filesystem.FileObject;

public class FtpCmdMlst extends AbstractFtpCmd {
	private static Log log = LogFactory.getLog(FtpCmdMlst.class);
	public void execute() throws FtpCmdException {
		String arg=getArguments();
        String path = getAbsPath(arg);
        FileObject file=getCtx().getFileSystemConnection().getFileObject(path);

        if (!file.exists()) {
            msgOut(MSG550);
            return;
        }
        StringBuffer sb=new StringBuffer("250- Listing " + arg + "\r\n");
        sb.append(" ");
        sb.append(printFact(file)).append("\r\n");
        sb.append("250 End");
        out(sb.toString());

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
        buffer.append(file.getCanonicalPath());
		return buffer.toString();
	}

	public String getHelp() {
		// TODO Auto-generated method stub
		return "MLST";
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
