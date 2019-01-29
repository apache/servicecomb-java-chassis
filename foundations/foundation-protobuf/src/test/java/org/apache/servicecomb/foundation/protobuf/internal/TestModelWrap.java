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
package org.apache.servicecomb.foundation.protobuf.internal;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.protobuf.ProtoMapper;
import org.apache.servicecomb.foundation.protobuf.ProtoMapperFactory;
import org.apache.servicecomb.foundation.protobuf.RootDeserializer;
import org.apache.servicecomb.foundation.protobuf.RootSerializer;
import org.junit.Assert;
import org.junit.Test;

import io.vertx.core.json.Json;

public class TestModelWrap {
  protected static ProtoMapperFactory factory = new ProtoMapperFactory();

  protected static ProtoMapper modelProtoMapper = factory.createFromName("model.proto");

  public static class User {
    public String name;

    public User(String name) {
      this.name = name;
    }
  }

  static User user = new User("uName");

  public static class PojoModel {
    public List<List<User>> listListUser;

    public List<Map<String, User>> listMapUser;

    public Map<String, List<User>> mapListUser;

    public Map<String, Map<String, User>> mapMapUser;

    public List<List<List<User>>> listListListUser;

    public List<List<Map<String, User>>> listListMapUser;

    public List<Map<String, List<User>>> listMapListUser;

    public List<Map<String, Map<String, User>>> listMapMapUser;

    public Map<String, List<List<User>>> mapListListUser;

    public Map<String, List<Map<String, User>>> mapListMapUser;

    public Map<String, Map<String, List<User>>> mapMapListUser;

    public Map<String, Map<String, Map<String, User>>> mapMapMapUser;

    public void init() {
      List<User> listUser = Arrays.asList(user, user);

      Map<String, User> mapUser = new LinkedHashMap<>();
      mapUser.put("k1", user);
      mapUser.put("k2", user);

      listListUser = Arrays.asList(listUser, listUser);
      listMapUser = Arrays.asList(mapUser, mapUser);

      mapListUser = new LinkedHashMap<>();
      mapListUser.put("k1", listUser);
      mapListUser.put("k2", listUser);

      mapMapUser = new LinkedHashMap<>();
      mapMapUser.put("k1", mapUser);
      mapMapUser.put("k2", mapUser);

      listListListUser = Arrays.asList(listListUser, listListUser);
      listListMapUser = Arrays.asList(listMapUser, listMapUser);
      listMapListUser = Arrays.asList(mapListUser, mapListUser);
      listMapMapUser = Arrays.asList(mapMapUser, mapMapUser);

      mapListListUser = new LinkedHashMap<>();
      mapListListUser.put("k1", listListUser);
      mapListListUser.put("k2", listListUser);

      mapListMapUser = new LinkedHashMap<>();
      mapListMapUser.put("k1", listMapUser);
      mapListMapUser.put("k2", listMapUser);

      mapMapListUser = new LinkedHashMap<>();
      mapMapListUser.put("k1", mapListUser);
      mapMapListUser.put("k2", mapListUser);

      mapMapMapUser = new LinkedHashMap<>();
      mapMapMapUser.put("k1", mapMapUser);
      mapMapMapUser.put("k2", mapMapUser);
    }
  }

  public static class ProtoModel {
    public List<ProtoListUser> listListUser;

    public List<ProtoMapUser> listMapUser;

    public Map<String, ProtoListUser> mapListUser;

    public Map<String, ProtoMapUser> mapMapUser;

    public List<ProtoListListUser> listListListUser;

    public List<ProtoListMapUser> listListMapUser;

    public List<ProtoMapListUser> listMapListUser;

    public List<ProtoMapMapUser> listMapMapUser;

    public Map<String, ProtoListListUser> mapListListUser;

    public Map<String, ProtoListMapUser> mapListMapUser;

    public Map<String, ProtoMapListUser> mapMapListUser;

    public Map<String, ProtoMapMapUser> mapMapMapUser;

    public void init() {
      ProtoListUser protoListUser = new ProtoListUser();
      protoListUser.init();

      ProtoMapUser protoMapUser = new ProtoMapUser();
      protoMapUser.init();

      ProtoListListUser protoListListUser = new ProtoListListUser();
      protoListListUser.init();

      ProtoListMapUser protoListMapUser = new ProtoListMapUser();
      protoListMapUser.init();

      ProtoMapListUser protoMapListUser = new ProtoMapListUser();
      protoMapListUser.init();

      ProtoMapMapUser protoMapMapUser = new ProtoMapMapUser();
      protoMapMapUser.init();

      listListUser = Arrays.asList(protoListUser, protoListUser);
      listMapUser = Arrays.asList(protoMapUser, protoMapUser);
      mapListUser = new LinkedHashMap<>();
      mapListUser.put("k1", protoListUser);
      mapListUser.put("k2", protoListUser);

      mapMapUser = new LinkedHashMap<>();
      mapMapUser.put("k1", protoMapUser);
      mapMapUser.put("k2", protoMapUser);

      listListListUser = Arrays.asList(protoListListUser, protoListListUser);
      listListMapUser = Arrays.asList(protoListMapUser, protoListMapUser);
      listMapListUser = Arrays.asList(protoMapListUser, protoMapListUser);
      listMapMapUser = Arrays.asList(protoMapMapUser, protoMapMapUser);

      mapListListUser = new LinkedHashMap<>();
      mapListListUser.put("k1", protoListListUser);
      mapListListUser.put("k2", protoListListUser);

      mapListMapUser = new LinkedHashMap<>();
      mapListMapUser.put("k1", protoListMapUser);
      mapListMapUser.put("k2", protoListMapUser);

      mapMapListUser = new LinkedHashMap<>();
      mapMapListUser.put("k1", protoMapListUser);
      mapMapListUser.put("k2", protoMapListUser);

      mapMapMapUser = new LinkedHashMap<>();
      mapMapMapUser.put("k1", protoMapMapUser);
      mapMapMapUser.put("k2", protoMapMapUser);
    }
  }

