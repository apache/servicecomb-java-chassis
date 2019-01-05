//========================================================================
//Copyright 2007-2010 David Yu dyuproject@gmail.com
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

import static io.protostuff.WireFormat.TAG_TYPE_BITS;
import static io.protostuff.WireFormat.WIRETYPE_END_GROUP;
import static io.protostuff.WireFormat.WIRETYPE_FIXED32;
import static io.protostuff.WireFormat.WIRETYPE_FIXED64;
import static io.protostuff.WireFormat.WIRETYPE_LENGTH_DELIMITED;
import static io.protostuff.WireFormat.WIRETYPE_START_GROUP;
import static io.protostuff.WireFormat.WIRETYPE_VARINT;
import static io.protostuff.WireFormat.getTagFieldNumber;
import static io.protostuff.WireFormat.getTagWireType;
import static io.protostuff.WireFormat.makeTag;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.fasterxml.jackson.databind.util.ArrayBuilders;

import io.protostuff.StringSerializer.STRING;

/**
 * Forked and modified from protostuff
 *
 * Reads and decodes protocol buffer message fields from an internal byte array buffer. This object is re-usable via
 * doing a reset on the byte array position and length. This is used internally by {@link IOUtil} where it catches
 * {@link ArrayIndexOutOfBoundsException} when a message is truncated.
 *
 * @author David Yu
 * @created Jun 22, 2010
 */
public final class ByteArrayInputEx implements InputEx {
  private final byte[] buffer;

  private int offset, limit, lastTag = 0;

  private int packedLimit = 0;

  private ArrayBuilders arrayBuilders;

  public ByteArrayInputEx(byte[] buffer) {
    this(buffer, 0, buffer.length);
  }

  public ByteArrayInputEx(byte[] buffer, int offset, int len) {
    this.buffer = buffer;
    this.offset = offset;
    this.limit = offset + len;
  }

  /**
   * Return true if currently reading packed field
   */
  public boolean isCurrentFieldPacked() {
    return packedLimit != 0 && packedLimit != offset;
  }

  /**
   * Attempt to read a field tag, returning zero if we have reached EOF. Protocol message parsers use this to read
   * tags, since a protocol message may legally end wherever a tag occurs, and zero is not a valid tag number.
   */
  public int readTag() throws IOException {
    if (offset == limit) {
      lastTag = 0;
      return 0;
    }

    final int tag = readRawVarint32();
    if (tag >>> TAG_TYPE_BITS == 0) {
      // If we actually read zero, that's not a valid tag.
      throw ProtobufException.invalidTag();
    }
    lastTag = tag;
    return tag;
  }

  /**
   * Verifies that the last call to readTag() returned the given tag value. This is used to verify that a nested group
   * ended with the correct end tag.
   *
   * @throws ProtobufException
   *             {@code value} does not match the last tag.
   */
  public void checkLastTagWas(final int value) throws ProtobufException {
    if (lastTag != value) {
      throw ProtobufException.invalidEndTag();
    }
  }

  /**
   * Reads and discards a single field, given its tag value.
   *
   * @return {@code false} if the tag is an endgroup tag, in which case nothing is skipped. Otherwise, returns
   *         {@code true}.
   */
  public boolean skipField(final int tag) throws IOException {
    switch (getTagWireType(tag)) {
      case WIRETYPE_VARINT:
        readInt32();
        return true;
      case WIRETYPE_FIXED64:
        readRawLittleEndian64();
        return true;
      case WIRETYPE_LENGTH_DELIMITED:
        final int size = readRawVarint32();
        if (size < 0) {
          throw ProtobufException.negativeSize();
        }
        offset += size;
        return true;
      case WIRETYPE_START_GROUP:
        skipMessage();
        checkLastTagWas(makeTag(getTagFieldNumber(tag), WIRETYPE_END_GROUP));
        return true;
      case WIRETYPE_END_GROUP:
        return false;
      case WIRETYPE_FIXED32:
        readRawLittleEndian32();
        return true;
      default:
        throw ProtobufException.invalidWireType();
    }
  }

  /**
   * Reads and discards an entire message. This will read either until EOF or until an endgroup tag, whichever comes
   * first.
   */
  public void skipMessage() throws IOException {
    while (true) {
      final int tag = readTag();
      if (tag == 0 || !skipField(tag)) {
        return;
      }
    }
  }

  @Override
  public void handleUnknownField(int fieldNumber) throws IOException {
    skipField(lastTag);
  }

  @Override
  public int readFieldNumber() throws IOException {
    if (offset == limit) {
      lastTag = 0;
      return 0;
    }

    // are we reading packed field?
    if (isCurrentFieldPacked()) {
      if (packedLimit < offset) {
        throw ProtobufException.misreportedSize();
      }

      // Return field number while reading packed field
      return lastTag >>> TAG_TYPE_BITS;
    }

    packedLimit = 0;
    final int tag = readRawVarint32();
    final int fieldNumber = tag >>> TAG_TYPE_BITS;
    if (fieldNumber == 0) {
      // If we actually read zero, that's not a valid tag.
      throw ProtobufException.invalidTag();
    }

    lastTag = tag;
    return fieldNumber;
  }

