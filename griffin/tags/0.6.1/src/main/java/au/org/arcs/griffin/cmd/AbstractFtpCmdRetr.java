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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.zip.DeflaterOutputStream;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.org.arcs.griffin.common.FtpConstants;
import au.org.arcs.griffin.exception.FtpCmdException;
import au.org.arcs.griffin.exception.FtpPermissionException;
import au.org.arcs.griffin.exception.FtpQuotaException;
import au.org.arcs.griffin.filesystem.FileObject;
import au.org.arcs.griffin.streams.BlockModeOutputStream;
import au.org.arcs.griffin.streams.RecordOutputStream;
import au.org.arcs.griffin.streams.RecordWriteSupport;
import au.org.arcs.griffin.streams.TextOutputStream;
import au.org.arcs.griffin.utils.TransferMonitor;

/**
 * Abstract base class for RETR command implementations.
 * 
 * @author Lars Behnke
 * @author Shunde Zhang
 */
public abstract class AbstractFtpCmdRetr extends AbstractFtpCmd implements FtpConstants {

    private static Log          log                 = LogFactory.getLog(AbstractFtpCmdRetr.class);

    private long                fileSize;

//    private long                completed;

    private boolean             abortRequested;

    /**
     * Checks availability and access rights for the current folder and passed file. The methods
     * acts as a primitive operation that is called by the template method
     * <code>execute(boolean)</code>;
     * 
     * @param file The destination file.
     * @throws IOException Thrown if one of the following conditions occurred: (1) IO failed or (3)
     *             access rights have been violated or (3) resource limits have been reached.
     */
    protected abstract void doPerformAccessChecks(FileObject file) throws IOException;

//    /**
//     * Retrieves record based data. Since native files generally do not support records, the
//     * assumption is made that each line of a text file corresponds to a record. The method acts as
//     * a primitive operation that is called by the template method <code>execute()</code>;
//     * Futhermore, text record data must be encoded by an 1-byte character set (ACII, ANSI or
//     * EBCDIC).
//     * 
//     * @param out The output stream.
//     * @param file The source file.
//     * @param fileOffset The file offset.
//     * @throws IOException Thrown if IO fails or if a resource limit has been reached.
//     */
//    protected abstract void doRetrieveRecordData(RecordWriteSupport out, FileObject file, long fileOffset)
//            throws IOException;

    /**
     * Retrieves file based data. The method acts as a primitive operation that is called by the
     * template method <code>execute()</code>;
     * 
     * @param out The output stream.
     * @param file The source file.
     * @param fileOffset The file offset.
     * @throws IOException Thrown if IO fails or if a resource limit has been reached.
     */
    protected abstract void doRetrieveFileData(DataChannel dc, FileObject file, long fileOffset)
	throws IOException;
//    protected abstract void doRetrieveFileData(OutputStream out, FileObject file, long fileOffset)
//            throws IOException;

