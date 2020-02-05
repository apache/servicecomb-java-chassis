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

package org.apache.servicecomb.codec.protobuf.internal.converter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.codec.protobuf.definition.OperationProtobuf;
import org.apache.servicecomb.codec.protobuf.definition.ProtobufManager;
import org.apache.servicecomb.codec.protobuf.definition.RequestRootDeserializer;
import org.apache.servicecomb.codec.protobuf.definition.RequestRootSerializer;
import org.apache.servicecomb.codec.protobuf.definition.ResponseRootDeserializer;
import org.apache.servicecomb.codec.protobuf.definition.ResponseRootSerializer;
import org.apache.servicecomb.codec.protobuf.internal.converter.model.ProtoSchema;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.foundation.test.scaffolding.model.Color;
import org.apache.servicecomb.foundation.test.scaffolding.model.Empty;
import org.apache.servicecomb.foundation.test.scaffolding.model.User;
import org.apache.servicecomb.swagger.engine.SwaggerEnvironment;
import org.apache.servicecomb.swagger.engine.SwaggerProducer;
import org.apache.servicecomb.swagger.engine.SwaggerProducerOperation;
import org.apache.servicecomb.swagger.generator.springmvc.SpringmvcSwaggerGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.models.Swagger;
import mockit.Expectations;
import mockit.Injectable;

/**
 * SchemaMetaCodec test cases. This test cases covers RestTemplate invoker and producer.
 */
public class TestSchemaMetaCodecRestTemplate {
  @Injectable
  private MicroserviceMeta providerMicroserviceMeta;

  @Injectable
  private MicroserviceMeta consumerMicroserviceMeta;

  private SchemaMeta providerSchemaMeta;

  private SchemaMeta consumerSchemaMeta;

  @Before
  public void setUp() {
    new Expectations() {
      {
        providerMicroserviceMeta.getMicroserviceName();
        result = "test";
        providerMicroserviceMeta.getExtData(ProtobufManager.EXT_ID);
        result = null;
        consumerMicroserviceMeta.getMicroserviceName();
        result = "test";
        consumerMicroserviceMeta.getExtData(ProtobufManager.EXT_ID);
        result = null;
      }
    };
    SpringmvcSwaggerGenerator swaggerGenerator = new SpringmvcSwaggerGenerator(ProtoSchema.class);
    Swagger swagger = swaggerGenerator.generate();
    SwaggerEnvironment swaggerEnvironment = new SwaggerEnvironment();

    providerSchemaMeta = new SchemaMeta(providerMicroserviceMeta, "ProtoSchema", swagger);
    SwaggerProducer swaggerProducer = swaggerEnvironment.createProducer(new ProtoSchema(), swagger);
    for (SwaggerProducerOperation producerOperation : swaggerProducer.getAllOperations()) {
      OperationMeta operationMeta = providerSchemaMeta.ensureFindOperation(producerOperation.getOperationId());
      operationMeta.setSwaggerProducerOperation(producerOperation);
    }

    consumerSchemaMeta = new SchemaMeta(consumerMicroserviceMeta, "ProtoSchema", swagger);
  }

  @Test
  public void testProtoSchemaOperationUser() throws Exception {
    OperationProtobuf providerOperationProtobuf = ProtobufManager
        .getOrCreateOperation(providerSchemaMeta.getOperations().get("user"));
    OperationProtobuf consumerOperationProtobuf = ProtobufManager
        .getOrCreateOperation(consumerSchemaMeta.getOperations().get("user"));
    User user = new User();
    user.name = "user";
    User friend = new User();
    friend.name = "friend";
    List<User> friends = new ArrayList<>();
    friends.add(friend);
    user.friends = friends;
    byte[] values;

    // request message
    Map<String, Object> args = new HashMap<>();
    RequestRootSerializer requestSerializer = consumerOperationProtobuf.getRequestRootSerializer();
    user.friends = friends;
    args.put("user", user);
    values = requestSerializer.serialize(args);

    RequestRootDeserializer<Object> requestDeserializer = providerOperationProtobuf.getRequestRootDeserializer();
    Map<String, Object> decodedUserArgs = requestDeserializer.deserialize(values);
    Assert.assertEquals(user.name, ((User) decodedUserArgs.get("user")).name);
    Assert.assertEquals(user.friends.get(0).name, ((User) decodedUserArgs.get("user")).friends.get(0).name);

    // response message
    ResponseRootSerializer responseSerializer = providerOperationProtobuf.findResponseRootSerializer(200);
    values = responseSerializer.serialize(user);
    ResponseRootDeserializer<Object> responseDeserializer = consumerOperationProtobuf.findResponseRootDeserializer(200);
    User decodedUser = (User) responseDeserializer.deserialize(values, TypeFactory.defaultInstance().constructType(User.class));
    Assert.assertEquals(user.name, decodedUser.name);
    Assert.assertEquals(user.friends.get(0).name, decodedUser.friends.get(0).name);

    user.friends = new ArrayList<>();
    values = responseSerializer.serialize(user);
    decodedUser = (User) responseDeserializer.deserialize(values, TypeFactory.defaultInstance().constructType(User.class));
    Assert.assertEquals(user.name, decodedUser.name);
    // proto buffer encode and decode empty list to be null
    Assert.assertEquals(null, decodedUser.friends);
  }

