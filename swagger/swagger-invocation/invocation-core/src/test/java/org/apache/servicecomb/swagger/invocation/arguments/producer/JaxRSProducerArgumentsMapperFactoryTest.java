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

package org.apache.servicecomb.swagger.invocation.arguments.producer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.Part;
import javax.ws.rs.BeanParam;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.apache.servicecomb.swagger.generator.jaxrs.JaxrsSwaggerGeneratorContext;
import org.apache.servicecomb.swagger.generator.pojo.PojoSwaggerGeneratorContext;
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentsMapperConfig;
import org.apache.servicecomb.swagger.invocation.arguments.ProviderParameter;
import org.apache.servicecomb.swagger.invocation.arguments.producer.ProducerArgumentsMapperFactory.ParamWrapper;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import io.swagger.models.parameters.CookieParameter;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import mockit.Deencapsulation;

public class JaxRSProducerArgumentsMapperFactoryTest {

  private final JaxRSProducerArgumentsMapperFactory producerArgumentsMapperFactory = new JaxRSProducerArgumentsMapperFactory();

  @Test
  public void canProcess() {
    ArgumentsMapperConfig argumentsMapperConfig = new ArgumentsMapperConfig();

    argumentsMapperConfig.setSwaggerGeneratorContext(new JaxrsSwaggerGeneratorContext());
    Assert.assertTrue(producerArgumentsMapperFactory.canProcess(argumentsMapperConfig));

    argumentsMapperConfig.setSwaggerGeneratorContext(new PojoSwaggerGeneratorContext());
    Assert.assertFalse(producerArgumentsMapperFactory.canProcess(argumentsMapperConfig));
  }

  @Test
  public void findAggregatedParamNames() throws NoSuchMethodException {
    Map<String, ProviderParameter> providerParamMap = new HashMap<>();
    providerParamMap.put("qqq", new ProviderParameter(1, String.class, "qqq"));
    Method aggregatedTestParamMethod = AggregatedParamProvider.class
        .getMethod("aggregatedParamTest", AggregatedTestParam.class);
    Annotation beanParamAnnotation = aggregatedTestParamMethod.getParameterAnnotations()[0][0];
    providerParamMap.put("aggregatedTestParam",
        new ProviderParameter(0, AggregatedTestParam.class, "aggregatedTestParam")
            .setAnnotations(new Annotation[] {beanParamAnnotation}));

    Map<String, ParamWrapper<Parameter>> swaggerParamMap = new HashMap<>();
    Parameter swaggerParam = new PathParameter().name("pathSwaggerParam");
    swaggerParamMap.put("pathSwaggerParam", new ParamWrapper<>(swaggerParam).setIndex(0));
    swaggerParam = new HeaderParameter().name("headerSwaggerParam");
    swaggerParamMap.put("headerSwaggerParam", new ParamWrapper<>(swaggerParam).setIndex(1));
    swaggerParam = new CookieParameter().name("intSwaggerVal");
    swaggerParamMap.put("intSwaggerVal", new ParamWrapper<>(swaggerParam).setIndex(2));
    swaggerParam = new FormParameter().name("longSwaggerVal");
    swaggerParamMap.put("longSwaggerVal", new ParamWrapper<>(swaggerParam).setIndex(3));
    swaggerParam = new QueryParameter().name("querySwaggerParam");
    swaggerParamMap.put("querySwaggerParam", new ParamWrapper<>(swaggerParam).setIndex(4));
    swaggerParam = new FormParameter().name("uploadSwaggerParam");
    swaggerParamMap.put("uploadSwaggerParam", new ParamWrapper<>(swaggerParam).setIndex(5));
    swaggerParam = new QueryParameter().name("qqq");
    swaggerParamMap.put("qqq", new ParamWrapper<>(swaggerParam).setIndex(6));

    Set<String> aggregatedParamNames = producerArgumentsMapperFactory
        .findAggregatedParamNames(providerParamMap, swaggerParamMap);
    Assert.assertThat(aggregatedParamNames, Matchers.contains("aggregatedTestParam"));
  }

