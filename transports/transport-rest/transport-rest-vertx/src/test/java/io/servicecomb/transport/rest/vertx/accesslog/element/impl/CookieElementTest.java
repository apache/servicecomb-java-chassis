package io.servicecomb.transport.rest.vertx.accesslog.element.impl;

import static org.junit.Assert.*;

import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.CookieImpl;

public class CookieElementTest {

  public static final String COOKIE_NAME = "cookieName";

  private static final CookieElement ELEMENT = new CookieElement(COOKIE_NAME);

  @Test
  public void getFormattedElement() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext mockContext = Mockito.mock(RoutingContext.class);
    HashSet<Cookie> cookieSet = new HashSet<>();
    String cookieValue = "cookieValue";
    CookieImpl cookie = new CookieImpl(COOKIE_NAME, cookieValue);

    cookieSet.add(cookie);
    Mockito.when(mockContext.cookieCount()).thenReturn(1);
    Mockito.when(mockContext.cookies()).thenReturn(cookieSet);
    param.setRoutingContext(mockContext);

    String result = ELEMENT.getFormattedElement(param);

    Assert.assertEquals(cookieValue, result);
  }

  @Test
  public void getFormattedElementOnCookieCountIsZero() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext mockContext = Mockito.mock(RoutingContext.class);
    HashSet<Cookie> cookieSet = new HashSet<>();

    Mockito.when(mockContext.cookieCount()).thenReturn(0);
    Mockito.when(mockContext.cookies()).thenReturn(cookieSet);
    param.setRoutingContext(mockContext);

    String result = ELEMENT.getFormattedElement(param);

    Assert.assertEquals("-", result);
  }

  @Test
  public void getFormattedElementOnCookieSetIsNull() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext mockContext = Mockito.mock(RoutingContext.class);

    Mockito.when(mockContext.cookieCount()).thenReturn(1);
    Mockito.when(mockContext.cookies()).thenReturn(null);
    param.setRoutingContext(mockContext);

    String result = ELEMENT.getFormattedElement(param);

    Assert.assertEquals("-", result);
  }

  @Test
  public void getFormattedElementOnNotFound() {
    AccessLogParam param = new AccessLogParam();
    RoutingContext mockContext = Mockito.mock(RoutingContext.class);
    HashSet<Cookie> cookieSet = new HashSet<>();
    String cookieValue = "cookieValue";
    CookieImpl cookie = new CookieImpl("anotherCookieName", cookieValue);

    cookieSet.add(cookie);
    Mockito.when(mockContext.cookieCount()).thenReturn(1);
    Mockito.when(mockContext.cookies()).thenReturn(cookieSet);
    param.setRoutingContext(mockContext);

    String result = ELEMENT.getFormattedElement(param);

    Assert.assertEquals("-", result);
  }
}