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

package au.org.arcs.griffin.server;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.org.arcs.griffin.common.FtpConstants;
import au.org.arcs.griffin.common.FtpEventListener;
import au.org.arcs.griffin.common.FtpServerOptions;
import au.org.arcs.griffin.common.FtpSessionContext;
import au.org.arcs.griffin.filesystem.FileSystem;
import au.org.arcs.griffin.session.FtpSession;
import au.org.arcs.griffin.usermanager.UserManager;
import au.org.arcs.griffin.utils.AbstractAppAwareBean;
import au.org.arcs.griffin.utils.IOUtils;
import au.org.arcs.griffin.utils.NetUtils;

/**
 * Ancestor class for FTP server implementations.
 * 
 * @author Lars Behnke
 */
public abstract class AbstractFtpServer extends AbstractAppAwareBean implements FtpServer, FtpConstants,
        FtpEventListener {

    private static final int       DEFAULT_TIMEOUT           = 3000;

    private static Log             log                       = LogFactory.getLog(FtpServer.class);

    private List<FtpSession>       sessions                  = new ArrayList<FtpSession>();

    private boolean                terminated;

    private FtpServerOptions       options;

    private String                 resources;
    
    private String                 name;

    private int                    status                    = SERVER_STATUS_UNDEF;

    private UserManager            userManager;

    private List<FtpEventListener> ftpEventListeners         = new ArrayList<FtpEventListener>();

    private int                    connectionCountHWMark;

    private Date                   connectionCountHWMarkDate = new Date();
    
    private FileSystem			   fileSystem;

    public FileSystem getFileSystem() {
		return fileSystem;
	}

	public void setFileSystem(FileSystem fileSystem) {
		this.fileSystem = fileSystem;
	}

	/**
     * Creates a server socket. Depending on the server implementation this can be a SSL or a
     * regular server socket.
     * 
     * @return The server socket.
     * @throws IOException Error on creating server socket.
     */
    protected abstract ServerSocket createServerSocket() throws IOException;

    /**
     * Creates the context object passed to the user session.
     * 
     * @return The session context.
     */
    protected abstract FtpSessionContext createFtpContext();

    /**
     * Halts the server.
     */
    public void abort() {
        this.terminated = true;
    }

    /**
     * Getter method for the java bean <code>options</code>.
     * 
     * @return Returns the value of the java bean <code>options</code>.
     */
    public FtpServerOptions getOptions() {
        return options;
    }

    /**
     * Setter method for the java bean <code>options</code>.
     * 
     * @param options The value of options to set.
     */
    public void setOptions(FtpServerOptions options) {
        this.options = options;
    }

    /**
     * {@inheritDoc}
     */
    public void run() {
        setStatus(SERVER_STATUS_INIT);
        ServerSocket serverSocket = null;
        try {
        	getFileSystem().init();
            serverSocket = createServerSocket();
            serverSocket.setSoTimeout(DEFAULT_TIMEOUT);
            setStatus(SERVER_STATUS_READY);
            while (!isTerminated()) {
                Socket clientSocket;
                try {
                    clientSocket = serverSocket.accept();
                } catch (SocketTimeoutException e) {
                    continue;
                }

                /* Check blacklisted IP v4 addresses */
                
                InetAddress clientAddr = clientSocket.getInetAddress();
                InetAddress localAddr = clientSocket.getLocalAddress();
                
                log.info("Client requests connection. ClientAddr.: " +  clientAddr + ", LocalAddr.: " + localAddr);
                
                String listKey = NetUtils.isIPv6(clientAddr) ? FtpConstants.OPT_IPV6_BLACK_LIST : FtpConstants.OPT_IPV4_BLACK_LIST;
                String ipBlackList = getOptions().getString(listKey, "");
                
                if (NetUtils.checkIPMatch(ipBlackList, clientAddr)) {
                    log.info("Client with IP address " + clientAddr.getHostAddress() + " rejected (blacklisted).");
                    IOUtils.closeGracefully(clientSocket);
                    continue;
                }

                /* Initialize session context */
                FtpSessionContext ctx = createFtpContext();
                ctx.setCreationTime(new Date());
                ctx.setClientSocket(clientSocket);
                // set default reply type to be clear
                ctx.setReplyType("clear");
                FtpSession session = (FtpSession) getApplicationContext().getBean(BEAN_SESSION);
                session.setFtpContext(ctx);

                /* Start session */
                log.debug("Accepting connection to " + clientAddr.getHostAddress());
                session.start();
                registerSession(session);

            }
            setStatus(SERVER_STATUS_HALTED);
        } catch (IOException e) {
            setStatus(SERVER_STATUS_UNDEF);
            log.error(e, e);
        } finally {
            terminateAllClientSessions();
            IOUtils.closeGracefully(serverSocket);
        }

    }

    private void registerSession(FtpSession session) {
        synchronized (this) {
            sessions.add(session);
            int sessionCount = getConnectionCount();
            if (sessionCount >= connectionCountHWMark) {
                connectionCountHWMark = sessionCount;
                connectionCountHWMarkDate = new Date();
            }
        }
    }

    /**
     * Getter method for the java bean <code>connectionCount</code>.
     * 
     * @return Returns the value of the java bean <code>connectionCount</code>.
     */
    public int getConnectionCount() {
        synchronized (this) {
            cleanUpSessions();
            return sessions.size();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void cleanUpSessions() {
        List<FtpSession> newList = new ArrayList<FtpSession>();
        for (FtpSession session : sessions) {
            if (!session.isTerminated()) {
                newList.add(session);
            }
        }
        sessions = newList;
    }

    private void terminateAllClientSessions() {
        for (FtpSession session : sessions) {
            session.abort();
        }
    }

    /**
     * Convenience method for accessing the application properties.
     * 
     * @param name The name of the requested property.
     * @return The property.
     */
    public String getOption(String name) {
        return getOptions().getProperty(name);
    }

    /**
     * Getter method for the java bean <code>resources</code>.
     * 
     * @return Returns the value of the java bean <code>resources</code>.
     */
    public String getResources() {
        return resources;
    }

    /**
     * Setter method for the java bean <code>resources</code>.
     * 
     * @param resources The value of resources to set.
     */
    public void setResources(String resources) {
        this.resources = resources;
    }

    /**
     * Getter method for the java bean <code>terminated</code>.
     * 
     * @return Returns the value of the java bean <code>terminated</code>.
     */
    public boolean isTerminated() {
        return terminated;
    }

    /**
     * Getter method for the java bean <code>status</code>.
     * 
     * @return Returns the value of the java bean <code>status</code>.
     */
    public int getStatus() {
        return status;
    }

    /**
     * Setter method for the java bean <code>status</code>.
     * 
     * @param status The value of status to set.
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Getter method for the java bean <code>userManager</code>.
     * 
     * @return Returns the value of the java bean <code>userManager</code>.
     */
    public UserManager getUserManager() {
        return userManager;
    }

    /**
     * Setter method for the java bean <code>userManager</code>.
     * 
     * @param userManager The value of userManager to set.
     */
    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    /**
     * {@inheritDoc}
     */
    public void addFtpEventListener(FtpEventListener lstnr) {
        this.ftpEventListeners.add(lstnr);
    }

    /**
     * {@inheritDoc}
     */
    public void downloadPerformed(String clientId, File file) {
        for (FtpEventListener listener : ftpEventListeners) {
            listener.downloadPerformed(clientId, file);
        }
        log.debug("Download event delegated to listeners.");
    }

    /**
     * {@inheritDoc}
     */
    public void loginPerformed(String clientId, boolean successful) {
        for (FtpEventListener listener : ftpEventListeners) {
            listener.loginPerformed(clientId, successful);
        }
        log.debug("Login event delegated to listeners.");
    }

    /**
     * {@inheritDoc}
     */
    public void uploadPerformed(String clientId, File file) {
        for (FtpEventListener listener : ftpEventListeners) {
            listener.uploadPerformed(clientId, file);
        }
        log.debug("Upload event delegated to listeners.");
    }

    /**
     * {@inheritDoc}
     */
    public void sessionOpened(Object sessionObj) {
        for (FtpEventListener listener : ftpEventListeners) {
            listener.sessionOpened(sessionObj);
        }
        log.debug("Session opened event delegated to listeners.");
    }

    /**
     * {@inheritDoc}
     */
    public void sessionClosed(Object sessionObj) {
        synchronized (this) {
            sessions.remove(sessionObj);
        }
        for (FtpEventListener listener : ftpEventListeners) {
            listener.sessionOpened(sessionObj);
        }
        log.debug("Session closed event delegated to listeners.");
    }

    /**
     * {@inheritDoc}
     */
    public List<FtpSession> getSessions() {
        return sessions;
    }

    /**
     * {@inheritDoc}
     */
    public int getConnectionCountHWMark() {
        return connectionCountHWMark;
    }

    /**
     * {@inheritDoc}
     */
    public Date getConnectionCountHWMarkDate() {
        return connectionCountHWMarkDate;
    }

    /**
     * Setter methode for property <code>connectionCountHWMark</code>.
     * 
     * @param connectionCountHWMark Value for <code>connectionCountHWMark</code>.
     */
    public void setConnectionCountHWMark(int connectionCountHWMark) {
        this.connectionCountHWMark = connectionCountHWMark;
    }

    /**
     * Setter methode for property <code>connectionCountHWMarkDate</code>.
     * 
     * @param connectionCountHWMarkDate Value for <code>connectionCountHWMarkDate</code>.
     */
    public void setConnectionCountHWMarkDate(Date connectionCountHWMarkDate) {
        this.connectionCountHWMarkDate = connectionCountHWMarkDate;
    }

    
    public String getName() {
        return name;
    }

    
    public void setName(String name) {
        this.name = name;
    }

}
