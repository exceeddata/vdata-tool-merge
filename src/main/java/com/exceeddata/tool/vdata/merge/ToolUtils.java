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
import java.io.IOException;
import java.util.Map;

/**
 * Tool utility class.
 *
 */
final class ToolUtils {
    private ToolUtils() {}
    
    /**
     * Parse a long value from string.
     * 
     * @param str the string
     * @param defaultValue the default value
     * @return long
     */
    static long parseLong(final String str, final long defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        
        final String s = str.trim();
        if (s.length() == 0) {
            return defaultValue;
        }
        
        try {
            return Math.round(Double.parseDouble(s));
        } catch(NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Parse an array of command line arguments to a configuration map.
     * 
     * @param args command line arguments
     * @param configs the configuration map
     * @return zero if successful, otherwise unsuccessful
     * @throws IOException if occurs
     */
    static int parseArguments (final String[] args,  final Map<String, String> configs) throws IOException {
        if (args == null || args.length<1) {
            throw new IOException ("TOOL_VDATA_PARAMETER_MISSING");
        }

        for (int i = 0; i < args.length; ++i) {
            String key = args[i].trim();
            if (key.length() == 0) {
                return 0;
            } else if (key.charAt(0)!='-' || key.length()<2) {
                throw new IOException ("TOOL_VDATA_PARAMETERS_INVALID");
            } else if (key.equalsIgnoreCase("-h")) {
                return -1;
            }
            
            if (++i < args.length) {
                String val = args[i].trim();
                if (key.length() == 0) {
                    throw new IOException ("TOOL_VDATA_PARAMETERS_INVALID: " + key); 
                }
                configs.put(key, val);
            } else {
                throw new IOException ("TOOL_VDATA_PARAMETERS_INVALID: " + key);
            }
        }
        
        return 0;
    }

    /**
     * Get a string value from the parameter map.
     * 
     * @param params the map
     * @param key the key to lookup
     * @param defaultValue the default value if not found
     * @return string
     */
    static String get(final Map<String, String> params, final String key, final String defaultValue) {
        if (params.containsKey(key)) {
            final String v = params.remove(key).trim();
            if (v.length() > 0) {
                return v;
            }
        }
        return defaultValue;
    }

    /**
     * Validate whether a file extension is a supported vsw extension.
     * 
     * @param file the file
     * @param base64Encoded true if it is base 64 encoded
     * @return true if the file is a supported vsw extension
     */
    static boolean validateFile(final File file, final boolean base64Encoded) {
        if (file.isHidden() || file.isDirectory()) {
            return false;
        }
        final String name = file.getName().toLowerCase();
        if (name.startsWith(".") || name.startsWith("_")) {
            return false;
        }
        if (name.endsWith(".vsw") || name.endsWith(".stf") || name.endsWith(".mmf")|| name.endsWith(".sdt")) {
            return true;
        } else if (base64Encoded && (name.endsWith(".kfk") || name.endsWith(".txt") || name.endsWith(".json"))) {
            return true;
        }
        return false;
    }
}
