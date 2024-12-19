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


import com.exceeddata.sdk.vdata.binary.BinarySeekableReader;
import com.exceeddata.sdk.vdata.binary.LittleEndianSeekableBytesReader;
import com.exceeddata.sdk.vdata.data.VDataReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MVDataReaderFactory  {
    private byte[][] datas = null;
    private List<BinarySeekableReader> readers = null;

    public void setData(byte [] data){
        datas = new byte[1][];
        datas[0]= data;
    }
    public Iterator<Map.Entry<String, VDataReader>> openMultipleVswFormats()throws IOException {
        BinarySeekableReader reader =null;
        if (readers != null && readers.size() != 1) {
            throw  new IOException( "Only one file support for MultiVswFormat decode.");
        }else if (readers != null){
            reader = readers.get(0);
        }

        if (datas != null && datas.length !=1) {
            throw  new IOException( "Only one byte array support for MultiVswFormat decode.");
        }else if (datas!=null){
            reader= new LittleEndianSeekableBytesReader(datas[0]);
        }
        MultiVDataFileReader mvfr = new MultiVDataFileReader( reader);
        mvfr.initialize();

        final  Iterator<MultiVswIndexInfo> rit = mvfr.getIterator();
        return new Iterator<Map.Entry<String, VDataReader>>() {
            @Override
            public boolean hasNext() {
                return rit.hasNext();
            }

            @Override
            public Map.Entry<String, VDataReader> next() {
                try {
                    MultiVswIndexInfo entry = rit.next();
                    final String deviceid = entry.getDeviceIdStr();
                    List<BinarySeekableReader> readers = new ArrayList<>();
                    readers.add(new LittleEndianSeekableBytesReader(entry.getVsw()));
                    //TODO add more parameters
//                    final VDataReader reader = new VDataReader(readers, signals, insensitiveCase, applyFormula, queryFilter, queryStartTime, queryEndTime,
//                            readLivingData, columnExpandMode, signalQueueMode, signalDecoders, keyIdMap);
                    final VDataReader reader = new VDataReader(readers, null);
                    return new Map.Entry<String, VDataReader>() {
                        @Override
                        public String getKey() {
                            return deviceid;
                        }

                        @Override
                        public VDataReader getValue() {
                            return reader;
                        }

                        @Override
                        public VDataReader setValue(VDataReader value) {
                            return null;
                        }
                    };
                }catch ( Exception e ){
                    throw new RuntimeException("Exception while open vdata reader" +e);
                }
            }
        };
    }
}
