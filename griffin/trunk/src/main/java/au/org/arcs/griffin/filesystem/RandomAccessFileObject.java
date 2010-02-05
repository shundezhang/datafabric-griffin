package au.org.arcs.griffin.filesystem;

import java.io.IOException;

public interface RandomAccessFileObject {
	public void seek(long offset) throws IOException;
	public int read() throws IOException;
	public int read(byte[] b) throws IOException;
	public int read(byte[] b, int off, int len) throws IOException;
	public void close() throws IOException;
	public String readLine() throws IOException;
	public void write(int b) throws IOException;
	public void write(byte[] b) throws IOException;
	public void write(byte[] b, int off, int len) throws IOException;
	public long length() throws IOException;
}
