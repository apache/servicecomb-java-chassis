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

package io.servicecomb.foundation.vertx;

import java.util.concurrent.CountDownLatch;

import javax.xml.ws.Holder;

import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

public class TestVertex {
    @Test
    public void testGetOrCreateVertx() throws InterruptedException {
        Vertx vertx = VertxUtils.getOrCreateVertxByName("ut", null);

        Holder<String> name = new Holder<>();
        CountDownLatch latch = new CountDownLatch(1);
        vertx.runOnContext(v -> {
            name.value = Thread.currentThread().getName();
            latch.countDown();
        });
        latch.await();

        Assert.assertEquals(name.value, "ut-vert.x-eventloop-thread-0");
        vertx.close();
    }

    @Test
    public void testVertxUtilsInitNullOptions() {
        Vertx vertx = VertxUtils.init(null);
        Assert.assertNotEquals(null, vertx);
        vertx.close();
    }

    @Test
    public void testVertxUtilsInitWithOptions() {
        VertxOptions oOptions = new VertxOptions();
        oOptions.setClustered(false);

        Vertx vertx = VertxUtils.init(oOptions);
        Assert.assertNotEquals(null, vertx);
        vertx.close();
    }

    @Test
    public void testSimpleJsonObject() {
        SimpleJsonObject oObject = new SimpleJsonObject();
        JsonObject oJsonObject = oObject.put("testKey", oObject);
        Assert.assertEquals(true, oJsonObject.containsKey("testKey"));
        JsonObject oCopyObject = oObject.copy();
        Assert.assertEquals(true, oCopyObject.containsKey("testKey"));
    }

}
