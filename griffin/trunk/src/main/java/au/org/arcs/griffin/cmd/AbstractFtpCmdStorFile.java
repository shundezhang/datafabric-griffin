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
import java.io.InputStream;
import java.io.OutputStream;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.org.arcs.griffin.exception.FtpCmdException;
import au.org.arcs.griffin.exception.FtpException;
import au.org.arcs.griffin.exception.FtpPermissionException;
import au.org.arcs.griffin.exception.FtpUniqueConstraintException;
import au.org.arcs.griffin.filesystem.FileObject;
import au.org.arcs.griffin.streams.EDataBlock;
import au.org.arcs.griffin.streams.RafOutputStream;
import au.org.arcs.griffin.streams.RecordReadSupport;
import au.org.arcs.griffin.utils.IOUtils;

/**
 * Abstract STOR command implementation that saves files to the local disk.
 * 
 * @author Lars Behnke
 * @author Shunde Zhang
 */
public abstract class AbstractFtpCmdStorFile extends AbstractFtpCmdStor {

    private static Log log = LogFactory.getLog(AbstractFtpCmdStorFile.class);

    /**
     * {@inheritDoc}
     */
    public void execute(boolean unique) throws FtpCmdException {
        super.execute(unique);
    }

    /**
     * {@inheritDoc}
     */
    protected void doPerformAccessChecks(boolean unique, FileObject file, long fileOffset) throws FtpException {
    	log.debug("file:"+file.getCanonicalPath()+"; parent perm:"+file.getParent().getPermission());
        if ((file.getParent().getPermission() & PRIV_WRITE) == 0) {
            throw new FtpPermissionException();
        }

        String[] limits = new String[] {STAT_FILES_UPLOADED, STAT_BYTES_UPLOADED};
//        getCtx().getUserManager().checkResourceConsumption(getCtx().getUser(), limits);

        if (file.isDirectory()) {
            throw new FtpPermissionException("Cannot store directory path.");
        }
        if (file.exists()) {
            if (unique) {
                throw new FtpUniqueConstraintException();
            } else if (fileOffset == 0) {
                try {
                    file.delete();
                } catch (SecurityException e) {
                    throw new FtpPermissionException("System access rights have been violated.");
                }
            }
        }
        getCtx().updateIncrementalStat(STAT_FILES_UPLOADED, 1);

    }

    /**
     * {@inheritDoc}
     */
    protected void doStoreFileData(InputStream is, FileObject file, long offset) throws IOException {
        OutputStream os = new RafOutputStream(file, offset);
        int bufferSize = getCtx().getBufferSize();
        byte[] buffer = new byte[bufferSize];
        int count;
        try {
            while ((count = is.read(buffer)) != -1) {
                os.write(buffer, 0, count);
                os.flush();
                getCtx().updateIncrementalStat(STAT_BYTES_UPLOADED, count);
                incCompleted(count);
                if (isAbortRequested()) {
                    log.debug("File transfer aborted");
                    msgOut(MSG426);
                    return;
                }
                getCtx().getTransferMonitor().execute(count);

            }
//            getCtx().updateAverageStat(STAT_UPLOAD_RATE,
//                (int) getTransferRateLimiter().getCurrentTransferRate());
//            msgOut(MSG226);
        } finally {
            IOUtils.closeGracefully(is);
            IOUtils.closeGracefully(os);
        }
    }
    
    protected void doStoreFileData(DataChannel dc, FileObject file, long offset) throws IOException {
        OutputStream os = new RafOutputStream(file, offset);
        int bufferSize = getCtx().getBufferSize();
        byte[] buffer = new byte[bufferSize];
        int count;
        try {
            while ((count = dc.read(buffer)) != -1) {
                os.write(buffer, 0, count);
                os.flush();
                getCtx().updateIncrementalStat(STAT_BYTES_UPLOADED, count);
                incCompleted(count);
                if (isAbortRequested()) {
                    log.debug("File transfer aborted");
                    msgOut(MSG426);
                    return;
                }
                getCtx().getTransferMonitor().execute(count);

            }
//            getCtx().updateAverageStat(STAT_UPLOAD_RATE,
//                (int) getTransferRateLimiter().getCurrentTransferRate());
//            msgOut(MSG226);
        } finally {
//            IOUtils.closeGracefully(is);
            IOUtils.closeGracefully(os);
        }
    }
    
