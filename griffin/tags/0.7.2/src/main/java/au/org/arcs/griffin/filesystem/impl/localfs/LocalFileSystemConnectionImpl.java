package au.org.arcs.griffin.filesystem.impl.localfs;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.org.arcs.griffin.common.FtpConstants;
import au.org.arcs.griffin.exception.FtpConfigException;
import au.org.arcs.griffin.filesystem.FileObject;
import au.org.arcs.griffin.filesystem.FileSystemConnection;
import au.org.arcs.griffin.usermanager.model.GroupDataList;
import au.org.arcs.griffin.usermanager.model.UserData;
import au.org.arcs.griffin.utils.VarMerger;

public class LocalFileSystemConnectionImpl implements FileSystemConnection {
	private static Log log = LogFactory.getLog(LocalFileSystemConnectionImpl.class);

	private UserData userData;
	private GroupDataList groupDataList;
	private String rootPath;
	
	private boolean isConnected;
	private String homeDir;
	
	public UserData getUserData() {
		return userData;
	}

	public void setUserData(UserData userData) {
		this.userData = userData;
	}

	public GroupDataList getGroupDataList() {
		return groupDataList;
	}

	public void setGroupDataList(GroupDataList groupDataList) {
		this.groupDataList = groupDataList;
	}

	public String getRootPath() {
		return rootPath;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	public LocalFileSystemConnectionImpl(String rootPath, UserData userData,
			GroupDataList groupDataList) throws IOException {
		
		this.rootPath=rootPath;
		this.userData=userData;
		this.groupDataList=groupDataList;
		
		homeDir=getStartDir();
		log.debug("default dir:"+homeDir);
	    File dir = new File(homeDir);
	    if (!dir.exists()) {
	        FileUtils.forceMkdir(dir);
	    }
	    isConnected=true;
	}
	
    private String getStartDir() throws FtpConfigException {
        if (userData == null) {
            throw new FtpConfigException("User data not available");
        }
        VarMerger varMerger = new VarMerger(userData.getDir());
        Properties props = new Properties();
//        props.setProperty("ftproot", FilenameUtils.separatorsToUnix(rootPath));
        props.setProperty("user", userData.getUid());
        varMerger.merge(props);
        if (!varMerger.isReplacementComplete()) {
            throw new FtpConfigException("Unresolved placeholders in user configuration file found.");
        }
        return varMerger.getText();
    }

	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	public FileObject getFileObject(String path) {
		return new LocalFileObject(path,this);
	}

	public long getFreeSpace(String path) {
		// TODO Auto-generated method stub
		return new File(rootPath).getFreeSpace();
	}

	public String getHomeDir() {
		// TODO Auto-generated method stub
		return homeDir;
	}

	public String getUser() {
		// TODO Auto-generated method stub
		return userData.getUid();
	}

	public boolean isConnected() {
		// TODO Auto-generated method stub
		return isConnected;
	}

}
