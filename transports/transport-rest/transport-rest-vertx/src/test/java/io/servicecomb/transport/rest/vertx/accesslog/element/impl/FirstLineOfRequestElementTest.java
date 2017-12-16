package io.servicecomb.transport.rest.vertx.accesslog.element.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpVersion;
import io.vertx.ext.web.RoutingContext;

public class FirstLineOfRequestElementTest {

  public static final FirstLineOfRequestElement ELEMENT = new FirstLineOfRequestElement();

  @Test
  public void getFormattedElement() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext mockContext = Mockito.mock(RoutingContext.class);
    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
    String uri = "/test/uri";

    param.setRoutingContext(mockContext);
    Mockito.when(mockContext.request()).thenReturn(request);
    Mockito.when(request.method()).thenReturn(HttpMethod.DELETE);
    Mockito.when(request.path()).thenReturn(uri);
    Mockito.when(request.version()).thenReturn(HttpVersion.HTTP_1_1);

    String result = ELEMENT.getFormattedElement(param);

    assertEquals("\"DELETE " + uri + " HTTP/1.1\"", result);
  }
}