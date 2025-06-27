/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.foundation.vertx.client.http;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.PoolOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class HttpClientPoolFactoryTest {
    private Context context;
    private Vertx vertx;
    private HttpClientPoolFactory factory;

    @BeforeEach
    void setUp() {
        // Mock the context and set up the Vertx instance
        context = Mockito.mock(Context.class);
        vertx = Vertx.vertx();
        Mockito.when(context.owner()).thenReturn(vertx);

        // Create HttpClientOptions with a specific max pool size
        HttpClientOptions options = new HttpClientOptions();
        options.setMaxPoolSize(123);

        // Initialize the HttpClientPoolFactory with the given options
        factory = new HttpClientPoolFactory(options);
    }

    @AfterEach
    void tearDown() {
        // Close the Vertx instance to release resources
        vertx.close();
    }

    @Test
    void testCreateClientPool() {
        // Create the client pool and get the HttpClient
        HttpClient httpClient = factory.createClientPool(context).getHttpClient();

        // Use ReflectionTestUtils to get the poolOptions field from the HttpClient
        if (ReflectionTestUtils.getField(httpClient, "poolOptions") instanceof PoolOptions poolOptions) {
            // Assert that the http1MaxSize is set to the expected value
            Assertions.assertEquals(123, poolOptions.getHttp1MaxSize());
        } else {
            // Fail the test if the poolOptions field is not found or not of the expected type
            Assertions.fail("poolOptions field not found or not of the expected type");
        }
    }

    @Test
    void testCreateClientPoolWithDefaultOptions() {
        // Create HttpClientOptions with default values
        HttpClientOptions defaultOptions = new HttpClientOptions();

        // Initialize the HttpClientPoolFactory with the default options
        HttpClientPoolFactory defaultFactory = new HttpClientPoolFactory(defaultOptions);

        // Create the client pool and get the HttpClient
        HttpClient defaultHttpClient = defaultFactory.createClientPool(context).getHttpClient();

        // Use ReflectionTestUtils to get the poolOptions field from the HttpClient
        if (ReflectionTestUtils.getField(defaultHttpClient, "poolOptions") instanceof PoolOptions defaultPoolOptions) {
            // Assert that the http1MaxSize is set to the default value
            Assertions.assertEquals(HttpClientOptions.DEFAULT_MAX_POOL_SIZE, defaultPoolOptions.getHttp1MaxSize());
        } else {
            // Fail the test if the poolOptions field is not found or not of the expected type
            Assertions.fail("poolOptions field not found or not of the expected type");
        }
    }
}
