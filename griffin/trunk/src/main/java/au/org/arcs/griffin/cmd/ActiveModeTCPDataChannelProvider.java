/*
 * ------------------------------------------------------------------------------
 * Hermes FTP Server
 * Copyright (c) 2005-2007 Lars Behnke
 * ------------------------------------------------------------------------------
 * 
 * This file is part of Hermes FTP Server.
 * 
 * Hermes FTP Server is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Hermes FTP Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Hermes FTP Server; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * ------------------------------------------------------------------------------
 */

package au.org.arcs.griffin.cmd;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.org.arcs.griffin.common.FtpConstants;
import au.org.arcs.griffin.common.FtpSessionContext;
import au.org.arcs.griffin.filesystem.FileObject;
import au.org.arcs.griffin.streams.RafInputStream;
import au.org.arcs.griffin.streams.SynchronizedInputStream;
import au.org.arcs.griffin.utils.IOUtils;

/**
 * Provider for the client socket (active transfer). The creation of the socket is deferred until it
 * is needed.
 * 
 * @author Behnke
 * @author Shunde Zhang
 */
public class ActiveModeTCPDataChannelProvider implements DataChannelProvider {
	private static Log          log               = LogFactory.getLog(ActiveModeTCPDataChannelProvider.class);
    private FtpSessionContext ctx;

    private DataChannelInfo   dataChannelInfo;

    private List<DataChannel>            channels;
    private int maxThread;
    private FileObject fileObject;
    private int direction;
    private SynchronizedInputStream sis;

    /**
     * Constructor.
     * 
     * @param ctx Session context.
     * @param info Channel about the data channel to open.
     */
    public ActiveModeTCPDataChannelProvider(FtpSessionContext ctx, DataChannelInfo info) {
        this.ctx = ctx;
        this.dataChannelInfo = info;
    }

    /**
     * {@inheritDoc}
     */
    public DataChannelInfo init() throws IOException {
    	closeProvider();
        return dataChannelInfo;
    }

    /**
     * {@inheritDoc}
     */
    public DataChannel provideDataChannel() throws IOException {
        if (channels == null) {
        	channels=new ArrayList<DataChannel>();
        }
        Socket socket = createClientSocket();
        DataChannel dc=new TCPDataChannel(socket, ctx, 0);
        channels.add(dc);
        return dc;
    }

    /**
     * {@inheritDoc}
     */
    public void closeProvider() {
        if (channels != null) {
            for (DataChannel channel:channels) channel.closeChannel();
            channels = null;
        }

    }

    private Socket createClientSocket() throws IOException {
        Socket dataSocket;
        dataSocket = SocketFactory.getDefault().createSocket(dataChannelInfo.getAddress(),
            dataChannelInfo.getPort());
        log.debug("established socket "+dataSocket+" to client:"+dataChannelInfo.getAddress()+" "+dataChannelInfo.getPort());
        return dataSocket;
    }

	public void setMaxThread(int maxThread) {
		this.maxThread=maxThread;
		
	}

	public void run() {
		channels=new ArrayList<DataChannel>();
		if (direction==DataChannel.DIRECTION_GET) sis=new SynchronizedInputStream(new RafInputStream(fileObject, 0));
		for (int i=0;i<maxThread;i++){
			try {
				Socket s=createClientSocket();
				TCPDataChannel dc=new TCPDataChannel(s,ctx,i);
				dc.setDirection(direction);
				dc.setFileObject(fileObject);
				dc.setDataChannelProvider(this);
				if (direction==DataChannel.DIRECTION_GET) {
					dc.setSynchronizedInputStream(sis);
				}
				channels.add(dc);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Thread[] transferThreads=new Thread[channels.size()];
		for (int i=0;i<channels.size();i++){
			transferThreads[i]=new Thread(channels.get(i));
		}
		for (int i=0;i<channels.size();i++){
			transferThreads[i].start();
		}
		for (int i=0;i<channels.size();i++){
			if (transferThreads[i].isAlive())
				try {
					log.debug("thread "+i+" joined.");
					transferThreads[i].join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		log.debug("all threads finished(?) channels.size()="+channels.size());
		if (channels.size()==0) {
//			running=false;
			if (sis!=null)
				try {
					sis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

	public void channelClosed(DataChannel dataChannel) {
//		channels.remove(dataChannel);
//		log.debug("1 channel is closed. "+channels.size()+" left.");
	}

	public void setDirection(int direction) {
		this.direction=direction;
		
	}

	public void setFileObject(FileObject file) {
		this.fileObject=file;
		
	}

	public int getChannelNumber(){
		return channels.size();
	}
	
	public int getMaxThread(){
		return this.maxThread;
	}

}
