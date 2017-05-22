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

package io.servicecomb.foundation.ssl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 和应用相关的信息，方便定制使用。 目前主要包含密码解密、证书路径、IP和Port等内容。
 * @author  
 */
public abstract class SSLCustom {
    private static final Logger LOG = LoggerFactory.getLogger(SSLCustom.class);

    /**
     * 构造默认的SSLCustom，不对配置值进行转换.
     * @return 默认的SSLCustom
     */
    public static SSLCustom defaultSSLCustom() {
        final SSLCustom custom = new SSLCustom() {
            @Override
            public char[] decode(char[] encrypted) {
                return encrypted;
            }

            @Override
            public String getFullPath(String filename) {
                return filename;
            }
        };
        return custom;
    }

    public static SSLCustom createSSLCustom(String name) {
        try {
            if (name != null && !name.isEmpty()) {
                return (SSLCustom) Class.forName(name).newInstance();
            }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            LOG.warn("init SSLCustom class failed, name is " + name);
        }
        return defaultSSLCustom();
    }

    /**
     * 将SSLOption中的密文解密为明文。
     * @param encrypted
     *            密文
     * @return 明文
     */
    public abstract char[] decode(char[] encrypted);

    /**
     * 将SSLOption配置的证书名称转换为绝对路径, 包括身份证书、信任证书、吊销证书、白名单文件等涉及文件路径的内容
     * @param filename
     *            证书名称。
     * @return 证书的绝对路径。
     */
    public abstract String getFullPath(String filename);

    /**
     * 获取host名称，用于CN检查。 一般可以通过SSLSession获取到peerHost，但是一些实现获取到null.
     * @return host名称
     */
    public String getHost() {
        return null;
    }
}
