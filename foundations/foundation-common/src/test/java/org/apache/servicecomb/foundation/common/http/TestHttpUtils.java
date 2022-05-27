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

import java.net.URISyntaxException;

import javax.ws.rs.core.MediaType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestHttpUtils {

  @Test
  public void parseParamFromHeaderValue_normal() {
    Assertions.assertEquals("v", HttpUtils.parseParamFromHeaderValue("xx;k=v", "k"));
  }

  @Test
  public void parseParamFromHeaderValue_normal_ignoreCase() {
    Assertions.assertEquals("v", HttpUtils.parseParamFromHeaderValue("xx;K=v", "k"));
  }

  @Test
  public void parseParamFromHeaderValue_null() {
    Assertions.assertNull(HttpUtils.parseParamFromHeaderValue(null, "k"));
  }

  @Test
  public void parseParamFromHeaderValue_noKv() {
    Assertions.assertNull(HttpUtils.parseParamFromHeaderValue("xx", "k"));
  }

  @Test
  public void parseParamFromHeaderValue_noV() {
    Assertions.assertEquals("", HttpUtils.parseParamFromHeaderValue("xx;k=", "k"));
  }

  @Test
  public void parseParamFromHeaderValue_keyNotFound() {
    Assertions.assertNull(HttpUtils.parseParamFromHeaderValue("xx;k=", "kk"));
  }

  @Test
  public void uriEncode_null() {
    Assertions.assertEquals("", HttpUtils.uriEncodePath(null));
  }

  @Test
  public void uriDecode_null() {
    Assertions.assertNull(HttpUtils.uriDecodePath(null));
  }

  @Test
  public void uriEncode_chineseAndSpace() {
    String encoded = HttpUtils.uriEncodePath("测 试");
    Assertions.assertEquals("%E6%B5%8B%20%E8%AF%95", encoded);
    Assertions.assertEquals("测 试", HttpUtils.uriDecodePath(encoded));
  }

  @Test
  public void uriEncode_failed() {
    IllegalArgumentException illegalArgumentException = Assertions.assertThrows(IllegalArgumentException.class,
            () -> HttpUtils.uriEncodePath(":"));
    Assertions.assertEquals("uriEncode failed, path=\":\".", illegalArgumentException.getMessage());
    Assertions.assertTrue(illegalArgumentException.getCause() instanceof URISyntaxException);
  }

  @Test
  public void uriEncode_plus() {
    String encoded = HttpUtils.uriEncodePath("a+b");
    Assertions.assertEquals("a+b", encoded);
    Assertions.assertEquals("a+b", HttpUtils.uriDecodePath(encoded));
  }

  @Test
  public void uriEncode_encodeEntirePath() {
    String encoded = HttpUtils.uriEncodePath("a%%'+b/def");
    Assertions.assertEquals("a%25%25'+b/def", encoded);
  }

  @Test
  public void pathParamEncode() {
    Assertions.assertEquals("a+b", HttpUtils.encodePathParam("a+b"));
    Assertions.assertEquals("a%25b", HttpUtils.encodePathParam("a%b"));
    Assertions.assertEquals("a%25%25b", HttpUtils.encodePathParam("a%%b"));
    Assertions.assertEquals("%3C%20%3E'%22%EF%BC%88)&%2F%20%20", HttpUtils.encodePathParam("< >'\"（)&/  "));
    Assertions.assertEquals("%E6%B5%8B%20%E8%AF%95", HttpUtils.encodePathParam("测 试"));
  }

  /**
   * SafeChar: the characters that are not encoded.
   * This test is to show those safe chars excepting 0..9, a..z and A..Z
   */
  @Test
  public void pathParamEncode_SafeChar() {
    Assertions.assertEquals("-._~!$'()*,;&=@:+", HttpUtils.encodePathParam("-._~!$'()*,;&=@:+"));
  }

  @Test
  public void uriDecode_failed() {
    IllegalArgumentException illegalArgumentException = Assertions.assertThrows(IllegalArgumentException.class,
            () -> HttpUtils.uriDecodePath(":"));
    Assertions.assertEquals("uriDecode failed, path=\":\".", illegalArgumentException.getMessage());
    Assertions.assertTrue(illegalArgumentException.getCause() instanceof URISyntaxException);
  }

  @Test
  public void parseFileNameFromHeaderValue() {
    String fileName = "测 试.txt";
    String encoded = HttpUtils.uriEncodePath(fileName);
    Assertions.assertEquals(fileName, HttpUtils.parseFileNameFromHeaderValue("xx;filename=" + encoded));
  }

  @Test
  public void parseFileNameFromHeaderValue_defaultName() {
    Assertions.assertEquals("default", HttpUtils.parseFileNameFromHeaderValue("xx"));
  }

  @Test
  public void parseFileNameFromHeaderValue_ignorePath() {
    Assertions.assertEquals("a.txt", HttpUtils.parseFileNameFromHeaderValue("xx;filename=../../a.txt"));
  }

  @Test
  public void getCharsetFromContentType_noContentType() {
    String character = HttpUtils.getCharsetFromContentType(null);

    Assertions.assertNull(character);
  }

  @Test
  public void getCharsetFromContentType_noCharset() {
    String character = HttpUtils.getCharsetFromContentType(MediaType.APPLICATION_JSON);

    Assertions.assertNull(character);
  }

  @Test
  public void getCharsetFromContentType_noSemicolonEnd() {
    String character = HttpUtils.getCharsetFromContentType(MediaType.APPLICATION_JSON + ";charset=utf-8");

    Assertions.assertEquals("utf-8", character);
  }

  @Test
  public void getCharsetFromContentType_semicolonEnd() {
    String character = HttpUtils.getCharsetFromContentType(MediaType.APPLICATION_JSON + ";charset=utf-8;");

    Assertions.assertEquals("utf-8", character);
  }

  @Test
  public void getCharsetFromContentType_needTrim() {
    String character = HttpUtils.getCharsetFromContentType(MediaType.APPLICATION_JSON + ";charset= utf-8 ;");

    Assertions.assertEquals("utf-8", character);
  }

  @Test
  public void getCharsetFromContentType_quotationMarks() {
    String character = HttpUtils.getCharsetFromContentType(MediaType.APPLICATION_JSON + ";charset=\"utf-8\";");

    Assertions.assertEquals("utf-8", character);
  }

  @Test
  public void getCharsetFromContentType_quotationMarks_needTrim() {
    String character = HttpUtils.getCharsetFromContentType(MediaType.APPLICATION_JSON + ";charset=\" utf-8 \";");

    Assertions.assertEquals("utf-8", character);
  }
}
