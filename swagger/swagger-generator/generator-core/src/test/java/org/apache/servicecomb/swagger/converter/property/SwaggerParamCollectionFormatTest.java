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

package org.apache.servicecomb.swagger.converter.property;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class SwaggerParamCollectionFormatTest {
  @Test
  public void splitParamNormal() {
    Assert.assertThat(SwaggerParamCollectionFormat.CSV.splitParam("a,b,c"),
        Matchers.arrayContaining("a", "b", "c"));
    Assert.assertThat(SwaggerParamCollectionFormat.SSV.splitParam("10 11 12"),
        Matchers.arrayContaining("10", "11", "12"));
    Assert.assertThat(SwaggerParamCollectionFormat.TSV.splitParam("a\tb\tc"),
        Matchers.arrayContaining("a", "b", "c"));
    Assert.assertThat(SwaggerParamCollectionFormat.PIPES.splitParam("a|b|c"),
        Matchers.arrayContaining("a", "b", "c"));
  }

  @Test
  public void splitParamMulti() {
    Assert.assertThat(SwaggerParamCollectionFormat.MULTI.splitParam("a,b,c"),
        Matchers.arrayContaining("a,b,c"));
  }

  @Test
  public void splitParam_SingleElement() {
    Assert.assertThat(SwaggerParamCollectionFormat.CSV.splitParam("a"),
        Matchers.arrayContaining("a"));
    Assert.assertThat(SwaggerParamCollectionFormat.SSV.splitParam("a"),
        Matchers.arrayContaining("a"));
    Assert.assertThat(SwaggerParamCollectionFormat.TSV.splitParam("a"),
        Matchers.arrayContaining("a"));
    Assert.assertThat(SwaggerParamCollectionFormat.PIPES.splitParam("a"),
        Matchers.arrayContaining("a"));
    Assert.assertThat(SwaggerParamCollectionFormat.MULTI.splitParam("a"),
        Matchers.arrayContaining("a"));
  }

  @Test
  public void splitParam_NullElement() {
    Assert.assertThat(SwaggerParamCollectionFormat.CSV.splitParam(null),
        Matchers.emptyArray());
    Assert.assertThat(SwaggerParamCollectionFormat.SSV.splitParam(null),
        Matchers.emptyArray());
    Assert.assertThat(SwaggerParamCollectionFormat.TSV.splitParam(null),
        Matchers.emptyArray());
    Assert.assertThat(SwaggerParamCollectionFormat.PIPES.splitParam(null),
        Matchers.emptyArray());
    Assert.assertThat(SwaggerParamCollectionFormat.MULTI.splitParam(null),
        Matchers.emptyArray());
  }

  @Test
  public void splitParam_BlankElement() {
    Assert.assertThat(SwaggerParamCollectionFormat.CSV.splitParam(""),
        Matchers.arrayContaining(""));
    Assert.assertThat(SwaggerParamCollectionFormat.SSV.splitParam(""),
        Matchers.arrayContaining(""));
    Assert.assertThat(SwaggerParamCollectionFormat.TSV.splitParam(""),
        Matchers.arrayContaining(""));
    Assert.assertThat(SwaggerParamCollectionFormat.PIPES.splitParam(""),
        Matchers.arrayContaining(""));
    Assert.assertThat(SwaggerParamCollectionFormat.MULTI.splitParam(""),
        Matchers.arrayContaining(""));

    Assert.assertThat(SwaggerParamCollectionFormat.CSV.splitParam("a,,b"),
        Matchers.arrayContaining("a", "", "b"));
    Assert.assertThat(SwaggerParamCollectionFormat.SSV.splitParam("a  b"),
        Matchers.arrayContaining("a", "", "b"));
    Assert.assertThat(SwaggerParamCollectionFormat.TSV.splitParam("a\t\tb"),
        Matchers.arrayContaining("a", "", "b"));
    Assert.assertThat(SwaggerParamCollectionFormat.PIPES.splitParam("a||b"),
        Matchers.arrayContaining("a", "", "b"));

    Assert.assertThat(SwaggerParamCollectionFormat.CSV.splitParam("a,,"),
        Matchers.arrayContaining("a", "", ""));
    Assert.assertThat(SwaggerParamCollectionFormat.SSV.splitParam("a  "),
        Matchers.arrayContaining("a", "", ""));
    Assert.assertThat(SwaggerParamCollectionFormat.TSV.splitParam("a\t\t"),
        Matchers.arrayContaining("a", "", ""));
    String[] actual = SwaggerParamCollectionFormat.PIPES.splitParam("a||");
    Assert.assertThat(Arrays.toString(actual), actual,
        Matchers.arrayContaining("a", "", ""));

    Assert.assertThat(SwaggerParamCollectionFormat.CSV.splitParam(",,b"),
        Matchers.arrayContaining("", "", "b"));
    Assert.assertThat(SwaggerParamCollectionFormat.SSV.splitParam("  b"),
        Matchers.arrayContaining("", "", "b"));
    Assert.assertThat(SwaggerParamCollectionFormat.TSV.splitParam("\t\tb"),
        Matchers.arrayContaining("", "", "b"));
    Assert.assertThat(SwaggerParamCollectionFormat.PIPES.splitParam("||b"),
        Matchers.arrayContaining("", "", "b"));
  }

  @Test
  public void joinNormal() {
    List<String> params = Arrays.asList("a", "b", "c");
    assertEquals("a,b,c", SwaggerParamCollectionFormat.CSV.joinParam(params));
    assertEquals("a b c", SwaggerParamCollectionFormat.SSV.joinParam(params));
    assertEquals("a\tb\tc", SwaggerParamCollectionFormat.TSV.joinParam(params));
    assertEquals("a|b|c", SwaggerParamCollectionFormat.PIPES.joinParam(params));
  }

  @Test
  public void join_SingleElement() {
    List<String> params = Collections.singletonList("a");
    assertEquals("a", SwaggerParamCollectionFormat.CSV.joinParam(params));
    assertEquals("a", SwaggerParamCollectionFormat.SSV.joinParam(params));
    assertEquals("a", SwaggerParamCollectionFormat.TSV.joinParam(params));
    assertEquals("a", SwaggerParamCollectionFormat.PIPES.joinParam(params));
  }

  @Test
  public void join_EmptyArray() {
    Assert.assertNull(SwaggerParamCollectionFormat.CSV.joinParam(Collections.EMPTY_LIST));
  }

  @Test
  public void join_NullAndBlankElement() {
    Assert.assertNull(SwaggerParamCollectionFormat.CSV.joinParam(Collections.singletonList(null)));

    assertEquals("", SwaggerParamCollectionFormat.CSV.joinParam(Collections.singleton("")));
    assertEquals("a,,b,c", SwaggerParamCollectionFormat.CSV.joinParam(Arrays.asList("a", "", "b", "c")));
    assertEquals("a  b c", SwaggerParamCollectionFormat.SSV.joinParam(Arrays.asList("a", "", "b", "c")));
    assertEquals("a\t\tb\tc", SwaggerParamCollectionFormat.TSV.joinParam(Arrays.asList("a", "", "b", "c")));
    assertEquals("a||b|c", SwaggerParamCollectionFormat.PIPES.joinParam(Arrays.asList("a", "", "b", "c")));

    assertEquals("a,b,,c",
        SwaggerParamCollectionFormat.CSV
            .joinParam(Arrays.asList(null, "a", null, "b", null, "", null, null, "c", null)));
    assertEquals("a b  c",
        SwaggerParamCollectionFormat.SSV
            .joinParam(Arrays.asList(null, "a", null, "b", null, "", null, null, "c", null)));
    assertEquals("a\tb\t\tc",
        SwaggerParamCollectionFormat.TSV
            .joinParam(Arrays.asList(null, "a", null, "b", null, "", null, null, "c", null)));
    assertEquals("a|b||c",
        SwaggerParamCollectionFormat.PIPES
            .joinParam(Arrays.asList(null, "a", null, "b", null, "", null, null, "c", null)));

    assertEquals("a,b,,c",
        SwaggerParamCollectionFormat.CSV
            .joinParam(Arrays.asList(null, null, "a", null, "b", null, "", null, null, "c", null, null)));
    assertEquals("a b  c",
        SwaggerParamCollectionFormat.SSV
            .joinParam(Arrays.asList(null, null, "a", null, "b", null, "", null, null, "c", null, null)));
    assertEquals("a\tb\t\tc",
        SwaggerParamCollectionFormat.TSV
            .joinParam(Arrays.asList(null, null, "a", null, "b", null, "", null, null, "c", null, null)));
    assertEquals("a|b||c",
        SwaggerParamCollectionFormat.PIPES
            .joinParam(Arrays.asList(null, null, "a", null, "b", null, "", null, null, "c", null, null)));
  }

  @Test
  public void join_NullArray() {
    assertNull(SwaggerParamCollectionFormat.CSV.joinParam(null));
  }

  /**
   * In fact, the {@link SwaggerParamCollectionFormat#joinParam(Collection)} of {@link SwaggerParamCollectionFormat#MULTI}
   * should never be invoked.
   * This test is just for ensuring the method does not throw exception.
   */
  @Test
  public void joinMulti() {
    SwaggerParamCollectionFormat.MULTI.joinParam(Arrays.asList("a", "b", "c"));
    SwaggerParamCollectionFormat.MULTI.joinParam(Collections.singletonList("a"));
    assertNull(SwaggerParamCollectionFormat.MULTI.joinParam(new ArrayList<String>()));
    assertNull(SwaggerParamCollectionFormat.MULTI.joinParam(Collections.singleton(null)));
    assertNull(SwaggerParamCollectionFormat.MULTI.joinParam(null));
  }
}