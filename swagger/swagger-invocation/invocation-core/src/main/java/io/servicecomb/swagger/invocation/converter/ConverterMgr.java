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
package io.servicecomb.swagger.invocation.converter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.servicecomb.swagger.invocation.InvocationType;
import io.servicecomb.swagger.invocation.converter.impl.ConverterCommon;
import io.servicecomb.swagger.invocation.converter.impl.ConverterSame;
import io.servicecomb.swagger.invocation.converter.impl.SameElementArrayToList;
import io.servicecomb.swagger.invocation.converter.impl.SameElementArrayToSet;
import io.servicecomb.swagger.invocation.converter.impl.SameElementCollectionToArray;

@Component
public class ConverterMgr {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConverterMgr.class);

  //  第一层key是src type，第二层key是target type
  private Map<Type, Map<Type, Converter>> srcTargetMap = new HashMap<>();

  // key为target type，这个场景，不关注源实例类型，直接用jackson convert
  // 效率最低，走到这个处理流程的，都需要打印日志，说明这是很坏的实践
  // 但是如果不做这个功能，又会有一堆人觉得约束太多
  private Map<Type, Converter> commonMap = new ConcurrentHashMap<>();

  private Converter same = ConverterSame.getInstance();

  private Converter arrayToList = SameElementArrayToList.getInstance();

  private Converter arrayToSet = SameElementArrayToSet.getInstance();

  // array的构造需要有类型，不能使用统一的处理
  private Map<Type, Converter> collectionToArrayMap = new ConcurrentHashMap<>();

  @Autowired(required = false)
  public void setCustomizedConverters(List<CustomizedConverter> converters) {
    for (CustomizedConverter converter : converters) {
      Map<Type, Converter> map = srcTargetMap.get(converter.getSrcType());
      if (map == null) {
        map = new HashMap<>();
        srcTargetMap.put(converter.getSrcType(), map);
      }
      map.put(converter.getTargetType(), converter);
    }
  }

  public Converter findConverter(InvocationType invocationType, Type provider, Type swagger) {
    if (InvocationType.CONSUMER.equals(invocationType)) {
      return findConverter(provider, swagger);
    }

    return findConverter(swagger, provider);
  }

  public Converter findConverter(Type src, Type target) {
    Converter converter = findSrcTarget(src, target);
    if (converter != null) {
      return converter;
    }

    converter = findAssignable(src, target);
    if (converter != null) {
      return converter;
    }

    converter = findCollectionToArray(src, target);
    if (converter != null) {
      return converter;
    }

    converter = findArrayToCollection(src, target);
    if (converter != null) {
      return converter;
    }

    LOGGER.warn("Bad practice, low performance, convert from {} to {}", src, target);
    return findCommonConverter(target);
  }

  protected Converter findSrcTarget(Type src, Type target) {
    Map<Type, Converter> map = srcTargetMap.get(src);
    if (map == null) {
      return null;
    }

    return map.get(target);
  }

  protected Converter findCommonConverter(Type target) {
    Converter converter = commonMap.get(target);
    if (converter != null) {
      return converter;
    }

    // 并发导致重复创建没有问题
    converter = new ConverterCommon(target);
    commonMap.put(target, converter);
    return converter;
  }

  protected Converter findArrayToCollection(Type src, Type target) {
    if (src.getClass().equals(Class.class) && ParameterizedType.class.isAssignableFrom(target.getClass())) {
      Class<?> srcCls = (Class<?>) src;
      ParameterizedType targetType = (ParameterizedType) target;
      Class<?> targetCls = (Class<?>) targetType.getRawType();

      if (srcCls.isArray() && srcCls.getComponentType().equals(targetType.getActualTypeArguments()[0])) {
        if (List.class.isAssignableFrom(targetCls)) {
          return arrayToList;
        }
        if (Set.class.isAssignableFrom(targetCls)) {
          return arrayToSet;
        }
      }
    }

    return null;
  }

  protected Converter findAssignable(Type src, Type target) {
    if (TypeUtils.isAssignable(src, target)) {
      return same;
    }

    return null;
  }

  protected Converter findCollectionToArray(Type src, Type target) {
    if (ParameterizedType.class.isAssignableFrom(src.getClass()) && target.getClass().equals(Class.class)) {
      ParameterizedType srcType = (ParameterizedType) src;
      Class<?> srcCls = (Class<?>) srcType.getRawType();
      Class<?> targetCls = (Class<?>) target;

      if (Collection.class.isAssignableFrom(srcCls) && targetCls.isArray()
          && srcType.getActualTypeArguments()[0].equals(targetCls.getComponentType())) {
        Converter converter = collectionToArrayMap.get(target);
        if (converter == null) {
          converter = new SameElementCollectionToArray(targetCls.getComponentType());
          collectionToArrayMap.put(target, converter);
        }
        return converter;
      }
    }

    return null;
  }
}