  @Test
  @SuppressWarnings({"rawtypes", "unchecked"})
  public void testProtoSchemaOperationBase() throws Exception {
    OperationProtobuf providerOperationProtobuf = ProtobufManager
        .getOrCreateOperation(providerSchemaMeta.getOperations().get("base"));
    OperationProtobuf consumerOperationProtobuf = ProtobufManager
        .getOrCreateOperation(consumerSchemaMeta.getOperations().get("base"));
    byte[] values;

    // request message
    RequestRootSerializer requestSerializer = consumerOperationProtobuf.getRequestRootSerializer();
    boolean boolValue = true;
    int iValue = 20;
    long lValue = 30L;
    float fValue = 40f;
    double dValue = 50D;
    String sValue = "hello";
    int[] iArray = new int[] {60, 70};
    Color color = Color.BLUE;
    LocalDate localDate = LocalDate.of(2019, 10, 1);
    Date date = new Date();
    Empty empty = new Empty();
    Map<String, Object> args = new HashMap<>();
    args.put("boolValue", boolValue);
    args.put("iValue", iValue);
    args.put("lValue", lValue);
    args.put("fValue", fValue);
    args.put("dValue", dValue);
    args.put("sValue", sValue);
    args.put("iArray", iArray);
    args.put("color", color);
    args.put("localDate", localDate);
    args.put("date", date);
    args.put("empty", empty);
    values = requestSerializer.serialize(args);
    RequestRootDeserializer<Object> requestDeserializer = providerOperationProtobuf.getRequestRootDeserializer();
    Map<String, Object> decodedArgs = requestDeserializer.deserialize(values);
    Assert.assertEquals(boolValue, decodedArgs.get("boolValue"));
    Assert.assertEquals(iValue, decodedArgs.get("iValue"));
    Assert.assertEquals(lValue, decodedArgs.get("lValue"));
    Assert.assertEquals(fValue, decodedArgs.get("fValue"));
    Assert.assertEquals(dValue, decodedArgs.get("dValue"));
    Assert.assertArrayEquals(iArray, (int[]) decodedArgs.get("iArray"));
    Assert.assertEquals(color, decodedArgs.get("color"));
    Assert.assertEquals(date, decodedArgs.get("date"));
    Assert.assertTrue(decodedArgs.get("localDate") instanceof LocalDate);
    Assert.assertEquals(localDate, decodedArgs.get("localDate"));
    Assert.assertTrue(decodedArgs.get("empty") instanceof Empty);

    // default value testing
    args.put("boolValue", false);
    args.put("iValue", 0);
    args.put("lValue", 0L);
    args.put("fValue", 0F);
    args.put("dValue", 0D);
    args.put("sValue", null);
    args.put("iArray", new int[0]);
    args.put("color", null);
    args.put("localDate", null);
    args.put("date", null);
    args.put("empty", null);
    values = requestSerializer.serialize(args);
    decodedArgs = requestDeserializer.deserialize(values);
    Assert.assertEquals(null, decodedArgs.get("boolValue"));
    Assert.assertEquals(null, decodedArgs.get("iValue"));
    Assert.assertEquals(null, decodedArgs.get("lValue"));
    Assert.assertEquals(null, decodedArgs.get("fValue"));
    Assert.assertEquals(null, decodedArgs.get("dValue"));
    Assert.assertEquals(null, decodedArgs.get("iArray"));
    Assert.assertEquals(null, decodedArgs.get("color"));
    Assert.assertEquals(null, decodedArgs.get("localDate"));
    Assert.assertEquals(null, decodedArgs.get("date"));
    Assert.assertEquals(null, decodedArgs.get("empty"));

    // response message
    ResponseRootSerializer responseSerializer = providerOperationProtobuf.findResponseRootSerializer(200);
    values = responseSerializer.serialize(30);
    ResponseRootDeserializer<Object> responseDeserializer = consumerOperationProtobuf.findResponseRootDeserializer(200);
    Object decodedValue = responseDeserializer.deserialize(values, TypeFactory.defaultInstance().constructType(int.class));
    Assert.assertEquals(30, (int) decodedValue);
  }
}
