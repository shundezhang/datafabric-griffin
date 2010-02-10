package au.org.arcs.griffin.filesystem;

import java.io.IOException;


import org.ietf.jgss.GSSCredential;

import au.org.arcs.griffin.exception.FtpConfigException;

/**
 * File system interface 
 * @author Shunde Zhang
 *
 */
public interface FileSystem {
	public String getPathSeparator();
	public void init();
	public FileSystemConnection createFileSystemConnection(GSSCredential credential) throws FtpConfigException, IOException;
	public long getFreeSpace(String path);
}
