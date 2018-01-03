package io.servicecomb.swagger.generator.core.processor.parametertype;

import io.servicecomb.swagger.extend.annotations.RequestParamColFmt;
import io.servicecomb.swagger.generator.core.OperationGenerator;
import io.servicecomb.swagger.generator.core.processor.parameter.AbstractParameterProcessor;
import io.swagger.models.parameters.QueryParameter;

public class RequestParamColFmtAnnotationProcessor extends AbstractParameterProcessor<QueryParameter> {
  @Override
  protected QueryParameter createParameter() {
    return new QueryParameter();
  }

  @Override
  protected String getAnnotationParameterName(Object annotation) {
    return ((RequestParamColFmt)annotation).name();
  }

  protected void fillParameter(Object annotation, OperationGenerator operationGenerator, int paramIdx,QueryParameter parameter){
    setParameterName(annotation, operationGenerator, paramIdx, parameter);
    setParameterType(operationGenerator, paramIdx, parameter);
    String collectionFormat = ((RequestParamColFmt) annotation).collectionFormat();
    parameter.setCollectionFormat(collectionFormat);
  }
}
