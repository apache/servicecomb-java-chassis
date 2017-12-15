package io.servicecomb.transport.rest.vertx.accesslog.element.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class BytesWrittenV1ElementTest {

  private static final BytesWrittenV1Element ELEMENT = new BytesWrittenV1Element();

  @Test
  public void getFormattedElement() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext mockContext = Mockito.mock(RoutingContext.class);
    HttpServerResponse mockResponse = Mockito.mock(HttpServerResponse.class);
    long bytesWritten = 16l;

    param.setRoutingContext(mockContext);
    Mockito.when(mockContext.response()).thenReturn(mockResponse);
    Mockito.when(mockResponse.bytesWritten()).thenReturn(bytesWritten);

    String result = ELEMENT.getFormattedElement(param);

    assertEquals(String.valueOf(bytesWritten), result);
  }


  @Test
  public void getFormattedElementOnResponseIsNull() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext mockContext = Mockito.mock(RoutingContext.class);

    param.setRoutingContext(mockContext);
    Mockito.when(mockContext.response()).thenReturn(null);

    String result = ELEMENT.getFormattedElement(param);

    assertEquals("0", result);
  }


  @Test
  public void getFormattedElementOnBytesWrittenIsZero() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext mockContext = Mockito.mock(RoutingContext.class);
    HttpServerResponse mockResponse = Mockito.mock(HttpServerResponse.class);
    long bytesWritten = 0l;

    param.setRoutingContext(mockContext);
    Mockito.when(mockContext.response()).thenReturn(mockResponse);
    Mockito.when(mockResponse.bytesWritten()).thenReturn(bytesWritten);

    String result = ELEMENT.getFormattedElement(param);

    assertEquals("0", result);
  }
}