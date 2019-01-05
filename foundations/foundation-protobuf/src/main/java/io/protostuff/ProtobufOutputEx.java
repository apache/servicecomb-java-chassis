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

import static io.protostuff.StringSerializer.writeUTF8VarDelimited;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Forked and modified from protostuff<br>
 *
 * Protobuf serialization where the messages must be fully buffered on memory before it can be written to the socket (
 * {@link OutputStream}).
 *
 * @author David Yu
 * @created May 18, 2010
 */
public final class ProtobufOutputEx extends WriteSession implements OutputEx {
  public static final int LITTLE_ENDIAN_32_SIZE = 4, LITTLE_ENDIAN_64_SIZE = 8;

  public ProtobufOutputEx() {
    super(LinkedBuffer.allocate());
  }

  public ProtobufOutputEx(LinkedBuffer buffer) {
    super(buffer);
  }

  public ProtobufOutputEx(LinkedBuffer buffer, int nextBufferSize) {
    super(buffer, nextBufferSize);
  }

  /**
   * Resets this output for re-use.
   */
  @Override
  public ProtobufOutputEx clear() {
    super.clear();
    return this;
  }

  @Override
  public final void writeInt32(int tag, int tagSize, int value) {
    if (value < 0) {
      tail = writeTagAndRawVarInt64(tag, tagSize, value, this, tail);
      return;
    }

    tail = writeTagAndRawVarInt32(tag, tagSize, value, this, tail);
  }

  @Override
  public final void writeUInt32(int tag, int tagSize, int value) {
    tail = writeTagAndRawVarInt32(tag, tagSize, value, this, tail);
  }

  @Override
  public final void writeSInt32(int tag, int tagSize, int value) {
    tail = writeTagAndRawVarInt32(tag, tagSize, encodeZigZag32(value), this, tail);
  }

  @Override
  public final void writeFixed32(int tag, int tagSize, int value) {
    tail = writeTagAndRawLittleEndian32(tag, tagSize, value, this, tail);
  }

  @Override
  public final void writeSFixed32(int tag, int tagSize, int value) {
    tail = writeTagAndRawLittleEndian32(tag, tagSize, value, this, tail);
  }

  @Override
  public final void writeInt64(int tag, int tagSize, long value) {
    tail = writeTagAndRawVarInt64(tag, tagSize, value, this, tail);
  }

  @Override
  public final void writeUInt64(int tag, int tagSize, long value) {
    tail = writeTagAndRawVarInt64(tag, tagSize, value, this, tail);
  }

  @Override
  public final void writeSInt64(int tag, int tagSize, long value) {
    tail = writeTagAndRawVarInt64(tag, tagSize, encodeZigZag64(value), this, tail);
  }

  @Override
  public final void writeFixed64(int tag, int tagSize, long value) {
    tail = writeTagAndRawLittleEndian64(tag, tagSize, value, this, tail);
  }

  @Override
  public final void writeSFixed64(int tag, int tagSize, long value) {
    tail = writeTagAndRawLittleEndian64(tag, tagSize, value, this, tail);
  }

  @Override
  public final void writeFloat(int tag, int tagSize, float value) {
    tail = writeTagAndRawLittleEndian32(tag, tagSize, Float.floatToRawIntBits(value), this, tail);
  }

  @Override
  public final void writeDouble(int tag, int tagSize, double value) {
    tail = writeTagAndRawLittleEndian64(tag, tagSize, Double.doubleToRawLongBits(value), this, tail);
  }

  @Override
  public final void writeBool(int tag, int tagSize, boolean value) {
    tail = writeTagAndRawVarInt32(tag, tagSize, value ? 1 : 0, this, tail);
  }

  @Override
  public final void writeEnum(int tag, int tagSize, int number) {
    writeInt32(tag, tagSize, number);
  }

  @Override
  public final void writeString(int tag, int tagSize, String value) {
    tail = writeUTF8VarDelimited(
        value,
        this,
        writeRawVarInt32(tag, this, tail));
  }

  @Override
  public final void writeBytes(int tag, int tagSize, ByteString value) {
    writeByteArray(tag, tagSize, value.getBytes());
  }

  @Override
  public final void writeByteArray(int tag, int tagSize, byte[] bytes) {
    tail = writeTagAndByteArray(
        tag, tagSize,
        bytes, 0, bytes.length,
        this,
        tail);
  }

