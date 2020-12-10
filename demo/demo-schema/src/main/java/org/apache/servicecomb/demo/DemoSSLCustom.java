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

package org.apache.servicecomb.demo;

import java.io.File;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.foundation.ssl.SSLCustom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DemoSSLCustom extends SSLCustom {
  private static final Logger LOGGER = LoggerFactory.getLogger(DemoSSLCustom.class);

  @Override
  public char[] decode(char[] encrypted) {
    return encrypted;
  }

  @Override
  public String getFullPath(String filename) {
    LOGGER.info("current working dir :" + System.getProperty("user.dir"));

    if (StringUtils.isEmpty(filename)) {
      return null;
    }

    // local for different IDEs
    File localFile = new File(
        System.getProperty("user.dir") + "/demo/demo-springmvc/springmvc-server/src/main/resources/certificates/"
            + filename);
    if (localFile.isFile()) {
      return localFile.getAbsolutePath();
    }

    localFile = new File(
        System.getProperty("user.dir") + "/src/main/resources/certificates/"
            + filename);
    if (localFile.isFile()) {
      return localFile.getAbsolutePath();
    }

    localFile = new File(System.getProperty("user.dir") + "/certificates/" + filename);
    if (localFile.isFile()) {
      return localFile.getAbsolutePath();
    }

    // docker
    localFile = new File("/maven/maven/certificates/" + filename);
    if (localFile.isFile()) {
      return localFile.getAbsolutePath();
    }

    // in jar, maybe
    LOGGER.info("not found file {} in file system, maybe in jar.", filename);
    return "certificates/" + filename;
  }
}
