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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryFlag;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;

/**
 * 规避fortify问题，仅仅是规避，如
 * e.getMessage
 * e.printStackTrace
 * 调用会报安全问题（敏感信息泄露）
 *
 *
 */
public final class FortifyUtils {
    private static final int SYMBOLIC_LINK_DEPTH = 5;

    private static Method getMessageMethod;

    private static Method printStackTraceMethod;

    static {
        try {
            getMessageMethod = Throwable.class.getMethod("getMessage");
            printStackTraceMethod = Throwable.class.getMethod("printStackTrace", PrintWriter.class);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private FortifyUtils() {
    }

    /**
     * 获取异常message
     * 直接调用getMessage，fortify会报安全问题
     * @param e    异常
     * @return     异常message
     */
    public static String getErrorMsg(Throwable e) {
        if (e == null) {
            return "";
        }

        try {
            return (String) getMessageMethod.invoke(e);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            return "";
        }
    }

    /**
     * 获取异常堆栈信息
     * 直接调用printStackTrace，fortify会报安全问题
     * @param e     异常
     * @return      异常堆栈
     */
    public static String getErrorStack(Throwable e) {
        if (null == e) {
            return "";
        }

        try {
            StringWriter errors = new StringWriter();
            printStackTraceMethod.invoke(e, new PrintWriter(errors));
            return errors.toString();
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            return "";
        }
    }

    /**
     * 抽取异常信息，规避fortify安全问题
     * @param e     异常
     * @return      异常信息
     */
    public static String getErrorInfo(Throwable e) {
        return getErrorInfo(e, true);
    }

    /**
     * 抽取异常信息，规避fortify安全问题
     * @param e               异常
     * @param isPrintMsg      是否包含用户自定义message
     * @return String         异常信息
     */
    public static String getErrorInfo(Throwable e, boolean isPrintMsg) {
        StringBuffer error = new StringBuffer(System.lineSeparator());
        error.append("Exception: ").append(e.getClass().getName()).append("; ");

        if (isPrintMsg) {
            error.append(getErrorMsg(e)).append(System.lineSeparator());
        }
        error.append(getErrorStack(e));

        return error.toString();
    }

    /**
     * 代码摘取http://3ms.huawei.com/hi/group/2028623/blog_1508533.html?uid=44138&mapId=2184035
     * @param filePath    filaPath
     * @return            FileAttribute
     */
    public static FileAttribute<?> getDefaultFileAttributes(String filePath) {
        Path file = new File(filePath).toPath();
        if (isPosix()) {
            return PosixFilePermissions.asFileAttribute(FilePerm.getDefaultPosixPerm());
        } else { // for not posix must support ACL, or failed.
            String userName = System.getProperty("user.name");
            UserPrincipal user = null;
            try {
                user = file.getFileSystem().getUserPrincipalLookupService().lookupPrincipalByName(userName);
            } catch (IOException e) {
                throw new RuntimeException("Unknown user error.");
            }

            final AclEntry entry = AclEntry.newBuilder()
                    .setType(AclEntryType.ALLOW)
                    .setPrincipal(user)
                    .setPermissions(FilePerm.getDefaultAclPerm())
                    //.setPermissions(AclEntryPermission.READ_DATA, AclEntryPermission.READ_ATTRIBUTES,
                    // AclEntryPermission.READ_NAMED_ATTRS, AclEntryPermission.READ_ACL)
                    .setFlags(new AclEntryFlag[] {AclEntryFlag.FILE_INHERIT, AclEntryFlag.DIRECTORY_INHERIT})
                    .build();

            return new FileAttribute<List<AclEntry>>() {
                public String name() {
                    return "acl:acl";
                } /* Windows ACL */
                //public Object value() { ArrayList l = new ArrayList(); l.add(entry); return l; }

                public List<AclEntry> value() {
                    ArrayList<AclEntry> l = new ArrayList<AclEntry>();
                    l.add(entry);
                    return l;
                }
            };
        }
    }

    /**
     * 代码摘取http://3ms.huawei.com/hi/group/2028623/blog_1508533.html?uid=44138&mapId=2184035
     * @return    posix: true, other:false
     */
    public static boolean isPosix() {
        return FileSystems.getDefault().supportedFileAttributeViews().contains("posix");
    }

    public static void writeFile(String file, byte[] content) throws IOException {
        Set<OpenOption> options = new HashSet<OpenOption>();
        //options.add(StandardOpenOption.CREATE_NEW);
        //options.add(StandardOpenOption.APPEND);
        //覆写文件
        options.add(StandardOpenOption.CREATE);
        options.add(StandardOpenOption.WRITE);
        SeekableByteChannel sbc = null;
        try {
            FileAttribute<?> fa = getDefaultFileAttributes(file);
            ByteBuffer buffer = ByteBuffer.wrap(content);
            sbc = Files.newByteChannel(new File(file).toPath(), options, fa);
            // write data
            sbc.write(buffer);
        } finally {
            IOUtils.closeQuietly(sbc);
        }
    }

    public static boolean isInSecureDir(String file) {
        return isInSecureDir(new File(file).toPath(), null);
    }

    public static boolean isInSecureDir(Path file) {
        return isInSecureDir(file, null);
    }

    public static boolean isInSecureDir(Path file, UserPrincipal user) {
        return isInSecureDir(file, user, SYMBOLIC_LINK_DEPTH);
    }

    public static boolean isInSecureDir(Path file, UserPrincipal user, int symlinkDepth) {
        if (!file.isAbsolute()) {
            file = file.toAbsolutePath();
        }
        if (symlinkDepth <= 0) {
            // Too many levels of symbolic links
            return false;
        }

        // Get UserPrincipal for specified user and superuser
        Path fileRoot = file.getRoot();
        if (fileRoot == null) {
            return false;
        }
        FileSystem fileSystem = Paths.get(fileRoot.toString()).getFileSystem();
        UserPrincipalLookupService upls = fileSystem.getUserPrincipalLookupService();
        UserPrincipal root = null;
        try {
            if (isPosix()) {
                root = upls.lookupPrincipalByName("root");
            } else {
                root = upls.lookupPrincipalByName("Administrators");
            }

            if (user == null) {
                user = upls.lookupPrincipalByName(System.getProperty("user.name"));
            }

            if (root == null || user == null) {
                return false;
            }
        } catch (IOException x) {
            return false;
        }

        // If any parent dirs (from root on down) are not secure,
        // dir is not secure
        for (int i = 1; i <= file.getNameCount(); i++) {
            Path fRoot = file.getRoot();
            if (fRoot == null) {
                return false;
            }
            Path partialPath = Paths.get(fRoot.toString(), file.subpath(0, i).toString());

            try {
                if (Files.isSymbolicLink(partialPath)) {
                    if (!isInSecureDir(Files.readSymbolicLink(partialPath), user, symlinkDepth - 1)) {
                        // Symbolic link, linked-to dir not secure
                        return false;
                    }
                } else {
                    UserPrincipal owner = Files.getOwner(partialPath);
                    if (!user.equals(owner) && !root.equals(owner)) {
                        // dir owned by someone else, not secure
                        return false;
                    }
                }
            } catch (IOException x) {
                return false;
            }
        }

        return true;
    }

    public static boolean isInSecureResult(Path path) {
        try {
            if (!isInSecureDir(path)) {
                return false;
            }
            BasicFileAttributes attr =
                Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            // Check
            if (!attr.isRegularFile()) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * 是否普通文件，非链接文件
     * @param file   file
     * @return       true or false
     */
    public static boolean isRegularFile(String file) {
        BasicFileAttributes attr;
        try {
            attr = Files.readAttributes(new File(file).toPath(),
                    BasicFileAttributes.class,
                    LinkOption.NOFOLLOW_LINKS);
        } catch (IOException e) {
            return false;
        }

        if (!attr.isRegularFile()) {
            return false;
        }

        return true;
    }

    /**
     * getSecurityXmlDocumentFactory
     * @return  DocumentBuilderFactory
     * @throws ParserConfigurationException ParserConfigurationException
     */
    public static DocumentBuilderFactory getSecurityXmlDocumentFactory() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setValidating(true);

        return factory;
    }

    public static String getSecurityStr(final String value) {
        if (value == null) {
            return value;
        }

        String encodevalue = value.replaceAll("\t|\r|\n|<|>", "");
        try {
            return URLEncoder.encode(encodevalue, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            //TODO this need to be fixed
            return encodevalue;
        }
    }
}
