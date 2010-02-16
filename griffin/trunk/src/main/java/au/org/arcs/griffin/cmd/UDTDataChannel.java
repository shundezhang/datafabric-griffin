package au.org.arcs.griffin.cmd;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.barchart.udt.ExceptionUDT;
import com.barchart.udt.MonitorUDT;
import com.barchart.udt.OptionUDT;
import com.barchart.udt.SocketUDT;
import com.barchart.udt.TypeUDT;

import au.org.arcs.griffin.common.FtpSessionContext;

public class UDTDataChannel implements DataChannel {
    private static final int  MAX_BIND_RETRIES     = 3;

    private static final int  DATA_CHANNEL_TIMEOUT = 10000;

    private static Log        log                  = LogFactory.getLog(UDTDataChannel.class);

    private FtpSessionContext ctx;
    
    private SocketUDT socketUDT;
    private SocketUDT clientUDT;
    
    private DataChannelInfo dci;

	public UDTDataChannel(FtpSessionContext ctx) throws IOException{
		this.ctx=ctx;
		dci=init();
	}
	public UDTDataChannel(FtpSessionContext ctx, String remoteAddr, int remotePort) throws IOException {
		this.ctx=ctx;
		//init local "socket"
//		dci=init();
		
		clientUDT = new SocketUDT(TypeUDT.STREAM);
		log.debug("init; acceptor={}"+clientUDT.socketID);
		//connect remote "socket"
		InetSocketAddress remoteSocketAddress = new InetSocketAddress(//
				remoteAddr, remotePort);

		clientUDT.connect(remoteSocketAddress);
		remoteSocketAddress = clientUDT.getRemoteSocketAddress();
		log.debug("connect; remoteSocketAddress={}"+remoteSocketAddress);

		StringBuilder text = new StringBuilder(1024);
		OptionUDT.appendSnapshot(clientUDT, text);
		text.append("\t\n");
		log.debug("sender options; {}"+text);

//		MonitorUDT monitor = socketUDT.monitor;

	}
	public void closeChannel() {
		try {
			clientUDT.close();
		} catch (ExceptionUDT e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			if (socketUDT!=null) socketUDT.close();
		} catch (ExceptionUDT e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public DataChannelInfo init() throws IOException {
        /* Get local machine address and check protocol version. */
        InetAddress localIp = ctx.getClientSocket().getLocalAddress();

        /* Get the next available port */
        int retries = MAX_BIND_RETRIES;
        Integer port=new Integer(0);
        while (retries > 0) {
            port = ctx.getNextPassiveUDPPort();
            port = port == null ? new Integer(0) : port;
            try {
                log.debug("Trying to bind server socket to port " + port);
                socketUDT = new SocketUDT(TypeUDT.STREAM);
    			log.debug("init; acceptor={}"+socketUDT.socketID);

    			InetSocketAddress localSocketAddress = new InetSocketAddress(
    					"0.0.0.0", port);

    			socketUDT.bind(localSocketAddress);
    			localSocketAddress = socketUDT.getLocalSocketAddress();
    			log.info("bind; localSocketAddress={}"+localSocketAddress);
    			socketUDT.setSoTimeout(DATA_CHANNEL_TIMEOUT);

                break;
            } catch (Exception e) {
                retries--;
                log.debug("Binding server socket to port " + port + " failed.");
            }
        }
        if (socketUDT == null) {
            throw new IOException("Initializing server socket failed.");
        }

        /* Wrap up connection parameter */
//        log.debug("Server socket successfully bound to port " + serverSocket.getLocalPort() + ".");
        return new DataChannelInfo(localIp.getHostAddress(), port.intValue());
	}

	public void start() throws IOException {
		if (clientUDT==null){
			if (socketUDT==null) {
                throw new IOException("UDT Server socket not initialized.");
            }
			socketUDT.listen(10);
			log.debug("listen;");
			clientUDT = socketUDT.accept();

			log.debug("accept; receiver={}"+clientUDT.socketID);
			InetSocketAddress remoteSocketAddress = clientUDT.getRemoteSocketAddress();

			log.debug("receiver; remoteSocketAddress={}"+remoteSocketAddress);
			StringBuilder text = new StringBuilder(1024);
			OptionUDT.appendSnapshot(clientUDT, text);
			text.append("\t\n");
			log.info("receiver options; {}"+text);

//			MonitorUDT monitor = receiver.monitor;
		}

	}
	public static void checkLibrary() throws ExceptionUDT {
		String libraryPath = System.getProperty("java.library.path");

		log.info("libraryPath={}"+libraryPath);
		TypeUDT type = TypeUDT.STREAM;

		SocketUDT socket = new SocketUDT(type);

		boolean isOpen = socket.isOpen();

		log.info("isOpen={}"+isOpen);
		// TODO Auto-generated method stub
		
	}
	public void write(byte[] b) throws IOException {
		if (clientUDT==null) throw new IOException("Data channel has not been initiated.");
		clientUDT.send(b);
		
	}
	public void write(byte[] b, int start, int len) throws IOException {
		if (clientUDT==null) throw new IOException("Data channel has not been initiated.");
		clientUDT.send(b, start, len);
		
	}

}
