/*
 * ------------------------------------------------------------------------------
 * Hermes FTP Server
 * Copyright (c) 2005-2007 Lars Behnke
 * ------------------------------------------------------------------------------
 * 
 * This file is part of Hermes FTP Server.
 * 
 * Hermes FTP Server is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Hermes FTP Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Hermes FTP Server; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * ------------------------------------------------------------------------------
 */

package au.org.arcs.griffin.cmd;

import java.io.IOException;
import java.net.Socket;

/**
 * Interface implemented by classes that provide for the data channel in passive or active transfer
 * mode.
 * 
 * @author Behnke
 */
public interface SocketProvider {

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
    Socket provideSocket() throws IOException;

    /**
     * Closes the socket, if necessary.
     */
    void closeSocket();

}
