package au.org.arcs.griffin.cmd.impl;

import au.org.arcs.griffin.cmd.AbstractFtpCmd;
import au.org.arcs.griffin.exception.FtpCmdException;

public class FtpCmdSite extends AbstractFtpCmd {

	public void execute() throws FtpCmdException {
		String arg=getArguments();
		
        if (arg.equals("")) {
            out("500 must supply the site specific command");
            return;
        }

        String args[] = arg.split(" ");

        if (args[0].equalsIgnoreCase("BUFSIZE")) {
            if (args.length != 2) {
                out("500 command must be in the form 'SITE BUFSIZE <number>'");
                return;
            }
            int bufsize;
            try {
                bufsize = Integer.parseInt(args[1]);
            } catch(NumberFormatException ex) {
                out("500 bufsize argument must be integer");
                return;
            }

            if (bufsize < 1) {
                out("500 bufsize must be positive.  Probably large, but at least positive");
                return;
            }

            getCtx().setBufferSize(bufsize);
            out("200 bufsize set to " + bufsize);

        }else{
    		StringBuffer msg=new StringBuffer();
    		msg.append("214-The following commands are recognized:\r\n");
    		msg.append("    ALLO    ESTO    RNTO    APPE    DCAU    MODE    SIZE    STRU\r\n");
    		msg.append("    TYPE    DELE    SITE    CWD     ERET    FEAT    LIST    NLST\r\n");
    		msg.append("    MLSD    MLST    PORT    PROT    EPRT    PWD     QUIT    REST\r\n");
    		msg.append("    STAT    SYST    MKD     RMD     CDUP    HELP    NOOP    EPSV\r\n");
    		msg.append("    PASV    TREV    SBUF    MDTM    CKSM    OPTS    PASS    SPAS\r\n");
    		msg.append("    PBSZ    SPOR    RETR    STOR    USER    RNFR    LANG\r\n");
    		msg.append("214 End");
    		out(msg.toString());
        }
	}

	public String getHelp() {
		// TODO Auto-generated method stub
		return "help on site";
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
