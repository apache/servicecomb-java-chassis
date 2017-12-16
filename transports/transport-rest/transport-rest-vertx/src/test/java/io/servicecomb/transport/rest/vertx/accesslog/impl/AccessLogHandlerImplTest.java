package io.servicecomb.transport.rest.vertx.accesslog.impl;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.TimeZone;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import io.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;
import io.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.DatetimeConfigurableElement;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.MethodElement;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.PlainTextElement;
import io.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogElementExtraction;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import mockit.Deencapsulation;

public class AccessLogHandlerImplTest {

  private static final AccessLogElement methodElement = new MethodElement();

  private static final AccessLogElement datetimeElement = new DatetimeConfigurableElement();

  private static final AccessLogElement plainTextElement = new PlainTextElement(" - ");

  private static final Logger logger = Mockito.mock(Logger.class);

  private static final AccessLogHandlerImpl accessLogHandlerImpl = new AccessLogHandlerImpl("rawPattern", s -> {
    assertEquals("rawPattern", s);
    return Arrays.asList(new AccessLogElementExtraction().setAccessLogElement(methodElement),
        new AccessLogElementExtraction().setAccessLogElement(plainTextElement),
        new AccessLogElementExtraction().setAccessLogElement(datetimeElement));
  });

  @BeforeClass
  public static void init() {
    Deencapsulation.setField(AccessLogHandlerImpl.class, "LOGGER", logger);
  }

  @Test
  public void testConstructor() {
    AccessLogElement[] elements = Deencapsulation.getField(accessLogHandlerImpl, "accessLogElements");
    assertEquals(3, elements.length);
    assertEquals(methodElement, elements[0]);
    assertEquals(plainTextElement, elements[1]);
    assertEquals(datetimeElement, elements[2]);
  }

  @Test
  public void handle() {
    RoutingContext testContext = Mockito.mock(RoutingContext.class);
    accessLogHandlerImpl.handle(testContext);
  }

  @Test
  public void testLog() {
    RoutingContext context = Mockito.mock(RoutingContext.class);
    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
    long startMillisecond = 1416863450581L;
    AccessLogParam accessLogParam = new AccessLogParam().setStartMillisecond(startMillisecond)
        .setRoutingContext(context);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DatetimeConfigurableElement.DEFAULT_DATETIME_PATTERN,
        DatetimeConfigurableElement.DEFAULT_LOCALE);
    simpleDateFormat.setTimeZone(TimeZone.getDefault());

    Mockito.when(context.request()).thenReturn(request);
    Mockito.when(request.method()).thenReturn(HttpMethod.DELETE);

    Deencapsulation.invoke(accessLogHandlerImpl, "log", accessLogParam);

    Mockito.verify(logger).info("DELETE" + " - " + simpleDateFormat.format(startMillisecond));
  }
}