  /**
   * Check if this field have been packed into a length-delimited field. If so, update internal state to reflect that
   * packed fields are being read.
   *
   */
  private void checkIfPackedField() throws IOException {
    // Do we have the start of a packed field?
    if (packedLimit == 0 && getTagWireType(lastTag) == WIRETYPE_LENGTH_DELIMITED) {
      final int length = readRawVarint32();
      if (length < 0) {
        throw ProtobufException.negativeSize();
      }

      if (offset + length > limit) {
        throw ProtobufException.misreportedSize();
      }

      this.packedLimit = this.offset + length;
    }
  }

  /**
   * Read a {@code double} field value from the internal buffer.
   */
  @Override
  public double readDouble() {
    return Double.longBitsToDouble(readRawLittleEndian64());
  }

  /**
   * Read a {@code float} field value from the internal buffer.
   */
  @Override
  public float readFloat() {
    return Float.intBitsToFloat(readRawLittleEndian32());
  }

  /**
   * Read a {@code uint64} field value from the internal buffer.
   */
  @Override
  public long readUInt64() throws IOException {
    return readRawVarint64();
  }

  /**
   * Read an {@code int64} field value from the internal buffer.
   */
  @Override
  public long readInt64() throws IOException {
    return readRawVarint64();
  }

  /**
   * Read an {@code int32} field value from the internal buffer.
   */
  @Override
  public int readInt32() throws IOException {
    return readRawVarint32();
  }

  /**
   * Read a {@code fixed64} field value from the internal buffer.
   */
  @Override
  public long readFixed64() {
    return readRawLittleEndian64();
  }

  /**
   * Read a {@code fixed32} field value from the internal buffer.
   */
  @Override
  public int readFixed32() {
    return readRawLittleEndian32();
  }

  /**
   * Read a {@code bool} field value from the internal buffer.
   */
  @Override
  public boolean readBool() {
    return buffer[offset++] != 0;
  }

  /**
   * Read a {@code uint32} field value from the internal buffer.
   */
  @Override
  public int readUInt32() throws IOException {
    return readRawVarint32();
  }

  /**
   * Read an enum field value from the internal buffer. Caller is responsible for converting the numeric value to an
   * actual enum.
   */
  @Override
  public int readEnum() throws IOException {
    return readRawVarint32();
  }

  /**
   * Read an {@code sfixed32} field value from the internal buffer.
   */
  @Override
  public int readSFixed32() {
    return readRawLittleEndian32();
  }

  /**
   * Read an {@code sfixed64} field value from the internal buffer.
   */
  @Override
  public long readSFixed64() {
    return readRawLittleEndian64();
  }

  /**
   * Read an {@code sint32} field value from the internal buffer.
   */
  @Override
  public int readSInt32() throws IOException {
    final int n = readRawVarint32();
    return (n >>> 1) ^ -(n & 1);
  }

  /**
   * Read an {@code sint64} field value from the internal buffer.
   */
  @Override
  public long readSInt64() throws IOException {
    final long n = readRawVarint64();
    return (n >>> 1) ^ -(n & 1);
  }

  @Override
  public String readString() throws IOException {
    final int length = readRawVarint32();
    if (length < 0) {
      throw ProtobufException.negativeSize();
    }

    if (offset + length > limit) {
      throw ProtobufException.misreportedSize();
    }

    final int offset = this.offset;

    this.offset += length;

    return STRING.deser(buffer, offset, length);
  }

  @Override
  public ByteString readBytes() throws IOException {
    return ByteString.wrap(readByteArray());
  }

  @Override
  public byte[] readByteArray() throws IOException {
    final int length = readRawVarint32();
    if (length < 0) {
      throw ProtobufException.negativeSize();
    }

    if (offset + length > limit) {
      throw ProtobufException.misreportedSize();
    }

    final byte[] copy = new byte[length];
    System.arraycopy(buffer, offset, copy, 0, length);

    offset += length;

    return copy;
  }

  @Override
  public <T> T mergeObject(T value, final SchemaReader<T> schema) throws IOException {
    final int length = readRawVarint32();
    if (length < 0) {
      throw ProtobufException.negativeSize();
    }

    // save old limit
    final int oldLimit = this.limit;

    this.limit = offset + length;

    if (value == null) {
      value = schema.newMessage();
    }
    schema.mergeFrom(this, value);
    checkLastTagWas(0);

    // restore old limit
    this.limit = oldLimit;

    return value;
  }

