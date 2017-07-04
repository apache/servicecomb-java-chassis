/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.servicecomb.foundation.common.utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;

public final class FileUtils {
    private FileUtils() {

    }

    // result is path of a directory or a jar
    // eg:
    // io.servicecomb.foundation.common.utils.ResourceUtils -> ....../target/classes
    // org.apache.commons.codec.binary.StringUtils -> ....../commons-codec/commons-codec/1.9/commons-codec-1.9.jar
    public static String findRootPath(Class<?> cls) {
        String resourceName =
            "/" + ClassUtils.convertClassNameToResourcePath(cls.getName()) + ClassUtils.CLASS_FILE_SUFFIX;
        URL url = cls.getResource(resourceName);
        return findRootPath(url, resourceName);
    }

    // ....../target/classes/config/conf.xml -> ....../target/classes/ 
    public static String findRootPath(URL url, String suffix) {
        String path = url.getPath();
        try {
            // convert 'a%20b' to 'a b'
            path = URLDecoder.decode(path, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Failed to decode url, path=" + path, e);
        }
        if (!path.endsWith(suffix)) {
            throw new IllegalArgumentException(String.format("url '%s' is not end with '%s'.", path, suffix));
        }

        if (ResourceUtils.isJarURL(url)) {
            // protocol:/....jar!....suffix
            int idx = path.indexOf('!');
            path = path.substring(url.getProtocol().length() + 2, idx);
        } else {
            path = path.substring(0, path.length() - suffix.length());
        }

        File file = new File(path);
        return file.getPath();
    }
}
