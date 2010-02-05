package au.org.arcs.griffin.cmd.impl;

import au.org.arcs.griffin.cmd.AbstractFtpCmd;
import au.org.arcs.griffin.exception.FtpCmdException;

public class FtpCmdDcau extends AbstractFtpCmd {

	public void execute() throws FtpCmdException {
		String arg=getArguments();
        if(arg.equalsIgnoreCase("N")) {
            out("200 data channel authtication switched off");
        }else{
            out("202 data channel authtication not sopported");
        }
	}

	public String getHelp() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isAuthenticationRequired() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isExtension() {
		// TODO Auto-generated method stub
		return false;
	}

}
