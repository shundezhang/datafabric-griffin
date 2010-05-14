package au.org.arcs.griffin.agent;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;

public class GridFTPAgent {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		File f=new File("/tmp/griffin-log");
		DataOutputStream os=new DataOutputStream(new FileOutputStream(f));
		os.write(("GridFTPAgent "+new Date().toString()+"\n").getBytes());
		
		Object stdInObject = (Object)System.inheritedChannel();
		os.write((stdInObject+"\n").getBytes());
		os.write(((stdInObject instanceof SocketChannel)+"\n").getBytes());
		os.write(((stdInObject instanceof ServerSocketChannel)+"\n").getBytes());
		
		if (stdInObject instanceof SocketChannel){
			SocketChannel client = (SocketChannel)stdInObject;
			os.write((client+"\n").getBytes());
			if (client!=null) {
				BufferedReader is = new BufferedReader(new InputStreamReader(client.socket().getInputStream()));
				String s;
				while ((s=is.readLine())!=null){
					Thread.sleep(30000);
					os.write(s.getBytes());
				}
			}
		}else if (stdInObject instanceof ServerSocketChannel) {
			// sshftp creates a server socket
			ServerSocketChannel channel =
				(ServerSocketChannel) stdInObject;
			ServerSocket socket =
				(ServerSocket)channel.socket();
			os.write(("channel: " + channel).getBytes());
			os.write(("socket: " + socket).getBytes());
			Thread.sleep(30000);
			Socket client=socket.accept();
			BufferedReader is = new BufferedReader(new InputStreamReader(client.getInputStream()));
			String s;
			while ((s=is.readLine())!=null){
				Thread.sleep(30000);
				os.write(s.getBytes());
			}
			socket.close();

		// started from command line??
		} else {
			System.err.println("stdInObject: " + stdInObject);
		}
	}

}
