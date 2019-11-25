/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicecomb.router.util;

/**
 * @Author GuoYl123
 * @Date 2019/10/16
 **/
public class VersionCompareUtil {
    /**
     * if first num is bigger then return a positive number
     *
     * @param version1
     * @param version2
     * @return
     */
    public static int compareVersion(String version1, String version2) {
        if (version1 == null || version2 == null) {
            throw new RuntimeException("version can not be null");
        }
        String[] versionArray1 = version1.split("\\.");
        String[] versionArray2 = version2.split("\\.");
        int idx = 0;
        int diff = 0;
        int minLength = Math.min(versionArray1.length, versionArray2.length);
        while (idx < minLength
                && versionArray1[idx].length() - versionArray2[idx].length() == 0
                && versionArray1[idx].compareTo(versionArray2[idx]) == 0) {
            ++idx;
        }
        idx = idx < minLength ? idx : idx - 1;
        if (versionArray1[idx].length() - versionArray2[idx].length() == 0) {
            diff = versionArray1[idx].compareTo(versionArray2[idx]);
        } else {
            diff = versionArray1[idx].length() - versionArray2[idx].length();
        }
        diff = (diff != 0) ? diff : versionArray1.length - versionArray2.length;
        return diff;
    }
}
