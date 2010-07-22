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
 *
 */
public class JargonFileObject implements FileObject {
	private static Log log = LogFactory.getLog(JargonFileObject.class);
	private RemoteFile remoteFile;
	public JargonFileObject(RemoteFileSystem rfs, String path){
		if (rfs instanceof IRODSFileSystem) remoteFile=new IRODSFile((IRODSFileSystem) rfs,path);
	}
	public JargonFileObject(RemoteFile file){
		this.remoteFile=file;
	}
	public boolean exists() {
		// TODO Auto-generated method stub
		return remoteFile.exists();
	}

	public String getName() {
		// TODO Auto-generated method stub
		return remoteFile.getName();
	}

	public String getPath() {
		// TODO Auto-generated method stub
		return remoteFile.getPath();
	}

	public int getPermission() {
		if (remoteFile.canRead()&&remoteFile.canWrite()) return FtpConstants.PRIV_READ_WRITE;
		else if (remoteFile.canRead()&&!remoteFile.canWrite()) return FtpConstants.PRIV_READ;
		else if (!remoteFile.canRead()&&remoteFile.canWrite()) return FtpConstants.PRIV_WRITE;
		return FtpConstants.PRIV_NONE;
	}

	public boolean isDirectory() {
		// TODO Auto-generated method stub
		return remoteFile.isDirectory();
	}

	public boolean isFile() {
		// TODO Auto-generated method stub
		return remoteFile.isFile();
	}
	public String getCanonicalPath() throws IOException {
		return remoteFile.getCanonicalPath();
	}
	public FileObject[] listFiles() {
		GeneralFile[] flist=remoteFile.listFiles();
		FileObject[] list=new FileObject[flist.length];
		for (int i=0;i<list.length;i++){
			list[i]=new JargonFileObject((RemoteFile) flist[i]);
		}
		return list;
	}
	public long lastModified() {
		return remoteFile.lastModified();
	}
	public long length() {
		return remoteFile.length();
	}
	public RandomAccessFileObject getRandomAccessFileObject(String type) throws IOException {
		return new JargonRandomAccessFileObjectImpl(remoteFile,type);
	}
	public boolean delete() {
		return remoteFile.delete();
		
	}
	public FileObject getParent() {
		return new JargonFileObject((RemoteFile) remoteFile.getParentFile());
	}
	public boolean mkdir() {
		return remoteFile.mkdir();
		
	}
	public boolean renameTo(FileObject file) {
		if (file instanceof JargonFileObject) 
			return remoteFile.renameTo(((JargonFileObject)file).getRemoteFile());
		else 
			return false;
	}
	public boolean setLastModified(long t) {
		return remoteFile.setLastModified(t);
	}
	public RemoteFile getRemoteFile(){
		return remoteFile;
	}

}
