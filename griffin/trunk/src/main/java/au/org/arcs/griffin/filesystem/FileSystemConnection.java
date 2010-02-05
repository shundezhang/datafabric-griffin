package au.org.arcs.griffin.filesystem;

import java.io.IOException;

public interface FileSystemConnection {
	public FileObject getFileObject(String path);
	public String getHomeDir();
	public String getUser();
	public void close() throws IOException;
	public boolean isConnected();
}
