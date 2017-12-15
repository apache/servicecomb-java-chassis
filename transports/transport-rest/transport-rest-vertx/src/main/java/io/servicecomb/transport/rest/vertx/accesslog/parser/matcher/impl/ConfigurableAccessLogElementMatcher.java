package io.servicecomb.transport.rest.vertx.accesslog.parser.matcher.impl;

import java.util.ArrayList;
import java.util.List;

import io.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;
import io.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogElementExtraction;
import io.servicecomb.transport.rest.vertx.accesslog.parser.matcher.AccessLogElementMatcher;

public abstract class ConfigurableAccessLogElementMatcher implements AccessLogElementMatcher {

  @Override
  public List<AccessLogElementExtraction> extractElementPlaceholder(String rawPattern) {
    List<AccessLogElementExtraction> extractionList = new ArrayList<>();
    int begin = -1;
    int end = 0;
    int cursor = 0;

    while (true) {
      end = rawPattern.indexOf(getPlaceholderSuffix(), cursor);
      if (end < 0) {
        break;
      }
      begin = locateBeginIndex(rawPattern, end, cursor);
      if (begin < 0) {
        break;
      }

      String identifier = rawPattern.substring(begin + getPlaceholderSuffix().length(), end);
      extractionList.add(new AccessLogElementExtraction(begin, end + getPlaceholderSuffix().length(),
          getAccessLogElement(identifier)));

      cursor = end + 1;
    }
    return extractionList;
  }

  private int locateBeginIndex(String rawPattern, int end, int cursor) {
    int preBegin = rawPattern.indexOf(getPlaceholderPrefix(), cursor);
    int begin = rawPattern.indexOf(getPlaceholderPrefix(), preBegin + 1);
    while (begin >= 0 && begin < end) {
      if (begin < end) {
        preBegin = begin;
      }
      begin = rawPattern.indexOf(getPlaceholderPrefix(), preBegin + 1);
    }
    return preBegin;
  }


  protected abstract String getPlaceholderSuffix();

  protected abstract String getPlaceholderPrefix();

  protected abstract AccessLogElement getAccessLogElement(String identifier);
}
