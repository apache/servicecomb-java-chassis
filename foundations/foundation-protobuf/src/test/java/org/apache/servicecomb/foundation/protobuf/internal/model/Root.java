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

import io.protostuff.Tag;

/**
 * Tag annotation is only for protoStuff, not necessary for ServiceComb codec
 */
public class Root {
  @Tag(1)
  private int int32;

  @Tag(2)
  private long int64;

  @Tag(3)
  private int uint32;

  @Tag(4)
  private long uint64;

  @Tag(5)
  private int sint32;

  @Tag(6)
  private long sint64;

  @Tag(7)
  private int fixed32;

  @Tag(8)
  private long fixed64;

  @Tag(9)
  private int sfixed32;

  @Tag(10)
  private long sfixed64;

  @Tag(11)
  private float floatValue;

  @Tag(12)
  private double doubleValue;

  @Tag(13)
  private boolean bool;

  @Tag(20)
  private Integer objInt32;

  @Tag(21)
  private Long objInt64;

  @Tag(22)
  private Integer objUint32;

  @Tag(23)
  private Long objUint64;

  @Tag(24)
  private Integer objSint32;

  @Tag(25)
  private Long objSint64;

  @Tag(26)
  private Integer objFixed32;

  @Tag(27)
  private Long objFixed64;

  @Tag(28)
  private Integer objSfixed32;

  @Tag(29)
  private Long objSfixed64;

  @Tag(30)
  private Float objFloatValue;

  @Tag(31)
  private Double objDoubleValue;

  @Tag(32)
  private Boolean objBool;

  @Tag(40)
  private String string;

  @Tag(41)
  private byte[] bytes;

  @Tag(42)
  private Color color;

  @Tag(43)
  private User user;

  @Tag(44)
  private Root typeRecursive;

  @Tag(50)
  private Object any;

  @Tag(51)
  private List<Object> anys;

  @Tag(60)
  private Map<String, String> ssMap;

  @Tag(61)
  private Map<String, Integer> sint32Map;

  @Tag(62)
  private Map<String, User> spMap;

  // repeated packed
  @Tag(70)
  private List<Integer> int32sPacked;

  @Tag(71)
  private List<Long> int64sPacked;

  @Tag(72)
  private List<Integer> uint32sPacked;

  @Tag(73)
  private List<Long> uint64sPacked;

  @Tag(74)
  private List<Integer> sint32sPacked;

  @Tag(75)
  private List<Long> sint64sPacked;

  @Tag(76)
  private List<Integer> fixed32sPacked;

  @Tag(77)
  private List<Long> fixed64sPacked;

  @Tag(78)
  private List<Integer> sfixed32sPacked;

  @Tag(79)
  private List<Long> sfixed64sPacked;

  @Tag(80)
  private List<Float> floatsPacked;

  @Tag(81)
  private List<Double> doublesPacked;

  @Tag(82)
  private List<Boolean> boolsPacked;

  @Tag(83)
  private List<Color> colorsPacked;

  // repeated not packed
  @Tag(90)
  private List<Integer> int32sNotPacked;

  @Tag(91)
  private List<Long> int64sNotPacked;

  @Tag(92)
  private List<Integer> uint32sNotPacked;

  @Tag(93)
  private List<Long> uint64sNotPacked;

  @Tag(94)
  private List<Integer> sint32sNotPacked;

  @Tag(95)
  private List<Long> sint64sNotPacked;

  @Tag(96)
  private List<Integer> fixed32sNotPacked;

  @Tag(97)
  private List<Long> fixed64sNotPacked;

  @Tag(98)
  private List<Integer> sfixed32sNotPacked;

  @Tag(99)
  private List<Long> sfixed64sNotPacked;

  @Tag(100)
  private List<Float> floatsNotPacked;

  @Tag(101)
  private List<Double> doublesNotPacked;

  @Tag(102)
  private List<Boolean> boolsNotPacked;

  @Tag(103)
  private List<Color> colorsNotPacked;

  @Tag(110)
  private List<String> strings;

  @Tag(111)
  private List<byte[]> bytess;

  @Tag(112)
  private List<User> users;

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

  public List<Integer> getInt32sPacked() {
    return int32sPacked;
  }

  public void setInt32sPacked(List<Integer> int32sPacked) {
    this.int32sPacked = int32sPacked;
  }

  public List<Long> getInt64sPacked() {
    return int64sPacked;
  }