  @Override
  public final void writeByteRange(boolean utf8String, int tag, int tagSize, byte[] value, int offset, int length) {
    tail = writeTagAndByteArray(
        tag, tagSize,
        value, offset, length,
        this,
        tail);
  }

  @Override
  public final <T> void writeObject(final int tag, int tagSize, final T value, final SchemaWriter<T> schemaWriter)
      throws IOException {
    final LinkedBuffer lastBuffer;

    // write the tag
    if (tagSize == 1 && tail.offset != tail.buffer.length) {
      lastBuffer = tail;
      size++;
      lastBuffer.buffer[lastBuffer.offset++] = (byte) tag;
    } else {
      tail = lastBuffer = writeRawVarInt32(tag, this, tail);
    }

    final int lastOffset = tail.offset, lastSize = size;

    if (lastOffset == lastBuffer.buffer.length) {
      // not enough size for the 1-byte delimiter
      final LinkedBuffer nextBuffer = new LinkedBuffer(nextBufferSize);
      // new buffer for the content
      tail = nextBuffer;

      schemaWriter.writeTo(this, value);

      final int msgSize = size - lastSize;

      final byte[] delimited = new byte[computeRawVarint32Size(msgSize)];
      writeRawVarInt32(msgSize, delimited, 0);

      size += delimited.length;

      // wrap the byte array (delimited) and insert between the two buffers
      new LinkedBuffer(delimited, 0, delimited.length, lastBuffer).next = nextBuffer;
      return;
    }

    // we have enough space for the 1-byte delim
    lastBuffer.offset++;
    size++;

    schemaWriter.writeTo(this, value);

    final int msgSize = size - lastSize - 1;

    // optimize for small messages
    if (msgSize < 128) {
      // fits
      lastBuffer.buffer[lastOffset] = (byte) msgSize;
      return;
    }

    // split into two buffers

    // the second buffer (contains the message contents)
    final LinkedBuffer view = new LinkedBuffer(lastBuffer.buffer,
        lastOffset + 1, lastBuffer.offset);

    if (lastBuffer == tail) {
      tail = view;
    } else {
      view.next = lastBuffer.next;
    }

    // the first buffer (contains the tag)
    lastBuffer.offset = lastOffset;

    final byte[] delimited = new byte[computeRawVarint32Size(msgSize)];
    writeRawVarInt32(msgSize, delimited, 0);

    // add the difference
    size += (delimited.length - 1);

    // wrap the byte array (delimited) and insert between the two buffers
    new LinkedBuffer(delimited, 0, delimited.length, lastBuffer).next = view;
  }

  /*
   * Write the nested message encoded as group.
   *
   * <T> void writeObjectEncodedAsGroup(final int fieldNumber, final T value, final SchemaEx<T> schema, final boolean
   * repeated) throws IOException { tail = writeRawVarInt32( makeTag(fieldNumber, WIRETYPE_START_GROUP), this, tail);
   *
   * schema.writeTo(this, value);
   *
   * tail = writeRawVarInt32( makeTag(fieldNumber, WIRETYPE_END_GROUP), this, tail); }
   */

  /* ----------------------------------------------------------------- */

  /**
   * Returns the buffer encoded with the variable int 32.
   */
  public static LinkedBuffer writeRawVarInt32(int value, final WriteSession session,
      LinkedBuffer lb) {
    final int size = computeRawVarint32Size(value);

    if (lb.offset + size > lb.buffer.length) {
      lb = new LinkedBuffer(session.nextBufferSize, lb);
    }

    final byte[] buffer = lb.buffer;
    int offset = lb.offset;
    lb.offset += size;
    session.size += size;

    if (size == 1) {
      buffer[offset] = (byte) value;
    } else {
      for (int i = 0, last = size - 1; i < last; i++, value >>>= 7) {
        buffer[offset++] = (byte) ((value & 0x7F) | 0x80);
      }

      buffer[offset] = (byte) value;
    }

    return lb;
  }

