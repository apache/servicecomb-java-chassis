package io.servicecomb.transport.rest.vertx.accesslog.parser;

import io.servicecomb.transport.rest.vertx.accesslog.element.AccessLogElement;

public class AccessLogElementExtraction {
  private int start;

  private int end;

  private AccessLogElement accessLogElement;

  public AccessLogElementExtraction() {

  }

  public AccessLogElementExtraction(int start, int end,
      AccessLogElement accessLogElement) {
    this.start = start;
    this.end = end;
    this.accessLogElement = accessLogElement;
  }

  public int getStart() {
    return start;
  }

  public AccessLogElementExtraction setStart(int start) {
    this.start = start;
    return this;
  }

  public int getEnd() {
    return end;
  }

  public AccessLogElementExtraction setEnd(int end) {
    this.end = end;
    return this;
  }

  public AccessLogElement getAccessLogElement() {
    return accessLogElement;
  }

  public AccessLogElementExtraction setAccessLogElement(
      AccessLogElement accessLogElement) {
    this.accessLogElement = accessLogElement;
    return this;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("AccessLogElementExtraction{");
    sb.append("start=").append(start);
    sb.append(", end=").append(end);
    sb.append(", accessLogElement=").append(accessLogElement);
    sb.append('}');
    return sb.toString();
  }
}