  public void setInt64sPacked(List<Long> int64sPacked) {
    this.int64sPacked = int64sPacked;
  }

  public List<Integer> getUint32sPacked() {
    return uint32sPacked;
  }

  public void setUint32sPacked(List<Integer> uint32sPacked) {
    this.uint32sPacked = uint32sPacked;
  }

  public List<Long> getUint64sPacked() {
    return uint64sPacked;
  }

  public void setUint64sPacked(List<Long> uint64sPacked) {
    this.uint64sPacked = uint64sPacked;
  }

  public List<Integer> getSint32sPacked() {
    return sint32sPacked;
  }

  public void setSint32sPacked(List<Integer> sint32sPacked) {
    this.sint32sPacked = sint32sPacked;
  }

  public List<Long> getSint64sPacked() {
    return sint64sPacked;
  }

  public void setSint64sPacked(List<Long> sint64sPacked) {
    this.sint64sPacked = sint64sPacked;
  }

  public List<Integer> getFixed32sPacked() {
    return fixed32sPacked;
  }

  public void setFixed32sPacked(List<Integer> fixed32sPacked) {
    this.fixed32sPacked = fixed32sPacked;
  }

  public List<Long> getFixed64sPacked() {
    return fixed64sPacked;
  }

  public void setFixed64sPacked(List<Long> fixed64sPacked) {
    this.fixed64sPacked = fixed64sPacked;
  }

  public List<Integer> getSfixed32sPacked() {
    return sfixed32sPacked;
  }

  public void setSfixed32sPacked(List<Integer> sfixed32sPacked) {
    this.sfixed32sPacked = sfixed32sPacked;
  }

  public List<Long> getSfixed64sPacked() {
    return sfixed64sPacked;
  }

  public void setSfixed64sPacked(List<Long> sfixed64sPacked) {
    this.sfixed64sPacked = sfixed64sPacked;
  }

  public List<Float> getFloatsPacked() {
    return floatsPacked;
  }

  public void setFloatsPacked(List<Float> floatsPacked) {
    this.floatsPacked = floatsPacked;
  }

  public List<Double> getDoublesPacked() {
    return doublesPacked;
  }

  public void setDoublesPacked(List<Double> doublesPacked) {
    this.doublesPacked = doublesPacked;
  }

  public List<Boolean> getBoolsPacked() {
    return boolsPacked;
  }

  public void setBoolsPacked(List<Boolean> boolsPacked) {
    this.boolsPacked = boolsPacked;
  }

  public List<Color> getColorsPacked() {
    return colorsPacked;
  }

  public void setColorsPacked(List<Color> colorsPacked) {
    this.colorsPacked = colorsPacked;
  }

  public List<Integer> getInt32sNotPacked() {
    return int32sNotPacked;
  }

  public void setInt32sNotPacked(List<Integer> int32sNotPacked) {
    this.int32sNotPacked = int32sNotPacked;
  }

  public List<Long> getInt64sNotPacked() {
    return int64sNotPacked;
  }

  public void setInt64sNotPacked(List<Long> int64sNotPacked) {
    this.int64sNotPacked = int64sNotPacked;
  }

  public List<Integer> getUint32sNotPacked() {
    return uint32sNotPacked;
  }

  public void setUint32sNotPacked(List<Integer> uint32sNotPacked) {
    this.uint32sNotPacked = uint32sNotPacked;
  }

  public List<Long> getUint64sNotPacked() {
    return uint64sNotPacked;
  }

  public void setUint64sNotPacked(List<Long> uint64sNotPacked) {
    this.uint64sNotPacked = uint64sNotPacked;
  }

  public List<Integer> getSint32sNotPacked() {
    return sint32sNotPacked;
  }

  public void setSint32sNotPacked(List<Integer> sint32sNotPacked) {
    this.sint32sNotPacked = sint32sNotPacked;
  }

  public List<Long> getSint64sNotPacked() {
    return sint64sNotPacked;
  }

  public void setSint64sNotPacked(List<Long> sint64sNotPacked) {
    this.sint64sNotPacked = sint64sNotPacked;
  }

  public List<Integer> getFixed32sNotPacked() {
    return fixed32sNotPacked;
  }

  public void setFixed32sNotPacked(List<Integer> fixed32sNotPacked) {
    this.fixed32sNotPacked = fixed32sNotPacked;
  }

  public List<Long> getFixed64sNotPacked() {
    return fixed64sNotPacked;
  }

