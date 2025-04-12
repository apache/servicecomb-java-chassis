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

package org.apache.servicecomb.swagger.jakarta;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.jackson.TypeNameResolver;
import io.swagger.models.Model;
import io.swagger.models.properties.Property;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.xml.bind.annotation.XmlElement;

/**
 * replace io.swagger.jackson.AbstractModelConverter to adapter JAVAEE9 jakarta API
 * modifying content: mapper register model change to SwaggerAnnotationIntrospectorAdapterJakarta to adapter jakarta.
 */
public abstract class AbstractModelConverterAdapterJakarta implements ModelConverter {
  protected final ObjectMapper _mapper;
  protected final AnnotationIntrospector _intr;
  protected final TypeNameResolver _typeNameResolver;
  /**
   * Minor optimization: no need to keep on resolving same types over and over
   * again.
   */
  protected Map<JavaType, String> _resolvedTypeNames = new ConcurrentHashMap<JavaType, String>();

  protected AbstractModelConverterAdapterJakarta(ObjectMapper mapper) {
    this(mapper, TypeNameResolver.std);
  }

  protected AbstractModelConverterAdapterJakarta(ObjectMapper mapper, TypeNameResolver typeNameResolver) {
    mapper.registerModule(
        new SimpleModule("swagger", Version.unknownVersion()) {
          @Override
          public void setupModule(SetupContext context) {
            context.insertAnnotationIntrospector(new SwaggerAnnotationIntrospectorAdapterJakarta());
          }
        });
    _mapper = mapper;
    _typeNameResolver = typeNameResolver;
    _intr = mapper.getSerializationConfig().getAnnotationIntrospector();

  }

  protected static Comparator<Property> getPropertyComparator() {
    return new Comparator<Property>() {
      @Override
      public int compare(Property one, Property two) {
        if (one.getPosition() == null && two.getPosition() == null) {
          return 0;
        }
        if (one.getPosition() == null) {
          return -1;
        }
        if (two.getPosition() == null) {
          return 1;
        }
        return one.getPosition().compareTo(two.getPosition());
      }
    };
  }

  @Override
  public Property resolveProperty(Type type,
      ModelConverterContext context,
      Annotation[] annotations,
      Iterator<ModelConverter> chain) {
    if (chain.hasNext()) {
      return chain.next().resolveProperty(type, context, annotations, chain);
    } else {
      return null;
    }
  }

  protected String _description(Annotated ann) {
    // while name suggests it's only for properties, should work for any Annotated thing.
    // also; with Swagger introspector's help, should get it from ApiModel/ApiModelProperty
    return _intr.findPropertyDescription(ann);
  }

  protected String _typeName(JavaType type) {
    return _typeName(type, null);
  }

  protected String _typeName(JavaType type, BeanDescription beanDesc) {
    String name = _resolvedTypeNames.get(type);
    if (name != null) {
      return name;
    }
    name = _findTypeName(type, beanDesc);
    _resolvedTypeNames.put(type, name);
    return name;
  }

  /**
   * whether to resolve schema name by first using AnnotationInspector registered implementations
   * defaults to false, override returning `true` to obtain pre-1.5.24 behaviour
   *
   * @return false
   * @since 1.5.24
   */
  protected boolean prioritizeAnnotationInspectorSchemaName() {
    return false;
  }
  protected String _findTypeName(JavaType type, BeanDescription beanDesc) {
    // First, handle container types; they require recursion
    if (type.isArrayType()) {
      return "Array";
    }

    if (type.isMapLikeType()) {
      return "Map";
    }

    if (type.isContainerType()) {
      if (Set.class.isAssignableFrom(type.getRawClass())) {
        return "Set";
      }
      return "List";
    }
    if (beanDesc == null) {
      beanDesc = _mapper.getSerializationConfig().introspectClassAnnotations(type);
    }

    if (!prioritizeAnnotationInspectorSchemaName()) {
      final ApiModel model = type.getRawClass().getAnnotation(ApiModel.class);
      if (model != null && StringUtils.isNotBlank(model.value())) {
        return _typeNameResolver.nameForType(type);
      }
    }
    PropertyName rootName = _intr.findRootName(beanDesc.getClassInfo());
    if (rootName != null && rootName.hasSimpleName()) {
      return rootName.getSimpleName();
    }
    return _typeNameResolver.nameForType(type);
  }

  protected String _typeQName(JavaType type) {
    return type.getRawClass().getName();
  }

  protected String _subTypeName(NamedType type) {
    // !!! TODO: should this use 'name' instead?
    return type.getType().getName();
  }

  protected String _findDefaultValue(Annotated a) {
    XmlElement elem = a.getAnnotation(XmlElement.class);
    if (elem != null) {
      if (!elem.defaultValue().isEmpty() && !"\u0000".equals(elem.defaultValue())) {
        return elem.defaultValue();
      }
    }
    return null;
  }

  protected String _findExampleValue(Annotated a) {
    ApiModelProperty prop = a.getAnnotation(ApiModelProperty.class);
    if (prop != null) {
      if (!prop.example().isEmpty()) {
        return prop.example();
      }
    }
    return null;
  }

  protected Boolean _findReadOnly(Annotated a) {
    ApiModelProperty prop = a.getAnnotation(ApiModelProperty.class);
    if (prop != null) {
      return prop.readOnly();
    }
    return null;
  }

  protected Boolean _findReadOnlyFromAccessMode(Annotated a) {
    ApiModelProperty prop = a.getAnnotation(ApiModelProperty.class);
    if (prop != null) {
      if (prop.accessMode().equals(ApiModelProperty.AccessMode.AUTO)) {
        return null;
      } else if(prop.accessMode().equals(ApiModelProperty.AccessMode.READ_ONLY)) {
        return true;
      }
      return false;
    }
    return null;
  }

  protected boolean _isSetType(Class<?> cls) {
    if (cls != null) {

      if (java.util.Set.class.equals(cls)) {
        return true;
      } else {
        for (Class<?> a : cls.getInterfaces()) {
          // this is dirty and ugly and needs to be extended into a scala model converter.  But to avoid bringing in scala runtime...
          if (java.util.Set.class.equals(a) || "interface scala.collection.Set".equals(a.toString())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public Model resolve(Type type, ModelConverterContext context, Iterator<ModelConverter> chain) {
    if (chain.hasNext()) {
      return chain.next().resolve(type, context, chain);
    } else {
      return null;
    }
  }
}
