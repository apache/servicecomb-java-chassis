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

package org.apache.servicecomb.foundation.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CRL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Set;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509ExtendedTrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 扩展TurstManager
 *
 */
public class TrustManagerExt extends X509ExtendedTrustManager {
  private static final Logger LOG = LoggerFactory.getLogger(TrustManagerExt.class);

  private static final int WHITE_SIZE = 1024;

  private X509ExtendedTrustManager trustManager;

  private SSLOption option;

  private SSLCustom custom;

  public TrustManagerExt(X509ExtendedTrustManager manager, SSLOption option,
      SSLCustom custom) {
    this.trustManager = manager;
    this.option = option;
    this.custom = custom;
  }

  @Override
  public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    if (!option.isAuthPeer()) {
      return;
    }

    checkTrustedCustom(chain, null);
    trustManager.checkClientTrusted(chain, authType);
  }

  @Override
  public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    if (!option.isAuthPeer()) {
      return;
    }

    checkTrustedCustom(chain, null);
    trustManager.checkServerTrusted(chain, authType);
  }

  @Override
  public X509Certificate[] getAcceptedIssuers() {
    return trustManager.getAcceptedIssuers();
  }

  @Override
  public void checkClientTrusted(X509Certificate[] chain, String authType,
      Socket socket) throws CertificateException {
    if (!option.isAuthPeer()) {
      return;
    }

    String ip = null;
    if (socket != null && socket.isConnected()
        && socket instanceof SSLSocket) {
      InetAddress inetAddress = socket.getInetAddress();
      if (inetAddress != null) {
        ip = inetAddress.getHostAddress();
      }
    }
    checkTrustedCustom(chain, ip);
    trustManager.checkClientTrusted(chain, authType, socket);
  }

  @Override
  public void checkClientTrusted(X509Certificate[] chain, String authType,
      SSLEngine engine) throws CertificateException {
    if (!option.isAuthPeer()) {
      return;
    }

    String ip = null;
    if (engine != null) {
      SSLSession session = engine.getHandshakeSession();
      ip = session.getPeerHost();
    }
    checkTrustedCustom(chain, ip);
    trustManager.checkClientTrusted(chain, authType, engine);
  }

  @Override
  public void checkServerTrusted(X509Certificate[] chain, String authType,
      Socket socket) throws CertificateException {
    if (!option.isAuthPeer()) {
      return;
    }

    String ip = null;
    if (socket != null && socket.isConnected()
        && socket instanceof SSLSocket) {
      InetAddress inetAddress = socket.getInetAddress();
      if (inetAddress != null) {
        ip = inetAddress.getHostAddress();
      }
    }
    checkTrustedCustom(chain, ip);
    trustManager.checkServerTrusted(chain, authType, socket);
  }

  @Override
  public void checkServerTrusted(X509Certificate[] chain, String authType,
      SSLEngine engine) throws CertificateException {
    if (!option.isAuthPeer()) {
      return;
    }

    String ip = null;
    if (engine != null) {
      SSLSession session = engine.getHandshakeSession();
      ip = session.getPeerHost();
    }
    checkTrustedCustom(chain, ip);
    trustManager.checkServerTrusted(chain, authType, engine);
  }

  private void checkTrustedCustom(X509Certificate[] chain, String ip) throws CertificateException {
    checkCNHost(chain, ip);
    checkCNWhite(chain);
    checkCRL(chain);
  }

  // ? : learn java default / apache CN check
  private void checkCNHost(X509Certificate[] chain, String ip) throws CertificateException {
    if (option.isCheckCNHost()) {
      X509Certificate owner = CertificateUtil.findOwner(chain);
      Set<String> cns = CertificateUtil.getCN(owner);
      String ipTmp = ip == null ? custom.getHost() : ip;
      // 从本机来的请求， 只要CN与本机的任何一个IP地址匹配即可
      if ("127.0.0.1".equals(ipTmp)) {
        try {
          Enumeration<NetworkInterface> interfaces =
              NetworkInterface.getNetworkInterfaces();
          if (interfaces != null) {
            while (interfaces.hasMoreElements()) {
              NetworkInterface nif = interfaces.nextElement();
              Enumeration<InetAddress> ias = nif.getInetAddresses();
              while (ias.hasMoreElements()) {
                InetAddress ia = ias.nextElement();
                String local = ia.getHostAddress();
                if (cnValid(cns, local)) {
                  return;
                }
              }
            }
          }
        } catch (SocketException e) {
          throw new CertificateException("Get local adrress fail.");
        }
      } else if (cnValid(cns, ipTmp)) {
        return;
      }
      LOG.error("CN does not match IP: e=" + cns.toString()
          + ",t=" + ip);
      throw new CertificateException("CN does not match IP: e=" + cns.toString()
          + ",t=" + ip);
    }
  }

  private boolean cnValid(Set<String> certsCN, String srcCN) {
    for (String cert : certsCN) {
      if (cert.equals(srcCN)) {
        return true;
      }
    }
    return false;
  }

  private void checkCNWhite(X509Certificate[] chain) throws CertificateException {
    if (option.isCheckCNWhite()) {
      FileInputStream fis = null;
      InputStreamReader reader = null;
      try {
        String white = option.getCheckCNWhiteFile();
        white = custom.getFullPath(white);
        fis = new FileInputStream(white);
        reader = new InputStreamReader(fis, StandardCharsets.UTF_8);
        char[] buffer = new char[WHITE_SIZE];
        int len = reader.read(buffer);
        String[] cns = new String(buffer, 0, len).split("\\s+");
        X509Certificate owner = CertificateUtil.findOwner(chain);
        Set<String> certCN = CertificateUtil.getCN(owner);
        for (String c : cns) {
          if (cnValid(certCN, c)) {
            return;
          }
        }
      } catch (FileNotFoundException e) {
        throw new CertificateException(
            "CN does not match white. no white file.");
      } catch (IOException e) {
        throw new CertificateException(
            "CN does not match white. can not read file.");
      } finally {
        try {
          if (reader != null) {
            reader.close();
          }
        } catch (IOException e) {
          ignore();
        }
        try {
          if (fis != null) {
            fis.close();
          }
        } catch (IOException e) {
          ignore();
        }
      }

      LOG.error("CN does not match white.");
      throw new CertificateException("CN does not match white.");
    }
  }

  private void checkCRL(X509Certificate[] chain) throws CertificateException {
    String crl = option.getCrl();
    crl = custom.getFullPath(crl);
    File file = new File(crl);
    if (!file.exists()) {
      return;
    }

    CRL[] crls = KeyStoreUtil.createCRL(crl);
    X509Certificate owner = CertificateUtil.findOwner(chain);
    for (CRL c : crls) {
      if (c.isRevoked(owner)) {
        LOG.error("certificate revoked");
        throw new CertificateException("certificate revoked");
      }
    }
  }

  private static void ignore() {
  }
}
