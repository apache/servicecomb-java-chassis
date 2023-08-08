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

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
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
import org.apache.servicecomb.codec.protobuf.internal.converter.model.ProtoSchemaPojo;
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
import org.apache.servicecomb.foundation.test.scaffolding.model.People;
import org.apache.servicecomb.foundation.test.scaffolding.model.User;
import org.apache.servicecomb.swagger.engine.SwaggerEnvironment;
import org.apache.servicecomb.swagger.engine.SwaggerProducer;
import org.apache.servicecomb.swagger.engine.SwaggerProducerOperation;
import org.apache.servicecomb.swagger.generator.core.AbstractSwaggerGenerator;
import org.apache.servicecomb.swagger.generator.pojo.PojoSwaggerGenerator;
import org.apache.servicecomb.swagger.generator.springmvc.SpringmvcSwaggerGenerator;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * SchemaMetaCodec test cases. This test cases covers POJO invoker and producer.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class TestSchemaMetaCodec {

  private SchemaMeta providerSchemaMeta;

  private SchemaMeta consumerSchemaMeta;

  @BeforeEach
  public void setUp() {
    ProtobufManager.clear();
  }

  private void mockSchemaMeta(String schemaId, AbstractSwaggerGenerator swaggerGenerator, Object producerInstance)
      throws Exception {
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
    SwaggerEnvironment swaggerEnvironment = new SwaggerEnvironment();
    SwaggerProducer swaggerProducer = swaggerEnvironment.createProducer(producerInstance);
    providerSchemaMeta = new SchemaMeta(providerMicroserviceMeta, schemaId, swaggerProducer.getSwagger());
    for (SwaggerProducerOperation producerOperation : swaggerProducer.getAllOperations()) {
      OperationMeta operationMeta = providerSchemaMeta.ensureFindOperation(producerOperation.getOperationId());
      operationMeta.setSwaggerProducerOperation(producerOperation);
    }

    consumerSchemaMeta = new SchemaMeta(consumerMicroserviceMeta, schemaId, swaggerProducer.getSwagger());
  }

  @Test
  public void testProtoSchemaOperationUserSpringMVC() throws Exception {
    mockSchemaMeta("ProtoSchema", new SpringmvcSwaggerGenerator(ProtoSchema.class), new ProtoSchema());
    testProtoSchemaOperationUserImpl();
  }

  @Test
  public void testProtoSchemaOperationUserPOJO() throws Exception {
    mockSchemaMeta("ProtoSchemaPojo", new PojoSwaggerGenerator(ProtoSchemaPojo.class), new ProtoSchemaPojo());
    testProtoSchemaOperationUserImpl();
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

  private void testProtoSchemaOperationUserImpl() throws IOException {
    Invocation consumerInvocation = mockInvocation("user", InvocationType.CONSUMER);
    Invocation providerInvocation = mockInvocation("user", InvocationType.PROVIDER);

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

    // write request map (pojo)
    args = new HashMap<>();
    Map<String, Object> userMap = new HashMap<>();
    userMap.put("name", "user");
    Map<String, Object> friendMap = new HashMap<>();
    friendMap.put("name", "friend");
    List<Map<String, Object>> friendsList = new ArrayList<>();
    friendsList.add(friendMap);
    userMap.put("friends", friendsList);
    args.put("user", userMap);
    values = requestSerializer.serialize(args);

    decodedUserArgs = requestDeserializer.deserialize(values);
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
  public void testProtoSchemaOperationmapUserSpringMVC() throws Exception {
    mockSchemaMeta("ProtoSchema", new SpringmvcSwaggerGenerator(ProtoSchema.class), new ProtoSchema());
    testProtoSchemaOperationmapUserImpl(false);
  }

  @Test
  public void testProtoSchemaOperationmapUserPOJO() throws Exception {
    mockSchemaMeta("ProtoSchemaPojo", new PojoSwaggerGenerator(ProtoSchemaPojo.class), new ProtoSchemaPojo());
    testProtoSchemaOperationmapUserImpl(true);
  }

  private void testProtoSchemaOperationmapUserImpl(boolean isPojo) throws IOException {
    Invocation consumerInvocation = mockInvocation("mapUser", InvocationType.CONSUMER);
    Invocation providerInvocation = mockInvocation("mapUser", InvocationType.PROVIDER);

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
    Map<String, User> userMap = new HashMap<>();
    userMap.put("test", user);

    // request message
    Map<String, Object> args = new HashMap<>();
    RequestRootSerializer requestSerializer = consumerOperationProtobuf.getRequestRootSerializer();
    user.friends = friends;
    args.put("users", userMap);
    if (isPojo) {
      Map<String, Object> swaggerArgs = new HashMap<>(1);
      swaggerArgs.put("listListUserBody", args);
      values = requestSerializer.serialize(swaggerArgs);
    } else {
      values = requestSerializer.serialize(args);
    }
    RequestRootDeserializer<Object> requestDeserializer = providerOperationProtobuf.getRequestRootDeserializer();
    Map<String, Object> decodedUserArgs = requestDeserializer.deserialize(values);
    if (isPojo) {
      decodedUserArgs = (Map<String, Object>) decodedUserArgs.get("mapUserBody");
      Assertions.assertEquals(user.name,
          ((Map<String, Map<String, Object>>) decodedUserArgs.get("users")).get("test").get("name"));
      Assertions.assertEquals(user.friends.get(0).name,
          ((List<Map<String, Object>>) ((Map<String, Map<String, Object>>) decodedUserArgs.get("users")).get("test")
              .get("friends")).get(0).get("name"));
    } else {
      Assertions.assertEquals(user.name, ((Map<String, User>) decodedUserArgs.get("users")).get("test").name);
      Assertions.assertEquals(user.friends.get(0).name,
          ((Map<String, User>) decodedUserArgs.get("users")).get("test").friends.get(0).name);
    }
    // response message
    ResponseRootSerializer responseSerializer = providerOperationProtobuf.findResponseRootSerializer(200);
    values = responseSerializer.serialize(userMap);
    ResponseRootDeserializer<Object> responseDeserializer = consumerOperationProtobuf.findResponseRootDeserializer(200);
    Map<String, User> decodedUser = (Map<String, User>) responseDeserializer.deserialize(values,
        TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, User.class));
    Assertions.assertEquals(user.name, decodedUser.get("test").name);
    Assertions.assertEquals(user.friends.get(0).name, decodedUser.get("test").friends.get(0).name);

    user.friends = new ArrayList<>();
    values = responseSerializer.serialize(userMap);
    decodedUser = (Map<String, User>) responseDeserializer.deserialize(values,
        TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, User.class));
    Assertions.assertEquals(user.name, decodedUser.get("test").name);
    // proto buffer encode and decode empty list to be null
    Assertions.assertNull(decodedUser.get("test").friends);
  }

  @Test
  public void testProtoSchemaOperationBaseSpringMVC() throws Exception {
    mockSchemaMeta("ProtoSchema", new SpringmvcSwaggerGenerator(ProtoSchema.class), new ProtoSchema());
    testProtoSchemaOperationBaseImpl(false);
  }

  @Test
  public void testProtoSchemaOperationBasePOJO() throws Exception {
    mockSchemaMeta("ProtoSchemaPojo", new PojoSwaggerGenerator(ProtoSchemaPojo.class), new ProtoSchemaPojo());
    testProtoSchemaOperationBaseImpl(true);
  }

  private void testProtoSchemaOperationBaseImpl(boolean isPojo) throws IOException {
    Invocation consumerInvocation = mockInvocation("base", InvocationType.CONSUMER);
    Invocation providerInvocation = mockInvocation("base", InvocationType.PROVIDER);

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
    if (isPojo) {
      Map<String, Object> swaggerArgs = new HashMap<>();
      swaggerArgs.put("baseBody", args);
      values = requestSerializer.serialize(swaggerArgs);
    } else {
      values = requestSerializer.serialize(args);
    }
    RequestRootDeserializer<Object> requestDeserializer = providerOperationProtobuf.getRequestRootDeserializer();
    Map<String, Object> decodedSwaggerArgs = requestDeserializer.deserialize(values);
    Map<String, Object> decodedArgs;
    if (isPojo) {
      Assertions.assertEquals(1, decodedSwaggerArgs.size());
      decodedArgs = (Map<String, Object>) decodedSwaggerArgs.get("baseBody");
    } else {
      decodedArgs = decodedSwaggerArgs;
    }
    Assertions.assertEquals(boolValue, decodedArgs.get("boolValue"));
    Assertions.assertEquals(iValue, decodedArgs.get("iValue"));
    Assertions.assertEquals(lValue, decodedArgs.get("lValue"));
    Assertions.assertEquals(fValue, decodedArgs.get("fValue"));
    Assertions.assertEquals(dValue, decodedArgs.get("dValue"));
    if (isPojo) {
      Assertions.assertEquals(2, ((List<Integer>) decodedArgs.get("iArray")).size());
      Assertions.assertEquals(60, (((List<Integer>) decodedArgs.get("iArray")).get(0).intValue()));
      Assertions.assertEquals(70, (((List<Integer>) decodedArgs.get("iArray")).get(1).intValue()));
      Assertions.assertEquals(color.ordinal(), decodedArgs.get("color"));
      Assertions.assertEquals(date.getTime(), decodedArgs.get("date"));
      Assertions.assertEquals(localDate.getLong(ChronoField.EPOCH_DAY), decodedArgs.get("localDate"));
      Assertions.assertTrue(((Map) decodedArgs.get("empty")).isEmpty());
    } else {
      Assertions.assertArrayEquals(iArray, (int[]) decodedArgs.get("iArray"));
      Assertions.assertEquals(color, decodedArgs.get("color"));
      Assertions.assertEquals(date, decodedArgs.get("date"));
      Assertions.assertTrue(decodedArgs.get("localDate") instanceof LocalDate);
      Assertions.assertEquals(localDate, decodedArgs.get("localDate"));
      Assertions.assertTrue(decodedArgs.get("empty") instanceof Empty);
    }

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

  @Test
  public void testProtoSchemaOperationlistListUserSpringMVC() throws Exception {
    mockSchemaMeta("ProtoSchema", new SpringmvcSwaggerGenerator(ProtoSchema.class), new ProtoSchema());
    testProtoSchemaOperationlistListUserImpl(false);
  }

  @Test
  public void testProtoSchemaOperationlistListUserPOJO() throws Exception {
    mockSchemaMeta("ProtoSchemaPojo", new PojoSwaggerGenerator(ProtoSchemaPojo.class), new ProtoSchemaPojo());
    testProtoSchemaOperationlistListUserImpl(true);
  }

  private void testProtoSchemaOperationlistListUserImpl(boolean isPojo) throws IOException {
    Invocation consumerInvocation = mockInvocation("listListUser", InvocationType.CONSUMER);
    Invocation providerInvocation = mockInvocation("listListUser", InvocationType.PROVIDER);

    OperationProtobuf providerOperationProtobuf = ProtobufManager
        .getOrCreateOperation(providerInvocation);
    OperationProtobuf consumerOperationProtobuf = ProtobufManager
        .getOrCreateOperation(consumerInvocation);
    byte[] values;

    // request message
    RequestRootSerializer requestSerializer = consumerOperationProtobuf.getRequestRootSerializer();
    User user = new User();
    user.name = "user";
    User friend = new User();
    friend.name = "friend";
    List<User> friends = new ArrayList<>();
    friends.add(friend);
    user.friends = friends;
    List<User> users = new ArrayList<>();
    users.add(user);
    List<List<User>> listOfUsers = new ArrayList<>();
    listOfUsers.add(users);
    Map<String, Object> args = new HashMap<>();
    args.put("value", listOfUsers);

    if (isPojo) {
      Map<String, Object> swaggerArgs = new HashMap<>();
      swaggerArgs.put("listListUserBody", args);
      values = requestSerializer.serialize(swaggerArgs);
    } else {
      values = requestSerializer.serialize(args);
    }
    RequestRootDeserializer<Object> requestDeserializer = providerOperationProtobuf.getRequestRootDeserializer();
    Map<String, Object> decodedSwaggerArgs = requestDeserializer.deserialize(values);
    Map<String, Object> decodedArgs;
    if (isPojo) {
      Assertions.assertEquals(1, decodedSwaggerArgs.size());
      decodedArgs = (Map<String, Object>) decodedSwaggerArgs.get("listListUserBody");
    } else {
      decodedArgs = decodedSwaggerArgs;
    }
    List<List<?>> listOfUsersRaw = (List<List<?>>) decodedArgs.get("value");
    Assertions.assertEquals(1, listOfUsersRaw.size());
    List<?> mapUsersRaw = (List<?>) listOfUsersRaw.get(0);
    Assertions.assertEquals(1, mapUsersRaw.size());
    if (isPojo) {
      Map<String, Object> userMap = (Map<String, Object>) mapUsersRaw.get(0);
      Assertions.assertEquals("user", userMap.get("name"));
      // proto buffer encode and decode empty list to be null
      friends = (List<User>) userMap.get("friends");
      Map<String, Object> friendMap = (Map<String, Object>) friends.get(0);
      Assertions.assertEquals("friend", friendMap.get("name"));
    } else {
      user = (User) mapUsersRaw.get(0);
      Assertions.assertEquals("user", user.name);
      // proto buffer encode and decode empty list to be null
      Assertions.assertEquals("friend", user.friends.get(0).name);
    }
  }

  @Test
  public void testProtoSchemaOperationObjSpringMVC() throws Exception {
    mockSchemaMeta("ProtoSchema", new SpringmvcSwaggerGenerator(ProtoSchema.class), new ProtoSchema());
    testProtoSchemaOperationObjImpl(false);
  }

  @Test
  public void testProtoSchemaOperationObjPOJO() throws Exception {
    mockSchemaMeta("ProtoSchemaPojo", new PojoSwaggerGenerator(ProtoSchemaPojo.class), new ProtoSchemaPojo());
    testProtoSchemaOperationObjImpl(true);
  }

  private void testProtoSchemaOperationObjImpl(boolean isPojo) throws IOException {
    Invocation consumerInvocation = mockInvocation("obj", InvocationType.CONSUMER);
    Invocation providerInvocation = mockInvocation("obj", InvocationType.PROVIDER);

    OperationProtobuf providerOperationProtobuf = ProtobufManager
        .getOrCreateOperation(providerInvocation);
    OperationProtobuf consumerOperationProtobuf = ProtobufManager
        .getOrCreateOperation(consumerInvocation);
    byte[] values;

    // request message
    RequestRootSerializer requestSerializer = consumerOperationProtobuf.getRequestRootSerializer();
    Map<String, Object> args = new HashMap<>();
    args.put("value", 2);

    values = requestSerializer.serialize(args);
    RequestRootDeserializer<Object> requestDeserializer = providerOperationProtobuf.getRequestRootDeserializer();
    Map<String, Object> decodedArgs = requestDeserializer.deserialize(values);
    int result = (int) decodedArgs.get("value");
    Assertions.assertEquals(2, result);

    User user = new User();
    user.name = "user";
    User friend = new User();
    friend.name = "friend";
    List<User> friends = new ArrayList<>();
    friends.add(friend);
    user.friends = friends;
    args.put("value", user);
    values = requestSerializer.serialize(args);
    decodedArgs = requestDeserializer.deserialize(values);
    Map<String, Object> userMap = (Map<String, Object>) decodedArgs.get("value");
    Assertions.assertEquals("user", userMap.get("name"));
    // proto buffer encode and decode empty list to be null
    friends = (List<User>) userMap.get("friends");
    Map<String, Object> friendMap = (Map<String, Object>) friends.get(0);
    Assertions.assertEquals("friend", friendMap.get("name"));

    args.clear();
    People people = new People();
    people.name = "user";
    People pFriend = new People();
    pFriend.name = "friend";
    List<People> pFriends = new ArrayList<>();
    pFriends.add(pFriend);
    people.friends = pFriends;
    args.put("value", people);
    values = requestSerializer.serialize(args);
    decodedArgs = requestDeserializer.deserialize(values);
    people = (People) decodedArgs.get("value");
    Assertions.assertEquals("user", people.name);
    // proto buffer encode and decode empty list to be null
    Assertions.assertEquals("friend", people.friends.get(0).name);
  }
}