    /**
     * {@inheritDoc}
     */
    public boolean handleAsyncCmd(String req) {
        boolean result;
        if (req == null || isResponded()) {
            result = false;
        } else if (req.toUpperCase().startsWith("STAT")) {
            String stat = "STAT: " + getCompleted() + " from " + getFileSize() + " completed";
            log.info(stat);
            // TODO Return statistics response.
            result = true;
        } else if (req.toUpperCase().startsWith("ABOR")) {
            abortRequested = true;
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public void execute() throws FtpCmdException {

        /* Get relevant information from context */
        FileObject file = getCtx().getFileSystemConnection().getFileObject(getPathArg());
        int mode = getCtx().getTransmissionMode();
//        int struct = getCtx().getStorageStructure();
        int type = getCtx().getDataType();
        String charset = type == DT_ASCII || type == DT_EBCDIC ? getCtx().getCharset() : null;
        long fileOffset = getAndResetFileOffset();
        int maxThread=getCtx().getParallelMax();
        if (maxThread<1) maxThread=1;
        fileSize=file.length();
        log.debug("retriving file:"+file.getCanonicalPath()+" in mode="+mode+"; max thread="+maxThread);
        try {

            /* Check availability and access rights */
            doPerformAccessChecks(file);

            msgOut(MSG150);
            getCtx().getTransferMonitor().init(-1,this); //getCtx().getMaxDownloadRate());

            if (mode==MODE_EBLOCK){
            	DataChannelProvider provider=getCtx().getDataChannelProvider();
            	provider.setMaxThread(maxThread);
            	provider.setDirection(DataChannel.DIRECTION_GET);
            	provider.setFileObject(file);
            	provider.prepare();
            	Thread thread=new Thread(provider);
            	thread.start();
            	try {
            		if (thread.isAlive()) thread.join();
            	} catch (InterruptedException e) {
					log.warn("interrupted exception, this is logged and ignored");
					e.printStackTrace();
				}
//            	provider.closeProvider();
				log.info("transfer is complete");
            }else{  // Stream mode
            	DataChannel dataChannel=getCtx().getDataChannelProvider().provideDataChannel();
            	doRetrieveFileData(dataChannel, file, fileOffset);
            }
            getCtx().getTransferMonitor().sendPerfMarker();
            getCtx().updateAverageStat(STAT_DOWNLOAD_RATE,
            		(int) getCtx().getTransferMonitor().getCurrentTransferRate());
            msgOut(MSG226);

//            if (getCtx().getNetworkStack()==NETWORK_STACK_UDP){
//            	DataChannel dc=getCtx().getDataChannel();
//            	dc.start();
//            	if (mode==MODE_EBLOCK){
//            		doRetrieveFileDataInEBlockMode(dc, file, maxThread);
//            	}else{
//            		doRetrieveFileData(dc, file, fileOffset);
//            	}
//            }else{
//	            /* Wrap outbound data stream and call handler method */
//	            Socket dataSocket = getCtx().getDataSocketProvider().provideSocket();
//	            OutputStream dataOut = dataSocket.getOutputStream();
//	            if (struct == STRUCT_RECORD) {
//	                RecordWriteSupport recordOut = createRecOutputStream(dataOut, mode, charset);
//	                doRetrieveRecordData(recordOut, file, fileOffset);
//	            } else if (struct == STRUCT_FILE) {
//	            	if (mode==MODE_EBLOCK){
//	            		doRetrieveFileDataInEBlockMode(dataOut, file, maxThread);
//	            	}else{
//		                OutputStream fileOut = createOutputStream(dataOut, mode, charset);
//		                doRetrieveFileData(fileOut, file, fileOffset);
//	            	}
//	            } else {
//	                log.error("Unknown data type");
//	                msgOut(MSG550, "Unsupported data type");
//	                return;
//	            }
//	            // TODO delegate event to FtpEventListener
//            }
        } catch (FtpQuotaException e) {
        	e.printStackTrace();
            msgOut(MSG550, e.getMessage());
            log.warn(e.getMessage());
        } catch (FtpPermissionException e) {
        	e.printStackTrace();
            msgOut(MSG550_PERM);
        } catch (UnsupportedEncodingException e) {
        	e.printStackTrace(System.out);
            msgOut(MSG550, "Unsupported Encoding: " + charset);
            log.error(e.toString());
        } catch (IOException e) {
        	e.printStackTrace();
            msgOut(MSG550);
            log.error(e.toString());
        } catch (RuntimeException e) {
        	e.printStackTrace();
            msgOut(MSG550);
            log.error(e.toString());
        } finally {
        	log.debug("in finally");
        	if (mode==MODE_STREAM) getCtx().closeDataChannels();
//        	if (getCtx().getNetworkStack()==NETWORK_STACK_UDP){
//        		getCtx().closeDataChannels();
//        	}else{
//        		if (mode==MODE_STREAM) getCtx().closeSockets();
//        	}
        }
    }

//    abstract protected void doRetrieveFileDataInEBlockMode(DataChannel dc, FileObject file, int maxThread) throws IOException;
//
//	abstract protected void doRetrieveFileDataInEBlockMode(OutputStream dataOut, FileObject file, int maxThread) throws IOException;

//	private OutputStream createOutputStream(OutputStream dataOut, int mode, String charset)
//            throws UnsupportedEncodingException {
//        OutputStream result = null;
//        if (mode == MODE_BLOCK) {
//            result = new BlockModeOutputStream(dataOut);
//        } else if (mode == MODE_STREAM) {
//            result = dataOut;
//        } else if (mode == MODE_ZIP) {
//            result = new DeflaterOutputStream(dataOut);
//        } else {
//            log.error("Unsupported file mode: " + mode);
//        }
//        if (charset != null) {
//            result = new TextOutputStream(result, charset);
//        }
//        return result;
//    }

//    private RecordWriteSupport createRecOutputStream(OutputStream dataOut, int mode, String charset)
//            throws UnsupportedEncodingException {
//        RecordWriteSupport result = null;
//        if (mode == MODE_BLOCK) {
//            result = new BlockModeOutputStream(dataOut);
//        } else if (mode == MODE_STREAM) {
//            result = new RecordOutputStream(dataOut);
//        } else if (mode == MODE_ZIP) {
//            result = new RecordOutputStream(new DeflaterOutputStream(dataOut));
//        } else {
//            log.error("Unsupported record mode: " + mode);
//        }
//        if (charset != null) {
//            result = new TextOutputStream((OutputStream) result, charset);
//        }
//        return result;
//    }

    /**
     * @return True, if transfer has been aborted.
     */
    protected boolean isAbortRequested() {
        return abortRequested;
    }

    /**
     * Getter method for the java bean <code>completed</code>.
     * 
     * @return Returns the value of the java bean <code>completed</code>.
     */
    public long getCompleted() {
        return getCtx().getTransferMonitor().getTransferredBytes(); //completed;
    }

//    /**
//     * Setter method for the java bean <code>completed</code>.
//     * 
//     * @param completed The value of completed to set.
//     */
//    public synchronized void incCompleted(long completed) {
//        this.completed += completed;
//    }

    /**
     * Getter method for the java bean <code>fileSize</code>.
     * 
     * @return Returns the value of the java bean <code>fileSize</code>.
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * Setter method for the java bean <code>fileSize</code>.
     * 
     * @param fileSize The value of fileSize to set.
     */
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

}
