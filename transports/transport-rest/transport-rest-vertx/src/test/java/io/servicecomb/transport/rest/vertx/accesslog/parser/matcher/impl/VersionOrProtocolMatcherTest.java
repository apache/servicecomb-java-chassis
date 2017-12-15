package io.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import io.servicecomb.transport.rest.vertx.accesslog.element.impl.VersionOrProtocolElement;
import io.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogElementExtraction;

public class VersionOrProtocolMatcherTest {
  private static final VersionOrProtocolMatcher MATCHER = new VersionOrProtocolMatcher();

  private static final String RAW_PATTERN = "%H %h %{PATTERN}t %H%H %H %H";

  @Test
  public void testExtractElementPlaceHolder() {
    List<AccessLogElementExtraction> extractionList = MATCHER.extractElementPlaceholder(RAW_PATTERN);

    assertEquals(5, extractionList.size());
    assertEquals(0, extractionList.get(0).getStart());
    assertEquals(2, extractionList.get(0).getEnd());
    assertEquals(MATCHER.getAccessLogElement(), extractionList.get(0).getAccessLogElement());
    assertEquals(18, extractionList.get(1).getStart());
    assertEquals(20, extractionList.get(1).getEnd());
    assertEquals(MATCHER.getAccessLogElement(), extractionList.get(1).getAccessLogElement());
    assertEquals(20, extractionList.get(2).getStart());
    assertEquals(22, extractionList.get(2).getEnd());
    assertEquals(MATCHER.getAccessLogElement(), extractionList.get(2).getAccessLogElement());
    assertEquals(23, extractionList.get(3).getStart());
    assertEquals(25, extractionList.get(3).getEnd());
    assertEquals(MATCHER.getAccessLogElement(), extractionList.get(3).getAccessLogElement());
    assertEquals(26, extractionList.get(4).getStart());
    assertEquals(28, extractionList.get(4).getEnd());
    assertEquals(MATCHER.getAccessLogElement(), extractionList.get(4).getAccessLogElement());
  }

  @Test
  public void testGetPlaceholderPattern() {
    assertEquals("%H", MATCHER.getPlaceholderPattern());
  }

  @Test
  public void getAccessLogElement() {
    assertTrue(VersionOrProtocolElement.class.equals(MATCHER.getAccessLogElement().getClass()));
  }
}