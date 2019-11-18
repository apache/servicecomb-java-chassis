package org.apache.servicecomb.router.util;

/**
 * @Author GuoYl123
 * @Date 2019/10/16
 **/
public class VersionCompareUtil {
    /**
     * 前者大则返回一个正数
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
