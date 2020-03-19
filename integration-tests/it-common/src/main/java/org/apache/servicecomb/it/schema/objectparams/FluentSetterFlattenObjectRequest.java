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

public class FluentSetterFlattenObjectRequest {
  private byte anByte;

  private short anShort;

  private int anInt;

  private long anLong;

  private float anFloat;

  private double anDouble;

  private boolean anBoolean;

  private char anChar;

  private Byte anWrappedByte;

  private Short anWrappedShort;

  private Integer anWrappedInteger;

  private Long anWrappedLong;

  private Float anWrappedFloat;

  private Double anWrappedDouble;

  private Boolean anWrappedBoolean;

  private Character anWrappedCharacter;

  private String string;

  private Color color;

  public byte getAnByte() {
    return anByte;
  }

  public FluentSetterFlattenObjectRequest setAnByte(byte anByte) {
    this.anByte = anByte;
    return this;
  }

  public short getAnShort() {
    return anShort;
  }

  public FluentSetterFlattenObjectRequest setAnShort(short anShort) {
    this.anShort = anShort;
    return this;
  }

  public int getAnInt() {
    return anInt;
  }

  public FluentSetterFlattenObjectRequest setAnInt(int anInt) {
    this.anInt = anInt;
    return this;
  }

  public long getAnLong() {
    return anLong;
  }

  public FluentSetterFlattenObjectRequest setAnLong(long anLong) {
    this.anLong = anLong;
    return this;
  }

  public float getAnFloat() {
    return anFloat;
  }

  public FluentSetterFlattenObjectRequest setAnFloat(float anFloat) {
    this.anFloat = anFloat;
    return this;
  }

  public double getAnDouble() {
    return anDouble;
  }

  public FluentSetterFlattenObjectRequest setAnDouble(double anDouble) {
    this.anDouble = anDouble;
    return this;
  }

  public boolean isAnBoolean() {
    return anBoolean;
  }

  public FluentSetterFlattenObjectRequest setAnBoolean(boolean anBoolean) {
    this.anBoolean = anBoolean;
    return this;
  }

  public char getAnChar() {
    return anChar;
  }

  public FluentSetterFlattenObjectRequest setAnChar(char anChar) {
    this.anChar = anChar;
    return this;
  }

  public Byte getAnWrappedByte() {
    return anWrappedByte;
  }

  public FluentSetterFlattenObjectRequest setAnWrappedByte(Byte anWrappedByte) {
    this.anWrappedByte = anWrappedByte;
    return this;
  }

  public Short getAnWrappedShort() {
    return anWrappedShort;
  }

  public FluentSetterFlattenObjectRequest setAnWrappedShort(Short anWrappedShort) {
    this.anWrappedShort = anWrappedShort;
    return this;
  }

  public Integer getAnWrappedInteger() {
    return anWrappedInteger;
  }

  public FluentSetterFlattenObjectRequest setAnWrappedInteger(Integer anWrappedInteger) {
    this.anWrappedInteger = anWrappedInteger;
    return this;
  }

  public Long getAnWrappedLong() {
    return anWrappedLong;
  }

  public FluentSetterFlattenObjectRequest setAnWrappedLong(Long anWrappedLong) {
    this.anWrappedLong = anWrappedLong;
    return this;
  }

  public Float getAnWrappedFloat() {
    return anWrappedFloat;
  }

  public FluentSetterFlattenObjectRequest setAnWrappedFloat(Float anWrappedFloat) {
    this.anWrappedFloat = anWrappedFloat;
    return this;
  }

  public Double getAnWrappedDouble() {
    return anWrappedDouble;
  }

  public FluentSetterFlattenObjectRequest setAnWrappedDouble(Double anWrappedDouble) {
    this.anWrappedDouble = anWrappedDouble;
    return this;
  }

  public Boolean getAnWrappedBoolean() {
    return anWrappedBoolean;
  }

  public FluentSetterFlattenObjectRequest setAnWrappedBoolean(Boolean anWrappedBoolean) {
    this.anWrappedBoolean = anWrappedBoolean;
    return this;
  }

