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
package org.apache.servicecomb.common.rest;

import org.apache.servicecomb.config.LegacyPropertyFactory;

import jakarta.servlet.MultipartConfigElement;

public class UploadConfig {
  /**
   * null means not support upload
   */
  public String getLocation() {
    return LegacyPropertyFactory
        .getStringProperty(RestConst.UPLOAD_DIR, RestConst.UPLOAD_DEFAULT_DIR);
  }

  /**
   * limit of one upload file, only available for servlet rest transport
   */
  public long getMaxFileSize() {
    return LegacyPropertyFactory.getLongProperty(RestConst.UPLOAD_MAX_FILE_SIZE, -1L);
  }

  /**
   * limit of upload request body
   */
  public long getMaxSize() {
    return LegacyPropertyFactory.getLongProperty(RestConst.UPLOAD_MAX_SIZE, -1L);
  }


  /**
   * the size threshold after which files will be written to disk, only available for servlet rest transport
   */
  public int getFileSizeThreshold() {
    return LegacyPropertyFactory.getIntProperty(RestConst.UPLOAD_FILE_SIZE_THRESHOLD, 0);
  }

  public MultipartConfigElement toMultipartConfigElement() {
    return new MultipartConfigElement(
        getLocation(),
        getMaxFileSize(),
        getMaxSize(),
        getFileSizeThreshold());
  }
}
