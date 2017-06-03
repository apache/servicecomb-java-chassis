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
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.EnumSet;
import java.util.Set;

public final class FilePerm {
    private FilePerm() {
    }

    public static final int FILE_PERM_UREAD = 256;

    public static final int FILE_PERM_UWRITE = 128;

    public static final int FILE_PERM_UEXEC = 64;

    public static final int FILE_PERM_GREAD = 32;

    public static final int FILE_PERM_GWRITE = 16;

    public static final int FILE_PERM_GEXEC = 8;

    public static final int FILE_PERM_OREAD = 4;

    public static final int FILE_PERM_OWRITE = 2;

    public static final int FILE_PERM_OEXEC = 1;

    public static final int FILE_PERM_MASK = 511;

    private static AclEntryPermission[] permList = new AclEntryPermission[] {
            AclEntryPermission.READ_DATA,
            AclEntryPermission.READ_ATTRIBUTES,
            AclEntryPermission.READ_NAMED_ATTRS,
            AclEntryPermission.READ_ACL,
            AclEntryPermission.WRITE_DATA,
            AclEntryPermission.APPEND_DATA,
            AclEntryPermission.WRITE_ATTRIBUTES,
            AclEntryPermission.WRITE_NAMED_ATTRS,
            AclEntryPermission.WRITE_ACL,
            AclEntryPermission.SYNCHRONIZE
    };

    public static Set<PosixFilePermission> getDefaultPosixPerm() {
        return PosixFilePermissions.fromString("rw-r-----");
    }

    public static Set<PosixFilePermission> getPosixPerm(int perm) {
        StringBuilder permStr = new StringBuilder();

        permStr.append(uCanRead(perm) ? "r" : "-");
        permStr.append(uCanWrite(perm) ? "w" : "-");
        permStr.append(uCanExec(perm) ? "x" : "-");
        permStr.append(gCanRead(perm) ? "r" : "-");
        permStr.append(gCanWrite(perm) ? "w" : "-");
        permStr.append(gCanExec(perm) ? "x" : "-");
        permStr.append(oCanRead(perm) ? "r" : "-");
        permStr.append(oCanWrite(perm) ? "w" : "-");
        permStr.append(oCanExec(perm) ? "x" : "-");

        return PosixFilePermissions.fromString(permStr.toString());

    }

    public static Set<AclEntryPermission> getDefaultAclPerm() {

        Set<AclEntryPermission> perms = EnumSet.noneOf(AclEntryPermission.class);
        for (AclEntryPermission aclPerm : permList) {
            perms.add(aclPerm);
        }

        return perms;

    }

    public static boolean uCanRead(int perm) {
        return (FILE_PERM_UREAD & perm) > 0;
    }

    public static boolean uCanWrite(int perm) {
        return (FILE_PERM_UWRITE & perm) > 0;
    }

    public static boolean uCanExec(int perm) {
        return (FILE_PERM_UEXEC & perm) > 0;
    }

    public static boolean gCanRead(int perm) {
        return (FILE_PERM_GREAD & perm) > 0;
    }

    public static boolean gCanWrite(int perm) {
        return (FILE_PERM_GWRITE & perm) > 0;
    }

    public static boolean gCanExec(int perm) {
        return (FILE_PERM_GEXEC & perm) > 0;
    }

    public static boolean oCanRead(int perm) {
        return (FILE_PERM_GREAD & perm) > 0;
    }

    public static boolean oCanWrite(int perm) {
        return (FILE_PERM_GWRITE & perm) > 0;
    }

    public static boolean oCanExec(int perm) {
        return (FILE_PERM_GEXEC & perm) > 0;
    }

    public static void setFilePerm(File file, String perm) {
        if (filePermSupported()) {
            try {
                Set<PosixFilePermission> perms = PosixFilePermissions.fromString(perm);
                PosixFileAttributes attr = Files.readAttributes(file.toPath(), PosixFileAttributes.class);
                attr.permissions().clear();
                Files.setPosixFilePermissions(file.toPath(), perms);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public static boolean filePermSupported() {
        FileSystem system = FileSystems.getDefault();
        for (String name : system.supportedFileAttributeViews()) {
            // see javadoc for PosixFileAttributeView.
            if ("posix".equals(name)) {
                return true;
            }
        }
        return false;
    }
}
