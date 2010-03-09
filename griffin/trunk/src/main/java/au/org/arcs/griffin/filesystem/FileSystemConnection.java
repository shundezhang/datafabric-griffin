package au.org.arcs.griffin.filesystem;

import java.io.IOException;

/**
 * File system connection interface, one user has one connection to a file system in each gridftp session. 
 * @author Shunde Zhang
 *
 */

public interface FileSystemConnection {
	public FileObject getFileObject(String path);
	public String getHomeDir();
	public String getUser();
	public void close() throws IOException;
	public boolean isConnected();
	public long getFreeSpace(String path);
}
