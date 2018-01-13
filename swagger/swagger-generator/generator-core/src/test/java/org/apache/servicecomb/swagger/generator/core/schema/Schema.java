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

package org.apache.servicecomb.swagger.generator.core.schema;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ResponseHeader;

public class Schema {
  @ApiOperation(value = "", hidden = true)
  public void hidden() {

  }

  @ApiResponse(responseHeaders = {@ResponseHeader(name = "h", response = String.class)}, code = 200, message = "")
  public void testApiResponse() {

  }

  @ApiOperation(responseHeaders = {@ResponseHeader(name = "h", response = String.class)}, value = "")
  public void testApiOperation() {

  }

  @ResponseHeader(name = "h", response = String.class)
  public void testResponseHeader() {

  }

  public void testboolean(boolean value) {

  }

  public void testBoolean(Boolean value) {

  }

  public void testbyte(byte value) {

  }

  public void testByte(Byte value) {

  }

  public void testshort(short value) {

  }

  public void testShort(Short value) {

  }

  public void testint(int value) {

  }

  public void testInteger(Integer value) {

  }

  public void testlong(long value) {

  }

  public void testLong(Long value) {

  }

  public void testfloat(float value) {

  }

  public void testFloat(Float value) {

  }

  public void testdouble(double value) {

  }

  public void testDouble(Double value) {

  }

  public void testEnum(Color color, Color color1) {

  }

  public void testchar(char value) {

  }

  public void testChar(Character value) {

  }

  public void testbytes(byte[] value) {

  }

  public void testBytes(Byte[] value) {

  }

  public void testString(String value) {

  }

  public void testObject(User user) {

  }

  public void testArray(String[] value) {

  }

  public void testSet(Set<String> value) {

  }

  public void testList(List<User> value) {

  }

  public void testMap(Map<String, User> value) {

  }

  public Date testDate() {
    return null;
  }

  public void testMapList(Map<String, List<User>> value) {

  }

  public CompletableFuture<String> testCompletableFuture() {
    return null;
  }

  public void testAllType(AllType obj) {
  }

  public List<String> testMultiParam(AllType obj,
      boolean bValue,
      byte byteValue,
      short sValue,
      int iValue,
      long lValue,
      float fValue,
      double dValue,
      Color enumValue,
      char cValue,
      byte[] bytes,
      String strValue,
      String[] strArray,
      Set<String> set,
      List<User> list,
      Map<String, User> map) {
    return Collections.emptyList();
  }
}
