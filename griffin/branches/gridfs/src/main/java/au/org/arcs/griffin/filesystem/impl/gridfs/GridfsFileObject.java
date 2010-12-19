/*
 * GridfsFileObject.java
 * 
 * File object that links to the GridFS instance.
 * 
 * Created: 2010-09-18 Guy K. Kloss <guy.kloss@aut.ac.nz>
 * Changed:
 * 
 * Copyright (C) 2010 Australian Research Collaboration Service
 *                    and Auckland University of Technology, New Zealand
 * 
 * Some rights reserved
 * 
 * http://www.arcs.org.au/
 * http://www.aut.ac.nz/
 */
 
package au.org.arcs.griffin.filesystem.impl.gridfs;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.bson.types.ObjectId;

import au.org.arcs.griffin.common.FtpConstants;
import au.org.arcs.griffin.filesystem.FileObject;
import au.org.arcs.griffin.filesystem.RandomAccessFileObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import com.mongodb.gridfs.GridFSInputFile;


/**
 * Implementation of the {@link au.org.arcs.griffin.filesystem.FileObject} 
 * interface linking to the GridFS stored content.
 *
 * @version $Revision: 1.1 $
 * @author Guy K. Kloss
 */
public class GridfsFileObject implements FileObject {
    // Constants for content attributes in the DB.
    private static final String ID = "_id";
    private static final String UPLOAD_DATE = "uploadDate";
    private static final String COLLECTION = "collection";
    private static final String FILENAME = "filename";
    
    private static Log log = LogFactory.getLog(GridfsRandomAccessFileObjectImpl.class);
    
    private String _path = null;
    private GridFSFile _fileHandle = null;
    private GridfsFileSystemConnectionImpl _connection = null;
    
