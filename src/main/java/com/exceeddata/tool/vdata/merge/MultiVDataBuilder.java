/*
 * Copyright (C) 2016-2025 Smart Software for Car Technologies Inc. and EXCEEDDATA
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
package com.exceeddata.tool.vdata.merge;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;

import com.exceeddata.sdk.vdata.binary.LittleEndianSeekableBytesReader;
import com.exceeddata.sdk.vdata.data.MultiVswIndexInfo;
import com.exceeddata.sdk.vdata.data.VDataMeta;
import com.exceeddata.sdk.vdata.data.VDataReader;
import com.exceeddata.sdk.vdata.util.LittleEndianBytesWriter;

public class MultiVDataBuilder {
    int formatVersion = 10;


    long sstime = 0L;
    long setime = 0L;
    int indexInfoSize = 0;

    int offset_vsw_block=0;
    byte[] extendedInfo= new byte[ 0];

    ArrayList<MultiVswIndexInfo> vsws = new ArrayList<>();

    /**
     * No additional information builder.
     */
    public MultiVDataBuilder ( ){

    }

    /**
     * More information for vsw file meta such as  algorithm information,trigger information can be added to
     * extended information in IndexBlock.
     * @param indexInfoSize
     */
    public MultiVDataBuilder (int indexInfoSize ){
        this.indexInfoSize = indexInfoSize;
    }


    public byte [] getMetaData() throws  IOException{
//        File Identifier CHAR 2 MD 也可以考虑用SD，以format flags来表示后续的内容为是多VSW合并文件
//        Format Version BYTE 1 版本号 = 10
//        Reserved BYTE 1 或者1
//        VSW Block Count INT32 4 VSW文件数量
//        VSW Index Info Size INT32 4 VSW 文件索引信息的长度，项目定制后此长度可变长
//        Storage Start Time (SST) INT64 8 存储信号的开始时间范围段起点，等同文件内第一个bucket的开始时间范围段起点 最早bucket时间
//        Storage End Time (SET) INT64 8 存储信号的结束时间范围段终点，等同文件内最后一个bucket的结束时间范围段终点 最晚bucket的storage end start
//        Extended Length (L) UINT16 2 扩展信息的长度
//        Reserved BYTE Array 2 预留
//        Extended Info UINT8 Array L  事件信息（注：项目定制信息可加在此）
        final LittleEndianBytesWriter writer = new LittleEndianBytesWriter();
        int blocksCount = vsws.size();
        final int extendedLength = extendedInfo == null ? 0 : extendedInfo.length;
        if (extendedLength > Short.MAX_VALUE) {
            throw new IOException("FORMAT_VDATA_EXTENDED_INFO_TOO_LONG: " + extendedLength + " bytes");
        }
        if (blocksCount == 0) {
            throw new IOException("FORMAT_VDATA_NO_BUCKETS_GENERATED");
        }
        if (sstime <= 0) {
            throw new IOException("FORMAT_VDATA_STORAGE_START_TIME_INVALID: " + sstime);
        }
        if (setime <= 0) {
            throw new IOException("FORMAT_VDATA_STORAGE_END_TIME_INVALID: " + setime);
        }

        final int hlen = extendedLength + 32;
        final byte[] _head = new byte[hlen];
        _head[0] = 'M'; //identifier
        _head[1] = 'D'; //identifier
        _head[2] = (byte) formatVersion; //high 4 bit encryption, low 4 bit compression
        _head[3] = 0;

        writer.writeINT32(_head, 4, blocksCount);
        writer.writeINT32(_head, 8, indexInfoSize);
        writer.writeINT64(_head, 12, sstime);
        writer.writeINT64(_head, 20, setime);

        int offset = 28;

        writer.writeUINT16(_head, offset, extendedLength);
        writer.writeUINT16(_head, offset+2, 0);
        if (extendedLength > 0) {
            writer.writeBytes(_head, offset+4, extendedLength, extendedInfo);
        }
        return _head;
    }

    public void collectVsw(String vin,  long collectTime, byte [] data)throws IOException {
        collectVsw(vin, collectTime, data , new byte[indexInfoSize]);
    }

    public void collectVsw( String vin,  long collectTime,String filename)throws IOException{
        collectVsw(vin, collectTime, filename , new byte[indexInfoSize]);
    }

    public void collectVsw(String vin,  long collectTime,String filename, byte [] vswExtendedInfo) throws IOException {
        byte [] content = Files.readAllBytes(new File(filename).toPath());
        collectVsw(vin, collectTime, content, vswExtendedInfo);
    }

    public void collectVsw(String vin,  long collectTime, byte [] data, byte [] vswExtendedInfo) throws IOException{
        if (null == vswExtendedInfo){
            vswExtendedInfo = new byte[0];
        }
        VDataMeta meta = VDataReader.getMeta( new LittleEndianSeekableBytesReader(data));
        if (vswExtendedInfo.length > indexInfoSize-1) {
            //TODO warn...
        }
        // check vswExtendedInfo Length and align to fixed length.
        byte [] extInfo = new byte[ indexInfoSize];//leave last byte zero;
        if (indexInfoSize > 0) {
            System.arraycopy(vswExtendedInfo, 0, extInfo, 0, vswExtendedInfo.length > indexInfoSize - 1 ? indexInfoSize - 1 : vswExtendedInfo.length);
        }

        if ( sstime ==0 || meta.getQueryStartTime() < sstime ){
            sstime = meta.getStorageStartTime();
        }

        if (setime == 0 || meta.getStorageEndTime() > setime) {
            setime = meta.getStorageEndTime();
        }

        MultiVswIndexInfo info = new MultiVswIndexInfo(vin, collectTime, meta.getQueryStartTime(), meta.getQueryEndTime(), offset_vsw_block, extInfo, data);

        offset_vsw_block += data.length;
        vsws.add( info);
    }

    public byte [] getVswData () throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        write(baos);

        return  baos.toByteArray();
    }

    public void write(OutputStream out)throws IOException{
        if (vsws.size() == 0){
            throw new IOException( "No VSW file to merge");
        }
        out.write(getMetaData());
        for (int i =0; i < vsws.size() ; i ++){
            out.write(vsws.get(i).encode());
        }
        for (int i =0; i < vsws.size() ; i ++){
            out.write(vsws.get(i).getVsw());
        }
        out.flush();
    }
}