  /**
   * Reads a var int 32 from the internal byte buffer.
   */
  public int readRawVarint32() throws IOException {
    byte tmp = buffer[offset++];
    if (tmp >= 0) {
      return tmp;
    }
    int result = tmp & 0x7f;
    if ((tmp = buffer[offset++]) >= 0) {
      result |= tmp << 7;
    } else {
      result |= (tmp & 0x7f) << 7;
      if ((tmp = buffer[offset++]) >= 0) {
        result |= tmp << 14;
      } else {
        result |= (tmp & 0x7f) << 14;
        if ((tmp = buffer[offset++]) >= 0) {
          result |= tmp << 21;
        } else {
          result |= (tmp & 0x7f) << 21;
          result |= (tmp = buffer[offset++]) << 28;
          if (tmp < 0) {
            // Discard upper 32 bits.
            for (int i = 0; i < 5; i++) {
              if (buffer[offset++] >= 0) {
                return result;
              }
            }
            throw ProtobufException.malformedVarint();
          }
        }
      }
    }
    return result;
  }

  /**
   * Reads a var int 64 from the internal byte buffer.
   */
  public long readRawVarint64() throws IOException {
    final byte[] buffer = this.buffer;
    int offset = this.offset;

    int shift = 0;
    long result = 0;
    while (shift < 64) {
      final byte b = buffer[offset++];
      result |= (long) (b & 0x7F) << shift;
      if ((b & 0x80) == 0) {
        this.offset = offset;
        return result;
      }
      shift += 7;
    }
    throw ProtobufException.malformedVarint();
  }

  /**
   * Read a 32-bit little-endian integer from the internal buffer.
   */
  public int readRawLittleEndian32() {
    final byte[] buffer = this.buffer;
    int offset = this.offset;

    final byte b1 = buffer[offset++];
    final byte b2 = buffer[offset++];
    final byte b3 = buffer[offset++];
    final byte b4 = buffer[offset++];

    this.offset = offset;

    return (((int) b1 & 0xff)) |
        (((int) b2 & 0xff) << 8) |
        (((int) b3 & 0xff) << 16) |
        (((int) b4 & 0xff) << 24);
  }

  /**
   * Read a 64-bit little-endian integer from the internal byte buffer.
   */
  public long readRawLittleEndian64() {
    final byte[] buffer = this.buffer;
    int offset = this.offset;

    final byte b1 = buffer[offset++];
    final byte b2 = buffer[offset++];
    final byte b3 = buffer[offset++];
    final byte b4 = buffer[offset++];
    final byte b5 = buffer[offset++];
    final byte b6 = buffer[offset++];
    final byte b7 = buffer[offset++];
    final byte b8 = buffer[offset++];

    this.offset = offset;

    return (((long) b1 & 0xff)) |
        (((long) b2 & 0xff) << 8) |
        (((long) b3 & 0xff) << 16) |
        (((long) b4 & 0xff) << 24) |
        (((long) b5 & 0xff) << 32) |
        (((long) b6 & 0xff) << 40) |
        (((long) b7 & 0xff) << 48) |
        (((long) b8 & 0xff) << 56);
  }

  /**
   * Reads a byte array/ByteBuffer value.
   */
  @Override
  public ByteBuffer readByteBuffer() throws IOException {
    return ByteBuffer.wrap(readByteArray());
  }

  @Override
  public ArrayBuilders getArrayBuilders() {
    if (arrayBuilders == null) {
      arrayBuilders = new ArrayBuilders();
    }
    return arrayBuilders;
  }

  @Override
  public int readPackedInt32() throws IOException {
    checkIfPackedField();
    return readRawVarint32();
  }

  @Override
  public int readPackedUInt32() throws IOException {
    checkIfPackedField();
    return readRawVarint32();
  }

  @Override
  public int readPackedSInt32() throws IOException {
    checkIfPackedField();
    final int n = readRawVarint32();
    return (n >>> 1) ^ -(n & 1);
  }

  @Override
  public int readPackedFixed32() throws IOException {
    checkIfPackedField();
    return readRawLittleEndian32();
  }

  @Override
  public int readPackedSFixed32() throws IOException {
    checkIfPackedField();
    return readRawLittleEndian32();
  }

  @Override
  public long readPackedInt64() throws IOException {
    checkIfPackedField();
    return readRawVarint64();
  }

  @Override
  public long readPackedUInt64() throws IOException {
    checkIfPackedField();
    return readRawVarint64();
  }

  @Override
  public long readPackedSInt64() throws IOException {
    checkIfPackedField();
    final long n = readRawVarint64();
    return (n >>> 1) ^ -(n & 1);
  }

  @Override
  public long readPackedFixed64() throws IOException {
    checkIfPackedField();
    return readRawLittleEndian64();
  }

  @Override
  public long readPackedSFixed64() throws IOException {
    checkIfPackedField();
    return readRawLittleEndian64();
  }

  @Override
  public float readPackedFloat() throws IOException {
    checkIfPackedField();
    return Float.intBitsToFloat(readRawLittleEndian32());
  }

  @Override
  public double readPackedDouble() throws IOException {
    checkIfPackedField();
    return Double.longBitsToDouble(readRawLittleEndian64());
  }

  @Override
  public boolean readPackedBool() throws IOException {
    checkIfPackedField();
    return buffer[offset++] != 0;
  }

  @Override
  public int readPackedEnum() throws IOException {
    checkIfPackedField();
    return readRawVarint32();
  }
}