    /**
     * Constructor.
     * 
     * @param path Absolute file path in storage system.
     * @param connection Connection to the GridFS connection object.
     */
    public GridfsFileObject(String path,
            GridfsFileSystemConnectionImpl connection) {
        if (connection == null) {
            // Bail out if we don't have a working GridFS connection.
            return;
        }
        this._path = path;
        this._connection = connection;
        
        log.debug("Making a GridFS file object for path " + this._path);
        
        DBObject query = new BasicDBObject(FILENAME, path);
        DBCursor results = this._connection.getFs().getFileList(query);
        if (results.count() == 0) {
            return;
        }

        DBObject newest = results.sort(new BasicDBObject(UPLOAD_DATE, -1)).limit(1).next();
        ObjectId newest_id = new ObjectId(newest.get(ID).toString());
        this._fileHandle = this._connection.getFs().find(newest_id); 
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileObject#getName()
     */
    public String getName() {
        String[] parts = this._path.split(GridfsConstants.FILE_SEP);
        if (parts.length > 1) {
            return parts[parts.length - 1];
        } else {
            return this._path;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileObject#getPath()
     */
    public String getPath() {
        return this._path;
    }

    /**
     * Returns entries starting with a certain path root.
     * 
     * @param aPath The path all returned entries are expected to start with.
     * @return The found paths.
     */
    private Set<String> _entriesStartingWith(String aPath) {
        Pattern searchPattern = Pattern.compile("^" + aPath);
        DBObject query = new BasicDBObject(FILENAME, searchPattern);
        List<GridFSDBFile> foundEntries = this._connection.getFs().find(query);
        
        if (foundEntries.isEmpty()) {
            return null;
        }

        // "Uniquify" entries (only one entry per path), and no deeper than
        // one level below current collection path.
        Set<String> uniqueEntries = new HashSet<String>();
        
        for (GridFSFile item : foundEntries) {
            uniqueEntries.add(item.getFilename());
        }
        
        return uniqueEntries;
    }
    
    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileObject#exists()
     */
    public boolean exists() {
        if (this._fileHandle != null) {
            return true;
        }
        
        boolean exists = false;
        Set<String> uniqueEntries = _entriesStartingWith(this._path);
        
        // If we didn't find any, it doesn't exist.
        if (uniqueEntries == null || uniqueEntries.size() == 0) {
            return false;
        }
        
        // Search for occurrences that indicate a "real" path element.
        String wildSearchPath = FilenameUtils.concat(this._path, "*");
        for (String item : uniqueEntries) {
            if (item.equals(this._path)) {
                exists = true;
                break;
            } else if (FilenameUtils.wildcardMatch(item, wildSearchPath)) {
                exists = true;
                break;
            }
        }
        
        return exists;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileObject#isFile()
     */
    public boolean isFile() {
        if (this._fileHandle != null) {
            if (this._fileHandle.getContentType() != null
                && this._fileHandle.getContentType().equals(COLLECTION)) {
                return false;
            } else {
                return true;
            }
        } else {
            if (this.exists()) {
                return false;
            } else {
                return true;
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileObject#isDirectory()
     */
    public boolean isDirectory() {
        return !this.isFile();
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileObject#getPermission()
     */
    public int getPermission() {
        // TODO Auto-generated method stub
        // See JargonFileObject, for now just read/write privilege.
        return FtpConstants.PRIV_READ_WRITE;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileObject#getCanonicalPath()
     */
    public String getCanonicalPath() throws IOException {
        // The path stored in the GridFS is always canonical.
        // Or did I misunderstand this?
        return this._path;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileObject#listFiles()
     */
    public FileObject[] listFiles() {
        if (this.isFile()) {
            // Bail out, a file does not have a file list.
            return null;
        }
        
        Set<String> uniqueEntries = _entriesStartingWith(this._path);
        Set<String> localEntries = new HashSet<String>();
        
        String wildSearchPath = FilenameUtils.concat(this._path, "*");
        for (String item : uniqueEntries) {
            if (FilenameUtils.wildcardMatch(item, wildSearchPath)) {
                // Do we have a further sub-directory?
                int index = item.indexOf(GridfsConstants.FILE_SEP,
                                         this._path.length() + 1);
                if (index == -1) {
                    // No, so add the item.
                    localEntries.add(item);
                } else {
                    // Yes, so only add the portion up until the next file sep.
                    String subPath = item.substring(0, index);
                    localEntries.add(subPath);
                }
            }
        }
        
        FileObject[] results = new FileObject[localEntries.size() + 2];
        
        // Add two entries for current and parent directory.
        results[0] = new GridfsFileObject(".", this._connection);
        results[1] = new GridfsFileObject("..", this._connection);
        
        int index = 2;
        for (String item : localEntries) {
            results[index] = new GridfsFileObject(item, this._connection);
            index++;
        }
        
        return results;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileObject#length()
     */
    public long length() {
        if (this._fileHandle != null) {
            return this._fileHandle.getLength();
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileObject#lastModified()
     */
    public long lastModified() {
        if (this._fileHandle != null) {
            return this._fileHandle.getUploadDate().getTime();
        }
        return 0L;
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileObject#getRandomAccessFileObject(java.lang.String)
     */
    public RandomAccessFileObject getRandomAccessFileObject(String mode)
            throws IOException {
        return new GridfsRandomAccessFileObjectImpl(this, mode);
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileObject#delete()
     */
    public boolean delete() {
        if (this._fileHandle != null) {
            this._connection.getFs().remove(this._path);
            return true;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileObject#getParent()
     */
    public FileObject getParent() {
        if (this._path.equals(GridfsConstants.FILE_SEP)) {
            return new GridfsFileObject(GridfsConstants.FILE_SEP,
                                        this._connection);
        } else {
            return new GridfsFileObject(this._path.substring(0, this._path.lastIndexOf(GridfsConstants.FILE_SEP)),
                                        this._connection);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileObject#mkdir()
     */
    public boolean mkdir() {
        if (this._fileHandle == null) {
            GridFSInputFile newEntry = this._connection.getFs().createFile(new byte[0]);
            newEntry.setContentType(COLLECTION);
            newEntry.setFilename(this._path);
            newEntry.save();
            this._fileHandle = newEntry;
            return true;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileObject#renameTo(au.org.arcs.griffin.filesystem.FileObject)
     */
    public boolean renameTo(FileObject file) {
        if (this._fileHandle != null) {
            this._fileHandle.put(FILENAME, file.getName());
            this._fileHandle.save();
            return true;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see au.org.arcs.griffin.filesystem.FileObject#setLastModified(long)
     */
    public boolean setLastModified(long t) {
        if (this._fileHandle != null) {
            this._fileHandle.put(UPLOAD_DATE, new Date(t));
            this._fileHandle.save();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the GridFS file handle of the file object.
     * 
     * @return Returns the file handle.
     */
    public GridFSFile getFileHandle() {
        return _fileHandle;
    }

    /**
     * Return the GridFS connection object.
     *  
     * @return Returns _connection.
     */
    public GridfsFileSystemConnectionImpl getConnection() {
        return _connection;
    }
}
