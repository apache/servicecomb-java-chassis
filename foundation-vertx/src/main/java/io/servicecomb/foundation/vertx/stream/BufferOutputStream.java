/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.foundation.vertx.stream;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.vertx.core.buffer.Buffer;

/**
 * BufferOutputStream
 * @author  
 *
 */
public class BufferOutputStream extends OutputStream {
    private static final int DIRECT_BUFFER_SIZE = 1024;

    protected ByteBuf byteBuf;

    private boolean needReleaseBuffer;

    /**
     * 构造函数
     */
    public BufferOutputStream() {
        // TODO:默认大小加配置项
        // TODO:如何与pool配合起来，vertx中默认都是unpool的，我们的阻塞模式下，申请与释放也不在一个线程，估计更用不上？
        //        后续通道没问题了，再来验证这个问题
        //        this(PooledByteBufAllocator.DEFAULT.directBuffer());

        //        this(PooledByteBufAllocator.DEFAULT.directBuffer(DIRECT_BUFFER_SIZE));

        //        this(UnpooledByteBufAllocator.DEFAULT.directBuffer(DIRECT_BUFFER_SIZE));
        //        needReleaseBuffer = false;

        //                this(UnpooledByteBufAllocator.DEFAULT.heapBuffer(DIRECT_BUFFER_SIZE));

        this(Buffer.buffer(DIRECT_BUFFER_SIZE).getByteBuf());
        needReleaseBuffer = false;
    }

    /**
     * 构造函数
     * @param buffer  buffer
     */

    public BufferOutputStream(ByteBuf buffer) {
        this.byteBuf = buffer;
    }

    public ByteBuf getByteBuf() {
        return byteBuf;
    }

    public Buffer getBuffer() {
        return Buffer.buffer(byteBuf);
    }

    /**
     * buffer readable length
     * @return       len
     */
    public int length() {
        return byteBuf.readableBytes();
    }

    /**
     * writeByte
     * @param value   value
     */
    public void writeByte(byte value) {
        byteBuf.writeByte(value);
    }

    // 实际是写byte
    @Override
    public void write(int byteValue) {
        byteBuf.writeByte((byte) byteValue);
    }

    /**
     * write
     * @param value   value
     */
    public void write(boolean value) {
        byteBuf.writeBoolean(value);
    }

    /**
     * writeInt
     * @param pos     pos
     * @param value   value
     */
    public void writeInt(int pos, int value) {
        byteBuf.setInt(pos, value);
    }

    /**
     * writeShort
     * @param value      value
     */
    public void writeShort(short value) {
        byteBuf.writeShort(value);
    }

    /**
     * writeInt
     * @param value      value
     */
    public void writeInt(int value) {
        byteBuf.writeInt(value);
    }

    /**
     * writeLong
     * @param value      value
     */
    public void writeLong(long value) {
        byteBuf.writeLong(value);
    }

    /**
     * writeString
     * @param value      value
     */
    public void writeString(String value) {
        byteBuf.writeInt(value.length());
        byteBuf.writeCharSequence(value, StandardCharsets.UTF_8);
    }

    /**
     * write
     * @param b   bytes
     */
    public void write(byte[] b) {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] bytes, int offset, int len) {
        byteBuf.writeBytes(bytes, offset, len);
    }

    @Override
    public void close() {
        if (needReleaseBuffer && byteBuf != null) {
            byteBuf.release();
        }
    }

    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @return
     */
    public int writerIndex() {
        return byteBuf.writerIndex();
    }
}
