package org.apache.servicecomb.demo.produceprocessor;

import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.common.rest.codec.produce.ProduceProcessor;
import org.apache.servicecomb.demo.utils.JAXBUtils;

import com.fasterxml.jackson.databind.JavaType;

public class ProduceAppXmlProcessor implements ProduceProcessor {

  @Override
  public String getName() {
    return MediaType.APPLICATION_XML;
  }

  @Override
  public int getOrder() {
    return 0;
  }

  @Override
  public void doEncodeResponse(OutputStream output, Object result) throws Exception {
    output.write(JAXBUtils.convertToXml(result).getBytes());
  }

  @Override
  public Object doDecodeResponse(InputStream input, JavaType type) throws Exception {
    return JAXBUtils.convertToJavaBean(input, type);
  }

}