  public void setFixed64sNotPacked(List<Long> fixed64sNotPacked) {
    this.fixed64sNotPacked = fixed64sNotPacked;
  }

  public List<Integer> getSfixed32sNotPacked() {
    return sfixed32sNotPacked;
  }

  public void setSfixed32sNotPacked(List<Integer> sfixed32sNotPacked) {
    this.sfixed32sNotPacked = sfixed32sNotPacked;
  }

  public List<Long> getSfixed64sNotPacked() {
    return sfixed64sNotPacked;
  }

  public void setSfixed64sNotPacked(List<Long> sfixed64sNotPacked) {
    this.sfixed64sNotPacked = sfixed64sNotPacked;
  }

  public List<Float> getFloatsNotPacked() {
    return floatsNotPacked;
  }

  public void setFloatsNotPacked(List<Float> floatsNotPacked) {
    this.floatsNotPacked = floatsNotPacked;
  }

  public List<Double> getDoublesNotPacked() {
    return doublesNotPacked;
  }

  public void setDoublesNotPacked(List<Double> doublesNotPacked) {
    this.doublesNotPacked = doublesNotPacked;
  }

  public List<Boolean> getBoolsNotPacked() {
    return boolsNotPacked;
  }

  public void setBoolsNotPacked(List<Boolean> boolsNotPacked) {
    this.boolsNotPacked = boolsNotPacked;
  }

  public List<Color> getColorsNotPacked() {
    return colorsNotPacked;
  }

  public void setColorsNotPacked(
      List<Color> colorsNotPacked) {
    this.colorsNotPacked = colorsNotPacked;
  }

  public List<String> getStrings() {
    return strings;
  }

  public void setStrings(List<String> strings) {
    this.strings = strings;
  }

  public List<byte[]> getBytess() {
    return bytess;
  }

  public void setBytess(List<byte[]> bytess) {
    this.bytess = bytess;
  }

  public List<User> getUsers() {
    return users;
  }

  public void setUsers(List<User> users) {
    this.users = users;
  }

  public Integer getObjInt32() {
    return objInt32;
  }

  public void setObjInt32(Integer objInt32) {
    this.objInt32 = objInt32;
  }

  public Long getObjInt64() {
    return objInt64;
  }

  public void setObjInt64(Long objInt64) {
    this.objInt64 = objInt64;
  }

  public Integer getObjUint32() {
    return objUint32;
  }

  public void setObjUint32(Integer objUint32) {
    this.objUint32 = objUint32;
  }

  public Long getObjUint64() {
    return objUint64;
  }

  public void setObjUint64(Long objUint64) {
    this.objUint64 = objUint64;
  }

  public Integer getObjSint32() {
    return objSint32;
  }

  public void setObjSint32(Integer objSint32) {
    this.objSint32 = objSint32;
  }

  public Long getObjSint64() {
    return objSint64;
  }

  public void setObjSint64(Long objSint64) {
    this.objSint64 = objSint64;
  }

  public Integer getObjFixed32() {
    return objFixed32;
  }

  public void setObjFixed32(Integer objFixed32) {
    this.objFixed32 = objFixed32;
  }

  public Long getObjFixed64() {
    return objFixed64;
  }

  public void setObjFixed64(Long objFixed64) {
    this.objFixed64 = objFixed64;
  }

  public Integer getObjSfixed32() {
    return objSfixed32;
  }

  public void setObjSfixed32(Integer objSfixed32) {
    this.objSfixed32 = objSfixed32;
  }

  public Long getObjSfixed64() {
    return objSfixed64;
  }

  public void setObjSfixed64(Long objSfixed64) {
    this.objSfixed64 = objSfixed64;
  }

  public Float getObjFloatValue() {
    return objFloatValue;
  }

  public void setObjFloatValue(Float objFloatValue) {
    this.objFloatValue = objFloatValue;
  }

  public Double getObjDoubleValue() {
    return objDoubleValue;
  }

  public void setObjDoubleValue(Double objDoubleValue) {
    this.objDoubleValue = objDoubleValue;
  }

  public Boolean isObjBool() {
    return objBool;
  }

  public void setObjBool(Boolean objBool) {
    this.objBool = objBool;
  }

  public Map<String, Integer> getSint32Map() {
    return sint32Map;
  }

  public void setSint32Map(Map<String, Integer> sint32Map) {
    this.sint32Map = sint32Map;
  }
}
