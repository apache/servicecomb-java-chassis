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

package com.huawei.paas.foundation.common.utils;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.UserPrincipal;

import org.junit.Assert;
import org.junit.Test;

import mockit.Mock;
import mockit.MockUp;

public class TestFortifyUtils {

    @Test
    public void testFortifyUtils() throws IOException {
        Assert.assertNotEquals(null, FortifyUtils.getDefaultFileAttributes("test"));
        FortifyUtils.writeFile("test", (new String("testSomething").getBytes()));
        Assert.assertEquals(true, (FortifyUtils.isInSecureDir("test")));
        Assert.assertEquals(true, (FortifyUtils.isInSecureDir(new File("test").toPath())));
        Assert.assertEquals(true, FortifyUtils.isInSecureResult(new File("test").toPath()));
        Assert.assertEquals(true, FortifyUtils.isRegularFile("test"));
        Assert.assertEquals("test", FortifyUtils.getSecurityStr("test"));
        Assert.assertEquals("", FortifyUtils.getErrorMsg(null));
        Assert.assertEquals("", FortifyUtils.getErrorStack(null));
        Assert.assertEquals(null, FortifyUtils.getSecurityStr(null));

    }

    @Test
    public void testFilePerm() {
        Assert.assertEquals(10, (FilePerm.getDefaultAclPerm().size()));
        Assert.assertEquals(3, (FilePerm.getDefaultPosixPerm().size()));
        Assert.assertEquals(4, (FilePerm.getPosixPerm(400).size()));
    }

    @Test
    public void testGetErrorMsg() {

        Throwable e = new Throwable();

        FortifyUtils.getErrorMsg(e);

        assertNull(FortifyUtils.getErrorMsg(e));
    }

    @Test
    public void testGetDefaultFileAttributes() {

        String filePath = "/foundation-common/src/test/resources/config/test.1.properties";

        new MockUp<FortifyUtils>() {

            @Mock
            public boolean isPosix() {
                return true;
            }
        };

        FortifyUtils.getDefaultFileAttributes(filePath);
        Assert.assertNotEquals(null, FortifyUtils.getDefaultFileAttributes(filePath));

    }

    @Test
    public void testGetDefaultFileAttribute() {

        String filePath = "/foundation-common/src/test/resources/config/test.1.properties";

        new MockUp<FortifyUtils>() {

            @Mock
            public boolean isPosix() {
                return false;
            }
        };

        FortifyUtils.getDefaultFileAttributes(filePath);
        Assert.assertNotEquals(null, FortifyUtils.getDefaultFileAttributes(filePath));
    }

    @Test
    public void testIsInSecureDir() {

        Path file = new File("src/test/resources/config/test.1.properties").toPath();
        UserPrincipal user = null;
        int symlinkDepth = 5;
        FortifyUtils.isInSecureDir(file, user, symlinkDepth);
        Assert.assertNotEquals(false, FortifyUtils.isInSecureDir(file, user, symlinkDepth));

    }

    @Test
    public void testIsInSecureDirSymLink() {

        Path file = new File("src/test/resources/config/test.1.properties").toPath();
        UserPrincipal user = null;
        int symlinkDepth = 0;

        FortifyUtils.isInSecureDir(file, user, symlinkDepth);
        Assert.assertNotEquals(true, FortifyUtils.isInSecureDir(file, user, symlinkDepth));
    }

    @Test
    public void testIsInSecureResult() {

        Path file = new File("src/test/resources/config/test.1.properties").toPath();

        new MockUp<FortifyUtils>() {

            @SuppressWarnings("unused")
            public boolean isInSecureDir(Path file, UserPrincipal user, int symlinkDepth) {
                return false;
            }
        };
        FortifyUtils.isInSecureResult(file);
        Assert.assertNotEquals(false, FortifyUtils.isInSecureResult(file));

    }

    @Test
    public void testGetSecurityXmlDocumentFactory() {

        try {
            FortifyUtils.getSecurityXmlDocumentFactory();
            assertNotNull(FortifyUtils.getSecurityXmlDocumentFactory());
        } catch (Exception e) {
            /* Do not Worry */
            Assert.assertTrue(false);
        }

    }

    @Test
    public void testReadAttributes() {

        Path file = new File("src/test/resources/config/test.1.properties").toPath();

        new MockUp<FortifyUtils>() {

            @SuppressWarnings("unused")
            public boolean isInSecureDir(Path file) {
                return true;
            }
        };

        try {
            Files.readAttributes(file, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Assert.assertTrue(false);
        }

        FortifyUtils.isInSecureResult(file);
        Assert.assertNotEquals(false, FortifyUtils.isInSecureResult(file));

    }

    @Test
    public void testIsRegularFile() {

        String file = "abc";
        FortifyUtils.isRegularFile(file);
        Assert.assertNotEquals(true, FortifyUtils.isRegularFile(file));

    }

    @Test
    public void testGetErrorStack() {

        Throwable e = new Throwable();
        FortifyUtils.getErrorStack(e);
        Assert.assertNotEquals(true, FortifyUtils.getErrorStack(e));

    }

    @Test
    public void testGetErrorInfo() {

        Throwable e = new Throwable();
        FortifyUtils.getErrorInfo(e, true);
        Assert.assertNotEquals(true, FortifyUtils.getErrorInfo(e, true));

    }
}
