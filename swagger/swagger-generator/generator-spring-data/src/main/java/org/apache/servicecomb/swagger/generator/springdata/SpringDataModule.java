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
package org.apache.servicecomb.swagger.generator.springdata;

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.foundation.common.utils.SPIOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;

public class SpringDataModule extends SimpleModule implements SPIOrder {
  private static final long serialVersionUID = 1L;

  @JsonDeserialize(as = PageImpl.class)
  @JsonPropertyOrder(alphabetic = true)
  public static class PageMixin<T> {
    @JsonCreator
    public PageMixin(@JsonProperty(value = "content") List<T> content,
        @JsonProperty("pageable") Pageable pageable,
        @JsonProperty("total") long total) {
    }
  }

  @JsonDeserialize(as = PageRequest.class)
  @JsonPropertyOrder(alphabetic = true)
  public static class PageableMixin {
    @JsonCreator
    public PageableMixin(@JsonProperty(value = "pageNumber") int page,
        @JsonProperty("pageSize") int size, @JsonProperty(value = "sort") Sort sort) {
    }
  }

  public static class SortConverter implements Converter<SortMixin, Sort> {
    @Override
    public Sort convert(SortMixin value) {
      return Sort.by(value.getProperties());
    }

    @Override
    public JavaType getInputType(TypeFactory typeFactory) {
      return typeFactory.constructType(SortMixin.class);
    }

    @Override
    public JavaType getOutputType(TypeFactory typeFactory) {
      return typeFactory.constructType(Sort.class);
    }
  }

  public static class SortMixinConverter implements Converter<Sort, SortMixin> {
    @Override
    public SortMixin convert(Sort value) {
      List<String> properties = new ArrayList<>();
      for (Order order : value) {
        properties.add(order.getProperty());
      }
      SortMixin result = new SortMixin();
      result.setProperties(properties.toArray(new String[0]));
      return result;
    }

    @Override
    public JavaType getInputType(TypeFactory typeFactory) {
      return typeFactory.constructType(Sort.class);
    }

    @Override
    public JavaType getOutputType(TypeFactory typeFactory) {
      return typeFactory.constructType(SortMixin.class);
    }
  }

  @JsonPropertyOrder(alphabetic = true)
  @JsonDeserialize(converter = SortConverter.class)
  @JsonSerialize(converter = SortMixinConverter.class)
  public static class SortMixin {
    private String[] properties;

    @JsonCreator
    public SortMixin() {
    }

    public void setProperties(String[] properties) {
      this.properties = properties;
    }

    public String[] getProperties() {
      return this.properties;
    }
  }

  public SpringDataModule() {
    super("springData");

    setMixInAnnotation(Page.class, PageMixin.class);
    setMixInAnnotation(Pageable.class, PageableMixin.class);
    setMixInAnnotation(Sort.class, SortMixin.class);

    setMixInAnnotation(PageImpl.class, PageMixin.class);
    setMixInAnnotation(PageRequest.class, PageableMixin.class);
  }

  @Override
  public Object getTypeId() {
    return getModuleName();
  }

  @Override
  public int getOrder() {
    return Short.MAX_VALUE;
  }
}
