package io.servicecomb.transport.rest.vertx.accesslog.element.impl;

import static org.junit.Assert.assertEquals;

import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;

import io.servicecomb.transport.rest.vertx.accesslog.AccessLogParam;

public class DatetimeConfigurableElementTest {

  @Test
  public void getFormattedElement() {
    DatetimeConfigurableElement element = new DatetimeConfigurableElement(
        "EEE, yyyy MMM dd HH:mm:ss zzz|GMT-08|zh-CN");

    AccessLogParam accessLogParam = new AccessLogParam().setStartMillisecond(1416863450581L);

    String result = element.getFormattedElement(accessLogParam);

    assertEquals("星期一, 2014 十一月 24 13:10:50 GMT-08:00", result);
  }

  @Test
  public void getFormattedElementOnNoPattern() {
    DatetimeConfigurableElement element = new DatetimeConfigurableElement(
        "|GMT+08|zh-CN");

    AccessLogParam accessLogParam = new AccessLogParam().setStartMillisecond(1416863450581L);

    String result = element.getFormattedElement(accessLogParam);

    assertEquals("星期二, 25 十一月 2014 05:10:50 GMT+08:00", result);
  }

  @Test
  public void getFormattedElementOnNoTimezone() {
    DatetimeConfigurableElement element = new DatetimeConfigurableElement(
        "yyyy/MM/dd zzz||zh-CN");

    AccessLogParam accessLogParam = new AccessLogParam().setStartMillisecond(1416863450581L);

    String result = element.getFormattedElement(accessLogParam);

    assertEquals("2014/11/25 CST", result);
  }

  @Test
  public void getFormattedElementOnNoLocale() {
    DatetimeConfigurableElement element = new DatetimeConfigurableElement(
        "EEE, dd MMM yyyy HH:mm:ss zzz|GMT+08|");

    AccessLogParam accessLogParam = new AccessLogParam().setStartMillisecond(1416863450581L);

    String result = element.getFormattedElement(accessLogParam);

    assertEquals("Tue, 25 Nov 2014 05:10:50 GMT+08:00", result);
  }

  @Test
  public void getFormattedElementOnNoConfig() {
    DatetimeConfigurableElement element = new DatetimeConfigurableElement(
        "||");

    AccessLogParam accessLogParam = new AccessLogParam().setStartMillisecond(1416863450581L);

    String result = element.getFormattedElement(accessLogParam);

    assertEquals("Tue, 25 Nov 2014 05:10:50 CST", result);
  }

  @Test
  public void testConstructorWithNoArg() {
    DatetimeConfigurableElement element = new DatetimeConfigurableElement();

    AccessLogParam accessLogParam = new AccessLogParam().setStartMillisecond(1416863450581L);

    String result = element.getFormattedElement(accessLogParam);

    assertEquals("EEE, dd MMM yyyy HH:mm:ss zzz", element.getPattern());
    assertEquals(Locale.US, element.getLocale());
    assertEquals(TimeZone.getDefault(), element.getTimezone());
    assertEquals("Tue, 25 Nov 2014 05:10:50 CST", result);
  }

  @Test
  public void testConstructorWithNoSeparator() {
    DatetimeConfigurableElement element = new DatetimeConfigurableElement("yyyy/MM/dd HH:mm:ss zzz");

    AccessLogParam accessLogParam = new AccessLogParam().setStartMillisecond(1416863450581L);

    String result = element.getFormattedElement(accessLogParam);

    assertEquals("yyyy/MM/dd HH:mm:ss zzz", element.getPattern());
    assertEquals(Locale.US, element.getLocale());
    assertEquals(TimeZone.getDefault(), element.getTimezone());
    assertEquals("2014/11/25 05:10:50 CST", result);
  }
}