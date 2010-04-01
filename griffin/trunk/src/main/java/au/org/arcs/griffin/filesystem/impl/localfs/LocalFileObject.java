package au.org.arcs.griffin.filesystem.impl.localfs;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.org.arcs.griffin.common.FtpConstants;
import au.org.arcs.griffin.exception.FtpConfigException;
import au.org.arcs.griffin.filesystem.FileObject;
import au.org.arcs.griffin.filesystem.FileSystemConnection;
import au.org.arcs.griffin.filesystem.RandomAccessFileObject;
import au.org.arcs.griffin.usermanager.model.GroupDataList;

public class LocalFileObject implements FileObject {
	private static Log log = LogFactory.getLog(LocalFileObject.class);
	private File file;
	private LocalFileSystemConnectionImpl connection;
	// this path is a relative path in the gridftp server context
	private String canonicalPath;
	private String relativePath;
	public LocalFileObject(String path, LocalFileSystemConnectionImpl connection){
		this.relativePath=path;
		this.connection=connection;
        path = FilenameUtils.normalizeNoEndSeparator(path);
    	file = new File(connection.getRootPath(), path);
    	try {
			canonicalPath=file.getCanonicalPath().substring(connection.getRootPath().length()-1);
	    	log.debug("create object with ftp path: "+canonicalPath+" and real path:"+file.getCanonicalPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public File getLocalFile(){
		return this.file;
	}
	
	public boolean delete() {
		// TODO Auto-generated method stub
		return file.delete();
	}

	public boolean exists() {
		// TODO Auto-generated method stub
		return file.exists();
	}

	public String getCanonicalPath() throws IOException {
		// TODO Auto-generated method stub
		return canonicalPath;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return file.getName();
	}

	public FileObject getParent() {
		if (canonicalPath.equals("/")) 
			return new LocalFileObject("/", connection);
		else
			return new LocalFileObject(canonicalPath.substring(0,canonicalPath.lastIndexOf("/")), connection);
	}

	public String getPath() {
		// TODO Auto-generated method stub
		return relativePath;
	}

	public int getPermission() {
        int result = FtpConstants.PRIV_NONE;
        try {
            GroupDataList list = connection.getGroupDataList();
            result = list.getPermission(canonicalPath, connection.getUser(), connection.getRootPath());
        } catch (FtpConfigException e) {
            log.error(e);
        }
        return result;
	}

	public RandomAccessFileObject getRandomAccessFileObject(String type)
			throws IOException {
		// TODO Auto-generated method stub
		return new LocalRandomAccessFileObjectImpl(file,type);
	}

	public boolean isDirectory() {
		// TODO Auto-generated method stub
		return file.isDirectory();
	}

	public boolean isFile() {
		// TODO Auto-generated method stub
		return file.isFile();
	}

	public long lastModified() {
		// TODO Auto-generated method stub
		return file.lastModified();
	}

	public long length() {
		// TODO Auto-generated method stub
		return file.length();
	}

	public FileObject[] listFiles() {
		File[] flist=file.listFiles();
		FileObject[] list=new FileObject[flist.length];
		String s;
		for (int i=0;i<list.length;i++){
			try {
				s=flist[i].getCanonicalPath();
				list[i]=new LocalFileObject(s.substring(0,s.lastIndexOf("/")), connection);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return list;
	}

	public boolean mkdir() {
		// TODO Auto-generated method stub
		return file.mkdir();
	}

	public boolean renameTo(FileObject file) {
		// TODO Auto-generated method stub
		if (file instanceof LocalFileObject)
			return this.file.renameTo(((LocalFileObject)file).getLocalFile());
		else
			return false;
	}

	public boolean setLastModified(long t) {
		// TODO Auto-generated method stub
		return file.setLastModified(t);
	}

}
