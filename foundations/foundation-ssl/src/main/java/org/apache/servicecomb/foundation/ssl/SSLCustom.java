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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 和应用相关的信息，方便定制使用。 目前主要包含密码解密、证书路径、IP和Port等内容。
 *
 */
public abstract class SSLCustom {
  private static final Logger LOG = LoggerFactory.getLogger(SSLCustom.class);

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

  public abstract char[] decode(char[] encrypted);

  public abstract String getFullPath(String filename);

  public String getHost() {
    return null;
  }
}
