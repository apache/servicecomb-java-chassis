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

package com.huawei.paas.foundation.ssl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Collection;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * 读取证书的一些公共方法。
 * @author  
 */
public final class KeyStoreUtil {
    private KeyStoreUtil() {

    }

    /**
     * 读取证书文件
     * @param storename
     *            证书文件
     * @param storetype
     *            证书文件类型
     * @param storevalue
     *            证书文件密码
     * @return 证书文件
     */
    public static KeyStore createKeyStore(String storename, String storetype,
            char[] storevalue) {
        InputStream is = null;
        try {
            KeyStore keystore = KeyStore.getInstance(storetype);
            is = new FileInputStream(storename);
            keystore.load(is, storevalue);
            return keystore;
        } catch (Exception e) {
            throw new IllegalArgumentException("Bad key store or value."
                    + e.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    ignore();
                }
            }
        }
    }

    /**
     * 读取吊销证书列表。
     * @param crlfile 吊销证书名称
     * @return 吊销列表
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static CRL[] createCRL(String crlfile) {
        InputStream is = null;
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            is = new FileInputStream(crlfile);
            Collection c = cf.generateCRLs(is);
            CRL[] crls = (CRL[]) c.toArray(new CRL[c.size()]);
            return crls;
        } catch (CertificateException e) {
            throw new IllegalArgumentException("bad cert file.");
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("crl file not found.");
        } catch (CRLException e) {
            throw new IllegalArgumentException("bad crl file.");
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    ignore();
                }
            }
        }
    }

    /**
     * 创建KeyManager
     * @param keystore
     *            证书对象
     * @param keyvalue
     *            证书私钥密码
     * @return KeyManager
     */
    public static KeyManager[] createKeyManagers(final KeyStore keystore,
            char[] keyvalue) {
        try {
            KeyManagerFactory kmfactory =
                KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmfactory.init(keystore, keyvalue);
            return kmfactory.getKeyManagers();
        } catch (Exception e) {
            throw new IllegalArgumentException("Bad key store."
                    + e.getMessage());
        }
    }

    /**
     * 创建TrustManager
     * @param keystore
     *            证书对象
     * @return TrustManager
     */
    public static TrustManager[] createTrustManagers(final KeyStore keystore) {
        try {
            TrustManagerFactory tmfactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmfactory.init(keystore);
            TrustManager[] trustmanagers = tmfactory.getTrustManagers();
            return trustmanagers;
        } catch (Exception e) {
            throw new IllegalArgumentException("Bad trust store."
                    + e.getMessage());
        }
    }

    private static void ignore() {
    };
}
