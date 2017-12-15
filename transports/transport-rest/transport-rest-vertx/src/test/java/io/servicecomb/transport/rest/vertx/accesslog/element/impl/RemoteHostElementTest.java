package io.servicecomb.transport.rest.vertx.accesslog.element.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.RoutingContext;

public class RemoteHostElementTest {

  public static final RemoteHostElement ELEMENT = new RemoteHostElement();

  @Test
  public void getFormattedElement() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext context = Mockito.mock(RoutingContext.class);
    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
    SocketAddress address = Mockito.mock(SocketAddress.class);
    String remoteHost = "remoteHost";

    param.setRoutingContext(context);
    Mockito.when(context.request()).thenReturn(request);
    Mockito.when(request.remoteAddress()).thenReturn(address);
    Mockito.when(address.host()).thenReturn(remoteHost);

    String result = ELEMENT.getFormattedElement(param);

    assertEquals(remoteHost, result);
  }

  @Test
  public void getFormattedElementOnRequestIsNull() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext context = Mockito.mock(RoutingContext.class);

    param.setRoutingContext(context);
    Mockito.when(context.request()).thenReturn(null);

    String result = ELEMENT.getFormattedElement(param);

    assertEquals("-", result);
  }


  @Test
  public void getFormattedElementOnRemoteAddressIsNull() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext context = Mockito.mock(RoutingContext.class);
    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);

    param.setRoutingContext(context);
    Mockito.when(context.request()).thenReturn(request);
    Mockito.when(request.remoteAddress()).thenReturn(null);

    String result = ELEMENT.getFormattedElement(param);

    assertEquals("-", result);
  }


  @Test
  public void getFormattedElementOnHostIsNull() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext context = Mockito.mock(RoutingContext.class);
    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
    SocketAddress address = Mockito.mock(SocketAddress.class);

    param.setRoutingContext(context);
    Mockito.when(context.request()).thenReturn(request);
    Mockito.when(request.remoteAddress()).thenReturn(address);
    Mockito.when(address.host()).thenReturn(null);

    String result = ELEMENT.getFormattedElement(param);

    assertEquals("-", result);
  }


  @Test
  public void getFormattedElementOnHostIsEmpty() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext context = Mockito.mock(RoutingContext.class);
    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
    SocketAddress address = Mockito.mock(SocketAddress.class);
    String remoteHost = "";

    param.setRoutingContext(context);
    Mockito.when(context.request()).thenReturn(request);
    Mockito.when(request.remoteAddress()).thenReturn(address);
    Mockito.when(address.host()).thenReturn(remoteHost);

    String result = ELEMENT.getFormattedElement(param);

    assertEquals("-", result);
  }
}