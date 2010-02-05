package au.org.arcs.griffin.cmd.impl;

import au.org.arcs.griffin.cmd.AbstractFtpCmd;
import au.org.arcs.griffin.exception.FtpCmdException;
import au.org.arcs.griffin.filesystem.FileObject;

public class FtpCmdMlst extends AbstractFtpCmd {

	public void execute() throws FtpCmdException {
		String arg=getArguments();
        String path = getAbsPath(arg);
        FileObject file=getCtx().getFileSystemConnection().getFileObject(path);; 

        if (!file.exists()) {
            msgOut(MSG550);
            return;
        }
        StringBuffer sb=new StringBuffer("250- Listing " + arg + "\r\n");
        sb.append(" ");
        sb.append(printFact(file));
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
        if (file.isDirectory()){
        	if ((file.getPermission()&PRIV_WRITE)==1) buffer.append("cdm");
        	buffer.append('e');
        	if ((file.getPermission()&PRIV_READ)==1) buffer.append("l");
        }else{
        	if ((file.getPermission()&PRIV_READ)==1) buffer.append("r");
        	if ((file.getPermission()&PRIV_WRITE)==1) buffer.append("d");
        }
        buffer.append(";");
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
