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
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.definition.InvocationRuntimeType;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.MicroserviceVersionsMeta;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.executor.ExecutorManager;
import org.apache.servicecomb.foundation.test.scaffolding.model.Color;
import org.apache.servicecomb.foundation.test.scaffolding.model.Empty;
import org.apache.servicecomb.foundation.test.scaffolding.model.User;
import org.apache.servicecomb.swagger.engine.SwaggerEnvironment;
import org.apache.servicecomb.swagger.engine.SwaggerProducer;
import org.apache.servicecomb.swagger.engine.SwaggerProducerOperation;
import org.apache.servicecomb.swagger.generator.springmvc.SpringmvcSwaggerGenerator;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.type.TypeFactory;
import io.swagger.models.Swagger;

/**
 * SchemaMetaCodec test cases. This test cases covers RestTemplate invoker and producer.
 */
public class TestSchemaMetaCodecRestTemplate {

  private SchemaMeta providerSchemaMeta;

  private SchemaMeta consumerSchemaMeta;

  @BeforeEach
  public void setUp() {
    ProtobufManager.clear();
    MicroserviceVersionsMeta microserviceVersionsMeta = Mockito.mock(MicroserviceVersionsMeta.class);
    ExecutorManager executorManager = Mockito.mock(ExecutorManager.class);
    SCBEngine scbEngine = Mockito.mock(SCBEngine.class);
    Mockito.when(scbEngine.getExecutorManager()).thenReturn(executorManager);

    MicroserviceMeta providerMicroserviceMeta = Mockito.mock(MicroserviceMeta.class);
    Mockito.when(providerMicroserviceMeta.getScbEngine()).thenReturn(scbEngine);
    Mockito.when(providerMicroserviceMeta.getMicroserviceVersionsMeta()).thenReturn(microserviceVersionsMeta);
    Mockito.when(providerMicroserviceMeta.getMicroserviceName()).thenReturn("test");
    Mockito.when(providerMicroserviceMeta.getExtData(ProtobufManager.EXT_ID)).thenReturn(null);

    MicroserviceMeta consumerMicroserviceMeta = Mockito.mock(MicroserviceMeta.class);
    Mockito.when(consumerMicroserviceMeta.getScbEngine()).thenReturn(scbEngine);
    Mockito.when(consumerMicroserviceMeta.getMicroserviceVersionsMeta()).thenReturn(microserviceVersionsMeta);
    Mockito.when(consumerMicroserviceMeta.getMicroserviceName()).thenReturn("test");
    Mockito.when(consumerMicroserviceMeta.getExtData(ProtobufManager.EXT_ID)).thenReturn(null);

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

  private Invocation mockInvocation(String operation, InvocationType invocationType) {
    OperationMeta operationMeta;
    boolean isConsumer;
    Invocation invocation = Mockito.mock(Invocation.class);
    InvocationRuntimeType invocationRuntimeType;

    if (InvocationType.CONSUMER == invocationType) {
      operationMeta = consumerSchemaMeta.getOperations().get(operation);
      isConsumer = true;
      Mockito.when(invocation.getSchemaMeta()).thenReturn(consumerSchemaMeta);
      invocationRuntimeType = operationMeta.buildBaseConsumerRuntimeType();
    } else {
      operationMeta = providerSchemaMeta.getOperations().get(operation);
      isConsumer = false;
      Mockito.when(invocation.getSchemaMeta()).thenReturn(providerSchemaMeta);
      invocationRuntimeType = operationMeta.buildBaseProviderRuntimeType();
    }

    MicroserviceMeta microserviceMeta = operationMeta.getMicroserviceMeta();
    Mockito.when(invocation.getOperationMeta()).thenReturn(operationMeta);
    Mockito.when(invocation.getInvocationRuntimeType())
        .thenReturn(invocationRuntimeType);
    Mockito.when(invocation.findResponseType(200))
        .thenReturn(invocationRuntimeType.findResponseType(200));
    Mockito.when(invocation.getInvocationType()).thenReturn(invocationType);
    Mockito.when(invocation.getMicroserviceMeta()).thenReturn(microserviceMeta);

    Mockito.when(invocation.isConsumer()).thenReturn(isConsumer);
    return invocation;
  }

  @Test
  public void testProtoSchemaOperationUser() throws Exception {
    Invocation consumerInvocation = mockInvocation("user", InvocationType.CONSUMER);
    Invocation providerInvocation = mockInvocation("user", InvocationType.PRODUCER);

    OperationProtobuf providerOperationProtobuf = ProtobufManager
        .getOrCreateOperation(providerInvocation);
    OperationProtobuf consumerOperationProtobuf = ProtobufManager
        .getOrCreateOperation(consumerInvocation);
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
    Assertions.assertEquals(user.name, ((User) decodedUserArgs.get("user")).name);
    Assertions.assertEquals(user.friends.get(0).name, ((User) decodedUserArgs.get("user")).friends.get(0).name);

    // response message
    ResponseRootSerializer responseSerializer = providerOperationProtobuf.findResponseRootSerializer(200);
    values = responseSerializer.serialize(user);
    ResponseRootDeserializer<Object> responseDeserializer = consumerOperationProtobuf.findResponseRootDeserializer(200);
    User decodedUser = (User) responseDeserializer
        .deserialize(values, TypeFactory.defaultInstance().constructType(User.class));
    Assertions.assertEquals(user.name, decodedUser.name);
    Assertions.assertEquals(user.friends.get(0).name, decodedUser.friends.get(0).name);

    user.friends = new ArrayList<>();
    values = responseSerializer.serialize(user);
    decodedUser = (User) responseDeserializer
        .deserialize(values, TypeFactory.defaultInstance().constructType(User.class));
    Assertions.assertEquals(user.name, decodedUser.name);
    // proto buffer encode and decode empty list to be null
    Assertions.assertNull(decodedUser.friends);
  }

  @Test
  @SuppressWarnings({"rawtypes", "unchecked"})
  public void testProtoSchemaOperationBase() throws Exception {
    Invocation consumerInvocation = mockInvocation("base", InvocationType.CONSUMER);
    Invocation providerInvocation = mockInvocation("base", InvocationType.PRODUCER);

    OperationProtobuf providerOperationProtobuf = ProtobufManager
        .getOrCreateOperation(providerInvocation);
    OperationProtobuf consumerOperationProtobuf = ProtobufManager
        .getOrCreateOperation(consumerInvocation);
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
    Assertions.assertEquals(boolValue, decodedArgs.get("boolValue"));
    Assertions.assertEquals(iValue, decodedArgs.get("iValue"));
    Assertions.assertEquals(lValue, decodedArgs.get("lValue"));
    Assertions.assertEquals(fValue, decodedArgs.get("fValue"));
    Assertions.assertEquals(dValue, decodedArgs.get("dValue"));
    Assertions.assertArrayEquals(iArray, (int[]) decodedArgs.get("iArray"));
    Assertions.assertEquals(color, decodedArgs.get("color"));
    Assertions.assertEquals(date, decodedArgs.get("date"));
    Assertions.assertTrue(decodedArgs.get("localDate") instanceof LocalDate);
    Assertions.assertEquals(localDate, decodedArgs.get("localDate"));
    Assertions.assertTrue(decodedArgs.get("empty") instanceof Empty);

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
    Assertions.assertNull(decodedArgs.get("boolValue"));
    Assertions.assertNull(decodedArgs.get("iValue"));
    Assertions.assertNull(decodedArgs.get("lValue"));
    Assertions.assertNull(decodedArgs.get("fValue"));
    Assertions.assertNull(decodedArgs.get("dValue"));
    Assertions.assertNull(decodedArgs.get("iArray"));
    Assertions.assertNull(decodedArgs.get("color"));
    Assertions.assertNull(decodedArgs.get("localDate"));
    Assertions.assertNull(decodedArgs.get("date"));
    Assertions.assertNull(decodedArgs.get("empty"));

    // response message
    ResponseRootSerializer responseSerializer = providerOperationProtobuf.findResponseRootSerializer(200);
    values = responseSerializer.serialize(30);
    ResponseRootDeserializer<Object> responseDeserializer = consumerOperationProtobuf.findResponseRootDeserializer(200);
    Object decodedValue = responseDeserializer
        .deserialize(values, TypeFactory.defaultInstance().constructType(int.class));
    Assertions.assertEquals(30, (int) decodedValue);
  }
}
