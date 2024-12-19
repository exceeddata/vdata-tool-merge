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
package com.exceeddata.sdk.merge;

import com.exceeddata.sdk.merge.build.MultiVDataBuilder;
import com.exceeddata.sdk.merge.data.MVDataReaderFactory;
import com.exceeddata.sdk.vdata.data.VDataReader;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class MultiVDataFileTest {

    /**
     * Example for combine all vsw file in specific directory , repeat "Count"  for vins.
     * @throws IOException
     */
//    @Test
    public void combineTest( )throws IOException {
        String inputPath="";
        String outFilename="/tmp/out.mvsw";
        String vinPattern ="VINTEST000%05d";
        int count =10;

        MultiVDataBuilder mvb = new MultiVDataBuilder();

        File dir = new File(inputPath);
        if (dir.isFile()){
            throw new IOException("Input should be a directory");
        }

        for (String vsw:dir.list() ){
            byte [] vswContent= Files.readAllBytes(FileSystems.getDefault().getPath(inputPath + File.separator+ vsw));

            for (int i =0 ; i< count ; i ++){
                mvb.collectVsw( String.format(vinPattern, i ), System.currentTimeMillis(), vswContent);
            }
        }

        OutputStream out = new FileOutputStream( outFilename);
        out.write( mvb.getVswData());
        out.flush();
        out.close();
    }

//    @Test
    public void readMultiTest (){
        try {

            MVDataReaderFactory factory = new MVDataReaderFactory();

            factory.setData(Files.readAllBytes(FileSystems.getDefault().getPath("/tmp/out.mvsw")));
            List<String> s = new ArrayList<>();

            try {
                Iterator<Map.Entry<String, VDataReader> > it=factory.openMultipleVswFormats();
                while (it.hasNext()){
                    Map.Entry<String, VDataReader> entry = it.next();

                    Object[][] obs = entry.getValue().df().objects();
                    System.out.println( "Decoded vin " +entry.getKey() +"  rows "+ obs.length);
                    assertTrue( obs.length >0);
                }
            }catch ( Exception e){
                e.printStackTrace();
            }

        }catch ( Exception e){
            e.printStackTrace();
            assertTrue(false);
        }
    }
}
