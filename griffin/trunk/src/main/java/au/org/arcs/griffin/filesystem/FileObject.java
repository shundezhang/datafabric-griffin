package au.org.arcs.griffin.filesystem;

import java.io.IOException;

/**
 * File object interface
 * @author Shunde Zhang
 *
 */

public interface FileObject {
	public String getName();
	public String getPath();
	public boolean exists();
	public boolean isFile();
	public boolean isDirectory();
	public int getPermission();
	public String getCanonicalPath() throws IOException;
	public FileObject[] listFiles();
	public long length();
	public long lastModified();
	public RandomAccessFileObject getRandomAccessFileObject(String type) throws IOException;
	public boolean delete();
	public FileObject getParent();
	public boolean mkdir();
	public boolean renameTo(FileObject file);
	public boolean setLastModified(long t);
}
