package au.org.arcs.griffin.cmd.impl;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.org.arcs.griffin.cmd.AbstractFtpCmd;
import au.org.arcs.griffin.cmd.ActiveModeSocketProvider;
import au.org.arcs.griffin.cmd.DataChannelInfo;
import au.org.arcs.griffin.cmd.SocketProvider;
import au.org.arcs.griffin.exception.FtpCmdException;

/**
 * <b>Striped Data Port (SPOR)</b>
 * <p>
 * This extension is to be used as a complement to the SPAS command to 
 * implement striped third-party transfers. To simplify interaction with 
 * the parallel data transfer extensions, the SPOR MUST only be done on 
 * a control connection when the data is to be retrieved from the file 
 * space served by that control connection for a third-party transfer.  
 * This command MUST always be used in conjunction with the extended block mode.  
 * 
 * @author Shunde Zhang
 */
public class FtpCmdSpor extends AbstractFtpCmd {
	private static Log log = LogFactory.getLog(FtpCmdSpor.class);
    private static final String DOT = ".";

    private int                 port;

    private String              addr;

    private String              lastArgs;

    public void execute() throws FtpCmdException {
        try {
            String args = getArguments();
            if (args.length() == 0) {
                msgOut(MSG501);
                return;
            }
            // *TODO* need to be fixed later
            if (args.split("\\s+").length > 1){
            	out("501 SPOR doesn't support striping now, please provide one endpoint only.");
            	return;
            }

            int protocolIdx = doReadProtocolIdx(args);
            String addr = doReadIPAddr(args);
            int port = doReadPort(args);
            log.debug("Data Channel Protocol: " + protocolIdx + ", IPAddr: " + addr + ", port: " + port);

            setupDataChannel(protocolIdx, addr, port);

            msgOut(MSG200);
        } catch (IOException e) {
            log.error(e.toString());
            msgOut(MSG500);
        } catch (IllegalArgumentException e) {
            log.error(e.toString());
            msgOut(MSG501);
        }
    }

    /**
     * Sets up the data channel in active transfer mode. IPv4 and IPv6 are supported.
     * 
     * @param protocolIdx Protocol index (IPv4 or IPv6)
     * @param ipAddr IPv4 or IPv6 compliant address.
     * @param port The port.
     * @throws IOException Setting up data channel failed.
     */
    protected void setupDataChannel(int protocolIdx, String ipAddr, int port) throws IOException {
        getCtx().closeSockets();
        DataChannelInfo info = new DataChannelInfo(ipAddr, port);
        SocketProvider provider = new ActiveModeSocketProvider(getCtx(), info);
        provider.init();
        getCtx().setDataSocketProvider(provider);
    }

    /**
     * {@inheritDoc}
     */
    public String getHelp() {
        return "Sets port for active transfer.";
    }

    /**
     * {@inheritDoc}
     */
    protected String doReadIPAddr(String args) {
        if (!paramsParsed(args)) {
            parseParams(args);
        }
        return addr;
    }

    /**
     * {@inheritDoc}
     */
    protected int doReadPort(String args) {
        if (!paramsParsed(args)) {
            parseParams(args);
        }
        return port;
    }

    /**
     * {@inheritDoc}
     */
    protected int doReadProtocolIdx(String args) {
        return 1;
    }

    private boolean paramsParsed(String args) {
        return lastArgs != null && lastArgs.equals(args);
    }

    private void parseParams(String args) {
        try {
            lastArgs = args;
            String[] argParts = getArguments().split(",");
            int idx = 0;
            addr = argParts[idx++].trim() + DOT + argParts[idx++].trim() + DOT + argParts[idx++].trim() + DOT
                    + argParts[idx++].trim();
            int p1 = Integer.parseInt(argParts[idx++].trim()) & BYTE_MASK;
            int p2 = Integer.parseInt(argParts[idx++].trim()) & BYTE_MASK;
            port = (p1 << BYTE_LENGTH) + p2;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid arguments: " + args);
        }
    }

	public boolean isExtension() {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean isAuthenticationRequired() {
		// TODO Auto-generated method stub
		return true;
	}

}
