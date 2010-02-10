package au.org.arcs.griffin.filesystem.impl;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ietf.jgss.GSSCredential;

import au.org.arcs.griffin.exception.FtpConfigException;
import au.org.arcs.griffin.filesystem.FileObject;
import au.org.arcs.griffin.filesystem.FileSystem;
import au.org.arcs.griffin.filesystem.FileSystemConnection;

import edu.sdsc.grid.io.irods.IRODSFile;

/**
 * an implementation for jargon
 * @author Shunde Zhang
 *
 */

public class JargonFileSystemImpl implements FileSystem {
	
	private static Log log = LogFactory.getLog(JargonFileSystemImpl.class);
	private String serverName;
	private int serverPort;
	private String serverType;
	
	public String getServerType() {
		return serverType;
	}

	public void setServerType(String serverType) {
		this.serverType = serverType;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public FileSystemConnection createFileSystemConnection(
			GSSCredential credential) throws FtpConfigException, IOException{
		try {
			FileSystemConnection connection = new JargonFileSystemConnectionImpl(serverName, serverPort, serverType, credential);
			return connection;
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			throw new FtpConfigException(e.getMessage());
		}
	}

	public void init() {
		log.debug("There is nothing to do when init'ing jargon file system.");
		
	}

	public String getPathSeparator() {
		// TODO Auto-generated method stub
		return String.valueOf(IRODSFile.PATH_SEPARATOR_CHAR);
	}

	public long getFreeSpace(String path) {
		// TODO Auto-generated method stub
		return -1;
	}


}
