/*
 * JargonFileObject.java
 * 
 * Implementation of Jargon file system storage interface.
 * 
 * Created: Shunde Zhang <shunde.zhang@arcs.org.au>
 * Changed:
 * 
 * Copyright (C) 2010 Australian Research Collaboration Service
 * 
 * Some rights reserved
 * 
 * http://www.arcs.org.au/
 */

package au.org.arcs.griffin.filesystem.impl.jargon;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.org.arcs.griffin.common.FtpConstants;
import au.org.arcs.griffin.filesystem.FileObject;
import au.org.arcs.griffin.filesystem.RandomAccessFileObject;
import edu.sdsc.grid.io.GeneralFile;
import edu.sdsc.grid.io.RemoteFile;
import edu.sdsc.grid.io.RemoteFileSystem;
import edu.sdsc.grid.io.irods.IRODSFile;
import edu.sdsc.grid.io.irods.IRODSFileSystem;

/**
 * an implementation for jargon
 * @author Shunde Zhang
 */
public class JargonFileObject implements FileObject {

    private RemoteFile remoteFile = null;
    private JargonFileSystemConnectionImpl connection = null;
    private String originalName;

    /**
     * Constructor using an iRODS remote file system connection and path.
     * 
     * @param aConnection Connection to file system handler.
     * @param rfs Connection to iRODS remote file system.
     * @param path Path of file/directory.
     */
    public JargonFileObject(JargonFileSystemConnectionImpl aConnection,
                            RemoteFileSystem rfs, String path) {
        this.connection = aConnection;
        if (rfs instanceof IRODSFileSystem) {
            remoteFile = new IRODSFile((IRODSFileSystem) rfs, path);
            this.originalName = path;
        }
    }

    /**
     * Constructor using an iRODS file object.
     * 
     * @param aConnection Connection to file system handler.
     * @param file iRODS file object.
     */
    public JargonFileObject(JargonFileSystemConnectionImpl aConnection,
                            RemoteFile file) {
        this.connection = aConnection;
        this.remoteFile = file;
    }

    /**
     * {@inheritDoc}
     */
    public boolean exists() {
        return remoteFile.exists();
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
    	if (originalName!=null&&(originalName.equals(".")||originalName.equals(".."))) return originalName;
        return remoteFile.getName();
    }

    /**
     * {@inheritDoc}
     */
    public String getPath() {
        return remoteFile.getPath();
    }

    /**
     * {@inheritDoc}
     */
    public int getPermission() {
        int permission = FtpConstants.PRIV_NONE;
        if (remoteFile.canRead() && remoteFile.canWrite()) {
            permission = FtpConstants.PRIV_READ_WRITE;
        } else if (remoteFile.canRead() && !remoteFile.canWrite()) {
            permission = FtpConstants.PRIV_READ;
        } else if (!remoteFile.canRead() && remoteFile.canWrite()) {
            permission = FtpConstants.PRIV_WRITE;
        }
        return permission;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDirectory() {
        return remoteFile.isDirectory();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isFile() {
        return remoteFile.isFile();
    }

    /**
     * {@inheritDoc}
     */
    public String getCanonicalPath() throws IOException {
        return remoteFile.getCanonicalPath();
    }

    /**
     * {@inheritDoc}
     */
    public FileObject[] listFiles() {
        GeneralFile[] flist = remoteFile.listFiles();
        FileObject[] list = new FileObject[flist.length + 2];
        
        // Add two entries for current and parent directory.
        list[0] = this.connection.getFileObject(".");
        list[1] = this.connection.getFileObject("..");
        
        for (int i = 0; i < flist.length; i++) {
            list[i + 2] = new JargonFileObject(this.connection,
                                               (RemoteFile) flist[i]);
        }
        return list;
    }

   /**
    * {@inheritDoc}
    */
    public long lastModified() {
        return remoteFile.lastModified();
    }

    /**
     * {@inheritDoc}
     */
    public long length() {
        return remoteFile.length();
    }

    /**
     * {@inheritDoc}
     */
    public RandomAccessFileObject getRandomAccessFileObject(String type)
            throws IOException {
        return new JargonRandomAccessFileObjectImpl(remoteFile, type);
    }

    /**
     * {@inheritDoc}
     */
    public boolean delete() {
        return remoteFile.delete();
    }

    /**
     * {@inheritDoc}
     */
    public FileObject getParent() {
        return new JargonFileObject(this.connection,
                                    (RemoteFile) remoteFile.getParentFile());
    }

    /**
     * {@inheritDoc}
     */
    public boolean mkdir() {
        return remoteFile.mkdir();
    }

    /**
     * {@inheritDoc}
     */
    public boolean renameTo(FileObject file) {
        if (file instanceof JargonFileObject) {
            return remoteFile.renameTo(((JargonFileObject) file).getRemoteFile());
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean setLastModified(long t) {
        return remoteFile.setLastModified(t);
    }

    /**
     * Gives access to the iRODS remote file object.
     * 
     * @return Reference to the iRODS file object.
     */
    public RemoteFile getRemoteFile() {
        return remoteFile;
    }
}
