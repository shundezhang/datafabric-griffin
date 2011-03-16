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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.org.arcs.griffin.common.FtpConstants;
import au.org.arcs.griffin.filesystem.FileObject;
import au.org.arcs.griffin.filesystem.RandomAccessFileObject;
import edu.sdsc.grid.io.GeneralFile;
import edu.sdsc.grid.io.GeneralMetaData;
import edu.sdsc.grid.io.MetaDataCondition;
import edu.sdsc.grid.io.MetaDataRecordList;
import edu.sdsc.grid.io.MetaDataSelect;
import edu.sdsc.grid.io.MetaDataSet;
import edu.sdsc.grid.io.Namespace;
import edu.sdsc.grid.io.RemoteFile;
import edu.sdsc.grid.io.RemoteFileSystem;
import edu.sdsc.grid.io.irods.IRODSFile;
import edu.sdsc.grid.io.irods.IRODSFileSystem;
import edu.sdsc.grid.io.irods.IRODSMetaDataSet;

/**
 * an implementation for jargon
 * @author Shunde Zhang
 */
public class JargonFileObject implements FileObject {
	private static Log          log                 = LogFactory.getLog(JargonFileObject.class);
	
    protected RemoteFile remoteFile = null;
    protected JargonFileSystemConnectionImpl connection = null;
    protected String originalName;
	public static final int JARGON_MAX_QUERY_NUM = 100000;

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
    public FileObject[] listFiles() throws IOException {
    	
//        GeneralFile[] flist = remoteFile.listFiles();

        MetaDataCondition conditionsFile[] = {
				MetaDataSet.newCondition(GeneralMetaData.DIRECTORY_NAME, MetaDataCondition.EQUAL, remoteFile.getAbsolutePath()),
//				MetaDataSet.newCondition(IRODSMetaDataSet.FILE_REPLICA_STATUS, MetaDataCondition.EQUAL, "1"),
//				MetaDataSet.newCondition(IRODSMetaDataSet.FILE_REPLICA_NUM,	MetaDataCondition.EQUAL, 0),
//				MetaDataSet.newCondition(IRODSMetaDataSet.USER_NAME, MetaDataCondition.EQUAL, ((IRODSFileSystem)file.getFileSystem()).getUserName()),
			};
			MetaDataSelect selectsFile[] = MetaDataSet.newSelection(new String[]{
					IRODSMetaDataSet.FILE_NAME,
					IRODSMetaDataSet.DIRECTORY_NAME,
					IRODSMetaDataSet.CREATION_DATE,
					IRODSMetaDataSet.MODIFICATION_DATE,
					IRODSMetaDataSet.SIZE,
					IRODSMetaDataSet.RESOURCE_NAME,
					IRODSMetaDataSet.FILE_REPLICA_STATUS,
//					IRODSMetaDataSet.META_DATA_ATTR_NAME,
//					IRODSMetaDataSet.META_DATA_ATTR_VALUE,
//					IRODSMetaDataSet.FILE_ACCESS_TYPE 
				});
			MetaDataCondition conditionsDir[] = {
				MetaDataSet.newCondition(IRODSMetaDataSet.PARENT_DIRECTORY_NAME, MetaDataCondition.EQUAL, remoteFile.getAbsolutePath()),
	//##			MetaDataSet.newCondition(IRODSMetaDataSet.FILE_REPLICA_STATUS, MetaDataCondition.EQUAL, "1"),
//				MetaDataSet.newCondition(IRODSMetaDataSet.DIRECTORY_USER_NAME, MetaDataCondition.EQUAL, ((IRODSFileSystem)file.getFileSystem()).getUserName()),
			};
			MetaDataSelect selectsDir[] = MetaDataSet.newSelection(new String[]{
					IRODSMetaDataSet.DIRECTORY_NAME,
					IRODSMetaDataSet.DIRECTORY_TYPE,
					IRODSMetaDataSet.DIRECTORY_CREATE_DATE,
					IRODSMetaDataSet.DIRECTORY_MODIFY_DATE,
	//##				IRODSMetaDataSet.RESOURCE_NAME,
//					IRODSMetaDataSet.META_COLL_ATTR_NAME,
//					IRODSMetaDataSet.META_COLL_ATTR_VALUE,
//					IRODSMetaDataSet.DIRECTORY_ACCESS_TYPE
				});
			Comparator<Object> comparator = new Comparator<Object>() {
				public int compare(Object file1, Object file2) {
					return (((GeneralFile)file1).getName().toLowerCase().compareTo(((GeneralFile)file2).getName().toLowerCase()));
				}     			
			};
			try {
				MetaDataRecordList[] fileDetails = ((IRODSFileSystem)remoteFile.getFileSystem()).query(conditionsFile, selectsFile, JARGON_MAX_QUERY_NUM);
	    		MetaDataRecordList[] dirDetails = ((IRODSFileSystem)remoteFile.getFileSystem()).query(conditionsDir, selectsDir, JARGON_MAX_QUERY_NUM, Namespace.DIRECTORY);

	    		if (fileDetails == null) fileDetails = new MetaDataRecordList[0];
	    		if (dirDetails == null) dirDetails = new MetaDataRecordList[0];
	    		Vector<CachedFile> fileList = new Vector<CachedFile>();
//	    		Vector <CachedFile> dirList = new Vector();
//	    		CachedFile[] files = new CachedFile[fileDetails.length];
	    		CachedFile[] dirs = new CachedFile[dirDetails.length];
	    		int i = 0;
	    		log.debug("file num:"+fileDetails.length);
	    		String lastName = null;
	    		for (MetaDataRecordList p:fileDetails) {
	    			CachedFile file = new CachedFile((RemoteFileSystem)remoteFile.getFileSystem(), (String)p.getValue(IRODSMetaDataSet.DIRECTORY_NAME), (String)p.getValue(IRODSMetaDataSet.FILE_NAME));
	    			if (file.getName().equals(lastName)) {
	    				if (p.getValue(IRODSMetaDataSet.FILE_REPLICA_STATUS).equals("1")) // Clean replica - replace previous replica in list
	    					fileList.removeElementAt(fileList.size()-1);	// Delete last item so that this replica replaces it
	    				else
	    					continue;	// Dirty replica. Given we already have a dirty or clean replica, just discard it
	    			}	
	    			lastName = file.getName();
	    			fileList.add(file);
	    			file.setLastModified(Long.parseLong((String) p.getValue(IRODSMetaDataSet.MODIFICATION_DATE))*1000);
	    			file.setLength(Long.parseLong((String)p.getValue(IRODSMetaDataSet.SIZE)));
	    			file.setDirFlag(false);
    				file.setCanWriteFlag(true);
    				file.setCanReadFlag(true);
//		    			files[i] = new CachedFile((RemoteFileSystem)collection.getFileSystem(), (String)p.getValue(IRODSMetaDataSet.DIRECTORY_NAME), (String)p.getValue(IRODSMetaDataSet.FILE_NAME));
//		    			files[i].setLastModified(Long.parseLong((String) p.getValue(IRODSMetaDataSet.MODIFICATION_DATE))*1000);
//		    			files[i].setLength(Long.parseLong((String)p.getValue(IRODSMetaDataSet.SIZE)));
//		    			files[i].setDirFlag(false);
//	    				files[i].setCanWriteFlag(true);
	    			if (p.getValue(IRODSMetaDataSet.FILE_REPLICA_STATUS).equals("0")) {
	    				String s = "";
	    				if (file.length() == 0)
	    					s = " (its length is 0)";
	    				log.warn("Using a dirty copy of "+file.getAbsolutePath()+s);
	    			}
	    			i++;
	    		}
	    		CachedFile[] files = fileList.toArray(new CachedFile[0]);
	    		Arrays.sort((Object[])files, comparator);
	    		
	    		log.debug("number of collections:"+dirDetails.length);
	    		i = 0;
	    		lastName = null;
	    		for (MetaDataRecordList p:dirDetails) {
//	    			CachedFile dir = new CachedFile((RemoteFileSystem)collection.getFileSystem(), (String)p.getValue(IRODSMetaDataSet.DIRECTORY_NAME));
//	    			if (dir.getName().equals(lastName))
//	    				continue;
//	    			lastName = dir.getName();
//	    			dirList.add(dir);
//	    			dir.setLastModified(Long.parseLong((String)p.getValue(IRODSMetaDataSet.DIRECTORY_MODIFY_DATE))*1000);
//	    			dir.setDirFlag(true);
//	    			dir.setCanWriteFlag(true);
	    			dirs[i] = new CachedFile((RemoteFileSystem)remoteFile.getFileSystem(), (String)p.getValue(IRODSMetaDataSet.DIRECTORY_NAME));
	    			try {
	    				dirs[i].setLastModified(Long.parseLong((String)p.getValue(IRODSMetaDataSet.DIRECTORY_MODIFY_DATE))*1000);
	    			} catch (Exception e) {
	    				log.error("failed to parse last modified time for "+dirs[i].getAbsolutePath()+": "+(String)p.getValue(IRODSMetaDataSet.DIRECTORY_MODIFY_DATE));
	    				dirs[i].setLastModified(0);
	    			}
	    			dirs[i].setDirFlag(true);
	    			dirs[i].setCanWriteFlag(true);
	    			dirs[i].setCanReadFlag(true);
	    			i++;
	    		}
//	    		CachedFile[] dirs = dirList.toArray(new CachedFile[0]);
	    		Arrays.sort((Object[])dirs, comparator);
	    		
//	    		CachedFile[] detailList = new CachedFile[files.length+dirs.length];
//	    		System.arraycopy(dirs, 0, detailList, 0, dirs.length);
//	    		System.arraycopy(files, 0, detailList, dirs.length, files.length);

	            FileObject[] list = new FileObject[files.length+dirs.length];
//	            list[0] = this.connection.getFileObject(".");
//	            list[1] = this.connection.getFileObject("..");
	            
	            for (int j = 0; j < files.length; j++) {
	                list[j] = new JargonFileObject(this.connection,
	                                                   (RemoteFile) files[j]);
	            }
	            for (int j = 0; j < dirs.length; j++) {
	                list[j + files.length] = new JargonFileObject(this.connection,
	                                                   (RemoteFile) dirs[j]);
	            }

	    		return list;
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new IOException(e.getMessage());
			}
    	
//        FileObject[] list = new FileObject[flist.length + 2];
//        
//        // Add two entries for current and parent directory.
//        list[0] = this.connection.getFileObject(".");
//        list[1] = this.connection.getFileObject("..");
//        
//        for (int i = 0; i < flist.length; i++) {
//            list[i + 2] = new JargonFileObject(this.connection,
//                                               (RemoteFile) flist[i]);
//        }
//        return list;
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
