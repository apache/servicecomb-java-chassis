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

package org.apache.servicecomb.common.rest.codec.param;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.codec.RestClientRequest;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.swagger.invocation.converter.Converter;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.inject.util.Types;

import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.FileProperty;
import io.swagger.models.properties.Property;

public class FormProcessorCreator implements ParamValueProcessorCreator {
  public static final String PARAMTYPE = "formData";

  public static class FormProcessor extends AbstractParamProcessor {
    private final boolean repeatedType;

    public FormProcessor(FormParameter formParameter, JavaType targetType) {
      super(formParameter.getName(), targetType, formParameter.getDefaultValue(), formParameter.getRequired());

      this.repeatedType = ArrayProperty.isType(formParameter.getType());
    }

    @Override
    public Object getValue(HttpServletRequest request) {
      @SuppressWarnings("unchecked")
      Map<String, Object> forms = (Map<String, Object>) request.getAttribute(RestConst.FORM_PARAMETERS);
      if (forms != null && !forms.isEmpty()) {
        return convertValue(forms.get(paramPath), targetType);
      }

      if (repeatedType) {
        //Even if the paramPath does not exist, it won't be null at now
        return convertValue(request.getParameterValues(paramPath), targetType);
      }

      Object value = request.getParameter(paramPath);
      if (value == null) {
        value = checkRequiredAndDefaultValue();
      }
      return convertValue(value, targetType);
    }

    private Object checkRequiredAndDefaultValue() {
      if (isRequired()) {
        throw new InvocationException(Status.BAD_REQUEST, "Parameter is required.");
      }
      return getDefaultValue();
    }

    @Override
    public void setValue(RestClientRequest clientRequest, Object arg) {
      clientRequest.addForm(paramPath, arg);
    }

    @Override
    public String getProcessorType() {
      return PARAMTYPE;
    }
  }

  public FormProcessorCreator() {
    ParamValueProcessorCreatorManager.INSTANCE.register(PARAMTYPE, this);
  }

  @Override
  public ParamValueProcessor create(Parameter parameter, Type genericParamType) {
    JavaType targetType =
        genericParamType == null ? null : TypeFactory.defaultInstance().constructType(genericParamType);

    if (isPart(parameter)) {
      return new PartProcessor((FormParameter) parameter, genericParamType);
    }
    return new FormProcessor((FormParameter) parameter, targetType);
  }

  private boolean isPart(Parameter parameter) {
    // no need to check Part[][] and so on
    FormParameter formParameter = (FormParameter) parameter;
    if ("array".equals(formParameter.getType())) {
      Property items = formParameter.getItems();
      return new FileProperty().getType().equals(items.getType());
    }
    return new FileProperty().getType().equals(formParameter.getType());
  }

  public static class PartProcessor extends AbstractParamProcessor {
    private static final Type partListType = Types.newParameterizedType(List.class, Part.class);

    // key is target type
    private static final Map<Type, Converter> partsToTargetConverters = SPIServiceUtils.getSortedService(Converter.class)
        .stream()
        .filter(c -> partListType.equals(c.getSrcType()))
        .collect(Collectors.toMap(Converter::getTargetType, Function.identity()));

    // key is target type
    private static final Map<Type, Converter> partToTargetConverters = SPIServiceUtils.getSortedService(Converter.class)
        .stream()
        .filter(c -> c.getSrcType() instanceof Class && Part.class.isAssignableFrom((Class<?>) c.getSrcType()))
        .collect(Collectors.toMap(Converter::getTargetType, Function.identity()));

    private final boolean repeatedType;

    private final Type genericParamType;

    private Converter converter;

    PartProcessor(FormParameter formParameter, Type genericParamType) {
      super(formParameter.getName(), null, formParameter.getDefaultValue(), formParameter.getRequired());

      this.genericParamType = genericParamType;
      this.repeatedType = ArrayProperty.isType(formParameter.getType());
      initConverter(genericParamType);
    }

    private void initConverter(Type genericParamType) {
      if (repeatedType) {
        initRepeatedConverter(genericParamType);
        return;
      }

      initNormalConverter(genericParamType);
    }

    private void initNormalConverter(Type genericParamType) {
      if (genericParamType instanceof JavaType) {
        genericParamType = ((JavaType) genericParamType).getRawClass();
      }
      converter = partToTargetConverters.get(genericParamType);
    }

    private void initRepeatedConverter(Type genericParamType) {
      if (genericParamType instanceof JavaType) {
        genericParamType = Types.newParameterizedType(((JavaType) genericParamType).getRawClass(),
            ((JavaType) genericParamType).getContentType());
      }
      converter = partsToTargetConverters.get(genericParamType);
    }

    @Override
    public Object getValue(HttpServletRequest request) throws Exception {
      if (repeatedType) {
        // get all parts
        List<Part> parts = request.getParts()
            .stream()
            .filter(part -> part.getName().equals(paramPath))
            .collect(Collectors.toList());
        return convertValue(converter, parts);
      }

      return convertValue(converter, request.getPart(paramPath));
    }

    public Object convertValue(Converter converter, Object value) {
      if (value == null || converter == null) {
        return value;
      }

      return converter.convert(value);
    }

    @Override
    public void setValue(RestClientRequest clientRequest, Object arg) throws Exception {
      clientRequest.attach(paramPath, arg);
    }

    @Override
    public String getProcessorType() {
      return PARAMTYPE;
    }
  }
}
