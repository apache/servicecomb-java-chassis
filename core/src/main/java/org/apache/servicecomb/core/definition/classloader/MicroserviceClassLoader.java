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

package org.apache.servicecomb.core.definition.classloader;

import org.apache.servicecomb.foundation.common.utils.JvmUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MicroserviceClassLoader extends ClassLoader {
  private static final Logger LOGGER = LoggerFactory.getLogger(MicroserviceClassLoader.class);

  private final String appId;

  private final String microserviceName;

  private final String version;


  public MicroserviceClassLoader(String appId, String microserviceName, String version) {
    super(JvmUtils.findClassLoader());

    this.appId = appId;
    this.microserviceName = microserviceName;
    this.version = version;

    LOGGER.info("create classloader for microservice {}:{}:{}.", appId, microserviceName, version);
  }

  public String getAppId() {
    return appId;
  }

  public String getMicroserviceName() {
    return microserviceName;
  }

  public String getVersion() {
    return version;
  }

  @Override
  public String toString() {
    return String.format("MicroserviceClassLoader %s:%s:%s", appId, microserviceName, version);
  }

  @Override
  protected void finalize() throws Throwable {
    LOGGER.info("gc: classloader of microservice {}:{}.", microserviceName, version);

    super.finalize();
  }
}
