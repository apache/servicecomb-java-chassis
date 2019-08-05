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

public class FlattenObjectResponse {
  private byte aByte;

  private short aShort;

  private int anInt;

  private long aLong;

  private float aFloat;

  private double aDouble;

  private boolean aBoolean;

  private char aChar;

  private Byte aWrappedByte;

  private Short aWrappedShort;

  private Integer aWrappedInteger;

  private Long aWrappedLong;

  private Float aWrappedFloat;

  private Double aWrappedDouble;

  private Boolean aWrappedBoolean;

  private Character aWrappedCharacter;

  private String string;

  private Color color;

  public byte getaByte() {
    return aByte;
  }

  public void setaByte(byte aByte) {
    this.aByte = aByte;
  }

  public short getaShort() {
    return aShort;
  }

  public void setaShort(short aShort) {
    this.aShort = aShort;
  }

  public int getAnInt() {
    return anInt;
  }

  public void setAnInt(int anInt) {
    this.anInt = anInt;
  }

  public long getaLong() {
    return aLong;
  }

  public void setaLong(long aLong) {
    this.aLong = aLong;
  }

  public float getaFloat() {
    return aFloat;
  }

  public void setaFloat(float aFloat) {
    this.aFloat = aFloat;
  }

  public double getaDouble() {
    return aDouble;
  }

  public void setaDouble(double aDouble) {
    this.aDouble = aDouble;
  }

  public boolean isaBoolean() {
    return aBoolean;
  }

  public void setaBoolean(boolean aBoolean) {
    this.aBoolean = aBoolean;
  }

  public char getaChar() {
    return aChar;
  }

  public void setaChar(char aChar) {
    this.aChar = aChar;
  }

  public Byte getaWrappedByte() {
    return aWrappedByte;
  }

  public void setaWrappedByte(Byte aWrappedByte) {
    this.aWrappedByte = aWrappedByte;
  }

  public Short getaWrappedShort() {
    return aWrappedShort;
  }

  public void setaWrappedShort(Short aWrappedShort) {
    this.aWrappedShort = aWrappedShort;
  }

  public Integer getaWrappedInteger() {
    return aWrappedInteger;
  }

  public void setaWrappedInteger(Integer aWrappedInteger) {
    this.aWrappedInteger = aWrappedInteger;
  }

  public Long getaWrappedLong() {
    return aWrappedLong;
  }

  public void setaWrappedLong(Long aWrappedLong) {
    this.aWrappedLong = aWrappedLong;
  }

  public Float getaWrappedFloat() {
    return aWrappedFloat;
  }

  public void setaWrappedFloat(Float aWrappedFloat) {
    this.aWrappedFloat = aWrappedFloat;
  }

  public Double getaWrappedDouble() {
    return aWrappedDouble;
  }

  public void setaWrappedDouble(Double aWrappedDouble) {
    this.aWrappedDouble = aWrappedDouble;
  }

  public Boolean getaWrappedBoolean() {
    return aWrappedBoolean;
  }

  public void setaWrappedBoolean(Boolean aWrappedBoolean) {
    this.aWrappedBoolean = aWrappedBoolean;
  }

  public Character getaWrappedCharacter() {
    return aWrappedCharacter;
  }

  public void setaWrappedCharacter(Character aWrappedCharacter) {
    this.aWrappedCharacter = aWrappedCharacter;
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
    final StringBuilder sb = new StringBuilder("FlattenObjectResponse{");
    sb.append("aByte=").append(aByte);
    sb.append(", aShort=").append(aShort);
    sb.append(", anInt=").append(anInt);
    sb.append(", aLong=").append(aLong);
    sb.append(", aFloat=").append(aFloat);
    sb.append(", aDouble=").append(aDouble);
    sb.append(", aBoolean=").append(aBoolean);
    sb.append(", aChar=").append(aChar);
    sb.append(", aWrappedByte=").append(aWrappedByte);
    sb.append(", aWrappedShort=").append(aWrappedShort);
    sb.append(", aWrappedInteger=").append(aWrappedInteger);
    sb.append(", aWrappedLong=").append(aWrappedLong);
    sb.append(", aWrappedFloat=").append(aWrappedFloat);
    sb.append(", aWrappedDouble=").append(aWrappedDouble);
    sb.append(", aWrappedBoolean=").append(aWrappedBoolean);
    sb.append(", aWrappedCharacter=").append(aWrappedCharacter);
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
    FlattenObjectResponse that = (FlattenObjectResponse) o;
    return aByte == that.aByte &&
        aShort == that.aShort &&
        anInt == that.anInt &&
        aLong == that.aLong &&
        Float.compare(that.aFloat, aFloat) == 0 &&
        Double.compare(that.aDouble, aDouble) == 0 &&
        aBoolean == that.aBoolean &&
        aChar == that.aChar &&
        Objects.equals(aWrappedByte, that.aWrappedByte) &&
        Objects.equals(aWrappedShort, that.aWrappedShort) &&
        Objects.equals(aWrappedInteger, that.aWrappedInteger) &&
        Objects.equals(aWrappedLong, that.aWrappedLong) &&
        Objects.equals(aWrappedFloat, that.aWrappedFloat) &&
        Objects.equals(aWrappedDouble, that.aWrappedDouble) &&
        Objects.equals(aWrappedBoolean, that.aWrappedBoolean) &&
        Objects.equals(aWrappedCharacter, that.aWrappedCharacter) &&
        Objects.equals(string, that.string) &&
        color == that.color;
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(aByte, aShort, anInt, aLong, aFloat, aDouble, aBoolean, aChar, aWrappedByte, aWrappedShort,
            aWrappedInteger,
            aWrappedLong, aWrappedFloat, aWrappedDouble, aWrappedBoolean, aWrappedCharacter, string, color);
  }
}
