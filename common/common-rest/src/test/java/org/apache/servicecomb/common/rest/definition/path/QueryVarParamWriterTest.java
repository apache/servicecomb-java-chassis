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

package org.apache.servicecomb.common.rest.definition.path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.servicecomb.common.rest.definition.RestParam;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import io.swagger.models.parameters.QueryParameter;

public class QueryVarParamWriterTest {

  private static QueryVarParamWriter queryVarParamWriterCsv;

  private static QueryVarParamWriter queryVarParamWriterMulti;

  private static QueryVarParamWriter queryVarParamWriterDefault;

  @BeforeClass
  public static void beforeClass() {
    QueryParameter parameter = new QueryParameter();
    parameter.setName("q");
    parameter.setCollectionFormat("csv");
    queryVarParamWriterCsv = new QueryVarParamWriter(
        '?',
        new RestParam(0, parameter, String[].class));

    parameter = new QueryParameter();
    parameter.setName("q");
    parameter.setCollectionFormat("multi");
    queryVarParamWriterMulti = new QueryVarParamWriter(
        '?',
        new RestParam(0, parameter, String[].class));

    parameter = new QueryParameter();
    parameter.setName("q");
    queryVarParamWriterDefault = new QueryVarParamWriter(
        '?',
        new RestParam(0, parameter, String[].class));
  }

  @Test
  public void write() throws Exception {
    StringBuilder stringBuilder = new StringBuilder();
    Object[] args = {"a"};
    queryVarParamWriterCsv.write(stringBuilder, args);
    Assert.assertEquals("?q=a", stringBuilder.toString());
    stringBuilder = new StringBuilder();
    queryVarParamWriterMulti.write(stringBuilder, args);
    Assert.assertEquals("?q=a", stringBuilder.toString());
    stringBuilder = new StringBuilder();
    queryVarParamWriterDefault.write(stringBuilder, args);
    Assert.assertEquals("?q=a", stringBuilder.toString());
  }

  @Test
  public void writeNull() throws Exception {
    StringBuilder stringBuilder = new StringBuilder();
    Object[] args = {null};
    queryVarParamWriterCsv.write(stringBuilder, args);
    Assert.assertEquals("?", stringBuilder.toString());
    stringBuilder = new StringBuilder();
    queryVarParamWriterMulti.write(stringBuilder, args);
    Assert.assertEquals("?", stringBuilder.toString());
    stringBuilder = new StringBuilder();
    queryVarParamWriterDefault.write(stringBuilder, args);
    Assert.assertEquals("?", stringBuilder.toString());
  }

  @Test
  public void writeArray() throws Exception {
    StringBuilder stringBuilder = new StringBuilder();
    Object[] args = new Object[] {new String[] {"ab", "cd", "ef"}};
    queryVarParamWriterCsv.write(stringBuilder, args);
    Assert.assertEquals("?q=ab%2Ccd%2Cef", stringBuilder.toString());
    stringBuilder = new StringBuilder();
    queryVarParamWriterMulti.write(stringBuilder, args);
    Assert.assertEquals("?q=ab&q=cd&q=ef", stringBuilder.toString());
    stringBuilder = new StringBuilder();
    queryVarParamWriterDefault.write(stringBuilder, args);
    Assert.assertEquals("?q=ab&q=cd&q=ef", stringBuilder.toString());

    // encode space char
    stringBuilder = new StringBuilder();
    args[0] = new String[] {"a b", " ", "", "ef"};
    queryVarParamWriterCsv.write(stringBuilder, args);
    Assert.assertEquals("?q=a+b%2C+%2C%2Cef", stringBuilder.toString());
    stringBuilder = new StringBuilder();
    queryVarParamWriterMulti.write(stringBuilder, args);
    Assert.assertEquals("?q=a+b&q=+&q=&q=ef", stringBuilder.toString());
    stringBuilder = new StringBuilder();
    queryVarParamWriterDefault.write(stringBuilder, args);
    Assert.assertEquals("?q=a+b&q=+&q=&q=ef", stringBuilder.toString());

    // pass blank string
    stringBuilder = new StringBuilder();
    args[0] = new String[] {""};
    queryVarParamWriterCsv.write(stringBuilder, args);
    Assert.assertEquals("?q=", stringBuilder.toString());
    stringBuilder = new StringBuilder();
    queryVarParamWriterMulti.write(stringBuilder, args);
    Assert.assertEquals("?q=", stringBuilder.toString());
    stringBuilder = new StringBuilder();
    queryVarParamWriterDefault.write(stringBuilder, args);
    Assert.assertEquals("?q=", stringBuilder.toString());

    // pass empty
    stringBuilder = new StringBuilder();
    args[0] = new String[] {};
    queryVarParamWriterCsv.write(stringBuilder, args);
    Assert.assertEquals("?", stringBuilder.toString());
    stringBuilder = new StringBuilder();
    queryVarParamWriterMulti.write(stringBuilder, args);
    Assert.assertEquals("?", stringBuilder.toString());
    stringBuilder = new StringBuilder();
    queryVarParamWriterDefault.write(stringBuilder, args);
    Assert.assertEquals("?", stringBuilder.toString());
    // pass null
    args[0] = new String[] {null};
    stringBuilder = new StringBuilder();
    queryVarParamWriterCsv.write(stringBuilder, args);
    Assert.assertEquals("?", stringBuilder.toString());
    stringBuilder = new StringBuilder();
    queryVarParamWriterMulti.write(stringBuilder, args);
    Assert.assertEquals("?", stringBuilder.toString());
    stringBuilder = new StringBuilder();
    queryVarParamWriterDefault.write(stringBuilder, args);
    Assert.assertEquals("?", stringBuilder.toString());
    args[0] = new String[] {null, "ab", null, "cd", null, null, "", null, "ef", null};
    stringBuilder = new StringBuilder();
    queryVarParamWriterCsv.write(stringBuilder, args);
    Assert.assertEquals("?q=ab%2Ccd%2C%2Cef", stringBuilder.toString());
    stringBuilder = new StringBuilder();
    queryVarParamWriterMulti.write(stringBuilder, args);
    Assert.assertEquals("?&q=ab&&q=cd&&&q=&&q=ef&", stringBuilder.toString());
    stringBuilder = new StringBuilder();
    queryVarParamWriterDefault.write(stringBuilder, args);
    Assert.assertEquals("?&q=ab&&q=cd&&&q=&&q=ef&", stringBuilder.toString());
  }

