package org.apache.servicecomb.swagger.invocation.validator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.validation.ParameterNameProvider;

import org.apache.servicecomb.swagger.generator.core.utils.ParamUtils;


public class DefaultParameterNameProviderEx implements ParameterNameProvider {

  @Override
  public List<String> getParameterNames(Constructor<?> constructor) {
    Parameter[] parameters = constructor.getParameters();
    List<String> parameterNames = new ArrayList<>(parameters.length);

    for (int i = 0; i < parameters.length; i++) {
      parameterNames.add(ParamUtils.getParameterName(constructor, i));
    }

    return Collections.unmodifiableList(parameterNames);
  }

  @Override
  public List<String> getParameterNames(Method method) {
    Parameter[] parameters = method.getParameters();
    List<String> parameterNames = new ArrayList<>(parameters.length);

    for (int i = 0; i < parameters.length; i++) {
      parameterNames.add(ParamUtils.getParameterName(method, i));
    }

    return Collections.unmodifiableList(parameterNames);
  }
}
