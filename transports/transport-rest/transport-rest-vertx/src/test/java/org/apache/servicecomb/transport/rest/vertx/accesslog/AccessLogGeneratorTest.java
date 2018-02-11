package org.apache.servicecomb.transport.rest.vertx.accesslog;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.TimeZone;

import org.apache.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.DatetimeConfigurableElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.MethodElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.PlainTextElement;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogElementExtraction;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import mockit.Deencapsulation;

public class AccessLogGeneratorTest {

  private static final AccessLogElement methodElement = new MethodElement();

  private static final AccessLogElement datetimeElement = new DatetimeConfigurableElement();

  private static final AccessLogElement plainTextElement = new PlainTextElement(" - ");

  private static final AccessLogGenerator ACCESS_LOG_GENERATOR = new AccessLogGenerator("rawPattern", s -> {
    assertEquals("rawPattern", s);
    return Arrays.asList(new AccessLogElementExtraction().setAccessLogElement(methodElement),
        new AccessLogElementExtraction().setAccessLogElement(plainTextElement),
        new AccessLogElementExtraction().setAccessLogElement(datetimeElement));
  });

  @Test
  public void testConstructor() {
    AccessLogElement[] elements = Deencapsulation.getField(ACCESS_LOG_GENERATOR, "accessLogElements");
    assertEquals(3, elements.length);
    assertEquals(methodElement, elements[0]);
    assertEquals(plainTextElement, elements[1]);
    assertEquals(datetimeElement, elements[2]);
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

    String log = ACCESS_LOG_GENERATOR.generateLog(accessLogParam);

    Assert.assertEquals("DELETE" + " - " + simpleDateFormat.format(startMillisecond), log);
  }
}