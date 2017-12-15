package io.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import io.servicecomb.transport.rest.vertx.accesslog.element.impl.UriPathIncludeQueryElement;
import io.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogElementExtraction;

public class UriPathIncludeQueryMatcherTest {
  private static final UriPathIncludeQueryMatcher MATCHER = new UriPathIncludeQueryMatcher();

  private static final String RAW_PATTERN = "cs-uri %h %{PATTERN}t cs-urics-uri %H cs-uri-query cs-uri";

  @Test
  public void testExtractElementPlaceHolder() {
    List<AccessLogElementExtraction> extractionList = MATCHER.extractElementPlaceholder(RAW_PATTERN);

    assertEquals(4, extractionList.size());
    assertEquals(0, extractionList.get(0).getStart());
    assertEquals(6, extractionList.get(0).getEnd());
    assertEquals(UriPathIncludeQueryElement.class, extractionList.get(0).getAccessLogElement().getClass());
    assertEquals(22, extractionList.get(1).getStart());
    assertEquals(28, extractionList.get(1).getEnd());
    assertEquals(UriPathIncludeQueryElement.class, extractionList.get(1).getAccessLogElement().getClass());
    assertEquals(28, extractionList.get(2).getStart());
    assertEquals(34, extractionList.get(2).getEnd());
    assertEquals(UriPathIncludeQueryElement.class, extractionList.get(2).getAccessLogElement().getClass());
    assertEquals(51, extractionList.get(3).getStart());
    assertEquals(57, extractionList.get(3).getEnd());
    assertEquals(UriPathIncludeQueryElement.class, extractionList.get(3).getAccessLogElement().getClass());
  }
}