  /**
   * Returns the buffer encoded with the tag and byte array
   */
  public static LinkedBuffer writeTagAndByteArray(int tag, int tagSize, final byte[] value,
      int offset, int valueLen,
      final WriteSession session, LinkedBuffer lb) {
    if (valueLen == 0) {
      // write only the tag and delimiter
      return writeTagAndRawVarInt32(tag, tagSize, valueLen, session, lb);
    }

    lb = writeTagAndRawVarInt32(tag, tagSize, valueLen, session, lb);

    session.size += valueLen;

    final int available = lb.buffer.length - lb.offset;
    if (valueLen > available) {
      if (available + session.nextBufferSize < valueLen) {
        // too large ... so we wrap and insert (zero-copy)
        if (available == 0) {
          // buffer was actually full ... return a fresh buffer
          return new LinkedBuffer(session.nextBufferSize,
              new LinkedBuffer(value, offset, offset + valueLen, lb));
        }

        // continue with the existing byte array of the previous buffer
        return new LinkedBuffer(lb,
            new LinkedBuffer(value, offset, offset + valueLen, lb));
      }

      // copy what can fit
      System.arraycopy(value, offset, lb.buffer, lb.offset, available);

      lb.offset += available;

      // grow
      lb = new LinkedBuffer(session.nextBufferSize, lb);

      final int leftover = valueLen - available;

      // copy what's left
      System.arraycopy(value, offset + available, lb.buffer, 0, leftover);

      lb.offset += leftover;

      return lb;
    }

    // it fits
    System.arraycopy(value, offset, lb.buffer, lb.offset, valueLen);

    lb.offset += valueLen;

    return lb;
  }

  /**
   * Returns the buffer encoded with the tag and var int 32
   */
  public static LinkedBuffer writeTagAndRawVarInt32(int tag, int tagSize, int value,
      final WriteSession session, LinkedBuffer lb) {
    final int size = computeRawVarint32Size(value);
    final int totalSize = tagSize + size;

    if (lb.offset + totalSize > lb.buffer.length) {
      lb = new LinkedBuffer(session.nextBufferSize, lb);
    }

    final byte[] buffer = lb.buffer;
    int offset = lb.offset;
    lb.offset += totalSize;
    session.size += totalSize;

    if (tagSize == 1) {
      buffer[offset++] = (byte) tag;
    } else {
      for (int i = 0, last = tagSize - 1; i < last; i++, tag >>>= 7) {
        buffer[offset++] = (byte) ((tag & 0x7F) | 0x80);
      }

      buffer[offset++] = (byte) tag;
    }

    if (size == 1) {
      buffer[offset] = (byte) value;
    } else {
      for (int i = 0, last = size - 1; i < last; i++, value >>>= 7) {
        buffer[offset++] = (byte) ((value & 0x7F) | 0x80);
      }

      buffer[offset] = (byte) value;
    }

    return lb;
  }

  /**
   * Returns the buffer encoded with the tag and var int 64
   */
  public static LinkedBuffer writeTagAndRawVarInt64(int tag, int tagSize, long value,
      final WriteSession session, LinkedBuffer lb) {
    final int size = computeRawVarint64Size(value);
    final int totalSize = tagSize + size;

    if (lb.offset + totalSize > lb.buffer.length) {
      lb = new LinkedBuffer(session.nextBufferSize, lb);
    }

    final byte[] buffer = lb.buffer;
    int offset = lb.offset;
    lb.offset += totalSize;
    session.size += totalSize;

    if (tagSize == 1) {
      buffer[offset++] = (byte) tag;
    } else {
      for (int i = 0, last = tagSize - 1; i < last; i++, tag >>>= 7) {
        buffer[offset++] = (byte) ((tag & 0x7F) | 0x80);
      }

      buffer[offset++] = (byte) tag;
    }

    if (size == 1) {
      buffer[offset] = (byte) value;
    } else {
      for (int i = 0, last = size - 1; i < last; i++, value >>>= 7) {
        buffer[offset++] = (byte) (((int) value & 0x7F) | 0x80);
      }

      buffer[offset] = (byte) value;
    }

    return lb;
  }

  /**
   * Returns the buffer encoded with the tag and little endian 32
   */
  public static LinkedBuffer writeTagAndRawLittleEndian32(int tag, int tagSize, int value,
      final WriteSession session, LinkedBuffer lb) {
    final int totalSize = tagSize + LITTLE_ENDIAN_32_SIZE;

    if (lb.offset + totalSize > lb.buffer.length) {
      lb = new LinkedBuffer(session.nextBufferSize, lb);
    }

    final byte[] buffer = lb.buffer;
    int offset = lb.offset;
    lb.offset += totalSize;
    session.size += totalSize;

    if (tagSize == 1) {
      buffer[offset++] = (byte) tag;
    } else {
      for (int i = 0, last = tagSize - 1; i < last; i++, tag >>>= 7) {
        buffer[offset++] = (byte) ((tag & 0x7F) | 0x80);
      }

      buffer[offset++] = (byte) tag;
    }

    writeRawLittleEndian32(value, buffer, offset);

    return lb;
  }