	@Override
	protected void doStoreFileDataInEBlockMode(InputStream dataIn,
			FileObject file) throws IOException {
		EDataBlock eDataBlock = new EDataBlock(getCtx().getUser());
		OutputStream os=null;
        try {

            boolean eod = false;
            long count;
            while (!eod && (count =eDataBlock.read(dataIn)) > -1) {
            	log.debug(eDataBlock+" count="+count);
                //If we're running a modeE demux then check for end of channel
                //allow the block to be forwarded as it may have data
                if (eDataBlock.isDescriptorSet(EDataBlock.DESC_CODE_EOF)) {
                    int dataChannelCount = (int) eDataBlock.getDataChannelCount();
                    log.debug("dataChannelCount:"+dataChannelCount);
//                    say(" Setting data channel count to " +dataChannelCount);
//                    synchronized (_parent) {
//                        _parent.setDataChannelCount(dataChannelCount);
//                    }
                    //Change the dc count to 1 for the pool
//                    eDataBlock.setDCCountTo1();
                }

                if (eDataBlock.isDescriptorSet(EDataBlock.DESC_CODE_EOD)) {
                    eod = true;
                    break;
                    //Turn off the eod flag
//                    say("EOD received");
//                    eDataBlock.unsetDescriptor(EDataBlock.DESC_CODE_EOD);
//                    synchronized (_parent) {
//                        _parent.addEODSeen();
//                    }
                }
                if (os==null){
                    long offset=eDataBlock.getOffset();
                    os=new RafOutputStream(file, offset);
                }
                os.write(eDataBlock.getData(), 0, (int)count);
                os.flush();
                getCtx().updateIncrementalStat(STAT_BYTES_UPLOADED, count);
                incCompleted(count);
                if (isAbortRequested()) {
                    log.debug("File transfer aborted");
                    msgOut(MSG426);
                    return;
                }
                getCtx().getTransferMonitor().execute(count);

//                synchronized (_parent) {
//                    ostr.write(eDataBlock.getHeader());
//                    ostr.write(eDataBlock.getData());
//                    ostr.flush();
//                }
                //say("Done writing");
            }
            getCtx().updateAverageStat(STAT_UPLOAD_RATE,
                    (int) getCtx().getTransferMonitor().getCurrentTransferRate());
                msgOut(MSG226);
//            say("Adapter: done, EOD received ? = " + eod);
        } catch (IOException e) {
        	e.printStackTrace();
//            esay(e);
            // what can we do here ??

            // TIMUR: I think it does not make sence to continue,
            // better to end this thread
//            esay("we failed: calling _parent.SubtractDataChannel()");
//            _parent.subtractDataChannel();
            return;
        } finally {
            IOUtils.closeGracefully(dataIn);
            if (os!=null) IOUtils.closeGracefully(os);
        }

//        synchronized (_parent) {
//            _parent.subtractDataChannel();
//
//            if (_parent.getDataChannelsClosed() == _parent.getDataChannelCount()) {
//                if (_parent.getEODSeen() == _parent.getDataChannelsClosed()) {
//                    //Send the real eod
//                    byte[] eodPacket = new byte[17];
//                    eodPacket[0] = 8;
//                    synchronized (_parent) {
//                        ostr.write(eodPacket, 0, 17);
//                    }
//                } else {
//                    esay("The last socket is closing, but we didn't see enough EOD's.  Not going to send EOD to pool.  Transfer failed");
//                    throw new IOException();
//                }
//            }
//        }

		
	}

