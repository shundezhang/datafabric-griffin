package au.org.arcs.griffin.filesystem;

import java.io.IOException;


import org.ietf.jgss.GSSCredential;

import au.org.arcs.griffin.exception.FtpConfigException;

public interface FileSystem {
	public String getPathSeparator();
	public void init();
	public FileSystemConnection createFileSystemConnection(GSSCredential credential) throws FtpConfigException, IOException;
}