  /**
   * Returns the buffer encoded with the tag and little endian 64
   */
  public static LinkedBuffer writeTagAndRawLittleEndian64(int tag, int tagSize, long value,
      final WriteSession session, LinkedBuffer lb) {
    final int totalSize = tagSize + LITTLE_ENDIAN_64_SIZE;

    if (lb.offset + totalSize > lb.buffer.length) {
      lb = new LinkedBuffer(session.nextBufferSize, lb);
    }

    final byte[] buffer = lb.buffer;
    int offset = lb.offset;
    lb.offset += totalSize;
    session.size += totalSize;

    if (tagSize == 1) {
      buffer[offset++] = (byte) tag;
    } else {
      for (int i = 0, last = tagSize - 1; i < last; i++, tag >>>= 7) {
        buffer[offset++] = (byte) ((tag & 0x7F) | 0x80);
      }

      buffer[offset++] = (byte) tag;
    }

    writeRawLittleEndian64(value, buffer, offset);

    return lb;
  }

  /** Encode and write a varint to the byte array */
  public static void writeRawVarInt32(int value, final byte[] buf, int offset) throws IOException {
    while (true) {
      if ((value & ~0x7F) == 0) {
        buf[offset] = (byte) value;
        return;
      } else {
        buf[offset++] = (byte) ((value & 0x7F) | 0x80);
        value >>>= 7;
      }
    }
  }

  /**
   * Writes the encoded little endian 32 and returns the bytes written
   */
  public static int writeRawLittleEndian32(int value, byte[] buffer, int offset) {
    if (buffer.length - offset < LITTLE_ENDIAN_32_SIZE) {
      throw new IllegalArgumentException("buffer capacity not enough.");
    }

    buffer[offset++] = (byte) (value & 0xFF);
    buffer[offset++] = (byte) (value >> 8 & 0xFF);
    buffer[offset++] = (byte) (value >> 16 & 0xFF);
    buffer[offset] = (byte) (value >> 24 & 0xFF);

    return LITTLE_ENDIAN_32_SIZE;
  }

  /**
   * Writes the encoded little endian 64 and returns the bytes written
   */
  public static int writeRawLittleEndian64(long value, byte[] buffer, int offset) {
    if (buffer.length - offset < LITTLE_ENDIAN_64_SIZE) {
      throw new IllegalArgumentException("buffer capacity not enough.");
    }

    buffer[offset++] = (byte) (value & 0xFF);
    buffer[offset++] = (byte) (value >> 8 & 0xFF);
    buffer[offset++] = (byte) (value >> 16 & 0xFF);
    buffer[offset++] = (byte) (value >> 24 & 0xFF);
    buffer[offset++] = (byte) (value >> 32 & 0xFF);
    buffer[offset++] = (byte) (value >> 40 & 0xFF);
    buffer[offset++] = (byte) (value >> 48 & 0xFF);
    buffer[offset] = (byte) (value >> 56 & 0xFF);

    return LITTLE_ENDIAN_64_SIZE;
  }

  /* METHODS FROM CodedOutput */

  // Protocol Buffers - Google's data interchange format
  // Copyright 2008 Google Inc. All rights reserved.
  // http://code.google.com/p/protobuf/
  //
  // Redistribution and use in source and binary forms, with or without
  // modification, are permitted provided that the following conditions are
  // met:
  //
  // * Redistributions of source code must retain the above copyright
  // notice, this list of conditions and the following disclaimer.
  // * Redistributions in binary form must reproduce the above
  // copyright notice, this list of conditions and the following disclaimer
  // in the documentation and/or other materials provided with the
  // distribution.
  // * Neither the name of Google Inc. nor the names of its
  // contributors may be used to endorse or promote products derived from
  // this software without specific prior written permission.
  //
  // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  // A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  // OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  // SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  // LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  // DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  // THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  // (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  // OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

