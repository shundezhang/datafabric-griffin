package au.org.arcs.griffin.filesystem.impl;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ietf.jgss.GSSCredential;

import au.org.arcs.griffin.filesystem.FileObject;
import au.org.arcs.griffin.filesystem.FileSystemConnection;

import edu.sdsc.grid.io.RemoteFileSystem;
import edu.sdsc.grid.io.irods.IRODSAccount;
import edu.sdsc.grid.io.irods.IRODSFileSystem;

/**
 * an implementation for jargon
 * @author Shunde Zhang
 *
 */

public class JargonFileSystemConnectionImpl implements FileSystemConnection {
	private static Log log = LogFactory.getLog(JargonFileSystemConnectionImpl.class);
	private RemoteFileSystem remoteFileSystem;
	private String user;
	private String homeCollection;
	
	public JargonFileSystemConnectionImpl(String serverName, int serverPort, String serverType, GSSCredential credential) throws NullPointerException, IOException{
		if (serverType.equalsIgnoreCase("irods")){
			log.debug("server:"+serverName+" serverPort:"+serverPort+" credential:"+credential.toString());
			IRODSAccount account=new IRODSAccount(serverName,serverPort,credential);
			remoteFileSystem=new IRODSFileSystem( account );
			user=account.getUserName();
			homeCollection=account.getHomeDirectory();
		}
	}
	public FileObject getFileObject(String path) {
		JargonFileObject fo=new JargonFileObject(remoteFileSystem, path);
		return fo;
	}
	public String getHomeDir() {
		// TODO Auto-generated method stub
		return homeCollection;
	}
	public String getUser() {
		// TODO Auto-generated method stub
		return user;
	}
	public void close() throws IOException {
		if (remoteFileSystem!=null) {
			if (remoteFileSystem instanceof IRODSFileSystem) ((IRODSFileSystem)remoteFileSystem).close();
		}
		
	}
	public boolean isConnected(){
		return remoteFileSystem.isConnected();
	}

}
