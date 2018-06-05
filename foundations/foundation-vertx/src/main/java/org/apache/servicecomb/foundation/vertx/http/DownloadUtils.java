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
package org.apache.servicecomb.foundation.vertx.http;

import java.io.IOException;

import javax.servlet.http.Part;

import org.apache.servicecomb.foundation.common.http.HttpUtils;
import org.apache.servicecomb.foundation.common.part.FilePartForSend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.http.HttpHeaders;

/**
 * internal api
 */
public final class DownloadUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(DownloadUtils.class);

  private DownloadUtils() {
  }

  public static void prepareDownloadHeader(HttpServletResponseEx responseEx, Part part) {
    if (responseEx.getHeader(HttpHeaders.CONTENT_LENGTH.toString()) == null) {
      responseEx.setChunked(true);
    }

    if (responseEx.getHeader(HttpHeaders.CONTENT_TYPE.toString()) == null) {
      responseEx.setHeader(HttpHeaders.CONTENT_TYPE.toString(), part.getContentType());
    }

    if (responseEx.getHeader(javax.ws.rs.core.HttpHeaders.CONTENT_DISPOSITION) == null) {
      // to support chinese and space filename in firefox
      // must use "filename*", (https://tools.ietf.org/html/rtf6266)
      String encodedFileName = HttpUtils.uriEncodePath(part.getSubmittedFileName());
      responseEx.setHeader(javax.ws.rs.core.HttpHeaders.CONTENT_DISPOSITION,
          "attachment;filename=" + encodedFileName + ";filename*=utf-8''" + encodedFileName);
    }
  }

  public static void clearPartResource(Part part) {
    if (FilePartForSend.class.isInstance(part) && ((FilePartForSend) part).isDeleteAfterFinished()) {
      try {
        part.delete();
      } catch (IOException e) {
        LOGGER.error("Failed to delete temp file: {}.", ((FilePartForSend) part).getAbsolutePath(), e);
      }
    }
  }
}
