package io.servicecomb.swagger.generator.core.processor.annotation;

import java.util.List;

import io.servicecomb.swagger.generator.core.OperationGenerator;
import io.servicecomb.swagger.generator.core.ParameterAnnotationProcessor;
import io.swagger.annotations.ApiParam;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;

public class ApiParamAnnotationProcessor implements ParameterAnnotationProcessor {
  @Override
  public void process(Object annotation, OperationGenerator operationGenerator, int paramIdx) {
    List<Parameter> providerParameters = operationGenerator.getProviderParameters();
    String collectionFormat = ((ApiParam) annotation).collectionFormat();
    Parameter parameter = providerParameters.get(paramIdx);
    if (parameter instanceof QueryParameter) {
      ((QueryParameter) parameter).setCollectionFormat(collectionFormat);
    }
  }
}
