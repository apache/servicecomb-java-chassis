package io.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import io.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogElementExtraction;

public class ImmutableAccessLogElementMatcherTest {
  public static final ImmutableAccessLogElementMatcher MATCHER = new MockImmutableAccessLogElementMatcher();

  @Test
  public void testExtractElementPlaceHolder() {
    final String rawPattern = "%m %m%m %m";
    List<AccessLogElementExtraction> extractionList = MATCHER.extractElementPlaceholder(rawPattern);

    assertEquals(4, extractionList.size());
    assertEquals(0, extractionList.get(0).getStart());
    assertEquals(2, extractionList.get(0).getEnd());
    assertEquals(3, extractionList.get(1).getStart());
    assertEquals(5, extractionList.get(1).getEnd());
    assertEquals(5, extractionList.get(2).getStart());
    assertEquals(7, extractionList.get(2).getEnd());
    assertEquals(8, extractionList.get(3).getStart());
    assertEquals(10, extractionList.get(3).getEnd());

    assertEquals(MockImmutableAccessLogElementMatcher.ELEMENT, extractionList.get(0).getAccessLogElement());
    assertEquals(MockImmutableAccessLogElementMatcher.ELEMENT, extractionList.get(1).getAccessLogElement());
    assertEquals(MockImmutableAccessLogElementMatcher.ELEMENT, extractionList.get(2).getAccessLogElement());
    assertEquals(MockImmutableAccessLogElementMatcher.ELEMENT, extractionList.get(3).getAccessLogElement());
  }

  @Test
  public void testExtractElementPlaceHolderOnNoMatch() {
    final String rawPattern = "%p %r%{PATTERN}tcs-status";
    List<AccessLogElementExtraction> extractionList = MATCHER.extractElementPlaceholder(rawPattern);

    assertEquals(0, extractionList.size());
  }

  public static class MockImmutableAccessLogElementMatcher extends ImmutableAccessLogElementMatcher {
    public static final AccessLogElement ELEMENT = Mockito.mock(AccessLogElement.class);

    public static final String PLACEHOLDER_PATTERN = "%m";

    @Override
    protected AccessLogElement getAccessLogElement() {
      return ELEMENT;
    }

    @Override
    public List<AccessLogElementExtraction> extractElementPlaceholder(String rawPattern) {
      List<AccessLogElementExtraction> extractionList = new ArrayList<>();
      matchElementPlaceholder(rawPattern, PLACEHOLDER_PATTERN, extractionList);

      return extractionList;
    }
  }
}