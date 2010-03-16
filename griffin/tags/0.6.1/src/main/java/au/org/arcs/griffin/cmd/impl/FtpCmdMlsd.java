package au.org.arcs.griffin.cmd.impl;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.org.arcs.griffin.cmd.AbstractFtpCmd;
import au.org.arcs.griffin.cmd.AbstractFtpCmdList;
import au.org.arcs.griffin.cmd.AbstractFtpCmdMlsx;
import au.org.arcs.griffin.cmd.DataChannel;
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
public class FtpCmdMlsd extends AbstractFtpCmdMlsx {
	private static Log log = LogFactory.getLog(FtpCmdMlsd.class);
	
    public void execute() throws FtpCmdException {
        String charset = getCtx().getCharset();
        PrintWriter dataOut = null;
        try {
            DataChannel dataChannel = getCtx().getDataChannelProvider().provideDataChannel();
            dataOut = new PrintWriter(new OutputStreamWriter(dataChannel.getOutputStream(), charset));

            String arg = getArguments();
            String path = getCtx().getRemoteDir();
            if (arg!=null&&arg.length()>0) path = getAbsPath(arg);

            FileObject dir=getCtx().getFileSystemConnection().getFileObject(path);
            log.debug("listing dir "+dir.getCanonicalPath());

            // TODO Allow filtering with wildcards *, ?

            if (!dir.exists()) {
                msgOut(MSG550);
                return;
            }

            out("150 BINARY connection open for MLSD "+arg);
            if (dir.isDirectory()) {
            	StringBuffer cur=new StringBuffer("Type=cdir;Modify=");
            	cur.append(formatTime(dir.lastModified())).append(";Perm=el; ");
            	cur.append(arg).append("\r\r\n");
            	log.debug("printing to data channel: "+cur);
            	dataOut.print(cur.toString());
            	
            	cur=new StringBuffer("Type=cdir;Modify=");
            	cur.append(formatTime(dir.lastModified())).append(";Perm=el; ");
            	cur.append(dir.getCanonicalPath()).append("\r\r\n");
            	log.debug("printing to data channel: "+cur);
            	dataOut.print(cur.toString());
            	
                FileObject[] files = dir.listFiles();

                for (int i = 0; i < files.length; i++) {
                    doPrintFileInfo(dataOut, files[i], getCtx());
                }
            } else {
                doPrintFileInfo(dataOut, dir, getCtx());
            }

            out("226 MLSD completed");
        } catch (IOException e) {
        	e.printStackTrace();
            msgOut(MSG550);
        } catch (Exception e) {
        	e.printStackTrace();
            msgOut(MSG550);
        } finally {
            IOUtils.closeGracefully(dataOut);
            getCtx().closeDataChannels();
        }
    }

    protected void doPrintFileInfo(PrintWriter out, FileObject file, FtpSessionContext ctx) throws IOException {
    	String str=printFact(file,file.getName());
    	log.debug("printing to data channel: "+str);
        out.print(str+"\r\r\n");
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
