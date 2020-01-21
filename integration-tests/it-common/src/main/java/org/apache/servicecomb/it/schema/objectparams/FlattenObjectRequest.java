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

public class FlattenObjectRequest {
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

  public void setAnByte(byte anByte) {
    this.anByte = anByte;
  }

  public short getAnShort() {
    return anShort;
  }

  public void setAnShort(short anShort) {
    this.anShort = anShort;
  }

  public int getAnInt() {
    return anInt;
  }

  public void setAnInt(int anInt) {
    this.anInt = anInt;
  }

  public long getAnLong() {
    return anLong;
  }

  public void setAnLong(long anLong) {
    this.anLong = anLong;
  }

  public float getAnFloat() {
    return anFloat;
  }

  public void setAnFloat(float anFloat) {
    this.anFloat = anFloat;
  }

  public double getAnDouble() {
    return anDouble;
  }

  public void setAnDouble(double anDouble) {
    this.anDouble = anDouble;
  }

  public boolean isAnBoolean() {
    return anBoolean;
  }

  public void setAnBoolean(boolean anBoolean) {
    this.anBoolean = anBoolean;
  }

  public char getAnChar() {
    return anChar;
  }

  public void setAnChar(char anChar) {
    this.anChar = anChar;
  }

  public Byte getAnWrappedByte() {
    return anWrappedByte;
  }

  public void setAnWrappedByte(Byte anWrappedByte) {
    this.anWrappedByte = anWrappedByte;
  }

  public Short getAnWrappedShort() {
    return anWrappedShort;
  }

  public void setAnWrappedShort(Short anWrappedShort) {
    this.anWrappedShort = anWrappedShort;
  }

  public Integer getAnWrappedInteger() {
    return anWrappedInteger;
  }

  public void setAnWrappedInteger(Integer anWrappedInteger) {
    this.anWrappedInteger = anWrappedInteger;
  }

  public Long getAnWrappedLong() {
    return anWrappedLong;
  }

  public void setAnWrappedLong(Long anWrappedLong) {
    this.anWrappedLong = anWrappedLong;
  }

  public Float getAnWrappedFloat() {
    return anWrappedFloat;
  }

  public void setAnWrappedFloat(Float anWrappedFloat) {
    this.anWrappedFloat = anWrappedFloat;
  }

  public Double getAnWrappedDouble() {
    return anWrappedDouble;
  }

  public void setAnWrappedDouble(Double anWrappedDouble) {
    this.anWrappedDouble = anWrappedDouble;
  }

  public Boolean getAnWrappedBoolean() {
    return anWrappedBoolean;
  }

  public void setAnWrappedBoolean(Boolean anWrappedBoolean) {
    this.anWrappedBoolean = anWrappedBoolean;
  }

  public Character getAnWrappedCharacter() {
    return anWrappedCharacter;
  }

  public void setAnWrappedCharacter(Character anWrappedCharacter) {
    this.anWrappedCharacter = anWrappedCharacter;
  }

  public String getString() {
    return string;
  }

  public void setString(String string) {
    this.string = string;
  }

  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
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
    FlattenObjectRequest that = (FlattenObjectRequest) o;
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
}