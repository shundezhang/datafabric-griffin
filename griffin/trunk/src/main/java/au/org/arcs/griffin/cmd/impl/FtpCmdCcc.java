package au.org.arcs.griffin.cmd.impl;

import au.org.arcs.griffin.cmd.AbstractFtpCmd;
import au.org.arcs.griffin.exception.FtpCmdException;

public class FtpCmdCcc extends AbstractFtpCmd {

	public void execute() throws FtpCmdException {
        // We should never received this, only through MIC, ENC or CONF,
        // in which case it will be intercepted by secure_command()
		msgOut("533 CCC must be protected");
	}

	public String getHelp() {
		// TODO Auto-generated method stub
		return "ccc";
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
