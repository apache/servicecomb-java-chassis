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
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

public class TestFileUtils {
    @Test
    public void testFileClassRootPath() {
        String path = FileUtils.findRootPath(FileUtils.class);
        File file = new File(path, FileUtils.class.getName().replace('.', '/') + ".class");
        Assert.assertTrue(file.exists());
    }

    @Test
    public void testJarClassRootPath() {
        String path = FileUtils.findRootPath(String.class);
        File file = new File(path);
        Assert.assertTrue(file.exists());
    }

    @Test
    public void testFileUrlRootPath() {
        String suffix = "config/config.inc.xml";
        URL url = Thread.currentThread().getContextClassLoader().getResource(suffix);
        String path = FileUtils.findRootPath(url, suffix);
        File file = new File(path, suffix);
        Assert.assertTrue(file.exists());

        String invalidSuffix = suffix + "x";
        try {
            FileUtils.findRootPath(url, invalidSuffix);
            Assert.fail("must throw exception");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(
                    String.format("url '%s' is not end with '%s'.", url.getPath(), invalidSuffix),
                    e.getMessage());
        }
    }

    @Test
    public void testJarUrlRootPath() {
        String suffix = "META-INF/maven/commons-codec/commons-codec/pom.xml";
        URL url = Thread.currentThread().getContextClassLoader().getResource(suffix);
        String path = FileUtils.findRootPath(url, suffix);
        File file = new File(path);
        Assert.assertTrue(file.exists());
    }
}
