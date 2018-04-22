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

import java.io.UnsupportedEncodingException;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class TestHttpUtils {
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void parseParamFromHeaderValue_normal() {
    Assert.assertEquals("v", HttpUtils.parseParamFromHeaderValue("xx;k=v", "k"));
  }

  @Test
  public void parseParamFromHeaderValue_normal_ignoreCase() {
    Assert.assertEquals("v", HttpUtils.parseParamFromHeaderValue("xx;K=v", "k"));
  }

  @Test
  public void parseParamFromHeaderValue_null() {
    Assert.assertNull(HttpUtils.parseParamFromHeaderValue(null, "k"));
  }

  @Test
  public void parseParamFromHeaderValue_noKv() {
    Assert.assertNull(HttpUtils.parseParamFromHeaderValue("xx", "k"));
  }

  @Test
  public void parseParamFromHeaderValue_noV() {
    Assert.assertEquals("", HttpUtils.parseParamFromHeaderValue("xx;k=", "k"));
  }

  @Test
  public void parseParamFromHeaderValue_keyNotFound() {
    Assert.assertNull(HttpUtils.parseParamFromHeaderValue("xx;k=", "kk"));
  }

  @Test
  public void uriEncode_chineseAndSpace() {
    String encoded = HttpUtils.uriEncode("测 试");
    Assert.assertEquals("%E6%B5%8B%20%E8%AF%95", encoded);
    Assert.assertEquals("测 试", HttpUtils.uriDecode(encoded));
  }

  @Test
  public void uriEncode_failed() {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage(Matchers.is("uriEncode failed, value=\"\", enc=\"notExistEnc\"."));
    expectedException.expectCause(Matchers.instanceOf(UnsupportedEncodingException.class));

    HttpUtils.uriEncode("", "notExistEnc");
  }

  @Test
  public void uriDecode_failed() {
    expectedException.expect(IllegalStateException.class);
    expectedException
        .expectMessage(Matchers.is("uriDecode failed, value=\"%E6%B5%8B%20%E8%AF%95\", enc=\"notExistEnc\"."));
    expectedException.expectCause(Matchers.instanceOf(UnsupportedEncodingException.class));

    HttpUtils.uriDecode("%E6%B5%8B%20%E8%AF%95", "notExistEnc");
  }

  @Test
  public void parseFileNameFromHeaderValue() {
    String fileName = "测 试.txt";
    String encoded = HttpUtils.uriEncode(fileName);
    Assert.assertEquals(fileName, HttpUtils.parseFileNameFromHeaderValue("xx;filename=" + encoded));
  }

  @Test
  public void parseFileNameFromHeaderValue_defaultName() {
    Assert.assertEquals("default", HttpUtils.parseFileNameFromHeaderValue("xx"));
  }

  @Test
  public void parseFileNameFromHeaderValue_ignorePath() {
    Assert.assertEquals("a.txt", HttpUtils.parseFileNameFromHeaderValue("xx;filename=../../a.txt"));
  }
}
