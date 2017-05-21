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

package io.servicecomb.common.rest.codec;

import org.junit.Assert;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.servicecomb.common.rest.codec.produce.ProduceProcessorManager;
import io.servicecomb.foundation.vertx.stream.BufferOutputStream;
import io.vertx.core.buffer.Buffer;

public class TestProduce {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test Produce
	 * 
	 * @throws Exception
	 */
	@Test
	public void testProduce() throws Exception {
		Assert.assertEquals("produce processor mgr", ProduceProcessorManager.INSTANCE.getName());
		Buffer oBuffer = ProduceProcessorManager.DEFAULT_PROCESSOR.encodeResponse("test");
		OutputStream oOutputStream = new BufferOutputStream();
		ProduceProcessorManager.DEFAULT_PROCESSOR.encodeResponse(oOutputStream, "test2");
		JavaType targetType = TypeFactory.defaultInstance().constructType(String.class);
		InputStream oInputStream = new ByteArrayInputStream(("true").getBytes());
		ProduceProcessorManager.DEFAULT_PROCESSOR.decodeResponse(oInputStream, targetType);
		ProduceProcessorManager.PLAIN_PROCESSOR.encodeResponse(new BufferOutputStream(), "test2");
		Assert.assertNotEquals(null, ProduceProcessorManager.PLAIN_PROCESSOR.decodeResponse(oInputStream, targetType));
		oInputStream = new ByteArrayInputStream(("true").getBytes());
		Assert.assertNotEquals(null,
				ProduceProcessorManager.DEFAULT_PROCESSOR.decodeResponse(oInputStream, targetType));
		ProduceProcessorManager.DEFAULT_PROCESSOR.decodeResponse(oBuffer, targetType);
		Assert.assertEquals(null, ProduceProcessorManager.DEFAULT_PROCESSOR.encodeResponse(null));

    }
}
