package au.org.arcs.griffin.filesystem.impl.jargon;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.org.arcs.griffin.filesystem.RandomAccessFileObject;

import edu.sdsc.grid.io.RemoteFile;
import edu.sdsc.grid.io.RemoteRandomAccessFile;
import edu.sdsc.grid.io.irods.IRODSFile;
import edu.sdsc.grid.io.irods.IRODSRandomAccessFile;

/**
 * an implementation for jargon
 * @author Shunde Zhang
 *
 */

public class JargonRandomAccessFileObjectImpl implements RandomAccessFileObject {
	private static Log log = LogFactory.getLog(JargonRandomAccessFileObjectImpl.class);
	private RemoteRandomAccessFile raf;
	public JargonRandomAccessFileObjectImpl(RemoteFile file,
			String type)  throws IOException{
		if (file instanceof IRODSFile) raf=new IRODSRandomAccessFile((IRODSFile) file,type);
		throw new IOException("Object type is not recognizable.");
	}

	public void close() throws IOException {
		raf.close();

	}

	public int read() throws IOException {
		// TODO Auto-generated method stub
		return raf.read();
	}

	public int read(byte[] b) throws IOException {
		// TODO Auto-generated method stub
		return raf.read(b);
	}

	public int read(byte[] b, int off, int len) throws IOException {
		// TODO Auto-generated method stub
		return raf.read(b, off, len);
	}

	public String readLine() throws IOException {
		// TODO Auto-generated method stub
		return raf.readLine();
	}

	public void seek(long offset) throws IOException {
		raf.seek(offset);

	}

	public long length() throws IOException {
		// TODO Auto-generated method stub
		return raf.length();
	}

	public void write(int b) throws IOException {
		raf.write(b);
		
	}

	public void write(byte[] b) throws IOException {
		raf.write(b);
		
	}

	public void write(byte[] b, int off, int len) throws IOException {
		raf.write(b, off, len);
		
	}

}
