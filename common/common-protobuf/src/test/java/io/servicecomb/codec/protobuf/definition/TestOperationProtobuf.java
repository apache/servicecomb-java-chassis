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

package io.servicecomb.codec.protobuf.definition;

import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.codec.protobuf.utils.WrapSchema;
import io.servicecomb.codec.protobuf.utils.schema.ArgsNotWrapSchema;
import io.servicecomb.codec.protobuf.utils.schema.NormalWrapSchema;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.core.definition.SchemaMeta;
import io.servicecomb.core.unittest.UnitTestMeta;
import io.swagger.annotations.ApiResponse;

public class TestOperationProtobuf {
  class Impl {
    @ApiResponse(code = 300, response = String.class, message = "")
    public int test(int x) {
      return 100;
    }
  }

  @Test
  public void testOperationProtobuf() throws Exception {
    UnitTestMeta meta = new UnitTestMeta();
    SchemaMeta schemaMeta = meta.getOrCreateSchemaMeta(Impl.class);
    OperationMeta operationMeta = schemaMeta.findOperation("test");

    OperationProtobuf operationProtobuf = ProtobufManager.getOrCreateOperation(operationMeta);
    Assert.assertEquals(operationMeta, operationProtobuf.getOperationMeta());
    Assert.assertEquals(ArgsNotWrapSchema.class, operationProtobuf.getRequestSchema().getClass());
    Assert.assertEquals(NormalWrapSchema.class, operationProtobuf.getResponseSchema().getClass());

    WrapSchema responseSchema = operationProtobuf.findResponseSchema(200);
    Assert.assertEquals(operationProtobuf.getResponseSchema(), responseSchema);

    responseSchema = operationProtobuf.findResponseSchema(300);
    Assert.assertNotNull(responseSchema);
    Assert.assertNotEquals(operationProtobuf.getResponseSchema(), responseSchema);
  }
}
