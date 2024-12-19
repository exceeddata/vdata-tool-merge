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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.exceeddata.sdk.vdata.app.LogUtils;

/**
 * 
 * Project: An demo application on using the SDK to merge vData files.
 * Author:  NX
 * Usage:   java -cp vdata-tool-merge.jar com.exceeddata.tool.vdata.merge.MergeFiles -i input_path -o output_path [...optional parameters]
 *
 * Note:    Files are assumed to have vin_xxxxxx.vsw format where vin is in the beginning of file name.
 */
public class MergeFiles {
    
    private static void printUsage() {
        System.out.println("java -cp vdata.jar com.exceeddata.tool.vdata.merge.MergeFiles -i inputPath -o outputPath [signalNames base64Encoded densifyNumRows]");
        System.out.println("     [-i|input <paths>]. Required. The input vdata file path(s). Multiple files are comma separated.");
        System.out.println("     [-o|output <path>]. Required. The output file path.");
        System.out.println("     [-h|help]. optional)");
        System.out.println("");
    }
    
    /**
     * The main entry method.
     * 
     * @param args the arguments
     * @throws Exception if exception occurs
     */
    public static void main(String[] args) throws Exception {
        
        LogUtils.setLogLevel();
        
        final Map<String, String> configs = new HashMap<>();
        if (ToolUtils.parseArguments(args, configs) < 0) {
            printUsage();
            return;
        }
        
        final String inputPath = ToolUtils.get(configs, "-i", "");
        final String outputPath = ToolUtils.get(configs, "-o", "");
        
        if (inputPath.length() == 0) {
            System.out.println("Error: input path parameter empty");
            return;
        }
        if (outputPath.length() == 0) {
            System.out.println("Error: output path parameter empty");
            return;
        }

        final List<File> inputFiles = new ArrayList<>();
        final String[] ps = inputPath.split(",");
        for (final String p : ps) {
            final String np = p.trim();
            if (np.isEmpty()) {
                continue;
            }

            final File fp = new File(np);
            if (!fp.exists()) {
                System.err.println("Error: input path not exist: " + fp);
                continue;
            }
            
            if (fp.isDirectory()) {
                for (File f : fp.listFiles()) {
                    if (ToolUtils.validateFile(f, false)) {
                        inputFiles.add(f);
                    }
                }
            } else if (ToolUtils.validateFile(fp, false)) {
                inputFiles.add(fp);
            }
        }
        
        if (inputFiles.size() == 0) {
            System.err.println("Error: no input files not found: " + inputPath);
            return;
        }
        
        long start = System.currentTimeMillis();
        
        merge(inputFiles, outputPath);
        
        long end = System.currentTimeMillis();
        System.out.println("Took " + (end - start) + " ms");
    }
    
    private static void merge(
            final List<File> inputFiles,
            final String outputPath) throws IOException {
        final MultiVDataBuilder mvb = new MultiVDataBuilder();
        int success = 0, failed = 0;
        
        for (final File inputFile : inputFiles) {
            final String filename = inputFile.getName().trim();
            final int underscore = filename.indexOf('_');
            if (underscore <= 0) {
                System.err.println("Warning: " + filename + " skipped, file name not in the vin_xxxxx.vsw format");
                continue;
            }
            
            try {
                final String vin = filename.substring(0, underscore);
                final String p = inputFile.getAbsolutePath().toString();
                final byte [] vswContent= Files.readAllBytes(FileSystems.getDefault().getPath(p));
                mvb.collectVsw( vin, System.currentTimeMillis(), vswContent);
                ++success;
            } catch (IOException e) {
                ++failed;
                System.err.println("Error: " + filename + " failed, " + e.getMessage());
            }
        }
        
        if (success > 0) {
            OutputStream out = null;
            try {
                out = new FileOutputStream(outputPath);
                out.write( mvb.getVswData());
                out.flush();
            } catch (IOException e) {
                throw e;
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException ex) {}
                }
            }
        }
        
        System.out.println("Merged " + success + " files, failed " + failed + " files.");
        System.out.println("------------------------------------------------");
    }
}
