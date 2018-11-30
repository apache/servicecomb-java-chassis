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
package org.apache.servicecomb.foundation.protobuf.internal.model;

import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.test.scaffolding.model.Color;

public class Root {
  private int int32;

  private long int64;

  private int uint32;

  private long uint64;

  private int sint32;

  private long sint64;

  private int fixed32;

  private long fixed64;

  private int sfixed32;

  private long sfixed64;

  private float floatValue;

  private double doubleValue;

  private boolean bool;

  private String string;

  private byte[] bytes;

  private Color color;

  private User user;

  private Map<String, String> ssMap;

  private Map<String, User> spMap;

  private List<String> sList;

  private List<User> pList;

  private Object any;

  private List<Object> anys;

  private Root typeRecursive;

  public int getInt32() {
    return int32;
  }

  public void setInt32(int int32) {
    this.int32 = int32;
  }

  public long getInt64() {
    return int64;
  }

  public void setInt64(long int64) {
    this.int64 = int64;
  }

  public int getUint32() {
    return uint32;
  }

  public void setUint32(int uint32) {
    this.uint32 = uint32;
  }

  public long getUint64() {
    return uint64;
  }

  public void setUint64(long uint64) {
    this.uint64 = uint64;
  }

  public int getSint32() {
    return sint32;
  }

  public void setSint32(int sint32) {
    this.sint32 = sint32;
  }

  public long getSint64() {
    return sint64;
  }

  public void setSint64(long sint64) {
    this.sint64 = sint64;
  }

  public int getFixed32() {
    return fixed32;
  }

  public void setFixed32(int fixed32) {
    this.fixed32 = fixed32;
  }

  public long getFixed64() {
    return fixed64;
  }

  public void setFixed64(long fixed64) {
    this.fixed64 = fixed64;
  }

  public int getSfixed32() {
    return sfixed32;
  }

  public void setSfixed32(int sfixed32) {
    this.sfixed32 = sfixed32;
  }

  public long getSfixed64() {
    return sfixed64;
  }

  public void setSfixed64(long sfixed64) {
    this.sfixed64 = sfixed64;
  }

  public float getFloatValue() {
    return floatValue;
  }

  public void setFloatValue(float floatValue) {
    this.floatValue = floatValue;
  }

  public double getDoubleValue() {
    return doubleValue;
  }

  public void setDoubleValue(double doubleValue) {
    this.doubleValue = doubleValue;
  }

  public boolean isBool() {
    return bool;
  }

  public void setBool(boolean bool) {
    this.bool = bool;
  }

  public String getString() {
    return string;
  }

  public void setString(String string) {
    this.string = string;
  }

  public byte[] getBytes() {
    return bytes;
  }

  public void setBytes(byte[] bytes) {
    this.bytes = bytes;
  }

  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Map<String, String> getSsMap() {
    return ssMap;
  }

  public void setSsMap(Map<String, String> ssMap) {
    this.ssMap = ssMap;
  }

  public Map<String, User> getSpMap() {
    return spMap;
  }

  public void setSpMap(Map<String, User> spMap) {
    this.spMap = spMap;
  }

  public List<String> getsList() {
    return sList;
  }

  public void setsList(List<String> sList) {
    this.sList = sList;
  }

  public List<User> getpList() {
    return pList;
  }

  public void setpList(List<User> pList) {
    this.pList = pList;
  }

  public Object getAny() {
    return any;
  }

  public void setAny(Object any) {
    this.any = any;
  }

  public List<Object> getAnys() {
    return anys;
  }

  public void setAnys(List<Object> anys) {
    this.anys = anys;
  }

  public Root getTypeRecursive() {
    return typeRecursive;
  }

  public void setTypeRecursive(Root typeRecursive) {
    this.typeRecursive = typeRecursive;
  }
}
