package au.org.arcs.griffin.filesystem;

import java.io.IOException;

/**
 * File object interface
 * 
 * @author Shunde Zhang
 * 
 */

public interface FileObject {

    public String getName();

    public String getPath();

    public boolean exists();

    /**
     * Check for a file.
     * 
     * @return True if it is a file.
     */
    public boolean isFile();

    /**
     * Check for a directory.
     * 
     * @return True if it is a directory.
     */
    public boolean isDirectory();

    /**
     * Permissions as UNIX file system like integer.
     * 
     * @return Permissions.
     */
    public int getPermission();

    public String getCanonicalPath() throws IOException;

    public FileObject[] listFiles();

    public long length();

    public long lastModified();

    public RandomAccessFileObject getRandomAccessFileObject(String type)
            throws IOException;

    public boolean delete();

    public FileObject getParent();

    public boolean mkdir();

    public boolean renameTo(FileObject aFile);

    public boolean setLastModified(long t);
}
