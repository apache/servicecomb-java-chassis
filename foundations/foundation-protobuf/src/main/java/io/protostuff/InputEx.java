//========================================================================
//Copyright 2007-2009 David Yu dyuproject@gmail.com
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package io.protostuff;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.fasterxml.jackson.databind.util.ArrayBuilders;

/**
 * Forked and modified from protostuff<br>
 *
 * An Input lets an application read primitive data types and objects from a source of data.
 *
 * @author David Yu
 * @created Nov 9, 2009
 */
public interface InputEx {

  /**
   * The underlying implementation should handle the unknown field.
   */
  <T> void handleUnknownField(int fieldNumber) throws IOException;

  /**
   * Reads field number.
   */
  int readFieldNumber() throws IOException;

  /**
   * Reads a variable int field value.
   */
  int readInt32() throws IOException;

  /**
   * Reads an unsigned int field value.
   */
  int readUInt32() throws IOException;

  /**
   * Reads a signed int field value.
   */
  int readSInt32() throws IOException;

  /**
   * Reads a fixed int(4 bytes) field value.
   */
  int readFixed32() throws IOException;

  /**
   * Reads a signed+fixed int(4 bytes) field value.
   */
  int readSFixed32() throws IOException;

  /**
   * Reads a variable long field value.
   */
  long readInt64() throws IOException;

  /**
   * Reads an unsigned long field value.
   */
  long readUInt64() throws IOException;

  /**
   * Reads a signed long field value.
   */
  long readSInt64() throws IOException;

  /**
   * Reads a fixed long(8 bytes) field value.
   */
  long readFixed64() throws IOException;

  /**
   * Reads a signed+fixed long(8 bytes) field value.
   */
  long readSFixed64() throws IOException;

  /**
   * Reads a float field value.
   */
  float readFloat() throws IOException;

  /**
   * Reads a double field value.
   */
  double readDouble() throws IOException;

  /**
   * Reads a boolean field value.
   */
  boolean readBool() throws IOException;

  /**
   * Reads an enum(its number) field value.
   */
  int readEnum() throws IOException;

  /**
   * Reads a {@link String} field value.
   */
  String readString() throws IOException;

  /**
   * Reads a {@link ByteString} field value.
   */
  ByteString readBytes() throws IOException;

  /**
   * Reads a byte array field value.
   */
  byte[] readByteArray() throws IOException;

  ByteBuffer readByteBuffer() throws IOException;

  /**
   * Merges an object(with schema) field value. The provided {@link Schema schema} handles the deserialization for the
   * object.
   */
  <T> T mergeObject(T value, SchemaReader<T> schema) throws IOException;

  ArrayBuilders getArrayBuilders();

  int readPackedInt32() throws IOException;

  int readPackedUInt32() throws IOException;

  int readPackedSInt32() throws IOException;

  int readPackedFixed32() throws IOException;

  int readPackedSFixed32() throws IOException;

  long readPackedInt64() throws IOException;

  long readPackedUInt64() throws IOException;

  long readPackedSInt64() throws IOException;

  long readPackedFixed64() throws IOException;

  long readPackedSFixed64() throws IOException;

  float readPackedFloat() throws IOException;

  double readPackedDouble() throws IOException;

  boolean readPackedBool() throws IOException;

  int readPackedEnum() throws IOException;
}