  public Character getAnWrappedCharacter() {
    return anWrappedCharacter;
  }

  public FluentSetterFlattenObjectRequest setAnWrappedCharacter(Character anWrappedCharacter) {
    this.anWrappedCharacter = anWrappedCharacter;
    return this;
  }

  public String getString() {
    return string;
  }

  public FluentSetterFlattenObjectRequest setString(String string) {
    this.string = string;
    return this;
  }

  public Color getColor() {
    return color;
  }

  public FluentSetterFlattenObjectRequest setColor(Color color) {
    this.color = color;
    return this;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("FlattenObjectRequest{");
    sb.append("anByte=").append(anByte);
    sb.append(", anShort=").append(anShort);
    sb.append(", anInt=").append(anInt);
    sb.append(", anLong=").append(anLong);
    sb.append(", anFloat=").append(anFloat);
    sb.append(", anDouble=").append(anDouble);
    sb.append(", anBoolean=").append(anBoolean);
    sb.append(", anChar=").append(anChar);
    sb.append(", anWrappedByte=").append(anWrappedByte);
    sb.append(", anWrappedShort=").append(anWrappedShort);
    sb.append(", anWrappedInteger=").append(anWrappedInteger);
    sb.append(", anWrappedLong=").append(anWrappedLong);
    sb.append(", anWrappedFloat=").append(anWrappedFloat);
    sb.append(", anWrappedDouble=").append(anWrappedDouble);
    sb.append(", anWrappedBoolean=").append(anWrappedBoolean);
    sb.append(", anWrappedCharacter=").append(anWrappedCharacter);
    sb.append(", string='").append(string).append('\'');
    sb.append(", color=").append(color);
    sb.append('}');
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FluentSetterFlattenObjectRequest that = (FluentSetterFlattenObjectRequest) o;
    return anByte == that.anByte &&
        anShort == that.anShort &&
        anInt == that.anInt &&
        anLong == that.anLong &&
        Float.compare(that.anFloat, anFloat) == 0 &&
        Double.compare(that.anDouble, anDouble) == 0 &&
        anBoolean == that.anBoolean &&
        anChar == that.anChar &&
        Objects.equals(anWrappedByte, that.anWrappedByte) &&
        Objects.equals(anWrappedShort, that.anWrappedShort) &&
        Objects.equals(anWrappedInteger, that.anWrappedInteger) &&
        Objects.equals(anWrappedLong, that.anWrappedLong) &&
        Objects.equals(anWrappedFloat, that.anWrappedFloat) &&
        Objects.equals(anWrappedDouble, that.anWrappedDouble) &&
        Objects.equals(anWrappedBoolean, that.anWrappedBoolean) &&
        Objects.equals(anWrappedCharacter, that.anWrappedCharacter) &&
        Objects.equals(string, that.string) &&
        color == that.color;
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(anByte, anShort, anInt, anLong, anFloat, anDouble, anBoolean, anChar, anWrappedByte, anWrappedShort,
            anWrappedInteger,
            anWrappedLong, anWrappedFloat, anWrappedDouble, anWrappedBoolean, anWrappedCharacter, string, color);
  }

  public static FluentSetterFlattenObjectRequest createFlattenObjectRequest() {
    FluentSetterFlattenObjectRequest request = new FluentSetterFlattenObjectRequest();
    return request.setAnByte((byte) 8)
        .setAnShort((short) 7)
        .setAnInt(6)
        .setAnLong(5)
        .setAnFloat(4.4f)
        .setAnDouble(3.3)
        .setAnBoolean(true)
        .setAnChar('c')
        .setAnWrappedByte((byte) 16)
        .setAnWrappedShort((short) 15)
        .setAnWrappedInteger(14)
        .setAnWrappedLong(13L)
        .setAnWrappedFloat(12.2f)
        .setAnWrappedDouble(11.1)
        .setAnWrappedBoolean(true)
        .setAnWrappedCharacter('d')
        .setString("abc")
        .setColor(Color.BLUE);
  }
}