  @Test
  public void writeList() throws Exception {
    List<String> queryList = Arrays.asList("ab", "cd", "ef");
    Object[] args = {queryList};
    StringBuilder stringBuilder = new StringBuilder();
    queryVarParamWriterCsv.write(stringBuilder, args);
    Assert.assertEquals("?q=ab%2Ccd%2Cef", stringBuilder.toString());
    stringBuilder = new StringBuilder();
    queryVarParamWriterMulti.write(stringBuilder, args);
    Assert.assertEquals("?q=ab&q=cd&q=ef", stringBuilder.toString());
    stringBuilder = new StringBuilder();
    queryVarParamWriterDefault.write(stringBuilder, args);
    Assert.assertEquals("?q=ab&q=cd&q=ef", stringBuilder.toString());

    // encode space char
    args[0] = Arrays.asList("a b", " ", "", "ef");
    stringBuilder = new StringBuilder();
    queryVarParamWriterCsv.write(stringBuilder, args);
    Assert.assertEquals("?q=a+b%2C+%2C%2Cef", stringBuilder.toString());
    stringBuilder = new StringBuilder();
    queryVarParamWriterMulti.write(stringBuilder, args);
    Assert.assertEquals("?q=a+b&q=+&q=&q=ef", stringBuilder.toString());
    stringBuilder = new StringBuilder();
    queryVarParamWriterDefault.write(stringBuilder, args);
    Assert.assertEquals("?q=a+b&q=+&q=&q=ef", stringBuilder.toString());

    // pass blank string
    stringBuilder = new StringBuilder();
    args[0] = Collections.singletonList("");
    queryVarParamWriterCsv.write(stringBuilder, args);
    Assert.assertEquals("?q=", stringBuilder.toString());
    stringBuilder = new StringBuilder();
    queryVarParamWriterMulti.write(stringBuilder, args);
    Assert.assertEquals("?q=", stringBuilder.toString());
    stringBuilder = new StringBuilder();
    queryVarParamWriterDefault.write(stringBuilder, args);
    Assert.assertEquals("?q=", stringBuilder.toString());

    // pass empty
    stringBuilder = new StringBuilder();
    args[0] = new ArrayList<>();
    queryVarParamWriterCsv.write(stringBuilder, args);
    Assert.assertEquals("?", stringBuilder.toString());
    stringBuilder = new StringBuilder();
    queryVarParamWriterMulti.write(stringBuilder, args);
    Assert.assertEquals("?", stringBuilder.toString());
    stringBuilder = new StringBuilder();
    queryVarParamWriterDefault.write(stringBuilder, args);
    Assert.assertEquals("?", stringBuilder.toString());
    // pass null
    args[0] = Collections.singletonList(null);
    stringBuilder = new StringBuilder();
    queryVarParamWriterCsv.write(stringBuilder, args);
    Assert.assertEquals("?", stringBuilder.toString());
    stringBuilder = new StringBuilder();
    queryVarParamWriterMulti.write(stringBuilder, args);
    Assert.assertEquals("?", stringBuilder.toString());
    stringBuilder = new StringBuilder();
    queryVarParamWriterDefault.write(stringBuilder, args);
    Assert.assertEquals("?", stringBuilder.toString());
    args[0] = Arrays.asList(null, "ab", null, "cd", null, null, "", null, "ef", null);
    stringBuilder = new StringBuilder();
    queryVarParamWriterCsv.write(stringBuilder, args);
    Assert.assertEquals("?q=ab%2Ccd%2C%2Cef", stringBuilder.toString());
    stringBuilder = new StringBuilder();
    queryVarParamWriterMulti.write(stringBuilder, args);
    Assert.assertEquals("?&q=ab&&q=cd&&&q=&&q=ef&", stringBuilder.toString());
    stringBuilder = new StringBuilder();
    queryVarParamWriterDefault.write(stringBuilder, args);
    Assert.assertEquals("?&q=ab&&q=cd&&&q=&&q=ef&", stringBuilder.toString());
  }
}
