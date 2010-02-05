package au.org.arcs.griffin.cmd.impl;

import au.org.arcs.griffin.cmd.AbstractFtpCmd;
import au.org.arcs.griffin.exception.FtpCmdException;

public class FtpCmdSbuf extends AbstractFtpCmd {

	public void execute() throws FtpCmdException {
		String arg=getArguments();
        if (arg.equals("")) {
            out("500 must supply a buffer size");
            return;
        }

        int bufsize;
        try {
            bufsize = Integer.parseInt(arg);
        } catch(NumberFormatException ex) {
            out("500 bufsize argument must be integer");
            return;
        }

        if (bufsize < 1) {
            out("500 bufsize must be positive.  Probably large, but at least positive");
            return;
        }

        getCtx().setBufferSize(bufsize);
        out("200 bufsize set to " + arg);
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
