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
package org.apache.servicecomb.foundation.common.http;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.util.StringUtils;

import com.google.common.net.UrlEscapers;

public final class HttpUtils {
  private HttpUtils() {
  }

  /**
   * paramName is not case sensitive
   * @param headerValue example: attachment;filename=a.txt
   *
   */
  // 
  public static String parseParamFromHeaderValue(String headerValue, String paramName) {
    if (StringUtils.isEmpty(headerValue)) {
      return null;
    }

    for (String value : headerValue.split(";")) {
      int idx = value.indexOf('=');
      if (idx == -1) {
        continue;
      }

      if (paramName.equalsIgnoreCase(value.substring(0, idx))) {
        return value.substring(idx + 1);
      }
    }
    return null;
  }

  /**
   * <pre>
   *          foo://example.com:8042/over/there?name=ferret#nose
   *          \_/   \______________/\_________/ \_________/ \__/
   *           |           |            |            |        |
   *        scheme     authority       path        query   fragment
   *           |   _____________________|__
   *          / \ /                        \
   *          urn:example:animal:ferret:nose
   * </pre>
   * <p>the URI syntax components above is referred from <a href="https://tools.ietf.org/html/rfc3986#page-16">RFC3986</a>.
   * This method is used to encode the entire path part(e.g. /over/there in the example).</p>
   * <em>In order to keep the structure of path, slash '/' will not be encoded. If you want to encode '/' into {@code %2F},
   * please consider the {@link #encodePathParam(String)}
   * </em>
   *
   * @param path the entire url path
   * @return the encoded url path
   */
  public static String uriEncodePath(String path) {
    try {
      URI uri = new URI(null, null, path, null);
      return uri.toASCIIString();
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(String.format("uriEncode failed, path=\"%s\".", path), e);
    }
  }

  /**
   * Encode path params. For example, if the path of an operation is {@code /over/there/{pathParam}/tail}, this method
   * should be used to encoded {@code {pathParam}}. In order to keep the path structure, the slash '/' will be encoded
   * into {@code %2F} to avoid path matching problem.
   *
   * @see UrlEscapers#urlPathSegmentEscaper()
   *
   * @param pathParam the path param to be encoded
   * @return the encoded path param
   */
  public static String encodePathParam(String pathParam) {
    return UrlEscapers.urlPathSegmentEscaper().escape(pathParam);
  }

  public static String uriDecodePath(String path) {
    if (path == null) {
      return null;
    }

    try {
      return new URI(path).getPath();
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(String.format("uriDecode failed, path=\"%s\".", path), e);
    }
  }

  /**
   * only used by SDK to download from  serviceComb producer<br>
   * no need to check rtf6266's "filename*" rule.
   */
  public static String parseFileNameFromHeaderValue(String headerValue) {
    String fileName = parseParamFromHeaderValue(headerValue, "filename");
    fileName = StringUtils.isEmpty(fileName) ? "default" : fileName;
    fileName = uriDecodePath(fileName);
    return new File(fileName).getName();
  }

  /**
   * Parse the character encoding from the specified content type header.
   * If the content type is null, or there is no explicit character encoding,
   * <code>null</code> is returned.
   *
   * @param contentType a content type header
   */
  public static String getCharsetFromContentType(String contentType) {
    if (contentType == null) {
      return null;
    }
    int start = contentType.indexOf("charset=");
    if (start < 0) {
      return null;
    }
    String encoding = contentType.substring(start + 8);
    int end = encoding.indexOf(';');
    if (end >= 0) {
      encoding = encoding.substring(0, end);
    }
    encoding = encoding.trim();
    if ((encoding.length() > 2) && (encoding.startsWith("\""))
        && (encoding.endsWith("\""))) {
      encoding = encoding.substring(1, encoding.length() - 1);
    }
    return encoding.trim();
  }
}