  /**
   * Compute the number of bytes that would be needed to encode a varint. {@code value} is treated as unsigned, so it
   * won't be sign-extended if negative.
   */
  public static int computeRawVarint32Size(final int value) {
    if ((value & (0xffffffff << 7)) == 0) {
      return 1;
    }
    if ((value & (0xffffffff << 14)) == 0) {
      return 2;
    }
    if ((value & (0xffffffff << 21)) == 0) {
      return 3;
    }
    if ((value & (0xffffffff << 28)) == 0) {
      return 4;
    }
    return 5;
  }

  /**
   * Compute the number of bytes that would be needed to encode a varint.
   */
  public static int computeRawVarint64Size(final long value) {
    if ((value & (0xffffffffffffffffL << 7)) == 0) {
      return 1;
    }
    if ((value & (0xffffffffffffffffL << 14)) == 0) {
      return 2;
    }
    if ((value & (0xffffffffffffffffL << 21)) == 0) {
      return 3;
    }
    if ((value & (0xffffffffffffffffL << 28)) == 0) {
      return 4;
    }
    if ((value & (0xffffffffffffffffL << 35)) == 0) {
      return 5;
    }
    if ((value & (0xffffffffffffffffL << 42)) == 0) {
      return 6;
    }
    if ((value & (0xffffffffffffffffL << 49)) == 0) {
      return 7;
    }
    if ((value & (0xffffffffffffffffL << 56)) == 0) {
      return 8;
    }
    if ((value & (0xffffffffffffffffL << 63)) == 0) {
      return 9;
    }
    return 10;
  }

  /**
   * Encode a ZigZag-encoded 32-bit value. ZigZag encodes signed integers into values that can be efficiently encoded
   * with varint. (Otherwise, negative values must be sign-extended to 64 bits to be varint encoded, thus always
   * taking 10 bytes on the wire.)
   *
   * @param n
   *            A signed 32-bit integer.
   * @return An unsigned 32-bit integer, stored in a signed int because Java has no explicit unsigned support.
   */
  public static int encodeZigZag32(final int n) {
    // Note: the right-shift must be arithmetic
    return (n << 1) ^ (n >> 31);
  }

  /**
   * Encode a ZigZag-encoded 64-bit value. ZigZag encodes signed integers into values that can be efficiently encoded
   * with varint. (Otherwise, negative values must be sign-extended to 64 bits to be varint encoded, thus always
   * taking 10 bytes on the wire.)
   *
   * @param n
   *            A signed 64-bit integer.
   * @return An unsigned 64-bit integer, stored in a signed int because Java has no explicit unsigned support.
   */
  public static long encodeZigZag64(final long n) {
    // Note: the right-shift must be arithmetic
    return (n << 1) ^ (n >> 63);
  }

  /**
   * Writes a ByteBuffer field.
   */
  @Override
  public final void writeBytes(int tag, int tagSize, ByteBuffer value) {
    writeByteRange(false, tag, tagSize, value.array(), value.arrayOffset() + value.position(), value.remaining());
  }

  public void toOutputStream(OutputStream outputStream) throws IOException {
    LinkedBuffer.writeTo(outputStream, head);
  }

  @Override
  public final void writePackedInt32(int value) {
    if (value >= 0) {
      tail = writeRawVarInt32(value, this, tail);
      return;
    }

    writePackedUInt64(value);
  }

  @Override
  public final void writeScalarInt32(int tag, int tagSize, int value) {
    if (value != 0) {
      writeInt32(tag, tagSize, value);
    }
  }

  @Override
  public final void writeScalarInt64(int tag, int tagSize, long value) {
    if (value != 0) {
      writeInt64(tag, tagSize, value);
    }
  }

  @Override
  public final void writePackedUInt32(int value) {
    tail = writeRawVarInt32(value, this, tail);
  }

  @Override
  public final void writeScalarUInt32(int tag, int tagSize, int value) {
    if (value != 0) {
      writeUInt32(tag, tagSize, value);
    }
  }

  @Override
  public final void writePackedUInt64(long value) {
    final int size = computeRawVarint64Size(value);

    if (tail.offset + size > tail.buffer.length) {
      tail = new LinkedBuffer(this.nextBufferSize, tail);
    }

    final byte[] buffer = tail.buffer;
    int offset = tail.offset;
    tail.offset += size;
    this.size += size;

    if (size == 1) {
      buffer[offset] = (byte) value;
    } else {
      for (int i = 0, last = size - 1; i < last; i++, value >>>= 7) {
        buffer[offset++] = (byte) (((int) value & 0x7F) | 0x80);
      }

      buffer[offset] = (byte) value;
    }
  }