  public static class ProtoListListUser {
    public List<ProtoListUser> value;

    public void init() {
      ProtoListUser protoListUser = new ProtoListUser();
      protoListUser.init();
      value = Arrays.asList(protoListUser, protoListUser);
    }
  }

  public static class ProtoListMapUser {
    public List<ProtoMapUser> value;

    public void init() {
      ProtoMapUser protoMapUser = new ProtoMapUser();
      protoMapUser.init();
      value = Arrays.asList(protoMapUser, protoMapUser);
    }
  }

  public static class ProtoMapListUser {
    public Map<String, ProtoListUser> value;

    public void init() {
      ProtoListUser protoListUser = new ProtoListUser();
      protoListUser.init();
      value = new LinkedHashMap<>();
      value.put("k1", protoListUser);
      value.put("k2", protoListUser);
    }
  }

  public static class ProtoMapMapUser {
    public Map<String, ProtoMapUser> value;

    public void init() {
      ProtoMapUser protoMapUser = new ProtoMapUser();
      protoMapUser.init();
      value = new LinkedHashMap<>();
      value.put("k1", protoMapUser);
      value.put("k2", protoMapUser);
    }
  }

  public static class ProtoListUser {
    public List<User> value;

    public void init() {
      value = Arrays.asList(user, user);
    }
  }

  public static class ProtoMapUser {
    public Map<String, User> value;

    public void init() {
      value = new LinkedHashMap<>();
      value.put("k1", user);
      value.put("k2", user);
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void pojoModel() throws IOException {
    RootSerializer pojoSerializer = modelProtoMapper.createRootSerializer("PojoModel", PojoModel.class);
    RootDeserializer<Map<String, Object>> pojoMapDeserializer = modelProtoMapper
        .createRootDeserializer("PojoModel", Map.class);
    RootDeserializer<PojoModel> pojoModelDeserializer = modelProtoMapper
        .createRootDeserializer("PojoModel", PojoModel.class);

    RootSerializer protoSerializer = modelProtoMapper.createRootSerializer("ProtoModel", ProtoModel.class);
    RootDeserializer<Map<String, Object>> protoMapDeserializer = modelProtoMapper
        .createRootDeserializer("ProtoModel", Map.class);
    RootDeserializer<ProtoModel> protoModelDeserializer = modelProtoMapper
        .createRootDeserializer("ProtoModel", ProtoModel.class);

    PojoModel pojoModel = new PojoModel();
    pojoModel.init();
    String jsonPojoModel = Json.encode(pojoModel);
    Map<String, Object> mapFromPojoModel = (Map<String, Object>) Json.decodeValue(jsonPojoModel, Map.class);

    ProtoModel protoModel = new ProtoModel();
    protoModel.init();
    String jsonProtoModel = Json.encode(protoModel);
    Map<String, Object> mapFromProtoModel = (Map<String, Object>) Json.decodeValue(jsonProtoModel, Map.class);

    // serialize
    byte[] bytes = protoSerializer.serialize(protoModel);
    Assert.assertArrayEquals(bytes, protoSerializer.serialize(mapFromProtoModel));
    Assert.assertArrayEquals(bytes, pojoSerializer.serialize(pojoModel));
    Assert.assertArrayEquals(bytes, pojoSerializer.serialize(mapFromPojoModel));

    // deserialize pojoModel
    PojoModel newPojoModel = pojoModelDeserializer.deserialize(bytes);
    Assert.assertEquals(jsonPojoModel, Json.encode(newPojoModel));
    Map<String, Object> mapFromNewPojoModel = pojoMapDeserializer.deserialize(bytes);
    Assert.assertEquals(jsonPojoModel, Json.encode(mapFromNewPojoModel));

    // deserialize protoModel
    ProtoModel newProtoModel = protoModelDeserializer.deserialize(bytes);
    Assert.assertEquals(jsonProtoModel, Json.encode(newProtoModel));
    Map<String, Object> mapFromNewProtoModel = protoMapDeserializer.deserialize(bytes);
    Assert.assertEquals(jsonProtoModel, Json.encode(mapFromNewProtoModel));
  }
}
