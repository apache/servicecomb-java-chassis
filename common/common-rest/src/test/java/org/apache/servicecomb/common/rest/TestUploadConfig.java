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

import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestUploadConfig {
  @Before
  public void setUp() {
    ArchaiusUtils.resetConfig();
  }

  @After
  public void tearDown() {
    ArchaiusUtils.resetConfig();
  }

  @Test
  public void getMultipartConfig_notSupport() {
    UploadConfig uploadConfig = new UploadConfig();

    Assert.assertNull(uploadConfig.toMultipartConfigElement());
    Assert.assertEquals(-1L, uploadConfig.getMaxFileSize());
    Assert.assertEquals(-1L, uploadConfig.getMaxSize());
    Assert.assertEquals(0, uploadConfig.getFileSizeThreshold());
  }

  @Test
  public void getMultipartConfig_default() {
    ArchaiusUtils.setProperty(RestConst.UPLOAD_DIR, "upload");

    UploadConfig uploadConfig = new UploadConfig();
    MultipartConfigElement multipartConfigElement = uploadConfig.toMultipartConfigElement();

    Assert.assertEquals("upload", uploadConfig.getLocation());
    Assert.assertEquals(-1L, uploadConfig.getMaxFileSize());
    Assert.assertEquals(-1L, uploadConfig.getMaxSize());
    Assert.assertEquals(0, uploadConfig.getFileSizeThreshold());

    Assert.assertEquals("upload", multipartConfigElement.getLocation());
    Assert.assertEquals(-1L, multipartConfigElement.getMaxFileSize());
    Assert.assertEquals(-1L, multipartConfigElement.getMaxRequestSize());
    Assert.assertEquals(0, multipartConfigElement.getFileSizeThreshold());
  }

  @Test
  public void getMultipartConfig_config() {
    ArchaiusUtils.setProperty(RestConst.UPLOAD_DIR, "upload");
    ArchaiusUtils.setProperty(RestConst.UPLOAD_MAX_FILE_SIZE, 1);
    ArchaiusUtils.setProperty(RestConst.UPLOAD_MAX_SIZE, 2);
    ArchaiusUtils.setProperty(RestConst.UPLOAD_FILE_SIZE_THRESHOLD, 3);

    UploadConfig uploadConfig = new UploadConfig();
    MultipartConfigElement multipartConfigElement = uploadConfig.toMultipartConfigElement();

    Assert.assertEquals("upload", uploadConfig.getLocation());
    Assert.assertEquals(1, uploadConfig.getMaxFileSize());
    Assert.assertEquals(2, uploadConfig.getMaxSize());
    Assert.assertEquals(3, uploadConfig.getFileSizeThreshold());

    Assert.assertEquals("upload", multipartConfigElement.getLocation());
    Assert.assertEquals(1, multipartConfigElement.getMaxFileSize());
    Assert.assertEquals(2, multipartConfigElement.getMaxRequestSize());
    Assert.assertEquals(3, multipartConfigElement.getFileSizeThreshold());
  }
}
