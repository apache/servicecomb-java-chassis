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

package org.apache.servicecomb.it.schema.objectparams;

import java.util.Objects;

public class TestNullFieldAndDefaultValueParam {
  private String s1;

  private int i1;

  private String s2;

  private int i2;

  private String s3;

  private int i3;

  private String rawRequest;

  public TestNullFieldAndDefaultValueParam(String s1, int i1, String s2, int i2, String s3, int i3) {
    this.s1 = s1;
    this.i1 = i1;
    this.s2 = s2;
    this.i2 = i2;
    this.s3 = s3;
    this.i3 = i3;
  }

  public TestNullFieldAndDefaultValueParam() {
  }

  public String getRawRequest() {
    return rawRequest;
  }

  public void setRawRequest(String rawRequest) {
    this.rawRequest = rawRequest;
  }

  public String getS1() {
    return s1;
  }

  public void setS1(String s1) {
    this.s1 = s1;
  }

  public int getI1() {
    return i1;
  }

  public void setI1(int i1) {
    this.i1 = i1;
  }

  public String getS2() {
    return s2;
  }

  public void setS2(String s2) {
    this.s2 = s2;
  }

  public int getI2() {
    return i2;
  }

  public void setI2(int i2) {
    this.i2 = i2;
  }

  public String getS3() {
    return s3;
  }

  public void setS3(String s3) {
    this.s3 = s3;
  }

  public int getI3() {
    return i3;
  }

  public void setI3(int i3) {
    this.i3 = i3;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TestNullFieldAndDefaultValueParam that = (TestNullFieldAndDefaultValueParam) o;
    return i1 == that.i1 &&
        i2 == that.i2 &&
        i3 == that.i3 &&
        Objects.equals(s1, that.s1) &&
        Objects.equals(s2, that.s2) &&
        Objects.equals(s3, that.s3) &&
        Objects.equals(rawRequest, that.rawRequest);
  }

  @Override
  public int hashCode() {
    return Objects.hash(s1, i1, s2, i2, s3, i3, rawRequest);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("TestNullFieldAndDefaultValueParam{");
    sb.append("s1='").append(s1).append('\'');
    sb.append(", i1=").append(i1);
    sb.append(", s2='").append(s2).append('\'');
    sb.append(", i2=").append(i2);
    sb.append(", s3='").append(s3).append('\'');
    sb.append(", i3=").append(i3);
    sb.append(", rawRequest='").append(rawRequest).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
