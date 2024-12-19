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
import com.exceeddata.sdk.vdata.util.LittleEndianBytesWriter;

import java.util.Date;

public class MultiVswIndexInfo {
    final public  static int VSW_INDEX_BASE_SIZE=56;
    final public static int DEVICE_ID_MAX_LENGTH=23;
    byte [] deviceId = new byte[24];
    long collectTime;
    long queryStartTime;
    long queryEndTime;
    int offset;

    int length;
    byte[] extendInfo;

    byte[] encoded;

    byte [] vsw;

    String sDeviceId;

    public MultiVswIndexInfo(String deviceId, long collectTime, long queryStartTime, long queryEndTime, int offset,byte[] extendInfo, byte [] vsw){
        byte [] buf = deviceId.getBytes();
        int device_id_length = buf.length > DEVICE_ID_MAX_LENGTH?DEVICE_ID_MAX_LENGTH: buf.length;
        System.arraycopy( buf, 0, this.deviceId,0, device_id_length);

        this.collectTime = collectTime;
        this.queryStartTime = queryStartTime;
        this.queryEndTime = queryEndTime;
        this.offset = offset;
        this.length = vsw.length;
        this.extendInfo = extendInfo;

        this.vsw = vsw;
        this.sDeviceId = deviceId;
        encoded = encode();
    }

    public MultiVswIndexInfo(byte [] deviceId, long collectTime, long queryStartTime, long queryEndTime, int offset,byte[] extendInfo, int length){
        byte [] buf = deviceId;
        int device_id_length = buf.length > DEVICE_ID_MAX_LENGTH?DEVICE_ID_MAX_LENGTH: buf.length;
        System.arraycopy( buf, 0, deviceId,0, device_id_length);

        this.collectTime = collectTime;
        this.queryStartTime = queryStartTime;
        this.queryEndTime = queryEndTime;
        this.offset = offset;
        this.length = length;
        this.extendInfo = extendInfo;
        this.sDeviceId = toCString(buf);
    }

    public byte [] encode(){
        int extendedLength = extendInfo.length;
        final int hlen = extendedLength + 56;
        final byte[] _head = new byte[hlen];
        final LittleEndianBytesWriter writer = new LittleEndianBytesWriter();
        writer.writeBytes(_head, 0, 24, deviceId);
        writer.writeINT64(_head, 24, collectTime);
        writer.writeINT64(_head, 32, queryStartTime);
        writer.writeINT64(_head, 40, queryEndTime);
        writer.writeINT32(_head, 48, offset);
        writer.writeINT32(_head, 52, length);
        writer.writeBytes(_head, 56, extendedLength, extendInfo);
        return _head;
    }

    public int getLength(){
        return length;
    }

    public int getOffset(){
        return offset;
    }

    public byte [] getVsw(){
        return  vsw;
    }

    public byte [] getIndexInfo (){
        return encoded;
    }

    public int getIndexLength(){
        return encoded.length;
    }

    public String  getDeviceIdStr(){
        return sDeviceId;
    }

    public void setVsw (byte [] vsw){
        if (length != vsw.length ){
            //TODO , should not happen.
        }
        this.vsw = vsw;
    }

    /**
     * Decode one Index Information
     * @param encoded
     * @return
     */
    public static MultiVswIndexInfo fromBytes(byte [] encoded){
        byte [] deviceId = new byte[ DEVICE_ID_MAX_LENGTH+1];
        System.arraycopy( encoded , 0, deviceId , 0, DEVICE_ID_MAX_LENGTH+1);
        long collectTime = BinaryLittleEndianUtils.bytesToINT64(encoded, 24);
        long queryStartTime = BinaryLittleEndianUtils.bytesToINT64(encoded, 32);
        long queryEndTime = BinaryLittleEndianUtils.bytesToINT64(encoded, 40);
        int offset= BinaryLittleEndianUtils.bytesToINT32(encoded, 48);
        int length= BinaryLittleEndianUtils.bytesToINT32(encoded, 52);

        int extLength = encoded.length - VSW_INDEX_BASE_SIZE ;
        byte [] extInfo = new byte[ extLength];
        System.arraycopy( encoded, 0, extInfo, 0, extLength);
        return new MultiVswIndexInfo(deviceId, collectTime, queryStartTime, queryEndTime, offset, extInfo, length );
    }

    public boolean isMatched(String sDeviceId, long queryStartTime, long queryEndTime){
        if (null == sDeviceId  || "".equals(sDeviceId)){
            return true;
        }
        if (this.queryEndTime ==0 || this.queryStartTime ==0){
            return this.sDeviceId.equals(sDeviceId);
        }

        //TODO toBe checked
        return (this.sDeviceId.equals(sDeviceId))&&((queryStartTime<=this.queryStartTime)||(queryEndTime<=this.queryEndTime));
    }

    private static String toCString(byte [] buf ){
        StringBuffer sbuf = new StringBuffer(buf.length);
        for (int i = 0; i < buf.length; i ++){
            if (buf[i]!= 0 ){
                sbuf.append( (char)buf[i]);
            }else{
                break;
            }
        }

        return  sbuf.toString();
    }


    public String toString(){
        return  String.format( "VSW Index Info for Device \"%s\", S/E Time %s , %s", sDeviceId, new Date(this.queryStartTime), new Date(this.queryEndTime));
    }
}