/*
 * Copyright (C) 2016-2024 Smart Software for Car Technologies Inc. and EXCEEDDATA
 *     https://www.smartsct.com
 *     https://www.exceeddata.com
 *
 *                            MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of a copyright holder
 * shall not be used in advertising or otherwise to promote the sale, use
 * or other dealings in this Software without prior written authorization
 * of the copyright holder.
 */
package com.exceeddata.sdk.merge.data;


import com.exceeddata.sdk.vdata.binary.BinaryLittleEndianUtils;
import com.exceeddata.sdk.vdata.binary.BinarySeekableReader;
import com.exceeddata.sdk.vdata.binary.LittleEndianSeekableBytesReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class MultiVDataFileReader {
    Logger LOG= Logger.getLogger("com.exceeddata.sdk.combine.data.MultiVDataFileReader" );
    int formatVersion = 10;
    int blocksCount =0;

    long sstime = 0L;
    long setime = 0L;
    int indexInfoSize = 0;

    int offset_vsw_block=0;
    byte[] extendedInfo= new byte[ 0];

    ArrayList<MultiVswIndexInfo> vsws = new ArrayList<>();
    BinarySeekableReader reader ;

    String deviceId="";

    MultiVDataFileReader(BinarySeekableReader reader) {
        this.reader = reader;
    }



    public void initialize() throws IOException {
        initialize("" , 0, Long.MAX_VALUE );
    }

    public void initialize(String deviceId, long queryStartTime, long queryEndTime) throws  IOException{
        this.deviceId = deviceId;
        this.sstime = queryStartTime;
        this.setime = queryEndTime;
        readMeta();
        readVswIndexInfo();
        readVswData();
    }

    /**
     * Get VSW Reader for specified Device ID if all vsw has loaded.
     * @param deviceId
     * @return List of BinarySeekableReader for the device id
     */
    public List<BinarySeekableReader> getVswReaders(String deviceId) {
        if (this.deviceId != null && (!this.deviceId.equals(deviceId))){
            throw  new RuntimeException( "Device ID not match for query the load,  load is "+ this.deviceId + " but  query is "+ deviceId);
        }
        ArrayList <BinarySeekableReader>  readers = new ArrayList<>();

        for (int i=0 ; i< vsws.size() ; i ++){
            readers.add( new LittleEndianSeekableBytesReader( vsws.get(i).getVsw()));
        }
        return readers;
    }

    public List<BinarySeekableReader> getVswReaders(){
        ArrayList <BinarySeekableReader>  readers = new ArrayList<>();

        for (int i=0 ; i< vsws.size() ; i ++){
            readers.add( new LittleEndianSeekableBytesReader( vsws.get(i).getVsw()));
        }
        return readers;
    }

    /**
     * Get deviceId -> List<vsw data> map
     * This method can be used if enough memory provided.
     * @return map of data
     */
    public Map<String, List<BinarySeekableReader>> getAllVswReaders(){
        Map<String, List<BinarySeekableReader>> result = new HashMap<>( );

        for (int i=0 ; i< vsws.size() ; i ++){
            MultiVswIndexInfo info = vsws.get(i);
            List <BinarySeekableReader>  readers = result.get(info.getDeviceIdStr());
            if (null == readers){
                readers = new ArrayList<>(60);
                result.put( info.getDeviceIdStr(), readers);
            }

            readers.add( new LittleEndianSeekableBytesReader( vsws.get(i).getVsw()));
        }
        return result;
    }

    public Iterator<MultiVswIndexInfo> getIterator(){
        return  vsws.iterator();
    }

    /**
     * Read the metadata info from a seekable file reader or a byte reader.
     *
     * @throws IOException on exception
     */
    public void readMeta() throws IOException {
        final byte[] databytes = reader.readBytes(32);
        final String blockid = BinaryLittleEndianUtils.bytesToANSI(databytes, 0, 2);
        if (!"MD".equals(blockid)) {
            throw new IOException("FORMAT_VSHADOW_MAGIC_NUMBER_INVALID");
        }

        formatVersion = BinaryLittleEndianUtils.bytesToUINT8int(databytes, 2);
        blocksCount = BinaryLittleEndianUtils.bytesToINT32(databytes, 4);
        if (blocksCount <= 0) {
            throw new IOException("FORMAT_VSHADOW_BLOCKS_COUNT_INVALID");
        }

        indexInfoSize = BinaryLittleEndianUtils.bytesToINT32(databytes, 8);

        sstime = BinaryLittleEndianUtils.bytesToINT64(databytes, 12);
        setime = BinaryLittleEndianUtils.bytesToINT64(databytes, 20);

        int extlength = BinaryLittleEndianUtils.bytesToUINT16(databytes, 28);

        extendedInfo = extlength > 0 ? reader.readBytes(extlength) : new byte[] {};
    }

    /**
     * Read VSW IndexBlockInformation
     * Filter Data by DeviceId QueryStart/End Time
     * Put all valid VswIndexInfo for load vsw binary data.
     * @throws IOException
     */
    public void readVswIndexInfo() throws  IOException{
        for (int i =0 ;i < blocksCount; i ++){
            byte [] vswIndexBlock = reader.readBytes(MultiVswIndexInfo.VSW_INDEX_BASE_SIZE + indexInfoSize );
            MultiVswIndexInfo indexInfo = MultiVswIndexInfo.fromBytes( vswIndexBlock);
            if (indexInfo.isMatched(this.deviceId, this.sstime, this.setime))  {
                vsws.add( indexInfo);
            }else{
                LOG.info( "Ignore VSW file "+ indexInfo.toString());
            }
        }
    }

    /**
     * Read filtered VSW Data.
     * @throws IOException
     */
    public void readVswData() throws  IOException {
        for (int i =0 ; i<vsws.size() ; i ++){
            byte [] vswData = reader.readBytes( vsws.get(i).getLength());
            vsws.get(i).setVsw(vswData);
        }
    }

    public byte[] getExtendedInfo(){
        return extendedInfo;
    }

    public String getDeviceId(   ){
        return  deviceId;
    }
}
