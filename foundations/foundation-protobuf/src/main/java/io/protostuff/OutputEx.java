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

import static io.protostuff.ProtobufOutputEx.encodeZigZag64;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * <pre>
 * Forked and modified from protostuff
 * difference for scalar types:
 *                   OutputEx         OutputEx
 * writeXXX          all scenes     repeated not packed, write with tag, not ignore default value
 * writePackedXXX    ---            repeated packed, write without tag, not ignore default value
 * writeScalarXXX    ---            not repeated field, not write default value
 * </pre>
 *
 * An OutputEx lets an application write primitive data types and objects to a sink of data.
 *
 * @author David Yu
 * @created Nov 9, 2009
 */
public interface OutputEx {

  /**
   * Writes a variable int field.
   */
  void writeInt32(int tag, int tagSize, int value) throws IOException;

  /**
   * Writes an unsigned int field.
   */
  void writeUInt32(int tag, int tagSize, int value) throws IOException;

  /**
   * Writes a signed int field.
   */
  void writeSInt32(int tag, int tagSize, int value) throws IOException;

  /**
   * Writes a fixed int(4 bytes) field.
   */
  void writeFixed32(int tag, int tagSize, int value) throws IOException;

  /**
   * Writes a signed+fixed int(4 bytes) field.
   */
  void writeSFixed32(int tag, int tagSize, int value) throws IOException;

  /**
   * Writes a variable long field.
   */
  void writeInt64(int tag, int tagSize, long value) throws IOException;

  /**
   * Writes an unsigned long field.
   */
  void writeUInt64(int tag, int tagSize, long value) throws IOException;

  /**
   * Writes a signed long field.
   */
  void writeSInt64(int tag, int tagSize, long value) throws IOException;

  /**
   * Writes a fixed long(8 bytes) field.
   */
  void writeFixed64(int tag, int tagSize, long value) throws IOException;

  /**
   * Writes a signed+fixed long(8 bytes) field.
   */
  void writeSFixed64(int tag, int tagSize, long value) throws IOException;

  /**
   * Writes a float field.
   */
  void writeFloat(int tag, int tagSize, float value) throws IOException;

  /**
   * Writes a double field.
   */
  void writeDouble(int tag, int tagSize, double value) throws IOException;

  /**
   * Writes a boolean field.
   */
  void writeBool(int tag, int tagSize, boolean value) throws IOException;

  /**
   * Writes a enum(its number) field.
   */
  void writeEnum(int tag, int tagSize, int value) throws IOException;

  /**
   * Writes a String field.
   */
  void writeString(int tag, int tagSize, String value) throws IOException;

  /**
   * Writes a ByteString(wraps byte array) field.
   */
  void writeBytes(int tag, int tagSize, ByteString value) throws IOException;

  /**
   * Writes a byte array field.
   */
  void writeByteArray(int tag, int tagSize, byte[] value) throws IOException;

  /**
   * Writes a binary or a pre-encoded utf8 string.
   */
  void writeByteRange(boolean utf8String, int tag, int tagSize, byte[] value, int offset, int length)
      throws IOException;

  /**
   * Writes an object(using its schema) field.
   */
  <T> void writeObject(int tag, int tagSize, T value, SchemaWriter<T> schemaWriter) throws IOException;

  void writeBytes(int tag, int tagSize, ByteBuffer value) throws IOException;

  byte[] toByteArray();

  void writePackedInt32(int value) throws IOException;

  void writeScalarInt32(int tag, int tagSize, int value) throws IOException;

  default void writePackedInt64(long value) throws IOException {
    writePackedUInt64(value);
  }

  void writeScalarInt64(int tag, int tagSize, long value) throws IOException;

  void writePackedUInt32(int value) throws IOException;

  void writeScalarUInt32(int tag, int tagSize, int value) throws IOException;

  void writePackedUInt64(long value) throws IOException;

  void writeScalarUInt64(int tag, int tagSize, long value) throws IOException;

  void writePackedSInt32(final int value) throws IOException;

  void writeScalarSInt32(int tag, int tagSize, int value) throws IOException;

  default void writePackedSInt64(long value) throws IOException {
    writePackedUInt64(encodeZigZag64(value));
  }

  void writeScalarSInt64(int tag, int tagSize, long value) throws IOException;

  void writePackedFixed32(int value) throws IOException;

  void writeScalarFixed32(int tag, int tagSize, int value) throws IOException;

  void writePackedFixed64(long value) throws IOException;

  void writeScalarFixed64(int tag, int tagSize, long value) throws IOException;

  default void writePackedSFixed32(int value) throws IOException {
    writePackedFixed32(value);
  }

  void writeScalarSFixed32(int tag, int tagSize, int value) throws IOException;

  default void writePackedSFixed64(long value) throws IOException {
    writePackedFixed64(value);
  }

  void writeScalarSFixed64(int tag, int tagSize, long value) throws IOException;

  default void writePackedFloat(float value) throws IOException {
    writePackedFixed32(Float.floatToRawIntBits(value));
  }

  void writeScalarFloat(int tag, int tagSize, float value) throws IOException;

  default void writePackedDouble(double value) throws IOException {
    writePackedFixed64(Double.doubleToRawLongBits(value));
  }

  void writeScalarDouble(int tag, int tagSize, double value) throws IOException;

  void writePackedBool(boolean value) throws IOException;

  void writeScalarBool(int tag, int tagSize, boolean value) throws IOException;

  default void writePackedEnum(int value) throws IOException {
    writePackedInt32(value);
  }

  void writeScalarEnum(int tag, int tagSize, int value) throws IOException;

  void writeScalarString(int tag, int tagSize, String value) throws IOException;
}
