package io.servicecomb.transport.rest.vertx.accesslog.element.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpVersion;
import io.vertx.ext.web.RoutingContext;

public class VersionOrProtocolElementTest {

  @Test
  public void getFormattedElement() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext context = Mockito.mock(RoutingContext.class);
    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);

    param.setRoutingContext(context);
    Mockito.when(context.request()).thenReturn(request);
    Mockito.when(request.version()).thenReturn(HttpVersion.HTTP_1_1);

    String result = new VersionOrProtocolElement().getFormattedElement(param);
    assertEquals("HTTP/1.1", result);

    Mockito.when(request.version()).thenReturn(HttpVersion.HTTP_1_0);
    result = new VersionOrProtocolElement().getFormattedElement(param);
    assertEquals("HTTP/1.0", result);

    Mockito.when(request.version()).thenReturn(HttpVersion.HTTP_2);
    result = new VersionOrProtocolElement().getFormattedElement(param);
    assertEquals("HTTP/2.0", result);
  }

  @Test
  public void getFormattedElementOnRequestIsNull() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext context = Mockito.mock(RoutingContext.class);

    param.setRoutingContext(context);
    Mockito.when(context.request()).thenReturn(null);

    String result = new VersionOrProtocolElement().getFormattedElement(param);

    assertEquals("-", result);
  }


  @Test
  public void getFormattedElementOnVersionIsNull() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext context = Mockito.mock(RoutingContext.class);
    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);

    param.setRoutingContext(context);
    Mockito.when(context.request()).thenReturn(request);
    Mockito.when(request.version()).thenReturn(null);

    String result = new VersionOrProtocolElement().getFormattedElement(param);

    assertEquals("-", result);
  }
}