  @Test
  public void generateAggregatedParamMapper() throws NoSuchMethodException {
    Map<String, ProviderParameter> providerParamMap = new HashMap<>();
    providerParamMap.put("qqq", new ProviderParameter(1, String.class, "qqq"));
    Method aggregatedTestParamMethod = AggregatedParamProvider.class
        .getMethod("aggregatedParamTest", AggregatedTestParam.class);
    Annotation beanParamAnnotation = aggregatedTestParamMethod.getParameterAnnotations()[0][0];
    providerParamMap.put("aggregatedTestParam",
        new ProviderParameter(0, AggregatedTestParam.class, "aggregatedTestParam")
            .setAnnotations(new Annotation[] {beanParamAnnotation}));

    Map<String, ParamWrapper<Parameter>> swaggerParamMap = new HashMap<>();
    Parameter swaggerParam = new PathParameter().name("pathSwaggerParam");
    swaggerParamMap.put("pathSwaggerParam", new ParamWrapper<>(swaggerParam).setIndex(0));
    swaggerParam = new HeaderParameter().name("headerSwaggerParam");
    swaggerParamMap.put("headerSwaggerParam", new ParamWrapper<>(swaggerParam).setIndex(1));
    swaggerParam = new CookieParameter().name("intSwaggerVal");
    swaggerParamMap.put("intSwaggerVal", new ParamWrapper<>(swaggerParam).setIndex(2));
    swaggerParam = new FormParameter().name("longSwaggerVal");
    swaggerParamMap.put("longSwaggerVal", new ParamWrapper<>(swaggerParam).setIndex(3));
    swaggerParam = new QueryParameter().name("querySwaggerParam");
    swaggerParamMap.put("querySwaggerParam", new ParamWrapper<>(swaggerParam).setIndex(4));
    swaggerParam = new FormParameter().name("uploadSwaggerParam");
    swaggerParamMap.put("uploadSwaggerParam", new ParamWrapper<>(swaggerParam).setIndex(5));
    swaggerParam = new QueryParameter().name("qqq");
    swaggerParamMap.put("qqq", new ParamWrapper<>(swaggerParam).setIndex(6));

    Set<String> aggregatedParamNames = new HashSet<>();
    aggregatedParamNames.add("aggregatedTestParam");
    ArgumentsMapperConfig argumentsMapperConfig = new ArgumentsMapperConfig();
    producerArgumentsMapperFactory.generateAggregatedParamMapper(
        argumentsMapperConfig, providerParamMap, swaggerParamMap, aggregatedParamNames);

    Assert.assertEquals(1, argumentsMapperConfig.getArgumentMapperList().size());
    Assert.assertEquals(ProducerBeanParamMapper.class, argumentsMapperConfig.getArgumentMapperList().get(0).getClass());
    ProducerBeanParamMapper producerBeanParamMapper =
        (ProducerBeanParamMapper) argumentsMapperConfig.getArgumentMapperList().get(0);
    Assert.assertEquals(Integer.valueOf(0), Deencapsulation.getField(producerBeanParamMapper, "producerIdx"));
    Map<String, Integer> swaggerParamIndexMap =
        Deencapsulation.getField(producerBeanParamMapper, "swaggerParamIndexMap");
    Assert.assertEquals(Integer.valueOf(0), swaggerParamIndexMap.get("pathParam"));
    Assert.assertEquals(Integer.valueOf(1), swaggerParamIndexMap.get("headerParam"));
    Assert.assertEquals(Integer.valueOf(2), swaggerParamIndexMap.get("intVal"));
    Assert.assertEquals(Integer.valueOf(3), swaggerParamIndexMap.get("longVal"));
    Assert.assertEquals(Integer.valueOf(4), swaggerParamIndexMap.get("q"));
    Assert.assertEquals(Integer.valueOf(5), swaggerParamIndexMap.get("uploaded"));
  }

  static class AggregatedParamProvider {
    public String aggregatedParamTest(@BeanParam AggregatedTestParam aggregatedTestParam) {
      return null;
    }
  }

  static class AggregatedTestParam {
    @PathParam("pathSwaggerParam")
    private String pathParam;

    private String queryParam;

    @DefaultValue("defaultHeader")
    @HeaderParam(value = "headerSwaggerParam")
    private String headerParam;

    @CookieParam("intSwaggerVal")
    private int intVal;

    @FormParam("longSwaggerVal")
    private long longVal;

    private Part uploaded;

    public String getPathParam() {
      return pathParam;
    }

    public AggregatedTestParam setPathParam(String pathParam) {
      this.pathParam = pathParam;
      return this;
    }

    public String getQ() {
      return queryParam;
    }

    @DefaultValue("defaultQuery")
    @QueryParam(value = "querySwaggerParam")
    public AggregatedTestParam setQ(String queryParam) {
      this.queryParam = queryParam;
      return this;
    }

    public String getHeaderParam() {
      return headerParam;
    }

    public AggregatedTestParam setHeaderParam(String headerParam) {
      this.headerParam = headerParam;
      return this;
    }

    public int getIntVal() {
      return intVal;
    }

    public AggregatedTestParam setIntVal(int intVal) {
      this.intVal = intVal;
      return this;
    }

    public long getLongVal() {
      return longVal;
    }

    public AggregatedTestParam setLongVal(long longVal) {
      this.longVal = longVal;
      return this;
    }

    public Part getUploaded() {
      return uploaded;
    }

    @FormParam("uploadSwaggerParam")
    public AggregatedTestParam setUploaded(Part uploaded) {
      this.uploaded = uploaded;
      return this;
    }
  }
}