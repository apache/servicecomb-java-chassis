package io.servicecomb.transport.rest.vertx.accesslog.parser.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import io.servicecomb.transport.rest.vertx.accesslog.element.impl.BytesWrittenV1Element;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.BytesWrittenV2Element;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.CookieElement;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.DatetimeConfigurableElement;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.DurationMillisecondElement;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.DurationSecondElement;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.FirstLineOfRequestElement;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.LocalHostElement;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.LocalPortElement;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.MethodElement;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.PlainTextElement;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.QueryOnlyElement;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.RemoteHostElement;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.RequestHeaderElement;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.ResponseHeaderElement;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.StatusElement;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.UriPathIncludeQueryElement;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.UriPathOnlyElement;
import io.servicecomb.transport.rest.vertx.accesslog.element.impl.VersionOrProtocolElement;
import io.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogElementExtraction;
import mockit.Deencapsulation;

public class DefaultAccessLogPatternParserTest {
  private static final String ROW_PATTERN = "[cs-method] %m %s%T%D%h%v%p%B%b%r%U%q"
      + "cs-uri-stemcs-uri-querycs-uri%H%t%{yyyy MM dd HH:mm:ss zzz}t"
      + "%{yyyy MM dd HH:mm:ss|GMT+0|en-US}t"
      + "%{incoming-header}i"
      + "%{outgoing-header}o"
      + "%{cookie}c";

  private static DefaultAccessLogPatternParser accessLogPatternParser = new DefaultAccessLogPatternParser();

  @Test
  public void testParsePattern() {
    List<AccessLogElementExtraction> result = accessLogPatternParser.parsePattern(ROW_PATTERN);
    assertEquals(26, result.size());
    assertEquals(PlainTextElement.class, result.get(0).getAccessLogElement().getClass());
    assertEquals(MethodElement.class, result.get(1).getAccessLogElement().getClass());
    assertEquals(PlainTextElement.class, result.get(2).getAccessLogElement().getClass());
    assertEquals(MethodElement.class, result.get(3).getAccessLogElement().getClass());
    assertEquals(PlainTextElement.class, result.get(4).getAccessLogElement().getClass());
    assertEquals(StatusElement.class, result.get(5).getAccessLogElement().getClass());
    assertEquals(DurationSecondElement.class, result.get(6).getAccessLogElement().getClass());
    assertEquals(DurationMillisecondElement.class, result.get(7).getAccessLogElement().getClass());
    assertEquals(RemoteHostElement.class, result.get(8).getAccessLogElement().getClass());
    assertEquals(LocalHostElement.class, result.get(9).getAccessLogElement().getClass());
    assertEquals(LocalPortElement.class, result.get(10).getAccessLogElement().getClass());
    assertEquals(BytesWrittenV1Element.class, result.get(11).getAccessLogElement().getClass());
    assertEquals(BytesWrittenV2Element.class, result.get(12).getAccessLogElement().getClass());
    assertEquals(FirstLineOfRequestElement.class, result.get(13).getAccessLogElement().getClass());
    assertEquals(UriPathOnlyElement.class, result.get(14).getAccessLogElement().getClass());
    assertEquals(QueryOnlyElement.class, result.get(15).getAccessLogElement().getClass());
    assertEquals(UriPathOnlyElement.class, result.get(16).getAccessLogElement().getClass());
    assertEquals(QueryOnlyElement.class, result.get(17).getAccessLogElement().getClass());
    assertEquals(UriPathIncludeQueryElement.class, result.get(18).getAccessLogElement().getClass());
    assertEquals(VersionOrProtocolElement.class, result.get(19).getAccessLogElement().getClass());
    assertEquals(DatetimeConfigurableElement.class, result.get(20).getAccessLogElement().getClass());
    assertEquals(DatetimeConfigurableElement.class, result.get(21).getAccessLogElement().getClass());
    assertEquals(DatetimeConfigurableElement.class, result.get(22).getAccessLogElement().getClass());
    assertEquals(RequestHeaderElement.class, result.get(23).getAccessLogElement().getClass());
    assertEquals(ResponseHeaderElement.class, result.get(24).getAccessLogElement().getClass());
    assertEquals(CookieElement.class, result.get(25).getAccessLogElement().getClass());
  }

  @Test
  public void testCheckExtractionList() {
    List<AccessLogElementExtraction> extractionList = new ArrayList<>(3);
    extractionList.add(new AccessLogElementExtraction().setStart(0).setEnd(3));
    extractionList.add(new AccessLogElementExtraction().setStart(3).setEnd(6));
    extractionList.add(new AccessLogElementExtraction().setStart(5).setEnd(9));

    try {
      Deencapsulation.invoke(new DefaultAccessLogPatternParser(), "checkExtractionList", extractionList);
      fail("expect an exception");
    } catch (Exception e) {
      assertEquals(IllegalArgumentException.class, e.getClass());
      assertEquals("access log pattern contains illegal placeholder, please check it.", e.getMessage());
    }
  }
}