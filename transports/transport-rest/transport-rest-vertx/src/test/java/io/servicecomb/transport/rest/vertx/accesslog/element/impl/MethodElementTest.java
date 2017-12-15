package io.servicecomb.transport.rest.vertx.accesslog.element.impl;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;

public class MethodElementTest {

  @Test
  public void getFormattedElement() {
    RoutingContext routingContext = Mockito.mock(RoutingContext.class);
    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
    Mockito.when(routingContext.request()).thenReturn(request);
    Mockito.when(request.method()).thenReturn(HttpMethod.DELETE);
    AccessLogParam param = new AccessLogParam().setRoutingContext(routingContext);

    Assert.assertEquals("DELETE", new MethodElement().getFormattedElement(param));
  }

  @Test
  public void getFormattedElementOnRequestIsNull() {
    RoutingContext routingContext = Mockito.mock(RoutingContext.class);
    AccessLogParam param = new AccessLogParam().setRoutingContext(routingContext);

    Mockito.when(routingContext.request()).thenReturn(null);

    Assert.assertEquals("-", new MethodElement().getFormattedElement(param));
  }

  @Test
  public void getFormattedElementOnMethodIsNull() {
    RoutingContext routingContext = Mockito.mock(RoutingContext.class);
    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
    AccessLogParam param = new AccessLogParam().setRoutingContext(routingContext);

    Mockito.when(routingContext.request()).thenReturn(request);
    Mockito.when(request.method()).thenReturn(null);

    Assert.assertEquals("-", new MethodElement().getFormattedElement(param));
  }
}