	@Override
	protected void doStoreFileDataInEBlockMode(DataChannel dc,
			FileObject file) throws IOException {
		EDataBlock eDataBlock = new EDataBlock(getCtx().getUser());
		OutputStream os=null;
        try {

            boolean eod = false;
            long count;
            while (!eod && (count = eDataBlock.read(dc)) > -1) {
            	log.debug(eDataBlock+" count="+count);
                //If we're running a modeE demux then check for end of channel
                //allow the block to be forwarded as it may have data
                if (eDataBlock.isDescriptorSet(EDataBlock.DESC_CODE_EOF)) {
                    int dataChannelCount = (int) eDataBlock.getDataChannelCount();
                    log.debug("dataChannelCount:"+dataChannelCount);
//                    say(" Setting data channel count to " +dataChannelCount);
//                    synchronized (_parent) {
//                        _parent.setDataChannelCount(dataChannelCount);
//                    }
                    //Change the dc count to 1 for the pool
//                    eDataBlock.setDCCountTo1();
                }

                if (eDataBlock.isDescriptorSet(EDataBlock.DESC_CODE_EOD)) {
                    eod = true;
                    break;
                    //Turn off the eod flag
//                    say("EOD received");
//                    eDataBlock.unsetDescriptor(EDataBlock.DESC_CODE_EOD);
//                    synchronized (_parent) {
//                        _parent.addEODSeen();
//                    }
                }
                if (os==null){
                    long offset=eDataBlock.getOffset();
                    os=new RafOutputStream(file, offset);
                }
                os.write(eDataBlock.getData(), 0, (int)count);
                os.flush();
                getCtx().updateIncrementalStat(STAT_BYTES_UPLOADED, count);
                incCompleted(count);
                if (isAbortRequested()) {
                    log.debug("File transfer aborted");
                    msgOut(MSG426);
                    return;
                }
                getCtx().getTransferMonitor().execute(count);

//                synchronized (_parent) {
//                    ostr.write(eDataBlock.getHeader());
//                    ostr.write(eDataBlock.getData());
//                    ostr.flush();
//                }
                //say("Done writing");
            }
            getCtx().updateAverageStat(STAT_UPLOAD_RATE,
                    (int) getCtx().getTransferMonitor().getCurrentTransferRate());
                msgOut(MSG226);
//            say("Adapter: done, EOD received ? = " + eod);
        } catch (IOException e) {
        	e.printStackTrace();
//            esay(e);
            // what can we do here ??

            // TIMUR: I think it does not make sence to continue,
            // better to end this thread
//            esay("we failed: calling _parent.SubtractDataChannel()");
//            _parent.subtractDataChannel();
            return;
        } finally {
//            IOUtils.closeGracefully(dataIn);
            if (os!=null) IOUtils.closeGracefully(os);
        }

//        synchronized (_parent) {
//            _parent.subtractDataChannel();
//
//            if (_parent.getDataChannelsClosed() == _parent.getDataChannelCount()) {
//                if (_parent.getEODSeen() == _parent.getDataChannelsClosed()) {
//                    //Send the real eod
//                    byte[] eodPacket = new byte[17];
//                    eodPacket[0] = 8;
//                    synchronized (_parent) {
//                        ostr.write(eodPacket, 0, 17);
//                    }
//                } else {
//                    esay("The last socket is closing, but we didn't see enough EOD's.  Not going to send EOD to pool.  Transfer failed");
//                    throw new IOException();
//                }
//            }
//        }

		
	}

    /**
     * {@inheritDoc}
     */
    protected void doStoreRecordData(RecordReadSupport rrs, FileObject file, long offset) throws IOException {
        RafOutputStream os = new RafOutputStream(file, offset);
        byte[] recordBuffer = null;
        byte[] lastRecordBuffer = null;
        try {
            while ((recordBuffer = rrs.readRecord()) != null) {
                writeRecord(os, lastRecordBuffer, false);
                lastRecordBuffer = recordBuffer;
                if (isAbortRequested()) {
                    log.debug("Record transfer aborted");
                    msgOut(MSG426);
                    return;
                }
                getCtx().getTransferMonitor().execute(recordBuffer.length);
            }
            writeRecord(os, lastRecordBuffer, true);
            getCtx().updateAverageStat(STAT_UPLOAD_RATE,
                (int) getCtx().getTransferMonitor().getCurrentTransferRate());
            msgOut(MSG226);
        } finally {
            IOUtils.closeGracefully(rrs);
            IOUtils.closeGracefully(os);
        }
    }

    private void writeRecord(RafOutputStream os, byte[] lastRecordBuffer, boolean eof) throws IOException {
        if (lastRecordBuffer != null) {
            os.writeRecord(lastRecordBuffer, eof);
            getCtx().updateIncrementalStat(STAT_BYTES_UPLOADED, lastRecordBuffer.length);
            incCompleted(lastRecordBuffer.length);
        }
    }
}
