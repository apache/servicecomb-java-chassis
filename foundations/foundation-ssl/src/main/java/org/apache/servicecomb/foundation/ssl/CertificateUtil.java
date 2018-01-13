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

import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 证书处理的功能方法
 *
 */
public final class CertificateUtil {
  private static final int SUBALTNAME_DNSNAME = 2;

  private static final int SUBALTNAME_IPADDRESS = 7;

  private CertificateUtil() {

  }

  /**
   * 将证书链进行排序。颁发机构的证书排在前面，所有者排在后面。如：rootCA > subCA > owner。
   * <B>注意：</B>传入的证书必须是“一条完整证书链"。
   * @param cerChain
   *            将要排序的证书链。
   * @return 排序后的证书链。
   */
  private static X509Certificate[] sort(X509Certificate[] cerChain) {
    X509Certificate[] chain = new X509Certificate[cerChain.length];
    X509Certificate root = findRootCA(cerChain);
    chain[0] = root;

    for (int i = 1; i < chain.length; i++) {
      X509Certificate parent = chain[i - 1];
      for (X509Certificate child : cerChain) {
        String parentDN = parent.getSubjectX500Principal().getName();
        String childDN = child.getSubjectX500Principal().getName();
        if (parentDN.equals(childDN)) {
          continue;
        }

        String childIssuerDN = child.getIssuerX500Principal().getName();
        if (parentDN.equals(childIssuerDN)) {
          chain[i] = child;
          break;
        }
      }
    }

    return chain;
  }

  /**
   * 从证书链里面返回根证书，即自签名的证书。
   * <B>注意：</B>传入的证书必须是“一条完整证书链"。
   * @param cerChain
   *            证书链。
   * @return 根证书。
   */
  private static X509Certificate findRootCA(X509Certificate[] cerChain) {
    if (cerChain.length == 1) {
      return cerChain[0];
    }

    for (X509Certificate item : cerChain) {
      String subjectDN = item.getSubjectX500Principal().getName();
      String issuserDN = item.getIssuerX500Principal().getName();
      if (subjectDN.equals(issuserDN)) {
        return item;
      }
    }

    throw new IllegalArgumentException("bad certificate chain: no root CA.");
  }

  /**
   * 从证书链里面返回证书所有者。即CA颁发的证书的所有者。位于证书链最下方。
   * <B>注意：</B>传入的证书必须是“一条完整证书链"。
   * @param cerChain
   *            证书链。
   * @return 所有者
   */
  public static X509Certificate findOwner(X509Certificate[] cerChain) {
    X509Certificate[] sorted = sort(cerChain);
    return sorted[sorted.length - 1];
  }

  public static Set<String> getCN(X509Certificate cert) {
    Set<String> names = new HashSet<>();

    // 读取CN
    String subjectDN = cert.getSubjectX500Principal().getName();
    String[] pairs = subjectDN.split(",");
    for (String p : pairs) {
      String[] kv = p.split("=");
      if (kv.length == 2 && kv[0].equals("CN")) {
        names.add(kv[1]);
      }
    }

    // 读取SubjectAlternativeNames
    try {
      Collection<List<?>> collection = cert.getSubjectAlternativeNames();
      if (collection != null) {
        for (List<?> list : collection) {
          if (list.size() == 2) {
            Object key = list.get(0);
            Object value = list.get(1);
            if (key instanceof Integer && value instanceof String) {
              int intKey = ((Integer) key).intValue();
              String strValue = (String) value;
              if (intKey == SUBALTNAME_DNSNAME || intKey == SUBALTNAME_IPADDRESS) {
                names.add(strValue);
              }
            }
          }
        }
      }
    } catch (CertificateParsingException e) {
      throw new IllegalArgumentException("can not read AlternativeNames.");
    }

    return names;
  }
}
