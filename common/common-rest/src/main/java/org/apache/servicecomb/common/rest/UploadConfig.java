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

import javax.servlet.MultipartConfigElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicPropertyFactory;

public class UploadConfig {
  private static final Logger LOGGER = LoggerFactory.getLogger(UploadConfig.class);

  /**
   * null means not support upload
   */
  private String location;

  /**
   * limit of one upload file, only available for servlet rest transport
   */
  private long maxFileSize;

  /**
   * limit of upload request body
   */
  private long maxSize;

  /**
   * the size threshold after which files will be written to disk, only available for servlet rest transport
   */
  private int fileSizeThreshold;

  public UploadConfig() {
    location = DynamicPropertyFactory.getInstance().getStringProperty(RestConst.UPLOAD_DIR, null).get();
    maxFileSize = DynamicPropertyFactory.getInstance().getLongProperty(RestConst.UPLOAD_MAX_FILE_SIZE, -1L).get();
    maxSize = DynamicPropertyFactory.getInstance().getLongProperty(RestConst.UPLOAD_MAX_SIZE, -1L).get();
    fileSizeThreshold = DynamicPropertyFactory.getInstance().getIntProperty(RestConst.UPLOAD_FILE_SIZE_THRESHOLD, 0)
        .get();
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public long getMaxFileSize() {
    return maxFileSize;
  }

  public void setMaxFileSize(long maxFileSize) {
    this.maxFileSize = maxFileSize;
  }

  public long getMaxSize() {
    return maxSize;
  }

  public void setMaxSize(long maxSize) {
    this.maxSize = maxSize;
  }

  public int getFileSizeThreshold() {
    return fileSizeThreshold;
  }

  public void setFileSizeThreshold(int fileSizeThreshold) {
    this.fileSizeThreshold = fileSizeThreshold;
  }

  public MultipartConfigElement toMultipartConfigElement() {
    String location = DynamicPropertyFactory.getInstance().getStringProperty(RestConst.UPLOAD_DIR, null).get();
    if (location == null) {
      LOGGER.info("{} is null, not support upload.", RestConst.UPLOAD_DIR);
      return null;
    }

    return new MultipartConfigElement(
        location,
        DynamicPropertyFactory.getInstance().getLongProperty(RestConst.UPLOAD_MAX_FILE_SIZE, -1L).get(),
        DynamicPropertyFactory.getInstance().getLongProperty(RestConst.UPLOAD_MAX_SIZE, -1L).get(),
        DynamicPropertyFactory.getInstance().getIntProperty(RestConst.UPLOAD_FILE_SIZE_THRESHOLD, 0).get());
  }
}
