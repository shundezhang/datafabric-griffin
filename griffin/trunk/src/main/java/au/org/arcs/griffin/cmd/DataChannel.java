package au.org.arcs.griffin.cmd;

import java.io.IOException;
import java.net.Socket;

public interface DataChannel {
    /**
     * Initializes the provider.
     * 
     * @throws IOException Error on initializing the data channel.
     * @return Information about the data channel is provided.
     */
    DataChannelInfo init() throws IOException;

    /**
     * Provides the socket for data transfer. Multiple calls of this method do not result in
     * multiple socket instance. One instance is created and cached.
     * 
     * @return The Socket.
     * @throws IOException Error on creating the data channel.
     */
    void start() throws IOException;

    /**
     * Closes the socket, if necessary.
     */
    void closeChannel();
    void write(byte[] b) throws IOException;
    void write(byte[] b, int start, int len) throws IOException;

}
