package au.org.arcs.griffin.filesystem;

import java.io.IOException;

public interface FileObject {
	public String getName();
	public String getPath();
	public boolean exists();
	public boolean isFile();
	public boolean isDirectory();
	public int getPermission();
	public String getCanonicalPath();
	public FileObject[] listFiles();
	public long length();
	public long lastModified();
	public RandomAccessFileObject getRandomAccessFileObject(String type) throws IOException;
	public boolean delete();
	public FileObject getParent();
	public boolean mkdir();
	public boolean renameTo(FileObject file);
}