  @Override
  public final void writeScalarUInt64(int tag, int tagSize, long value) {
    if (value != 0) {
      writeUInt64(tag, tagSize, value);
    }
  }

  @Override
  public final void writePackedSInt32(int value) {
    tail = writeRawVarInt32(encodeZigZag32(value), this, tail);
  }

  @Override
  public final void writeScalarSInt32(int tag, int tagSize, int value) {
    if (value != 0) {
      writeSInt32(tag, tagSize, value);
    }
  }

  @Override
  public final void writeScalarSInt64(int tag, int tagSize, long value) {
    if (value != 0) {
      writeSInt64(tag, tagSize, value);
    }
  }

  @Override
  public final void writePackedFixed32(int value) {
    final int size = LITTLE_ENDIAN_32_SIZE;

    if (tail.offset + size > tail.buffer.length) {
      tail = new LinkedBuffer(this.nextBufferSize, tail);
    }

    final byte[] buffer = tail.buffer;
    int offset = tail.offset;
    tail.offset += size;
    this.size += size;

    buffer[offset++] = (byte) (value & 0xFF);
    buffer[offset++] = (byte) (value >> 8 & 0xFF);
    buffer[offset++] = (byte) (value >> 16 & 0xFF);
    buffer[offset] = (byte) (value >> 24 & 0xFF);
  }

  @Override
  public final void writeScalarFixed32(int tag, int tagSize, int value) {
    if (value != 0) {
      writeFixed32(tag, tagSize, value);
    }
  }

  @Override
  public final void writePackedFixed64(long value) {
    final int size = LITTLE_ENDIAN_64_SIZE;

    if (tail.offset + size > tail.buffer.length) {
      tail = new LinkedBuffer(this.nextBufferSize, tail);
    }

    final byte[] buffer = tail.buffer;
    int offset = tail.offset;
    tail.offset += size;
    this.size += size;

    buffer[offset++] = (byte) (value & 0xFF);
    buffer[offset++] = (byte) (value >> 8 & 0xFF);
    buffer[offset++] = (byte) (value >> 16 & 0xFF);
    buffer[offset++] = (byte) (value >> 24 & 0xFF);
    buffer[offset++] = (byte) (value >> 32 & 0xFF);
    buffer[offset++] = (byte) (value >> 40 & 0xFF);
    buffer[offset++] = (byte) (value >> 48 & 0xFF);
    buffer[offset] = (byte) (value >> 56 & 0xFF);
  }

  @Override
  public final void writeScalarFixed64(int tag, int tagSize, long value) {
    if (value != 0) {
      writeFixed64(tag, tagSize, value);
    }
  }

  @Override
  public final void writeScalarSFixed32(int tag, int tagSize, int value) {
    if (value != 0) {
      writeSFixed32(tag, tagSize, value);
    }
  }

  @Override
  public final void writeScalarSFixed64(int tag, int tagSize, long value) {
    if (value != 0) {
      writeSFixed64(tag, tagSize, value);
    }
  }

  @Override
  public final void writeScalarFloat(int tag, int tagSize, float value) {
    if (value != 0) {
      writeFloat(tag, tagSize, value);
    }
  }

  @Override
  public final void writeScalarDouble(int tag, int tagSize, double value) {
    if (value != 0) {
      writeDouble(tag, tagSize, value);
    }
  }

  @Override
  public final void writePackedBool(boolean value) {
    final int size = 1;

    if (tail.offset + size > tail.buffer.length) {
      tail = new LinkedBuffer(this.nextBufferSize, tail);
    }

    final byte[] buffer = tail.buffer;
    int offset = tail.offset;
    tail.offset += size;
    this.size += size;

    buffer[offset] = (byte) (value ? 1 : 0);
  }

  @Override
  public final void writeScalarBool(int tag, int tagSize, boolean value) {
    if (value) {
      writeBool(tag, tagSize, true);
    }
  }

  @Override
  public final void writeScalarEnum(int tag, int tagSize, int value) {
    writeScalarInt32(tag, tagSize, value);
  }

  public final void writeScalarString(int tag, int tagSize, String value) {
    if (!value.isEmpty()) {
      writeString(tag, tagSize, value);
    }
  }
}
